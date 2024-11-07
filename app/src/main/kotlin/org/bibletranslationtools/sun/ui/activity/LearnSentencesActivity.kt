package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivityLearnSentencesBinding
import org.bibletranslationtools.sun.ui.adapter.LearnSentenceAdapter
import org.bibletranslationtools.sun.ui.model.LessonMode
import org.bibletranslationtools.sun.ui.viewmodel.LearnSentencesViewModel
import org.bibletranslationtools.sun.utils.Section
import org.bibletranslationtools.sun.utils.TallyMarkConverter
import org.bibletranslationtools.sun.utils.putEnumExtra
import kotlin.math.max

class LearnSentencesActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLearnSentencesBinding.inflate(layoutInflater) }
    private val adapter by lazy { LearnSentenceAdapter() }
    private val viewModel: LearnSentencesViewModel by viewModels()
    private var pagerCurrentItem = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.topNavBar.toolbar)
        supportActionBar?.title = null

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        binding.topNavBar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        viewModel.lessonId.value = intent.getIntExtra("id", 1)
        viewModel.initializeLessonMode()

        binding.topNavBar.pageTitle.text = getString(R.string.lesson_name, viewModel.lessonId.value)
        binding.topNavBar.tallyNumber.text = TallyMarkConverter.toText(viewModel.lessonId.value)

        setupCardsView()
        setupButtons()
    }

    private fun setupButtons() {
        with(binding) {
            prevButton.setOnClickListener {
                setPreviousSentence()
            }
            nextButton.setOnClickListener {
                setNextSentence()
            }
        }
    }

    private fun setupCardsView() {
        with(binding) {
            viewPager.adapter = adapter
            viewPager.registerOnPageChangeCallback(callback)

            TabLayoutMediator(tabs, viewPager) {_,_ ->}.attach()

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.sentences.collect { sentences ->
                        adapter.submitList(sentences)

                        if (viewModel.mode.value == LessonMode.NORMAL) {
                            launch(Dispatchers.Main) {
                                delay(100)
                                val lastPosition = viewModel.getLastPosition()
                                viewPager.currentItem = lastPosition
                            }
                        }
                    }
                }
            }

            viewModel.loadSentences()
        }
    }

    private fun setPreviousSentence() {
        val previousItem = max(0, binding.viewPager.currentItem - 1)
        binding.viewPager.currentItem = previousItem
    }

    private fun setNextSentence() {
        val currentItem = binding.viewPager.currentItem
        val unlearnedItem = viewModel.sentences.value.indexOfFirst {
            if (viewModel.mode.value == LessonMode.REPEAT) {
                !it.sentence.passed
            } else !it.sentence.learned
        }

        when {
            currentItem < viewModel.sentences.value.size - 1 -> {
                binding.viewPager.currentItem = currentItem + 1
            }
            unlearnedItem > -1 && unlearnedItem < viewModel.sentences.value.size - 1 -> {
                binding.viewPager.currentItem = unlearnedItem
            }
            else -> {
                saveCard(pagerCurrentItem)
                viewModel.saveLastPosition(0)
                finishLesson()
            }
        }
    }

    private val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            adapter.notifyItemChanged(pagerCurrentItem)
            viewModel.sentences.value.let { sentences ->
                val sentence = sentences[position]
                if (viewModel.mode.value == LessonMode.REPEAT) {
                    sentence.sentence.passed = true
                } else {
                    saveCard(pagerCurrentItem)
                }
            }
            if (position > 0) {
                viewModel.saveLastPosition(position)
                binding.prevButton.visibility = View.VISIBLE
            } else {
                binding.prevButton.visibility = View.INVISIBLE
            }
            pagerCurrentItem = position
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewPager.unregisterOnPageChangeCallback(callback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (viewModel.mode.value == LessonMode.REPEAT) {
                val intent = Intent(baseContext, LessonListActivity::class.java)
                intent.putExtra("selected", viewModel.lessonId.value)
                startActivity(intent)
            } else {
                val intent = Intent(baseContext, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun finishLesson() {
        val intent = Intent(baseContext, SectionCompleteActivity::class.java)
        intent.putExtra("id", viewModel.lessonId.value)
        intent.putEnumExtra("section", Section.LEARN_SENTENCES)
        intent.putEnumExtra("mode", viewModel.mode.value)
        startActivity(intent)
    }

    private fun saveCard(position: Int) {
        if (position >= 0) {
            val sentence = viewModel.sentences.value[position]
            if (!sentence.sentence.learned) {
                sentence.sentence.learned = true
                viewModel.saveSentence(sentence)
            }
        }
    }
}