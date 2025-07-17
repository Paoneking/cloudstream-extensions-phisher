package com.paoneking

import com.lagradost.api.Log
import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieSearchResponse

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
        BASE_URL.format("serie") to "Series",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        Log.d("veex", "request: $request, page: $page")
        val response = app.get(request.data)
        val stringRes = response.text
        Log.d("veex", "resposetxt: $stringRes")
        val res = response.parsedSafe<List<MovieItem>>()
        Log.d("veex", "response: $res")
        val searchResponses: List<SearchResponse> = res?.map { it.toSearchResponse() }
            ?: throw ErrorLoadingException("Invalid JSON response")
        return newHomePageResponse(
            listOf(
                HomePageList(
                    request.name,
                    searchResponses,
                    false
                )
            )
        )
    }

    private fun MovieItem.toSearchResponse(type: String? = null): SearchResponse {
        return newMovieSearchResponse(
            title,
            image,
            TvType.Movie
        )
    }

    companion object {
        const val BASE_URL =
            "https://netflix.veex.cc/api/%s/by/filtres/0/created/0/4F5A9C3D9A86FA54EACEDDD635185/26a3547f-6db2-44f3-b4c8-3b8dcf1e871a/"
    }
}

