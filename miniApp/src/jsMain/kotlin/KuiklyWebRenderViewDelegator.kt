import com.tencent.kuikly.core.render.web.IKuiklyRenderExport
import com.tencent.kuikly.core.render.web.runtime.miniapp.expand.KuiklyRenderViewDelegator
import com.tencent.kuikly.core.render.web.expand.KuiklyRenderViewDelegatorDelegate
import com.tencent.kuikly.core.render.web.runtime.miniapp.core.Transform
import components.KRMyView
import components.KRWebView
import dom.MiniWebViewElement

/**
 * Implement the delegate interface provided by Web Render
 */

class KuiklyWebRenderViewDelegator : KuiklyRenderViewDelegatorDelegate {

    // mini render delegate
    val delegate = KuiklyRenderViewDelegator(this)

    /**
     * Register custom modules
     */
    override fun registerExternalModule(kuiklyRenderExport: IKuiklyRenderExport) {
        // Register bridge module
//        kuiklyRenderExport.moduleExport(HRBridgeModule.MODULE_NAME) {
//            HRBridgeModule()
//        }
//        kuiklyRenderExport.moduleExport(HRCacheModule.MODULE_NAME) {
//            HRCacheModule()
//        }
        super.registerExternalModule(kuiklyRenderExport)
    }

    override fun registerExternalRenderView(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalRenderView(kuiklyRenderExport)

        // Add template alias for custom views
        Transform.addComponentsAlias(
            MiniWebViewElement.NODE_NAME,
            MiniWebViewElement.componentsAlias
        )

        // Register custom views
        kuiklyRenderExport.renderViewExport(KRWebView.VIEW_NAME, {
            KRWebView()
        })

        // Register custom views
        kuiklyRenderExport.renderViewExport(KRMyView.VIEW_NAME, {
            KRMyView()
        })
    }
}