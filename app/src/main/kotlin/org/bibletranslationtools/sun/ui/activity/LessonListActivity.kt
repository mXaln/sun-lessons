package org.bibletranslationtools.sun.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivityListLessonsBinding
import org.bibletranslationtools.sun.ui.adapter.LessonListAdapter
import org.bibletranslationtools.sun.ui.model.LessonModel
import org.bibletranslationtools.sun.ui.viewmodel.LessonListViewModel
import org.bibletranslationtools.sun.utils.Constants

class LessonListActivity : AppCompatActivity(), LessonListAdapter.OnLessonSelectedListener {
    private val binding by lazy { ActivityListLessonsBinding.inflate(layoutInflater) }
    private val viewModel: LessonListViewModel by viewModels()
    private val lessonsAdapter by lazy { LessonListAdapter(this, this) }

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

        val selectedLessonId = intent.getIntExtra("selected", 1)

        binding.lessonsList.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        binding.lessonsList.adapter = lessonsAdapter

        binding.navBar.bottomNavigation.selectedItemId = R.id.lessons
        binding.navBar.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(baseContext, HomeActivity::class.java)
                    startActivity(intent)
                }
                R.id.progress -> {
                    val intent = Intent(baseContext, TrackProgressActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        lifecycleScope.launch {
            viewModel.lessons.collect {
                lessonsAdapter.submitList(it)
                lessonsAdapter.notifyDataSetChanged()
            }
        }

        viewModel.setActiveLesson(selectedLessonId)
    }

    override fun onLessonSelected(lesson: LessonModel, position: Int) {
        viewModel.setActiveLesson(lesson.lesson.id)

        viewModel.lessons.value.indexOfFirst { it.isSelected }.let { prevPosition ->
            if (prevPosition >= 0 && prevPosition != position) {
                viewModel.lessons.value[prevPosition].let { prevLesson ->
                    prevLesson.isSelected = false
                    lessonsAdapter.refreshLesson(prevPosition)
                }
            }
        }

        lesson.isSelected = !lesson.isSelected
        lessonsAdapter.refreshLesson(position)
    }

    override fun onLessonAction(lessonId: Int, action: Int) {
        val intent = Intent(this, SectionStartActivity::class.java)
        intent.putExtra("id", lessonId)
        intent.putExtra("type", action)
        startActivity(intent)
    }

    private fun refreshData() {
        viewModel.loadLessons()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(baseContext, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}
