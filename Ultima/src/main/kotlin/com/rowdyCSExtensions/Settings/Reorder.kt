package com.KillerDogeEmpire

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lagradost.cloudstream3.CommonActivity.showToast

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UltimaReorder(val plugin: UltimaPlugin) : BottomSheetDialogFragment() {
    private var param1: String? = null
    private var param2: String? = null
    private val sm = UltimaStorageManager
    private val providers = sm.fetchExtensions()
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
        val settings = getLayout("reorder", inflater, container)

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

        return settings
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {}

    override fun onDetach() {
        val settings = UltimaSettings(plugin)
        settings.show(
                activity?.supportFragmentManager
                        ?: throw Exception("Unable to open configure settings"),
                ""
        )
        super.onDetach()
    }
}
