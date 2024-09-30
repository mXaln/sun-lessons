package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivitySectionStartedBinding
import org.bibletranslationtools.sun.ui.viewmodel.SectionStatusViewModel
import org.bibletranslationtools.sun.utils.Section
import org.bibletranslationtools.sun.utils.TallyMarkConverter
import org.bibletranslationtools.sun.utils.getEnumExtra
import org.bibletranslationtools.sun.utils.putEnumExtra

class SectionStartActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySectionStartedBinding.inflate(layoutInflater) }
    private val viewModel: SectionStatusViewModel by viewModels()

    private var id: Int = 1
    private var type: Section = Section.LEARN_SYMBOLS

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

        // Finish lesson if there are no sentences
        lifecycleScope.launch {
            if (type == Section.LEARN_SENTENCES || type == Section.TEST_SENTENCES) {
                finishLesson()
            }
        }

        when (type) {
            Section.LEARN_SYMBOLS -> {
                binding.sectionTitle.text = getString(R.string.learn_symbols)
                binding.lessonTitle.text = getString(R.string.lesson_name, id)
                binding.image.setImageResource(R.drawable.ic_learn_large)
                binding.startButton.setOnClickListener {
                    startNextSection<LearnSymbolsActivity>()
                }
            }
            Section.TEST_SYMBOLS -> {
                binding.sectionTitle.text = getString(R.string.test_symbols)
                binding.lessonTitle.text = getString(R.string.lesson_name, id)
                binding.image.setImageResource(R.drawable.ic_test_large)
                binding.startButton.setOnClickListener {
                    startNextSection<TestSymbolsActivity>()
                }
            }
            Section.LEARN_SENTENCES -> {
                binding.sectionTitle.text = getString(R.string.learn_sentences)
                binding.lessonTitle.text = getString(R.string.lesson_name, id)
                binding.image.setImageResource(R.drawable.ic_learn_sentences_large)
                binding.startButton.setOnClickListener {
                    startNextSection<LearnSentencesActivity>()
                }
            }
            else -> {
                binding.sectionTitle.text = getString(R.string.test_sentences)
                binding.lessonTitle.text = getString(R.string.lesson_name, id)
                binding.image.setImageResource(R.drawable.ic_test_sentences_large)
                binding.startButton.setOnClickListener {
                    startNextSection<TestSentencesActivity>()
                }
            }
        }

        binding.topNavBar.pageTitle.text = getString(R.string.lesson_name, id)
        binding.topNavBar.tallyNumber.text = TallyMarkConverter.toText(10)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(baseContext, HomeActivity::class.java)
            intent.putExtra("selected", id)
            startActivity(intent)
        }
    }

    private inline fun <reified T : AppCompatActivity> startNextSection() {
        val intent = Intent(baseContext, T::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    private suspend fun finishLesson() {
        if (viewModel.sentencesByLessonCount(id) == 0) {
            val intent = Intent(baseContext, SectionCompleteActivity::class.java)
            intent.putExtra("id", id)
            intent.putEnumExtra("type", Section.TEST_SENTENCES)
            startActivity(intent)
        }
    }
}