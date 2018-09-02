package pw.janyo.whatanime.factory

import com.google.gson.Gson
import okhttp3.ResponseBody
import java.io.InputStream
import java.io.InputStreamReader

object GsonFactory {
	val gson = Gson()

	fun <T> parseInputStream(inputStream: InputStream, clazz: Class<T>): T = gson.fromJson(InputStreamReader(inputStream), clazz)

	inline fun <reified T> parse(inputStream: InputStream): T = parseInputStream(inputStream, T::class.java)

	inline fun <reified T> parse(responseBody: ResponseBody): T = parseInputStream(responseBody.byteStream(), T::class.java)
}