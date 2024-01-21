package com.IndianTV

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import java.net.URL

class IndianTVProvider : MainAPI() {
    override var mainUrl = "https://livesportsclub.me"
    override var name = "IndianTV"
    override val supportedTypes = setOf(TvType.Live)
    override var lang = "hi"
    override val hasMainPage = true

    override val mainPage = mainPageOf(
        "$mainUrl/hls/tata/" to "Tata",
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get(request.data + page).document
        val home = document.select("div.box1").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse {
        val href = fixUrl(this.select("a").attr("href")).toString()
        val titleRaw = this.selectFirst("h2.text-center.text-sm.font-bold")?.text()?.trim() ?: "Unknown Title"
        val title = if (titleRaw.isNullOrBlank()) "Unknown LiveStream" else titleRaw.toString()
        val posterUrl = URL(mainUrl).resolve(this.selectFirst("img")?.attr("src")).toString()

        // Extract category information
        val categoryRaw = this.selectFirst("p.text-xs.text-center")?.text()?.trim() ?: "Unknown Category"
        val category = if (categoryRaw.isNullOrBlank()) "Unknown Category" else categoryRaw.toString()

        return newMovieSearchResponse(title, href, TvType.Live) {
            this.posterUrl = posterUrl
            this.category = category
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return listOf()
    }
}