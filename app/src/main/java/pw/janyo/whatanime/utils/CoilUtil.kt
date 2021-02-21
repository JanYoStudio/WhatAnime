package pw.janyo.whatanime.utils

import android.net.Uri
import android.widget.ImageView
import coil.load
import coil.request.CachePolicy
import coil.request.Disposable
import java.io.File

fun ImageView.loadWithoutCache(uri: String?): Disposable = load(uri) {
    diskCachePolicy(CachePolicy.DISABLED)
}

fun ImageView.loadWithoutCache(uri: Uri?): Disposable = load(uri) {
    diskCachePolicy(CachePolicy.DISABLED)
}

fun ImageView.loadWithoutCache(file: File?): Disposable = load(file) {
    diskCachePolicy(CachePolicy.DISABLED)
}