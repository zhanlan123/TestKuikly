import com.tencent.kuikly.core.render.web.ktx.SizeI
import kotlinx.browser.document
import kotlinx.browser.window
import utils.URL

/**
 * WebApp entry, use renderView delegate method to initialize and create renderView
 */
fun main() {
    console.log("##### Kuikly Web Render")
    // Root container id, note that this needs to match the container id in the actual index.html file
    val containerId = "root"
    val webSign = "is_web"
    // Process URL parameters
    val urlParams = URL.parseParams(window.location.href)
    // Page name, default is router
    val pageName = urlParams["page_name"] ?: "router"
    // Container size
    val containerWidth = window.innerWidth
    val containerHeight = window.innerHeight
    // Business parameters
    val params: MutableMap<String, String> = mutableMapOf()
    // Add business parameters
    if (urlParams.isNotEmpty()) {
        // Append all URL parameters to business parameters
        urlParams.forEach { (key, value) ->
            params[key] = value
        }
    }
    // Add web-specific parameters
    params[webSign] = "1"
    // Page parameter Map
    val paramMap = mapOf(
        "statusBarHeight" to 0f,
        "activityWidth" to containerWidth,
        "activityHeight" to containerHeight,
        "param" to params,
    )

    // Initialize delegator
    val delegator = KuiklyWebRenderViewDelegator()
    // Create render view
    delegator.init(
        containerId, pageName, paramMap, SizeI(
            containerWidth,
            containerHeight,
        )
    )
    // Trigger resume
    delegator.resume()

    // Register visibility event
    document.addEventListener("visibilitychange", {
        val hidden = document.asDynamic().hidden as Boolean
        if (hidden) {
            // Page hidden
            delegator.pause()
        } else {
            // Page restored
            delegator.resume()
        }
    })
}