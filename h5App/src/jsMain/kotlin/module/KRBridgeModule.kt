package module

import com.tencent.kuikly.core.render.web.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.web.ktx.KuiklyRenderCallback
import com.tencent.kuikly.core.render.web.nvi.serialization.json.JSONException
import com.tencent.kuikly.core.render.web.nvi.serialization.json.JSONObject
import utils.Ui
import kotlin.js.Date

/**
 * Bridge interface module used by business side
 */
class KRBridgeModule : KuiklyRenderBaseModule() {
    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        return when (method) {
            "toast" -> {
                toast(params)
            }

            "log" -> {
                console.log(params)
            }

            "currentTimestamp" -> {
                currentTimestamp(params)
            }

            "dateFormatter" -> {
                dateFormatter(params)
            }

            else -> {
                callback?.invoke(
                    mapOf(
                        "code" to -1,
                        "message" to "Method does not exist"
                    )
                )
            }
        }
    }

    /**
     * Show toast message on page
     */
    private fun toast(params: String?) {
        if (params != null) {
            try {
                val message = JSONObject(params)
                Ui.showToast(message)
            } catch (e: JSONException) {
                // JSON parsing failed
                console.error("toast json parse error", e)
            }
        }
    }

    private fun currentTimestamp(params: String?): String = Date.now().toString()

    private fun formatDate(date: Date, format: String): String {
        fun pad(num: Int) = num.toString().padStart(2, '0')
        val replacements = mapOf(
            "yyyy" to date.getFullYear().toString(),
            "MM" to pad(date.getMonth() + 1),
            "dd" to pad(date.getDate()),
            "HH" to pad(date.getHours()),
            "mm" to pad(date.getMinutes()),
            "ss" to pad(date.getSeconds())
        )
        var result = format
        for ((k, v) in replacements) {
            result = result.replace(k, v)
        }
        return result
    }

    private fun dateFormatter(params: String?): String {
        val paramJSONObject = JSONObject(params ?: "{}")
        val date = Date(paramJSONObject.optLong("timeStamp"))
        return formatDate(date, paramJSONObject.optString("format"))
    }

    companion object {
        const val MODULE_NAME = "HRBridgeModule"
    }
}