package pw.janyo.whatanime.model.request

import pw.janyo.whatanime.BuildConfig

data class TestRequest(val versionName: String = BuildConfig.VERSION_NAME,
					   val versionCode: Int = BuildConfig.VERSION_CODE)