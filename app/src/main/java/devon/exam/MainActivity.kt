package devon.exam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import devon.exam.network.HttpCache
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        HttpCache.init(this.application)
    }

    private fun setupView() {
        button.setOnClickListener { Main2Activity.start(this) }
    }

}
