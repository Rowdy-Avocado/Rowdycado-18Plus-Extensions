package com.RowdyAvocado

import android.util.Base64
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.app

class AllWishExtractor {
    suspend fun getStreamUrl(serverName: String?, dataId: String): List<String> {
        var links = emptySet<String>()
        val res =
                app.get("${AllWish.mainUrl}/ajax/server?get=$dataId", AllWish.xmlHeader)
                        .parsedSafe<APIResponse>()
        val serverUrl = res?.result?.url
        if (!serverUrl.isNullOrEmpty()) {
            when (serverName) {
                "VidPlay" -> {
                    val tempRes =
                            app.get(
                                    "https://cdn.animixplay.tube/player/?id=6f72652d64616b652d6c6576656c2d75702d6e612d6b656e2d657069736f64652d31&autostart=true",
                                    headers = AllWish.refHeader
                            )
                    val encryptedSource = tempRes.url.substringAfterLast("?data=#")
                    if (encryptedSource.isNotBlank()) {
                        links +=
                                Base64.decode(encryptedSource, Base64.DEFAULT)
                                        .toString(Charsets.UTF_8)
                    }
                }
                "Vidstreaming" -> {}
                "Gogo server" -> {}
                "Streamwish" -> {
                    val serverRes = app.get(serverUrl)
                    if (serverRes.code == 200) {
                        val doc = serverRes.document
                        val script =
                                doc.selectFirst("script:containsData(sources)")?.data().toString()
                        Regex("file:\"(.*?)\"").find(script)?.groupValues?.get(1)?.let {
                            links += it
                        }
                    }
                }
                "Mp4Upload" -> {
                    links += serverUrl
                }
                "Doodstream" -> {
                    links += serverUrl
                }
                "Filelions" -> {
                    links += serverUrl
                }
            }
        }
        return links.toList()
    }

    data class APIResponse(
            @JsonProperty("status") val status: Int? = null,
            @JsonProperty("result") val result: ServerUrl? = null,
    )

    data class ServerUrl(
            @JsonProperty("url") val url: String? = null,
    )
}

// {
//     "status": 200,
//     "result": {
//         "url":
// "https:\/\/cdn.animixplay.tube\/player\/?id=6e696e6a612d6b616d75692d657069736f64652d37",
//         "skip_data": {
//             "intro": [
//                 0,
//                 0
//             ],
//             "outro": [
//                 0,
//                 0
//             ]
//         }
//     }
// }
