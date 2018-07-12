package pw.janyo.whatanime.repository

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.factory.RetrofitFactory
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.utils.Base64
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import vip.mystery0.tools.utils.FileTools
import java.io.File

object SearchRepository {
	private val searchApi = RetrofitFactory.retrofit.create(SearchApi::class.java)
	private val token = String(Base64.decode(Constant.token))

	fun convertUriToFile(path: String): LiveData<File> {
		val file = MutableLiveData<File>()
		file.value = File(path)
		return file
	}

	fun search(file: File): LiveData<ArrayList<Docs>> {
		val docs = MutableLiveData<ArrayList<Docs>>()
		val base64 = FileTools.compressImage(Bitmap.CompressFormat.JPEG, file, 1000, 10)
		searchApi.search(token, base64, null)
				.enqueue(object : Callback<Animation> {
					override fun onFailure(call: Call<Animation>?, t: Throwable?) {
						t?.printStackTrace()
					}

					override fun onResponse(call: Call<Animation>?, response: Response<Animation>) {
						docs.value = response.body()!!.docs
					}
				})
		return docs
	}
}