package com.Rowdycado

import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.network.WebViewResolver
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.math.E


class HentaiBar : MainAPI() {
    override var mainUrl = "https://hentaibar.com"
    override var name = "HentaiBar"
    override val hasQuickSearch = false
    override val hasMainPage = true
    override val supportedTypes = setOf(
        TvType.NSFW
    )

    override val mainPage =
    mainPageOf(
        "" to "Latest",
        "most-popular" to "Most Popular",
        "top-rated" to "Top Rated",
        "longest" to "Longest",
        "most-commented" to "Most Commented"

    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest):
            HomePageResponse? {
        var list = mutableListOf<AnimeSearchResponse>()
        if (request.data == "series") {
            val res = app.get("$mainUrl/${request.data}/$page").document
            res.select("div.cards__list > .cards__item").forEach { cards ->

                val name = cards.selectFirst("a")?.attr("title") ?: ""
                val poster = cards.selectFirst("img")?.attr("src") ?: ""
                val url = cards.selectFirst("a")?.attr("href") ?: ""
                list.add(newAnimeSearchResponse(name, url)
                {
                    this.posterUrl = poster
                })
            }
            return newHomePageResponse(
                list = HomePageList(
                    name = request.name,
                    list = list,
                    isHorizontalImages = true
                ),
                hasNext = true
            )

        } else {
            val res = app.get("$mainUrl/${request.data}/$page").document
            res.select("div.cards__list > .cards__item").forEach { cards ->

                val name = cards.selectFirst("a")?.attr("title") ?: ""
                val poster = cards.selectFirst("a > span img")?.attr("src") ?: ""
                val url = cards.selectFirst("a")?.attr("href") ?: ""
                list.add(newAnimeSearchResponse(name, url)
                {
                    this.posterUrl = poster
                })
            }
            return newHomePageResponse(
                list = HomePageList(
                    name = request.name,
                    list = list,
                    isHorizontalImages = true
                ),
                hasNext = true
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
       val result = app.get(url).document
        val background =
            result.selectFirst("div.player-bg")?.attr("style")?.substringAfter("url('")
                ?.replace("')", "")
        val name = result.selectFirst("meta[property=og:title]")?.attr("content") ?: ""

        return newMovieLoadResponse(name, url, TvType.Anime, url) {
            this.backgroundPosterUrl = if (background.isNullOrEmpty()) result.selectFirst("meta[property=og:image]")?.attr("content")?.trim() else background
        }
    }
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
               var url = "https://www.youtube.com/watch?v=PLWyAeEg2_k&list=WL&index=3"
        loadExtractor(url, subtitleCallback, callback)


      





    return true
    }
}
