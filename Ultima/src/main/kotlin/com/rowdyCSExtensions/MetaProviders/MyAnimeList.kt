package com.KillerDogeEmpire

import android.util.Log
import com.KillerDogeEmpire.UltimaMediaProvidersUtils.invokeExtractors
import com.KillerDogeEmpire.UltimaUtils.Category
import com.KillerDogeEmpire.UltimaUtils.LinkData
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.DubStatus
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addMalId
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addEpisodes
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.mapper
import com.lagradost.cloudstream3.newAnimeLoadResponse
import com.lagradost.cloudstream3.newAnimeSearchResponse
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.syncproviders.AccountManager
import com.lagradost.cloudstream3.syncproviders.SyncIdName
import com.lagradost.cloudstream3.utils.AppUtils
import com.lagradost.cloudstream3.utils.ExtractorLink

class MyAnimeList(val plugin: UltimaPlugin) : MainAPI() {
        override var name = "MyAnimeList"
        override var mainUrl = "https://myanimelist.net"
        override var supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)
        override var lang = "en"
        override val supportedSyncNames = setOf(SyncIdName.Anilist)
        override val hasMainPage = true
        override val hasQuickSearch = false
        private val api = AccountManager.malApi
        private val apiUrl = "https://api.myanimelist.net/v2"
        private final val mediaLimit = 20
        private val auth = BuildConfig.MAL_API

        protected fun Any.toStringData(): String {
                return mapper.writeValueAsString(this)
        }

        private suspend fun MainPageRequest.toSearchResponseList(
                        page: Int
        ): Pair<List<SearchResponse>, Boolean> {
                val res =
                                app.get(
                                                                "${this.data}${(page - 1) * mediaLimit}",
                                                                headers =
                                                                                mapOf(
                                                                                                "Authorization" to
                                                                                                                "Bearer $auth"
                                                                                )
                                                )
                                                .parsedSafe<MalApiResponse>()
                                                ?: throw Exception(
                                                                "Unable to fetch content from API"
                                                )
                val data =
                                res.data?.map {
                                        newAnimeSearchResponse(
                                                        it.node.title,
                                                        "$mainUrl/${it.node.id}"
                                        ) { this.posterUrl = it.node.picture.large }
                                }
                                                ?: throw Exception(
                                                                "Unable to fetch content from API"
                                                )

                return data to true
        }

        override val mainPage =
                        mainPageOf(
                                        "$apiUrl/anime/ranking?ranking_type=all&limit=$mediaLimit&offset=" to
                                                        "Top Anime Series",
                                        "$apiUrl/anime/ranking?ranking_type=airing&limit=$mediaLimit&offset=" to
                                                        "Top Airing Anime",
                                        "$apiUrl/anime/ranking?ranking_type=bypopularity&limit=$mediaLimit&offset=" to
                                                        "Popular Anime",
                                        "$apiUrl/anime/ranking?ranking_type=favorite&limit=$mediaLimit&offset=" to
                                                        "Top Favorited Anime",
                                        "$apiUrl/anime/suggestions?limit=$mediaLimit&offset=" to
                                                        "Suggestions",
                                        "Personal" to "Personal"
                        )

        override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
                if (request.name.equals("Personal")) {
                        // Reading and manipulating personal library
                        api.loginInfo()
                                        ?: return newHomePageResponse(
                                                        "Login required for personal content.",
                                                        emptyList<SearchResponse>(),
                                                        false
                                        )
                        var homePageList =
                                        api.getPersonalLibrary().allLibraryLists.mapNotNull {
                                                if (it.items.isEmpty()) return@mapNotNull null
                                                val libraryName =
                                                                it.name.asString(
                                                                                plugin.activity
                                                                                                ?: return@mapNotNull null
                                                                )
                                                HomePageList(
                                                                "${request.name}: $libraryName",
                                                                it.items
                                                )
                                        }
                        return newHomePageResponse(homePageList, false)
                } else {
                        // Other new sections will be generated if toSearchResponseList() is
                        // overridden
                        val data = request.toSearchResponseList(page)
                        return newHomePageResponse(request.name, data.first, data.second)
                }
        }

        override suspend fun load(url: String): LoadResponse {
                val id = url.removeSuffix("/").substringAfterLast("/")
                val data =
                                api.getResult(id)
                                                ?: throw ErrorLoadingException(
                                                                "Unable to fetch show details"
                                                )
                Log.d("rowdy", data.toString())
                var year = data.startDate?.div(1000)?.div(86400)?.div(365)?.plus(1970)?.toInt()
                val epCount = data.nextAiring?.episode?.minus(1) ?: data.totalEpisodes ?: 0
                val episodes =
                                (1..epCount).map { i ->
                                        val linkData =
                                                        LinkData(
                                                                                        title =
                                                                                                        data.title,
                                                                                        year = year,
                                                                                        season = 1,
                                                                                        episode = i,
                                                                                        isAnime =
                                                                                                        true
                                                                        )
                                                                        .toStringData()
                                        Episode(linkData, season = 1, episode = i)
                                }
                return newAnimeLoadResponse(data.title ?: "", url, TvType.Anime) {
                        addMalId(id.toInt())
                        addEpisodes(DubStatus.Subbed, episodes)
                        this.recommendations = data.recommendations
                }
        }

        override suspend fun loadLinks(
                        data: String,
                        isCasting: Boolean,
                        subtitleCallback: (SubtitleFile) -> Unit,
                        callback: (ExtractorLink) -> Unit
        ): Boolean {
                val mediaData = AppUtils.parseJson<LinkData>(data)
                invokeExtractors(Category.ANIME, mediaData, subtitleCallback, callback)
                return true
        }

        data class MalApiResponse(
                        @JsonProperty("data") val data: Array<MalApiData>? = null,
        ) {
                data class MalApiData(
                                @JsonProperty("node") val node: MalApiNode,
                ) {
                        data class MalApiNode(
                                        @JsonProperty("id") val id: Int,
                                        @JsonProperty("title") val title: String,
                                        @JsonProperty("main_picture")
                                        val picture: MalApiNodePicture,
                        ) {
                                data class MalApiNodePicture(
                                                @JsonProperty("medium") val medium: String,
                                                @JsonProperty("large") val large: String,
                                )
                        }
                }
        }
}
