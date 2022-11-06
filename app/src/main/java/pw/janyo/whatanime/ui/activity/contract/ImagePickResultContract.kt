package pw.janyo.whatanime.ui.activity.contract

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

class ImagePickResultContract : ActivityResultContract<String, Intent?>() {
    override fun createIntent(context: Context, input: String): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = input
            }
        } else {
            Intent(Intent.ACTION_PICK).apply {
                setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, input)
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? = intent
}