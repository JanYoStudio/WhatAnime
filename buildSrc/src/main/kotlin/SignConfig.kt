import java.io.File
import java.io.FileInputStream
import java.util.*

object SignConfig {
    private val propertiesFile = File("local.properties")
    private val properties = Properties()

    init {
        if (propertiesFile.exists()) {
            FileInputStream(propertiesFile).use {
                properties.load(it)
            }
        }
    }

    val signKeyStoreFile: String
        get() = get("SIGN_KEY_STORE_FILE")
    val signKeyStorePassword: String
        get() = get("SIGN_KEY_STORE_PASSWORD")
    val signKeyAlias: String
        get() = get("SIGN_KEY_ALIAS")
    val signKeyPassword: String
        get() = get("SIGN_KEY_PASSWORD")

    private fun get(key: String): String = System.getenv(key) ?: properties.getProperty(key) ?: ""
}