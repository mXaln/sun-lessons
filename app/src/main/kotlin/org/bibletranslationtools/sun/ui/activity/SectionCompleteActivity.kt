package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivitySectionCompletedBinding
import org.bibletranslationtools.sun.ui.viewmodel.SectionStatusViewModel
import org.bibletranslationtools.sun.utils.Section
import org.bibletranslationtools.sun.utils.TallyMarkConverter
import org.bibletranslationtools.sun.utils.getEnumExtra
import org.bibletranslationtools.sun.utils.putEnumExtra

class SectionCompleteActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySectionCompletedBinding.inflate(layoutInflater) }
    private val viewModel: SectionStatusViewModel by viewModels()

    private var id: Int = 1
    private var type: Section = Section.LEARN_SYMBOLS
    private var isGlobal = false

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
        type = intent.getEnumExtra("type", Section.LEARN_SYMBOLS)
        isGlobal = intent.getBooleanExtra("global", false)

        when (type) {
            Section.LEARN_SYMBOLS -> {
                binding.sectionTitle.text = getString(R.string.learn_symbols_completed)
                binding.startButton.setOnClickListener {
                    navigateToNextSection(Section.TEST_SYMBOLS)
                }
            }
            Section.TEST_SYMBOLS -> {
                binding.sectionTitle.text = getString(R.string.test_symbols_completed)
                binding.startButton.setOnClickListener {
                    navigateToNextSection(Section.LEARN_SENTENCES)
                }
            }
            Section.LEARN_SENTENCES -> {
                binding.sectionTitle.text = getString(R.string.learn_sentences_completed)
                binding.startButton.setOnClickListener {
                    navigateToNextSection(Section.TEST_SENTENCES)
                }
            }
            else -> {
                binding.sectionTitle.text = if (isGlobal) {
                    getString(R.string.test_sentences_completed)
                } else getString(R.string.lesson_completed, id)

                binding.startButton.setOnClickListener {
                    navigateToNextLesson()
                }
            }
        }

        binding.topNavBar.pageTitle.text = getString(R.string.lesson_name, id)
        binding.topNavBar.tallyNumber.text = TallyMarkConverter.toText(id)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isGlobal) {
                val intent = Intent(baseContext, LessonListActivity::class.java)
                intent.putExtra("selected", id)
                startActivity(intent)
            } else {
                val intent = Intent(baseContext, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun navigateToNextSection(type: Section) {
        lifecycleScope.launch {
            if (isGlobal) {
                navigateToLessonsList()
            } else {
                viewModel.saveSectionStatus(id, type)

                val intent = Intent(baseContext, SectionStartActivity::class.java)
                intent.putExtra("id", id)
                intent.putEnumExtra("type", type)
                startActivity(intent)
            }
        }
    }

    private fun navigateToNextLesson() {
        lifecycleScope.launch {
            if (isGlobal) {
                navigateToLessonsList()
            } else {
                val next = viewModel.getNextLesson(id)
                viewModel.saveSectionStatus(next, Section.LEARN_SYMBOLS)

                val intent = Intent(baseContext, SectionStartActivity::class.java)
                intent.putExtra("id", next)
                intent.putEnumExtra("type", Section.LEARN_SYMBOLS)
                startActivity(intent)
            }
        }
    }

    private fun navigateToLessonsList() {
        val intent = Intent(baseContext, LessonListActivity::class.java)
        intent.putExtra("selected", id)
        startActivity(intent)
    }
}