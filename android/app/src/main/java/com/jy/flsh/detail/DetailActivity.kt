package com.jy.flsh.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jy.flsh.R
import com.jy.flsh.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        binding.viewModel = viewModel

        // 取上一页传来的参数
        val id = intent.getStringExtra(EXTRA_ID)
        val title = intent.getStringExtra(EXTRA_TITLE)
        Log.d(TAG, "onCreate: id=$id, title=$title")

        if (id.isNullOrBlank()) {
            Log.w(TAG, "onCreate: 缺少 id，结束页面")
            finish()
            return
        }
        viewModel.title.set(title?.ifBlank { null } ?: getString(R.string.detail_title))
        viewModel.detail.set(id)
        Log.d(
            TAG,
            "onCreate: 已绑定 viewModel, title=${viewModel.title.get()}, detail=${viewModel.detail.get()}"
        )
    }

    companion object {
        private const val TAG = "DetailActivity"

        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"

        fun newIntent(context: Context, id: String, title: String?): Intent =
            Intent(context, DetailActivity::class.java)
                .putExtra(EXTRA_ID, id)
                .putExtra(EXTRA_TITLE, title)
    }
}
