package com.lk21

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addScore
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import java.net.URI

class LK21Provider : MainAPI() {
    override var mainUrl = "https://tv12.lk21official.cc"
    override var name = "LK21"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.AsianDrama,
        TvType.Anime,
    )

    override val mainPage get() = mainPageOf(
        "latest/page/%d/"         to "Terbaru",
        "latest-series/page/%d/"  to "Series Terbaru",
        "populer/page/%d/"        to "Terpopuler",
        "genre/action/page/%d/"   to "Action",
        "genre/horror/page/%d/"   to "Horror",
        "genre/comedy/page/%d/"   to "Comedy",
        "genre/romance/page/%d/"  to "Romance",
        "genre/drama/page/%d/"    to "Drama",
        "genre/animation/page/%d/" to "Animation",
        "country/south-korea/page/%d/" to "Korea",
        "country/japan/page/%d/"  to "Japan",
        "country/usa/page/%d/"    to "USA",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page == 1) "$mainUrl/${request.data.replace("page/%d/", "")}"
                  else "$mainUrl/${request.data.format(page)}"
        val document = app.get(url).document
        val items = document.select("div.gallery-grid a[itemprop=url], div.gallery-grid a")
            .mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val href = fixUrl(attr("href").takeIf { it.isNotBlank() } ?: return null)
        val title = selectFirst("h3.poster-title")?.text()?.trim()
            ?: attr("title").removePrefix("Nonton movie ").removePrefix("Nonton series ")
                .substringBefore(" streaming").trim()
        if (title.isBlank()) return null
        val poster = selectFirst("img")?.let {
            it.attr("src").takeIf { s -> s.isNotBlank() }
                ?: it.attr("data-src")
        }?.let { fixUrlNull(it) }
        val eps = selectFirst("span.episode")
        val quality = selectFirst("span.label")?.text()?.trim()

        return if (eps != null) {
            newAnimeSearchResponse(title, href, TvType.TvSeries) {
                posterUrl = poster
                addSub(eps.selectFirst("strong")?.text()?.toIntOrNull())
            }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) {
                posterUrl = poster
                quality?.let { addQuality(it) }
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/search/?s=$query").document
        return document.select("div.gallery-grid a[itemprop=url], div.gallery-grid a")
            .mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val baseUrl = URI(url).let { "${it.scheme}://${it.host}" }

        val title = document.selectFirst("h1.entry-title, h1[itemprop=name]")?.text()
            ?.substringBefore("Season")?.substringBefore("Episode")?.trim().orEmpty()
        val poster = document.selectFirst("div.single-poster img, img[itemprop=image]")
            ?.let { it.attr("src").takeIf { s -> s.isNotBlank() } ?: it.attr("data-src") }
            ?.let { fixUrlNull(it) }
        val description = document.selectFirst("div.entry-content p, p[itemprop=description]")
            ?.text()?.trim()
        val tags = document.select("a[rel=tag], div.movie-info a[href*=genre]").map { it.text() }
        val year = document.selectFirst("span[itemprop=datePublished], span.year")
            ?.text()?.trim()?.toIntOrNull()
        val rating = document.selectFirst("span[itemprop=ratingValue]")?.text()?.trim()
        val actors = document.select("span[itemprop=actor] a, a[itemprop=actor]").map { it.text() }
        val trailer = document.selectFirst("a.yt-lightbox[href]")?.attr("href")
        val isSeries = document.selectFirst("span.episode") != null || url.contains("/series/")

        return if (isSeries) {
            val episodes = document.select("div#episode-list a, div.eps-list a, ul.episode-list a")
                .mapNotNull { eps ->
                    val epHref = fixUrl(eps.attr("href"))
                    val rawTitle = eps.text().trim()
                    val epNum = Regex("(\\d+)").find(rawTitle)?.value?.toIntOrNull()
                    newEpisode(epHref) {
                        name = rawTitle.ifBlank { "Episode $epNum" }
                        episode = epNum
                        posterUrl = poster
                    }
                }
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                posterUrl = poster
                this.year = year
                plot = description
                this.tags = tags
                addScore(rating)
                addActors(actors)
                trailer?.let { addTrailer(it) }
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                posterUrl = poster
                this.year = year
                plot = description
                this.tags = tags
                addScore(rating)
                addActors(actors)
                trailer?.let { addTrailer(it) }
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        val baseUrl = URI(data).let { "${it.scheme}://${it.host}" }

        // Collect all videonode.de player links from #player-list
        document.select("#player-list a[data-url]").forEach { el ->
            val playerUrl = el.attr("data-url").let { httpsify(it) }
            val server = el.attr("data-server").ifBlank { "default" }
            loadVideonodePlayer(playerUrl, server, baseUrl, subtitleCallback, callback)
        }

        // Fallback: iframe src in main-player
        if (document.select("#player-list a[data-url]").isEmpty()) {
            document.selectFirst("#main-player[src]")?.attr("src")?.let { src ->
                loadVideonodePlayer(httpsify(src), "main", baseUrl, subtitleCallback, callback)
            }
        }

        return true
    }

    private suspend fun loadVideonodePlayer(
        playerUrl: String,
        server: String,
        referer: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        try {
            // Parse host and id from videonode.de URL: /iframe3/<host>/<id>
            val parts = URI(playerUrl).path.trimEnd('/').split("/")
            val host = parts.getOrNull(parts.size - 2) ?: return
            val id = parts.lastOrNull() ?: return

            val apiUrl = "https://videonode.de/api.php"
            val response = app.post(
                apiUrl,
                data = mapOf("host" to host, "id" to id),
                referer = playerUrl,
                headers = mapOf("Content-Type" to "application/x-www-form-urlencoded")
            ).parsedSafe<VideonodeResponse>() ?: return

            val embedUrl = response.embedUrl?.let { httpsify(it) } ?: return
            loadExtractor(embedUrl, referer, subtitleCallback, callback)
        } catch (_: Exception) { }
    }

    private data class VideonodeResponse(val embedUrl: String?)

    private fun Element.getImageAttr(): String = when {
        hasAttr("data-src") -> attr("abs:data-src")
        hasAttr("data-lazy-src") -> attr("abs:data-lazy-src")
        else -> attr("abs:src")
    }

    private fun getBaseUrl(url: String): String =
        URI(url).let { "${it.scheme}://${it.host}" }
}
