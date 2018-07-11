package pw.janyo.whatanime.classes

import org.litepal.crud.DataSupport

import java.io.Serializable

/**
 * Created by myste.
 */

class History : DataSupport(), Serializable {
	var imaPath: String? = null
	var cachePath: String? = null
	var title: String? = null
	var saveFilePath: String? = null
}
