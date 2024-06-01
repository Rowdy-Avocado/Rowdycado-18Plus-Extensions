package com.KillerDogeEmpire

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.utils.AppUtils.setDefaultFocus

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UltimaSettings(val plugin: UltimaPlugin) : BottomSheetDialogFragment() {
    private var param1: String? = null
    private var param2: String? = null
    private val providers = plugin.fetchSections()
    private val res: Resources = plugin.resources ?: throw Exception("Unable to read resources")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    // #region - necessary functions
    private fun getLayout(name: String, inflater: LayoutInflater, container: ViewGroup?): View {
        val id = res.getIdentifier(name, "layout", BuildConfig.LIBRARY_PACKAGE_NAME)
        val layout = res.getLayout(id)
        return inflater.inflate(layout, container, false)
    }

    private fun getDrawable(name: String): Drawable {
        val id = res.getIdentifier(name, "drawable", BuildConfig.LIBRARY_PACKAGE_NAME)
        return res.getDrawable(id, null) ?: throw Exception("Unable to find drawable $name")
    }

    private fun getString(name: String): String {
        val id = res.getIdentifier(name, "string", BuildConfig.LIBRARY_PACKAGE_NAME)
        return res.getString(id)
    }

    private fun <T : View> View.findView(name: String): T {
        val id = res.getIdentifier(name, "id", BuildConfig.LIBRARY_PACKAGE_NAME)
        return this.findViewById(id)
    }

    private fun View.makeTvCompatible() {
        val outlineId = res.getIdentifier("outline", "drawable", BuildConfig.LIBRARY_PACKAGE_NAME)
        this.background = res.getDrawable(outlineId, null)
    }
    // #endregion - necessary functions

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val settings = getLayout("settings", inflater, container)

        // #region - building save button and its click listener
        val saveBtn = settings.findView<ImageView>("save")
        saveBtn.setImageDrawable(getDrawable("save_icon"))
        saveBtn.makeTvCompatible()
        saveBtn.setOnClickListener(
                object : OnClickListener {
                    override fun onClick(btn: View) {
                        plugin.reload(context)
                        showToast("Saved")
                        dismiss()
                    }
                }
        )
        // #endregion - building save button and its click listener

        // #region - building toggle for extension_name_on_home and its click listener
        val extNameOnHomeBtn = settings.findView<Switch>("ext_name_on_home_toggle")
        extNameOnHomeBtn.makeTvCompatible()
        extNameOnHomeBtn.isChecked = plugin.extNameOnHome
        extNameOnHomeBtn.setOnClickListener(
                object : OnClickListener {
                    override fun onClick(btn: View) {
                        plugin.extNameOnHome = extNameOnHomeBtn.isChecked
                    }
                }
        )
        // #endregion - building toggle for extension_name_on_home and its click listener

        // #region - building list of extensions and its sections with its click listener
        val parentLayout = settings.findView<LinearLayout>("parent_list")
        providers.forEach { provider ->
            val parentLayoutView = buildExtensionView(provider, inflater, container)
            parentLayout.addView(parentLayoutView)
        }
        // #endregion - building list of extensions and its sections with its click listener

        // #region - building reset button with its click listener
        val deleteIconId = res.getIdentifier("delete_icon", "drawable", "com.KillerDogeEmpire")
        val deleteBtn = settings.findView<ImageView>("delete")
        deleteBtn.setImageDrawable(res.getDrawable(deleteIconId, null))
        deleteBtn.makeTvCompatible()
        deleteBtn.setOnClickListener(
                object : OnClickListener {
                    override fun onClick(btn: View) {
                        AlertDialog.Builder(
                                        context ?: throw Exception("Unable to build alert dialog")
                                )
                                .setTitle("Reset Ultima")
                                .setMessage("This will delete all selected sections.")
                                .setPositiveButton(
                                        "Reset",
                                        object : DialogInterface.OnClickListener {
                                            override fun onClick(p0: DialogInterface, p1: Int) {
                                                plugin.currentSections = emptyArray()
                                                plugin.reload(context)
                                                showToast("Sections cleared")
                                                dismiss()
                                            }
                                        }
                                )
                                .setNegativeButton("Cancel", null)
                                .show()
                                .setDefaultFocus()
                    }
                }
        )
        // #endregion - building reset button with its click listener

        return settings
    }

    fun buildExtensionView(
            provider: UltimaPlugin.PluginInfo,
            inflater: LayoutInflater,
            container: ViewGroup?
    ): View {

        // collecting required resources
        val parentLayoutView = getLayout("parent_layout", inflater, container)
        val parentTextViewBtn = parentLayoutView.findView<TextView>("parent_textview")
        val childList = parentLayoutView.findView<LinearLayout>("child_list")

        // building extension textview and its click listener
        parentTextViewBtn.text = "▶ " + provider.name
        parentTextViewBtn.makeTvCompatible()
        parentTextViewBtn.setOnClickListener(
                object : OnClickListener {
                    override fun onClick(btn: View) {
                        if (childList.visibility == View.VISIBLE) {
                            childList.visibility = View.GONE
                            parentTextViewBtn.text = "▶ " + provider.name
                        } else {
                            childList.visibility = View.VISIBLE
                            parentTextViewBtn.text = "▼ " + provider.name
                        }
                    }
                }
        )

        // building list of sections of current extnesion with its click listener
        provider.sections?.forEach { section ->
            val newSectionView = buildSectionView(section, inflater, container)
            childList.addView(newSectionView)
        }
        return parentLayoutView
    }

    fun buildSectionView(
            section: UltimaPlugin.SectionInfo,
            inflater: LayoutInflater,
            container: ViewGroup?
    ): View {

        // collecting required resources
        val sectionView = getLayout("child_checkbox", inflater, container)
        val childCheckBoxBtn = sectionView.findView<CheckBox>("child_checkbox")
        val counterLayout = sectionView.findView<LinearLayout>("counter_layout")

        // building section checkbox and its click listener
        childCheckBoxBtn.text = section.name
        childCheckBoxBtn.makeTvCompatible()
        childCheckBoxBtn.isChecked = section.enabled
        childCheckBoxBtn.setOnCheckedChangeListener(
                object : OnCheckedChangeListener {
                    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                        section.enabled = isChecked
                        plugin.currentSections = providers
                        counterLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
                    }
                }
        )

        // configure priority counter next to the section
        configureCounterView(section, counterLayout)

        return sectionView
    }

    fun configureCounterView(
            section: UltimaPlugin.SectionInfo,
            counterLayout: LinearLayout,
    ) {

        // collecting required resources
        val decreasePriorityBtn = counterLayout.findView<TextView>("decrease")
        val priorityTextview = counterLayout.findView<TextView>("priority_count")
        val increasePriorityBtn = counterLayout.findView<TextView>("increase")

        // counter visible only if section enabled
        counterLayout.visibility = if (section.enabled) View.VISIBLE else View.GONE
        priorityTextview.text = section.priority.toString()

        // configuring click listener for decrease button
        decreasePriorityBtn.makeTvCompatible()
        decreasePriorityBtn.setOnClickListener(
                object : OnClickListener {
                    override fun onClick(btn: View) {
                        val count = priorityTextview.text.toString().toInt()
                        if (count > 1) {
                            section.priority -= 1
                            plugin.currentSections = providers
                            priorityTextview.text = (count - 1).toString()
                        }
                    }
                }
        )

        // configuring click listener for increase button
        increasePriorityBtn.makeTvCompatible()
        increasePriorityBtn.setOnClickListener(
                object : OnClickListener {
                    override fun onClick(btn: View) {
                        val count = priorityTextview.text.toString().toInt()
                        if (count < 50) {
                            section.priority += 1
                            plugin.currentSections = providers
                            priorityTextview.text = (count + 1).toString()
                        }
                    }
                }
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {}
}
