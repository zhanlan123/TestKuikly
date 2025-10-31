package top.yinxueqin.testkuikly.adapter

import top.yinxueqin.testkuikly.KRApplication
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tencent.kuikly.core.render.android.KuiklyRenderViewContext
import com.tencent.kuikly.core.render.android.adapter.HRImageLoadOption
import com.tencent.kuikly.core.render.android.adapter.IKRImageAdapter
import kotlin.math.roundToInt

class KRImageAdapter(val context: Context) : IKRImageAdapter {

    override fun fetchDrawable(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit,
    ) {
        if (imageLoadOption.isBase64()) {
            loadFromBase64(imageLoadOption, callback)
        } else if (imageLoadOption.isWebUrl() || imageLoadOption.isAssets() || imageLoadOption.isFile()) {
            // http/assets/file 图片使用 glide 加载
            requestImage(imageLoadOption, callback)
        }
    }

    override fun getDrawableWidth(
        kuiklyRenderViewContext: KuiklyRenderViewContext,
        drawable: Drawable
    ): Float {
        return drawable.intrinsicWidth.toFloat()
    }

    override fun getDrawableHeight(
        kuiklyRenderViewContext: KuiklyRenderViewContext,
        drawable: Drawable
    ): Float {
        return drawable.intrinsicHeight.toFloat()
    }

    private fun requestImage(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit,
    ) {
        val src = if (imageLoadOption.isAssets()) {
            val assetPath = imageLoadOption.src.substring(HRImageLoadOption.SCHEME_ASSETS.length)
            "file:///android_asset/$assetPath"
        } else {
            imageLoadOption.src
        }
        val requestBuilder = if (src.endsWith(".gif")) {
            Glide.with(KRApplication.application)
                .asGif()
                .load(src) as RequestBuilder<Drawable>
        } else {
            Glide.with(KRApplication.application)
                .asDrawable()
                .load(src)
        }

        if (imageLoadOption.needResize) {
            requestBuilder.override(imageLoadOption.requestWidth, imageLoadOption.requestHeight)
            when (imageLoadOption.scaleType) {
                ImageView.ScaleType.CENTER_CROP -> requestBuilder.centerCrop()
                ImageView.ScaleType.FIT_CENTER -> requestBuilder.fitCenter()
                else -> {}
            }
        }
        requestBuilder
            .into(object : CustomTarget<Drawable>() {

                override fun onLoadCleared(placeholder: Drawable?) {
                    callback.invoke(null)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    callback.invoke(null)
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?,
                ) {
                    callback.invoke(resource)
                }
            })
    }

    private fun loadFromBase64(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit,
    ) {
        execOnSubThread {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val bytes = Base64.decode(imageLoadOption.src.split(",")[1], Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            try {
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                options.inJustDecodeBounds = false
                try {
                    options.inSampleSize = calculateInSampleSize(
                        options,
                        imageLoadOption.requestWidth,
                        imageLoadOption.requestHeight
                    )
                } catch (e: ArithmeticException) { // 偶现报除以0，可能是inSampleSize超过int的范围溢出了。这里catch兜底使用原始inSampleSize
                    Log.d("ECHRImageAdapter", "loadFromBase64: $e")
                }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                callback.invoke(BitmapDrawable(Resources.getSystem(), bitmap))
            } catch (e: OutOfMemoryError) {
                Log.d("ECHRImageAdapter", "oom happen: $e")
            }
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        return if (reqWidth != 0 && reqHeight != 0 && reqWidth != -1 && reqHeight != -1) {
            var height = options.outHeight
            var width = options.outWidth
            var inSampleSize: Int
            inSampleSize = 1
            while (height > reqHeight && width > reqWidth) {
                val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
                val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
                val ratio = if (heightRatio > widthRatio) heightRatio else widthRatio
                if (ratio < 2) {
                    break
                }
                width = width shr 1
                height = height shr 1
                inSampleSize = inSampleSize shl 1
            }
            inSampleSize
        } else {
            1
        }
    }

}