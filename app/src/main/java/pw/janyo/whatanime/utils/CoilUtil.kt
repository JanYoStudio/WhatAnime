package pw.janyo.whatanime.utils

import android.net.Uri
import android.widget.ImageView
import coil.api.load
import coil.request.CachePolicy
import coil.request.RequestDisposable
import java.io.File

fun ImageView.loadWithoutCache(uri: String?): RequestDisposable = load(uri) {
	diskCachePolicy(CachePolicy.DISABLED)
}

fun ImageView.loadWithoutCache(uri: Uri?): RequestDisposable = load(uri) {
	diskCachePolicy(CachePolicy.DISABLED)
}

fun ImageView.loadWithoutCache(file: File?): RequestDisposable = load(file) {
	diskCachePolicy(CachePolicy.DISABLED)
}