package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivitySectionCompletedBinding
import org.bibletranslationtools.sun.ui.viewmodel.SectionStatusViewModel
import org.bibletranslationtools.sun.utils.Constants
import org.bibletranslationtools.sun.utils.TallyMarkConverter

class SectionCompleteActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySectionCompletedBinding.inflate(layoutInflater) }
    private val viewModel: SectionStatusViewModel by viewModels()

    private var id: Int = 1
    private var type: Int = Constants.LEARN_SYMBOLS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.topNavBar.toolbar)
        supportActionBar?.title = null

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        binding.topNavBar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        id = intent.getIntExtra("id", 1)
        type = intent.getIntExtra("type", Constants.LEARN_SYMBOLS)

        when (type) {
            Constants.LEARN_SYMBOLS -> {
                binding.pageTitle.text = getString(R.string.learn_symbols_completed)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, SectionStartActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("type", Constants.TEST_SYMBOLS)
                    startActivity(intent)
                }
            }
            Constants.TEST_SYMBOLS -> {
                binding.pageTitle.text = getString(R.string.test_symbols_completed)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, SectionStartActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("type", Constants.LEARN_SENTENCES)
                    startActivity(intent)
                }
            }
            Constants.LEARN_SENTENCES -> {
                binding.pageTitle.text = getString(R.string.learn_sentences_completed)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, SectionStartActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("type", Constants.TEST_SENTENCES)
                    startActivity(intent)
                }
            }
            else -> {
                binding.pageTitle.text = getString(R.string.lesson_completed, id)
                binding.startButton.setOnClickListener {
                    goToNextLesson()
                }
            }
        }

        binding.topNavBar.pageTitle.text = getString(R.string.lesson_name, id)
        binding.topNavBar.tallyNumber.text = TallyMarkConverter.toText(id)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(baseContext, LessonListActivity::class.java)
            intent.putExtra("selected", id)
            startActivity(intent)
        }
    }

    private fun goToNextLesson() {
        lifecycleScope.launch {
            val lessons = viewModel.getAllLessons().map { it.id }
            val current = lessons.indexOf(id)
            var next = 1
            if (current < lessons.size - 1) {
                next = lessons[current + 1]
            }

            runOnUiThread {
                val intent = Intent(baseContext, SectionStartActivity::class.java)
                intent.putExtra("id", next)
                intent.putExtra("type", Constants.LEARN_SYMBOLS)
                startActivity(intent)
            }
        }
    }
}