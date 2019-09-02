package devon.exam

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import androidx.collection.LruCache
import devon.exam.model.TypicodePhotos
import devon.exam.network.BitmapWorkerTask
import kotlinx.android.synthetic.main.gridview_item.view.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class CustomGridAdapter(
    private val context: Context,
    private val photos: List<TypicodePhotos>,
    private val callback: TaskCallback
) : BaseAdapter() {

    class ViewHolder(
        itemView: View
    ) {
        private val idTextView = itemView.grid_id
        private val titleTextView = itemView.grid_title
        private val thumbnailUrlTextView = itemView.grid_thumbnailUrl
        val layout = itemView.gridLayout
        var asyncTask:AsyncTask<String, Void, Bitmap>? = null

        fun bind(item: TypicodePhotos) {
            idTextView.text = item.id.toString()
            titleTextView.text = item.title
            thumbnailUrlTextView.text = item.thumbnailUrl
        }
    }

    private var memoryCache: LruCache<String, Bitmap>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
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

    @Suppress("REDUNDANT_LABEL_WARNING")
    private fun onCreateView(parent: ViewGroup) =
        LayoutInflater.from(context)
            .inflate(R.layout.gridview_item, parent, false).apply {
                layoutParams = GridView@ AbsListView.LayoutParams(GridView.AUTO_FIT, 200)
            }

    @Suppress("DEPRECATION")
    private fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (viewHolder.asyncTask != null) {
            viewHolder.asyncTask!!.cancel(true)
        }
        getBitmapFromMemCache(photos[position].thumbnailUrl)?.also {
            val image = BitmapDrawable(context.resources, it)
            viewHolder.layout.background = image
        } ?: run {
            viewHolder.layout.setBackgroundColor(context.resources.getColor(android.R.color.white))
            val customerExecutorService: ExecutorService =
                ThreadPoolExecutor(
                    5, 5, 0, TimeUnit.MILLISECONDS,
                    LinkedBlockingDeque<Runnable>()
                )
            viewHolder.asyncTask = BitmapWorkerTask(context, viewHolder.layout, memoryCache)
                    .executeOnExecutor(customerExecutorService, photos[position].thumbnailUrl)
            callback.onSuccess(viewHolder.asyncTask!!)
        }
        viewHolder.bind(getItem(position))
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: onCreateView(parent).apply {
            tag = ViewHolder(this)
        }
        onBindViewHolder(view.tag as ViewHolder, position)
        return view
    }

    private fun getBitmapFromMemCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }

}
