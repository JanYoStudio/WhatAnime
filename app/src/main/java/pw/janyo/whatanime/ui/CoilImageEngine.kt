package pw.janyo.whatanime.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.widget.ImageView
import coil.ImageLoader
import coil.api.load
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.zhihu.matisse.engine.ImageEngine

class CoilImageEngine : ImageEngine {
	override fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri?) {
		imageView.scaleType = ImageView.ScaleType.FIT_CENTER
		imageView.load(uri) {
			size(resizeX, resizeY)
		}
	}

	override fun loadGifImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri?) {
		val imageLoader = ImageLoader.Builder(context)
				.componentRegistry {
					if (SDK_INT >= 28) {
						add(ImageDecoderDecoder())
					} else {
						add(GifDecoder())
					}
				}
				.build()
		imageView.load(uri, imageLoader) {
			size(resizeX, resizeY)
		}
	}

	override fun supportAnimatedGif(): Boolean = true

	override fun loadGifThumbnail(context: Context, resize: Int, placeholder: Drawable?, imageView: ImageView, uri: Uri?) {
		imageView.scaleType = ImageView.ScaleType.CENTER_CROP
		imageView.load(uri) {
			placeholder(placeholder)
			size(resize)
		}
	}

	override fun loadThumbnail(context: Context, resize: Int, placeholder: Drawable?, imageView: ImageView, uri: Uri?) {
		imageView.scaleType = ImageView.ScaleType.CENTER_CROP
		imageView.load(uri) {
			placeholder(placeholder)
			size(resize)
		}
	}
}