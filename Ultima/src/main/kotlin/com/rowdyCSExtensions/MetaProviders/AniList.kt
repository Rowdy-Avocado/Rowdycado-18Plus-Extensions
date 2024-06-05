package com.KillerDogeEmpire

import com.KillerDogeEmpire.UltimaMediaProvidersUtils.invokeExtractors
import com.KillerDogeEmpire.UltimaUtils.Category
import com.KillerDogeEmpire.UltimaUtils.LinkData
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.DubStatus
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addAniListId
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
import com.lagradost.cloudstream3.syncproviders.providers.AniListApi
import com.lagradost.cloudstream3.syncproviders.providers.AniListApi.LikePageInfo
import com.lagradost.cloudstream3.syncproviders.providers.AniListApi.Media
import com.lagradost.cloudstream3.utils.AppUtils
import com.lagradost.cloudstream3.utils.ExtractorLink

class AniList(val plugin: UltimaPlugin) : MainAPI() {
        override var name = "AniList"
        override var mainUrl = "https://anilist.co"
        override var supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)
        override var lang = "en"
        override val supportedSyncNames = setOf(SyncIdName.Anilist)
        override val hasMainPage = true
        override val hasQuickSearch = false
        private val api = AccountManager.aniListApi
        private val apiUrl = "https://graphql.anilist.co"
        private val headerJSON =
                        mapOf("Accept" to "application/json", "Content-Type" to "application/json")

        protected fun Any.toStringData(): String {
                return mapper.writeValueAsString(this)
        }

        private suspend fun anilistAPICall(query: String): AnilistData {
                val data = mapOf("query" to query)
                val res =
                                app.post(apiUrl, headers = headerJSON, data = data)
                                                .parsedSafe<AnilistAPIResponse>()
                                                ?: throw Exception(
                                                                "Unable to fetch or parse Anilist api response"
                                                )
                return res.data
        }

        private fun AniListApi.Media.toSearchResponse(): SearchResponse {
                val title = this.title.english ?: this.title.romaji ?: ""
                val url = "$mainUrl/anime/${this.id}"
                val posterUrl = this.coverImage.large
                return newAnimeSearchResponse(title, url, TvType.Anime) {
                        this.posterUrl = posterUrl
                }
        }

        private suspend fun MainPageRequest.toSearchResponseList(
                        page: Int
        ): Pair<List<SearchResponse>, Boolean> {
                val res = anilistAPICall(this.data.replace("###", "$page"))
                val data = res.page.media.map { it.toSearchResponse() }
                val hasNextPage = res.page.pageInfo.hasNextPage ?: false
                return data to hasNextPage
        }

        override val mainPage =
                        mainPageOf(
                                        "query (\$page: Int = ###, \$sort: [MediaSort] = [TRENDING_DESC, POPULARITY_DESC], \$isAdult: Boolean = false) { Page(page: \$page, perPage: 20) { pageInfo { total perPage currentPage lastPage hasNextPage } media(sort: \$sort, isAdult: \$isAdult, type: ANIME) { id idMal season seasonYear format episodes chapters title { english romaji } coverImage { extraLarge large medium } synonyms nextAiringEpisode { timeUntilAiring episode } } } }" to
                                                        "Trending Now",
                                        "query (\$page: Int = ###, \$seasonYear: Int = 2024, \$sort: [MediaSort] = [TRENDING_DESC, POPULARITY_DESC], \$isAdult: Boolean = false) { Page(page: \$page, perPage: 20) { pageInfo { total perPage currentPage lastPage hasNextPage } media(sort: \$sort, seasonYear: \$seasonYear, season: SPRING, isAdult: \$isAdult, type: ANIME) { id idMal season seasonYear format episodes chapters title { english romaji } coverImage { extraLarge large medium } synonyms nextAiringEpisode { timeUntilAiring episode } } } }" to
                                                        "Popular This Season",
                                        "query (\$page: Int = ###, \$sort: [MediaSort] = [POPULARITY_DESC], \$isAdult: Boolean = false) { Page(page: \$page, perPage: 20) { pageInfo { total perPage currentPage lastPage hasNextPage } media(sort: \$sort, isAdult: \$isAdult, type: ANIME) { id idMal season seasonYear format episodes chapters title { english romaji } coverImage { extraLarge large medium } synonyms nextAiringEpisode { timeUntilAiring episode } } } }" to
                                                        "All Time Popular",
                                        "query (\$page: Int = ###, \$sort: [MediaSort] = [SCORE_DESC], \$isAdult: Boolean = false) { Page(page: \$page, perPage: 20) { pageInfo { total perPage currentPage lastPage hasNextPage } media(sort: \$sort, isAdult: \$isAdult, type: ANIME) { id idMal season seasonYear format episodes chapters title { english romaji } coverImage { extraLarge large medium } synonyms nextAiringEpisode { timeUntilAiring episode } } } }" to
                                                        "Top 100 Anime",
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
                val data = api.getResult(id)
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
                        addAniListId(id.toInt())
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

        data class AnilistAPIResponse(
                        @JsonProperty("data") val data: AnilistData,
        )

        data class AnilistData(
                        @JsonProperty("Page") val page: AnilistPage,
        )

        data class AnilistPage(
                        @JsonProperty("pageInfo") val pageInfo: LikePageInfo,
                        @JsonProperty("media") val media: List<Media>,
        )
}
