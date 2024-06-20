package com.rowdyCSExtensions.ExtractorProviders

import com.KillerDogeEmpire.UltimaMediaProvidersUtils.getIndexQuality
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.INFER_TYPE
import com.lagradost.cloudstream3.utils.loadExtractor

open class Mdrive : ExtractorApi() {
    override val name: String = "Mdrive"
    override val mainUrl: String = "https://gamerxyt.com"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val host=url.substringAfter("?").substringBefore("&")
        val id=url.substringAfter("id=").substringBefore("&")
        val token=url.substringAfter("token=").substringBefore("&")
        val Cookie="$host; hostid=$id; hosttoken=$token"
        val doc = app.get("$mainUrl/games/",headers = mapOf("Cookie" to Cookie)).document
        val links = doc.select("div.card-body > h2 > a").attr("href")
        val header = doc.selectFirst("div.card-header")?.text()
        if (links.contains("pixeldrain"))
        {
            callback.invoke(
                ExtractorLink(
                    "MovieDrive",
                    "PixelDrain",
                    links,
                    referer = links,
                    quality = getIndexQuality(header),
                    type = INFER_TYPE
                )
            )
        }else
            if (links.contains("gofile")) {
                loadExtractor(links, subtitleCallback, callback)
            }
            else {
                callback.invoke(
                    ExtractorLink(
                        "MovieDrive",
                        "MovieDrive",
                        links,
                        referer = "",
                        quality = getIndexQuality(header),
                        type = INFER_TYPE
                    )
                )
            }
    }
}