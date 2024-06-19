package com.KillerDogeEmpire

import android.util.Log
import com.KillerDogeEmpire.UltimaMediaProvidersUtils.ServerName.*
import com.KillerDogeEmpire.UltimaMediaProvidersUtils.commonLinkLoader
import com.KillerDogeEmpire.UltimaMediaProvidersUtils.createSlug
import com.KillerDogeEmpire.UltimaMediaProvidersUtils.getBaseUrl
import com.KillerDogeEmpire.UltimaUtils.Category
import com.KillerDogeEmpire.UltimaUtils.LinkData
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.apmap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorLink

class MultiMoviesProvider : MediaProvider() {
    override val name = "MultiMovies"
    override val domain = "https://multimovies.icu"
    override val categories = listOf(Category.MEDIA)

    override suspend fun loadContent(
        url: String,
        data: LinkData,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val fixTitle = data.title.createSlug()
        val mediaurl = if (data.season == null) {
            "$url/movies/$fixTitle"
        } else {
            "$url/episodes/$fixTitle-${data.season}x${data.episode}"
        }
        val req = app.get(mediaurl).document
        req.select("ul#playeroptionsul li").map {
            Triple(
                it.attr("data-post"),
                it.attr("data-nume"),
                it.attr("data-type")
            )
        }.apmap { (id, nume, type) ->
            if (!nume.contains("trailer")) {
                val source = app.post(
                    url = "$url/wp-admin/admin-ajax.php",
                    data = mapOf(
                        "action" to "doo_player_ajax",
                        "post" to id,
                        "nume" to nume,
                        "type" to type
                    ),
                    referer = url,
                    headers = mapOf("X-Requested-With" to "XMLHttpRequest")
                ).parsed<ResponseHash>().embed_url
                Log.d("Phisher",source)
                val link = source.substringAfter("\"").substringBefore("\"")
                Log.d("Phisher",link)
                val domain= getBaseUrl(link)
                when(domain) {
                    "https://server2.shop"-> commonLinkLoader(
                        name, Vidhide,link,null,null,subtitleCallback, callback
                    )
                    "https://multimovies.cloud"-> commonLinkLoader(
                        name, StreamWish,link,null,null,subtitleCallback, callback
                    )
                    "https://allinonedownloader.fun"-> commonLinkLoader(
                        name, StreamWish,link,null,null,subtitleCallback, callback
                    )
                    "https://aa.clonimeziud"-> commonLinkLoader(
                        name, Vidhide,link,null,null,subtitleCallback, callback
                    )
                }
            }
        }
    }

    // #region - Encryption and Decryption handlers
    // #endregion - Encryption and Decryption handlers

    // #region - Data classes

    data class ResponseHash(
        @JsonProperty("embed_url") val embed_url: String,
        @JsonProperty("key") val key: String? = null,
        @JsonProperty("type") val type: String? = null,
    )
    // #endregion - Data classes
}
