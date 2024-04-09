package com.RowdyAvocado

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.extractors.FileMoon
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

class CineZoneExtractor : ExtractorApi() {
    override val mainUrl = CineZone.mainUrl
    override val name = CineZone.name
    override val requiresReferer = false

    override suspend fun getUrl(
            url: String,
            referer: String?,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ) {
        val serverName = referer
        if (url.isNotEmpty()) {
            when (serverName) {
                null -> {}
                "filemoon" -> FileMoon().getUrl(url, null, subtitleCallback, callback)
                else -> loadExtractor(url, subtitleCallback, callback)
            }
        }
    }
}
