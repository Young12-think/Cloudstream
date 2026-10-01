package com.filmkita

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class FilmkitaPlugin : Plugin() {
    override fun load(context: Context) {
        FilmkitaDomain.appContext = context.applicationContext
        openSettings = { ctx -> FilmkitaDomain.openSettings(ctx) }
        registerMainAPI(Filmkita())
        registerExtractorAPI(Dingtezuni())
        registerExtractorAPI(Movearnpre())
        registerExtractorAPI(Mivalyo())
        registerExtractorAPI(Bingezove())
        registerExtractorAPI(Ryderjet())
        registerExtractorAPI(HlsTereaLayarwibu())
    }
}
