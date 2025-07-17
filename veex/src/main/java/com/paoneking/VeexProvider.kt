package com.paoneking

import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import kotlinx.coroutines.runBlocking

class VeexProvider : MainAPI() { // all providers must be an instance of MainAPI
    override var name = "Veex Netflix"
    override val hasMainPage = true
    override var lang = "ne"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Cartoon
    )

    override val mainPage = mainPageOf(
        BASE_URL.format("movie") to "Movies",
//        BASE_URL.format("serie") to "Series",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val res = app.get(request.data).parsedSafe<List<MovieItem>>()
        val searchResponses: List<SearchResponse> = res?.map { it.toSearchResponse() }
            ?: throw ErrorLoadingException("Invalid JSON response")
        return newHomePageResponse(
            request.name, searchResponses
        )
    }

    private fun MovieItem.toSearchResponse(): SearchResponse {
        return newMovieSearchResponse(
            title,
            id.toString(),
            TvType.Movie
        ) {
            this.posterUrl = cover
        }
    }

    companion object {
        const val BASE_URL =
            "https://netflix.veex.cc/api/%s/by/filtres/0/created/0/4F5A9C3D9A86FA54EACEDDD635185/26a3547f-6db2-44f3-b4c8-3b8dcf1e871a/"
    }
}

fun main() = runBlocking {
    val res =
        app.get("https://netflix.veex.cc/api/movie/by/filtres/0/created/0/4F5A9C3D9A86FA54EACEDDD635185/26a3547f-6db2-44f3-b4c8-3b8dcf1e871a/")
            .parsedSafe<List<MovieItem>>()
    println("res: $res")
}

