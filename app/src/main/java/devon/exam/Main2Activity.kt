package devon.exam

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    companion object {

        fun start(context: Context) {
            context.startActivity(Intent(context, Main2Activity::class.java))
        }
    }

    private lateinit var viewModel: Main2ViewModel
    private lateinit var customGridAdapter: CustomGridAdapter
    private val myAsyncTasks = arrayListOf<AsyncTask<String, Void, Bitmap>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        viewModel = ViewModelProviders.of(this).get(Main2ViewModel::class.java)
        setupView()
        setupViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRunningTasks()
    }

    private fun setupView() {
        customGridAdapter = CustomGridAdapter(this@Main2Activity,
            viewModel.photos,
            object : TaskCallback() {
                override fun onSuccess(task: AsyncTask<String, Void, Bitmap>) {
                    myAsyncTasks.add(task)
                }
            })
        grid.adapter = customGridAdapter
    }

    private fun setupViewModel() {
        viewModel.apply {
            notifyDataChanged.observe(this@Main2Activity, Observer {
                customGridAdapter.notifyDataSetChanged()
                progress_circular.visibility = View.GONE
            })
            error.observe(this@Main2Activity, Observer {
                Toast.makeText(this@Main2Activity, "糟糕網路錯誤！", Toast.LENGTH_LONG).show()
            })
            getPhotos()
        }
    }

    private fun cancelRunningTasks() {
        for (myAsyncTask in myAsyncTasks) {
            if (myAsyncTask.status == AsyncTask.Status.RUNNING) {
                myAsyncTask.cancel(true)
            }
        }
        myAsyncTasks.clear()
    }
}
