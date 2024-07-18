package org.bibletranslationtools.sun.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.databinding.ActivityTrackProgressBinding
import org.bibletranslationtools.sun.ui.adapter.GridItemOffsetDecoration
import org.bibletranslationtools.sun.ui.adapter.LessonGridAdapter
import org.bibletranslationtools.sun.ui.viewmodel.TrackProgressViewModel

class TrackProgressActivity : AppCompatActivity() {
    private val binding by lazy { ActivityTrackProgressBinding.inflate(layoutInflater) }
    private val lessonsAdapter by lazy { LessonGridAdapter(this) }
    private val viewModel: TrackProgressViewModel by viewModels()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.lessons.layoutManager = GridLayoutManager(this, 5)
        binding.lessons.adapter = lessonsAdapter
        binding.lessons.addItemDecoration(
            GridItemOffsetDecoration(5, 30, false)
        )

        lifecycleScope.launch {
            viewModel.lessons.collect {
                lessonsAdapter.submitList(it)
                lessonsAdapter.notifyDataSetChanged()

                if (it.isNotEmpty()) {
                    setLearnProgress()
                    setTestScore()
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(baseContext, HomeActivity::class.java)
            startActivity(intent)
        }

        viewModel.loadLessons()
    }

    private fun setLearnProgress() {
        val lessons = viewModel.lessons.value

        binding.learnCount.text = lessons.sumOf { it.cardsLearned }.toString()
        binding.learnProgress.progress =
            lessons.sumOf { it.cardsLearnedProgress }.toInt() / lessons.size
    }

    @SuppressLint("DefaultLocale")
    private fun setTestScore() {
        val lessons = viewModel.lessons.value
        val progress = lessons.sumOf { it.totalProgress }.toInt() / lessons.size
        binding.testProgress.progress = progress
        binding.testScore.text = String.format("%1d%%", progress)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(baseContext, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}