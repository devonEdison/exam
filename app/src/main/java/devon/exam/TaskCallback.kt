package devon.exam

import android.graphics.Bitmap
import android.os.AsyncTask

abstract class TaskCallback {

    abstract fun onSuccess(task: AsyncTask<String, Void, Bitmap>)
}