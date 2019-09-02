package devon.exam

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import androidx.collection.LruCache
import com.bumptech.glide.disklrucache.DiskLruCache
import devon.exam.model.TypicodePhotos
import devon.exam.network.BitmapWorkerTask
import kotlinx.android.synthetic.main.gridview_item.view.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class CustomGridAdapter(
    private val context: Context,
    private val photos: List<TypicodePhotos>,
    private val callback: TaskCallback
) : BaseAdapter() {

    class ViewHolder(itemView: View, val callback: TaskCallback) {
        private val idTextView = itemView.grid_id
        private val titleTextView = itemView.grid_title
        private val thumbnailUrlTextView = itemView.grid_thumbnailUrl
        private val layout = itemView.gridLayout

        private var memoryCache: LruCache<String, Bitmap>

        private val DISK_CACHE_SIZE = 1024 * 1024 * 100 // 100MB
        private val DISK_CACHE_SUBDIR = "thumbnails"
        private var diskLruCache: DiskLruCache? = null
        private val diskCacheLock = ReentrantLock()
        private val diskCacheLockCondition: Condition = diskCacheLock.newCondition()
        private var diskCacheStarting = true

        init {
            // Get max available VM memory, exceeding this amount will throw an
            // OutOfMemory exception. Stored in kilobytes as LruCache takes an
            // int in its constructor.
            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

            // Use 1/8th of the available memory for this memory cache.
            val cacheSize = maxMemory

            memoryCache = object : LruCache<String, Bitmap>(cacheSize) {

                override fun sizeOf(key: String, bitmap: Bitmap): Int {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.byteCount / 1024
                }
            }

            // Initialize disk cache on background thread
//            val cacheDir = getDiskCacheDir(itemView.context, DISK_CACHE_SUBDIR)
//            InitDiskCacheTask().execute(cacheDir)
        }

        fun bind(context: Context, item: TypicodePhotos) {
            idTextView.text = item.id.toString()
            titleTextView.text = item.title
            thumbnailUrlTextView.text = item.thumbnailUrl

            getBitmapFromMemCache(item.thumbnailUrl)?.also {
                val image = BitmapDrawable(context.resources, it)
                layout.background = image
            } ?: run {
                layout.setBackgroundColor(context.resources.getColor(android.R.color.white))
                val customerExecutorService: ExecutorService =
                    ThreadPoolExecutor(
                        5, 5, 0, TimeUnit.MILLISECONDS,
                        LinkedBlockingDeque<Runnable>()
                    )
                val task =
                    BitmapWorkerTask(context, layout, memoryCache)
                        .executeOnExecutor(customerExecutorService, item.thumbnailUrl)
                callback.onSuccess(task)
            }
        }

        private fun getBitmapFromMemCache(key: String): Bitmap? {
            return memoryCache.get(key)
        }

        // Creates a unique subdirectory of the designated app cache directory. Tries to use external
        // but if not mounted, falls back on internal storage.
//        fun getDiskCacheDir(context: Context, uniqueName: String): File {
//            // Check if media is mounted or storage is built-in, if so, try and use external cache dir
//            // otherwise use internal cache dir
//            val cachePath =
//                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
//                    || !isExternalStorageRemovable()
//                ) {
//                    context.externalCacheDir.path
//                } else {
//                    context.cacheDir.path
//                }
//
//            return File(cachePath + File.separator + uniqueName)
//        }
//
//        inner class InitDiskCacheTask : AsyncTask<File, Void, Void>() {
//            override fun doInBackground(vararg params: File): Void? {
//                diskCacheLock.withLock {
//                    val cacheDir = params[0]
//                    diskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE.toLong())
//                    diskCacheStarting = false // Finished initialization
//                    diskCacheLockCondition.signalAll() // Wake any waiting threads
//                }
//                return null
//            }
//        }
    }

    override fun getCount(): Int {
        return photos.size
    }

    override fun getItem(position: Int): TypicodePhotos {
        return photos[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun onCreateView(parent: ViewGroup) =
        LayoutInflater.from(context)
            .inflate(R.layout.gridview_item, parent, false).apply {
                layoutParams = GridView@ AbsListView.LayoutParams(GridView.AUTO_FIT, 200)
            }

    private fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(context, getItem(position))
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: onCreateView(parent).apply {
            tag = ViewHolder(this, callback)
        }
        onBindViewHolder(view.tag as ViewHolder, position)
        return view
    }
}
