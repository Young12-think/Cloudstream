

package com.hexated

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class NimegamiPlugin : Plugin() {
    override fun load(context: Context) {
        NimegamiDomain.appContext = context.applicationContext
        openSettings = { ctx -> NimegamiDomain.openSettings(ctx) }
    
        registerMainAPI(Nimegami())
        registerExtractorAPI(DlganExtractor())
        registerExtractorAPI(BerkasDriveExtractor())
        registerExtractorAPI(StorDlExtractor())
        registerExtractorAPI(DlganHalahganExtractor())
    }
}
