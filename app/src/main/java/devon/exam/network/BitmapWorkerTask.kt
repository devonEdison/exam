package devon.exam.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.view.ViewGroup
import androidx.collection.LruCache
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class BitmapWorkerTask(
    context: Context,
    layout: ViewGroup,
    private val memoryCache: LruCache<String, Bitmap>
) : AsyncTask<String, Void, Bitmap>() {

    private val layoutReference: WeakReference<ViewGroup>?
    private val context: WeakReference<Context>?
    private lateinit var imageUrl: String

    init {
        // Use a WeakReference to ensure the view can be garbage collected
        layoutReference = WeakReference(layout)
        this.context = WeakReference(context)
    }

    override fun doInBackground(vararg params: String): Bitmap? {
        if (isCancelled) {
            return null
        }
        imageUrl = params[0]
        return downloadImage(imageUrl)
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        if (isCancelled) {
            return
        }
        if (layoutReference != null && bitmap != null) {
            val layout = layoutReference.get()
            try {
                val image = BitmapDrawable(context?.get()?.resources, bitmap)
                layout?.background = image
            } catch (e: Exception) {
            }
        }
    }

    private fun getBitmapFromMemCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }

    private fun downloadImage(URL: String): Bitmap? {
        var bitmap: Bitmap? = null
        var input: InputStream?=null
        try {
            input = openHttpConnection(URL)
            if (input != null) {
                bitmap = BitmapFactory.decodeStream(input)
                addBitmapToMemoryCache(URL, bitmap)
            }
        } catch (e1: IOException) {
        }finally {
            input?.close()
        }

        return bitmap
    }

    private fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        // Add to memory cache as before
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }

    @Throws(IOException::class)
    private fun openHttpConnection(strURL: String?): InputStream? {
        var inputStream: InputStream? = null
        val url = URL(strURL!!)
        val conn = url.openConnection()

        try {
            val httpConn = conn as HttpURLConnection
            httpConn.requestMethod = "GET"
            httpConn.connect()
            if (isCancelled) {
                return null
            }
            if (httpConn.responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.inputStream
            }
        } catch (ex: Exception) {
        }

        return inputStream
    }
}