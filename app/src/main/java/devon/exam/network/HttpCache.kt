package devon.exam.network

import android.app.Application
import okhttp3.Cache
import java.io.File

/**
 * Http cache.
 */
object HttpCache {

    private lateinit var cache: Cache

    @JvmStatic
    fun init(application: Application) {
        cache = Cache(File(application.applicationContext.cacheDir, "http"), 10 * 1024 * 1024L)
    }

    @JvmStatic
    fun getInstance() = cache
}
