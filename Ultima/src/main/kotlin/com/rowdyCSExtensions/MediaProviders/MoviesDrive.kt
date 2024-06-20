package com.KillerDogeEmpire

import android.util.Log
import com.KillerDogeEmpire.UltimaMediaProvidersUtils.ServerName.*
import com.KillerDogeEmpire.UltimaMediaProvidersUtils.createSlug
import com.KillerDogeEmpire.UltimaMediaProvidersUtils.getBaseUrl
import com.KillerDogeEmpire.UltimaUtils.Category
import com.KillerDogeEmpire.UltimaUtils.LinkData
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.apmap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorLink

class MoviesDriveProvider : MediaProvider() {
    override val name = "MoviesDrive"
    override val domain = "https://moviesdrive.online"
    override val categories = listOf(Category.MEDIA)

    override suspend fun loadContent(
        url: String,
        data: LinkData,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val title=data.title
        val season=data.season
        val episode=data.episode
        Log.d("Phisher",data.toString())
        try {
            val fixTitle = title.createSlug()
            val mediaurl = "$url/$fixTitle"
            val document = app.get(mediaurl).document
            if (season == null) {
                document.select("h5 > a").map {
                    val link = it.attr("href")
                    val urls = ExtractMdrive(link)
                    Log.d("Phisher",urls.toString())
                    urls.forEach { servers ->
                        val domain= getBaseUrl(servers)
                        when(domain) {
                            "https://gamerxyt.com"-> UltimaMediaProvidersUtils.commonLinkLoader(
                                name, Mdrive, servers, null, null, subtitleCallback, callback
                            )
                        }
                    }
                }
            } else {
                val stag = "Season $season"
                val sep = "Ep$episode"
                val entries = document.select("h5:matches((?i)$stag)")
                entries.apmap { entry ->
                    val href = entry.nextElementSibling()?.selectFirst("a")?.attr("href") ?: ""
                    if (href.isNotBlank()) {
                        val doc = app.get(href).document
                        doc.select("h5:matches((?i)$sep)").forEach { epElement ->
                            val linklist = mutableListOf<String>()
                            epElement.nextElementSibling()?.let { sibling ->
                                sibling.selectFirst("h5 > a")
                                    ?.let { linklist.add(it.attr("href")) }
                                sibling.nextElementSibling()?.let { nextSibling ->
                                    nextSibling.selectFirst("h5 > a")
                                        ?.let { linklist.add(it.attr("href")) }
                                }
                            }
                            linklist.forEach { url ->
                                val links = ExtractMdriveSeries(url)
                                Log.d("Phisher",links.toString())
                                links.forEach { link ->
                                    val domain= getBaseUrl(link)
                                    when(domain) {
                                        "https://gamerxyt.com"-> UltimaMediaProvidersUtils.commonLinkLoader(
                                            name, Mdrive, link, null, null, subtitleCallback, callback
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (e: Exception)
        {
            Log.e("Exception Error", e.toString())
        }
    }

    suspend fun ExtractMdrive(url: String): MutableList<String> {
        val doc= app.get(url).document
        val linklist= mutableListOf(String())
        doc.select("h5 > a").forEach {
            val link=it.attr("href").replace("lol","day")
            if (!link.contains("gdtot"))
            {
                val mainpage= app.get(link).document.selectFirst("a.btn.btn-primary")?.attr("href").toString()
                if (!mainpage.contains("https://"))
                {
                    val newlink= "https://hubcloud.day$mainpage"
                    linklist.add(newlink)
                }
                else
                {
                    linklist.add(mainpage)
                }
            }
        }
        return linklist
    }

    suspend fun ExtractMdriveSeries(url: String): MutableList<String> {
        val linklist= mutableListOf(String())
        val mainpage = app.get(url.replace("lol","day")).document.selectFirst("a.btn.btn-primary")?.attr("href").toString()
        if (!mainpage.contains("https://")) {
            val newlink = "https://hubcloud.day$mainpage"
            linklist.add(newlink)
        } else {
            linklist.add(mainpage)
        }
        return linklist
    }

}
