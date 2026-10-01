
package com.hexated

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class KuramanimeProviderPlugin: Plugin() {
    override fun load(context: Context) {
        KuramanimeProviderDomain.appContext = context.applicationContext
        openSettings = { ctx -> KuramanimeProviderDomain.openSettings(ctx) }
        // All providers should be added in this manner. Please don't edit the providers list directly.
        registerMainAPI(KuramanimeProvider())
        registerExtractorAPI(Nyomo())
        registerExtractorAPI(Streamhide())
        registerExtractorAPI(Kuramadrive())
        registerExtractorAPI(Lbx())
        registerExtractorAPI(Sunrong())
    }
}