package com.nomat

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.M3u8Helper.Companion.generateM3u8
import java.net.URI
import com.lagradost.api.Log
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.extractors.VidHidePro
import com.lagradost.cloudstream3.extractors.Filesim

open class Dingtezuni : ExtractorApi() {
    override val name = "Earnvids"
    override val mainUrl = "https://dingtezuni.com"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val headers = mapOf(
            "Sec-Fetch-Dest" to "empty",
            "Sec-Fetch-Mode" to "cors",
            "Sec-Fetch-Site" to "cross-site",
            "Origin" to mainUrl,
            "User-Agent" to USER_AGENT,
        )

        val response = app.get(getEmbedUrl(url), referer = referer)
        val script = if (!getPacked(response.text).isNullOrEmpty()) {
            var result = getAndUnpack(response.text)
            if (result.contains("var links")) result = result.substringAfter("var links")
            result
        } else {
            response.document.selectFirst("script:containsData(sources:)")?.data()
        } ?: return

        Regex(":\\s*\"(.*?m3u8.*?)\"").findAll(script).forEach { match ->
            generateM3u8(
                name,
                fixUrl(match.groupValues[1]),
                referer = "$mainUrl/",
                headers = headers
            ).forEach(callback)
        }
    }

    private fun getEmbedUrl(url: String): String = when {
        url.contains("/d/") -> url.replace("/d/", "/v/")
        url.contains("/download/") -> url.replace("/download/", "/v/")
        url.contains("/file/") -> url.replace("/file/", "/v/")
        else -> url.replace("/f/", "/v/")
    }
}

class Movearnpre : Dingtezuni() {
    override var name = "Movearnpre"
    override var mainUrl = "https://movearnpre.com"
}

class Mivalyo : Dingtezuni() {
    override var name = "Earnvids"
    override var mainUrl = "https://mivalyo.com"
}

class Ryderjet : Dingtezuni() {
    override var name = "Ryderjet"
    override var mainUrl = "https://ryderjet.com"
}

class Bingezove : Dingtezuni() {
    override var name = "Earnvids"
    override var mainUrl = "https://bingezove.com"
}

class Hydrax: VidHidePro() {
    override var name = "Hydrax"
    override var mainUrl = "https://playhydrax.com"
}


private suspend fun getUrlWithReferer(
    sourceName: String,
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
) {
    val trueReferer = referer ?: "https://nontonhemat.link/"
    val embedUrl = url.replace("/download/", "/e/")
    val res = app.get(embedUrl, referer = trueReferer, headers = mapOf(
        "Referer" to trueReferer,
        "Sec-Fetch-Dest" to "iframe",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-Site" to "cross-site"
    ))
    val packed = getPacked(res.text)
    val script = if (!packed.isNullOrEmpty()) getAndUnpack(res.text)
    else res.document.selectFirst("script:containsData(sources:)")?.data() ?: ""

    Regex("""sources\s*:\s*\[\s*\{[^}]*file\s*:\s*['"]([^'"]+)['"]""").find(script)?.groupValues?.getOrNull(1)?.let { m3u8 ->
        M3u8Helper.generateM3u8(sourceName, m3u8, referer = trueReferer).forEach(callback)
    }
    if (script.isEmpty()) {
        // fallback: WebView
        M3u8Helper.generateM3u8(sourceName, url, referer = trueReferer).forEach(callback)
    }
}

class FileMoonSx : Filesim() {
    override val mainUrl = "https://filemoon.sx"
    override val name = "FileMoonSx"
    override val requiresReferer = true
    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) =
        getUrlWithReferer(name, url, referer, subtitleCallback, callback)
}

class Streamhide : Filesim() {
    override var name = "Streamhide"
    override var mainUrl = "https://streamhide.to"
    override val requiresReferer = true
    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) =
        getUrlWithReferer(name, url, referer, subtitleCallback, callback)
}

class Filelions : Filesim() {
    override var name = "Filelions"
    override var mainUrl = "https://filelions.to"
    override val requiresReferer = true
    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) =
        getUrlWithReferer(name, url, referer, subtitleCallback, callback)
}
