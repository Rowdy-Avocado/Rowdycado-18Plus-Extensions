package com.KillerDogeEmpire

import com.KillerDogeEmpire.UltimaUtils.Category
import com.KillerDogeEmpire.UltimaUtils.LinkData
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.amap
import com.lagradost.cloudstream3.extractors.Filesim
import com.lagradost.cloudstream3.extractors.Jeniusplay
import com.lagradost.cloudstream3.extractors.Mp4Upload
import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.extractors.VidhideExtractor
import com.lagradost.cloudstream3.extractors.Vidplay
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import java.net.URI
import java.net.URL

abstract class MediaProvider {
    abstract val name: String
    abstract val domain: String
    abstract val categories: List<Category>

    abstract suspend fun loadContent(
            url: String,
            data: LinkData,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    )
}

@OptIn(kotlin.ExperimentalStdlibApi::class)
object UltimaMediaProvidersUtils {
    val mediaProviders =
            listOf<MediaProvider>(
                    AniwaveMediaProvider(),
                    CineZoneMediaProvider(),
                    IdliXMediaProvider(),
                    MoflixMediaProvider(),
                    RidomoviesMediaProvider(),
                    VidsrcToMediaProvider(),
                    NowTvMediaProvider(),
                    DahmerMoviesMediaProvider(),
                    NoverseMediaProvider(),
                    AllMovielandMediaProvider(),
                    TwoEmbedMediaProvider(),
                    EMoviesMediaProvider(),
                    MultiEmbededAPIProvider(),
                    MultiMoviesProvider(),
                    AnitakuMediaProvider(),
                    MoviesDriveProvider()
            )

    suspend fun invokeExtractors(
            category: Category,
            data: LinkData,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ) {
        UltimaStorageManager.currentMediaProviders.toList().amap {
            val provider = it.getProvider()
            if (provider.categories.contains(category) && it.enabled) {
                try {
                    provider.loadContent(it.getDomain(), data, subtitleCallback, callback)
                } catch (e: Exception) {}
            }
        }
    }

    enum class ServerName {
        MyCloud,
        Mp4upload,
        Streamtape,
        Vidplay,
        Filemoon,
        Jeniusplay,
        Uqload,
        StreamWish,
        Vidhide,
        MDrive,
        Custom,
        NONE
    }

    fun getBaseUrl(url: String): String {
        return URI(url).let { "${it.scheme}://${it.host}" }
    }

    fun getEpisodeSlug(
            season: Int? = null,
            episode: Int? = null,
    ): Pair<String, String> {
        return if (season == null && episode == null) {
            "" to ""
        } else {
            (if (season!! < 10) "0$season" else "$season") to
                    (if (episode!! < 10) "0$episode" else "$episode")
        }
    }

    fun getIndexQuality(str: String?): Int {
        return Regex("(\\d{3,4})[pP]").find(str ?: "")?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: Qualities.Unknown.value
    }

    fun getIndexQualityTags(str: String?, fullTag: Boolean = false): String {
        return if (fullTag)
                Regex("(?i)(.*)\\.(?:mkv|mp4|avi)").find(str ?: "")?.groupValues?.get(1)?.trim()
                        ?: str ?: ""
        else
                Regex("(?i)\\d{3,4}[pP]\\.?(.*?)\\.(mkv|mp4|avi)")
                        .find(str ?: "")
                        ?.groupValues
                        ?.getOrNull(1)
                        ?.replace(".", " ")
                        ?.trim()
                        ?: str ?: ""
    }

    fun String.encodeUrl(): String {
        val url = URL(this)
        val uri = URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref)
        return uri.toURL().toString()
    }

    fun String?.createSlug(): String? {
        return this?.filter { it.isWhitespace() || it.isLetterOrDigit() }
                ?.trim()
                ?.replace("\\s+".toRegex(), "-")
                ?.lowercase()
    }

    fun fixUrl(url: String, domain: String): String {
        if (url.startsWith("http")) {
            return url
        }
        if (url.isEmpty()) {
            return ""
        }

        val startsWithNoHttp = url.startsWith("//")
        if (startsWithNoHttp) {
            return "https:$url"
        } else {
            if (url.startsWith('/')) {
                return domain + url
            }
            return "$domain/$url"
        }
    }

    // #region - Main Link Handler
    suspend fun commonLinkLoader(
            providerName: String?,
            serverName: ServerName?,
            url: String,
            referer: String?,
            dubStatus: String?,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit,
            quality: Int = Qualities.Unknown.value,
            isM3u8: Boolean = false,
            tag: String? = null
    ) {
        try {
            val domain = referer ?: getBaseUrl(url)
            when (serverName) {
                ServerName.Vidplay ->
                        AnyVidplay(providerName, dubStatus, domain)
                                .getUrl(url, domain, subtitleCallback, callback)
                ServerName.MyCloud ->
                        AnyMyCloud(providerName, dubStatus, domain)
                                .getUrl(url, domain, subtitleCallback, callback)
                ServerName.Filemoon ->
                        AnyFileMoon(providerName, dubStatus, domain)
                                .getUrl(url, null, subtitleCallback, callback)
                ServerName.Mp4upload ->
                        AnyMp4Upload(providerName, dubStatus, domain)
                                .getUrl(url, domain, subtitleCallback, callback)
                ServerName.Jeniusplay ->
                        AnyJeniusplay(providerName, dubStatus, domain)
                                .getUrl(url, domain, subtitleCallback, callback)
                ServerName.Uqload ->
                        AnyUqload(providerName, dubStatus, domain)
                                .getUrl(url, domain, subtitleCallback, callback)
                ServerName.StreamWish ->
                    AnyStreamwish(providerName, dubStatus, domain)
                        .getUrl(url, domain, subtitleCallback, callback)
                ServerName.Vidhide ->
                    AnyVidhide(providerName, dubStatus, domain)
                        .getUrl(url, domain, subtitleCallback, callback)
                ServerName.MDrive ->
                    AnyMDrive(providerName, dubStatus, domain)
                        .getUrl(url, domain, subtitleCallback, callback)
                ServerName.Custom -> {
                    callback.invoke(
                            ExtractorLink(
                                    providerName ?: return,
                                    tag?.let { "$providerName: $it" } ?: providerName,
                                    url,
                                    domain,
                                    quality,
                                    isM3u8
                            )
                    )
                }
                else -> {
                    loadExtractor(url, subtitleCallback, callback)
                }
            }
        } catch (e: Exception) {}
    }
    // #endregion - Main Link Handler
}

// #region - Custom Extractors
class AnyFileMoon(provider: String?, dubType: String?, domain: String = "") : Filesim() {
    override val name =
            (if (provider != null) "$provider: " else "") +
                    "Filemoon" +
                    (if (dubType != null) ": $dubType" else "")
    override val mainUrl = domain
    override val requiresReferer = false
}

class AnyMyCloud(provider: String?, dubType: String?, domain: String = "") : Vidplay() {
    override val name =
            (if (provider != null) "$provider: " else "") +
                    "MyCloud" +
                    (if (dubType != null) ": $dubType" else "")
    override val mainUrl = domain
    override val requiresReferer = false
}

class AnyVidplay(provider: String?, dubType: String?, domain: String = "") : Vidplay() {
    override val name =
            (if (provider != null) "$provider: " else "") +
                    "Vidplay" +
                    (if (dubType != null) ": $dubType" else "")
    override val mainUrl = domain
    override val requiresReferer = false
}


class AnyMp4Upload(provider: String?, dubType: String?, domain: String = "") : Mp4Upload() {
    override var name =
            (if (provider != null) "$provider: " else "") +
                    "Mp4Upload" +
                    (if (dubType != null) ": $dubType" else "")
    override var mainUrl = domain
    override val requiresReferer = false
}

class AnyJeniusplay(provider: String?, dubType: String?, domain: String = "") : Jeniusplay() {
    override var name =
            (if (provider != null) "$provider: " else "") +
                    "JeniusPlay" +
                    (if (dubType != null) ": $dubType" else "")
    override var mainUrl = domain
    override val requiresReferer = false
}

class AnyUqload(provider: String?, dubType: String?, domain: String = "") : Filesim() {
    override val name =
            (if (provider != null) "$provider: " else "") +
                    "Uqloads" +
                    (if (dubType != null) ": $dubType" else "")
    override val mainUrl = domain
    override val requiresReferer = false
}

class AnyStreamwish(provider: String?, dubType: String?, domain: String = "") : StreamWishExtractor() {
    override var name =
        (if (provider != null) "$provider: " else "") +
                "SteamWish" +
                (if (dubType != null) ": $dubType" else "")
    override var mainUrl = domain
    override val requiresReferer = false
}

class AnyVidhide(provider: String?, dubType: String?, domain: String = "") : VidhideExtractor() {
    override var name =
        (if (provider != null) "$provider: " else "") +
                "Vidhide" +
                (if (dubType != null) ": $dubType" else "")
    override var mainUrl = domain
    override val requiresReferer = false
}

class AnyMDrive(provider: String?, dubType: String?, domain: String = "") : MoviesDriveProvider.Mdrive() {
    override var name =
        (if (provider != null) "$provider: " else "") +
                "MDrive" +
                (if (dubType != null) ": $dubType" else "")
    override var mainUrl = domain
    override val requiresReferer = false
}

// #endregion - Custom Extractors
