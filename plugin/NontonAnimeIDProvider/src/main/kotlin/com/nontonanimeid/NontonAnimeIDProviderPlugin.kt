package com.nontonanimeid

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class NontonAnimeIDProviderPlugin: Plugin() {
    override fun load(context: Context) {
        NontonAnimeIDProviderDomain.appContext = context.applicationContext
        openSettings = { ctx -> NontonAnimeIDProviderDomain.openSettings(ctx) }
        registerMainAPI(NontonAnimeIDProvider())
        registerExtractorAPI(Nontonanimeid())
        registerExtractorAPI(EmbedKotakAnimeid())
        registerExtractorAPI(KotakAnimeidCom())
        registerExtractorAPI(KotakAnimeidLinkS1())
        registerExtractorAPI(KotakAnimeidLinkS2())
        registerExtractorAPI(Gdplayer())
        registerExtractorAPI(Kotaksb())
        registerExtractorAPI(Vidhidepre())
        registerExtractorAPI(Rpmvip())
    }
}