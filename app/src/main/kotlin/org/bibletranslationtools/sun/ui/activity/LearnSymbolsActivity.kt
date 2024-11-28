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
import com.wajahatkarim3.easyflipview.EasyFlipView
import com.wajahatkarim3.easyflipview.EasyFlipView.OnFlipAnimationListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivityLearnSymbolsBinding
import org.bibletranslationtools.sun.ui.adapter.LearnSymbolAdapter
import org.bibletranslationtools.sun.ui.model.LessonMode
import org.bibletranslationtools.sun.ui.viewmodel.LearnSymbolViewModel
import org.bibletranslationtools.sun.utils.Section
import org.bibletranslationtools.sun.utils.TallyMarkConverter
import org.bibletranslationtools.sun.utils.putEnumExtra
import kotlin.math.max

class LearnSymbolsActivity : AppCompatActivity(), OnFlipAnimationListener {
    private val binding by lazy { ActivityLearnSymbolsBinding.inflate(layoutInflater) }
    private val adapter by lazy { LearnSymbolAdapter(this) }
    private val viewModel: LearnSymbolViewModel by viewModels()
    private var pagerCurrentItem = -1
    private val tabDots = arrayListOf<View>()

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

    override fun onViewFlipCompleted(
        easyFlipView: EasyFlipView?,
        newCurrentSide: EasyFlipView.FlipState?
    ) {
        if (newCurrentSide == EasyFlipView.FlipState.BACK_SIDE) {
            viewModel.cards.value.let { cards ->
                val card = cards[pagerCurrentItem]
                if (viewModel.mode.value == LessonMode.REPEAT) {
                    card.passed = true
                } else {
                    saveCard(pagerCurrentItem)
                }
                enableNextButton(true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewPager.unregisterOnPageChangeCallback(callback)
    }

    private fun setupButtons() {
        with(binding) {
            prevButton.setOnClickListener {
                setPreviousSymbol()
            }
            nextButton.setOnClickListener {
                setNextSymbol()
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
                    viewModel.cards.collect { cards ->
                        adapter.submitList(cards)

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

            viewModel.loadCards()
        }
    }

    private fun setPreviousSymbol() {
        val previousItem = max(0, binding.viewPager.currentItem - 1)
        binding.viewPager.currentItem = previousItem
    }

    private fun setNextSymbol() {
        val currentItem = binding.viewPager.currentItem
        val unlearnedItem = viewModel.cards.value.indexOfFirst {
            if (viewModel.mode.value == LessonMode.REPEAT) !it.passed else !it.learned
        }

        when {
            currentItem < viewModel.cards.value.size - 1 -> {
                binding.viewPager.currentItem = currentItem + 1
            }
            unlearnedItem > -1 && unlearnedItem < viewModel.cards.value.size - 1 -> {
                binding.viewPager.currentItem = unlearnedItem
            }
            else -> {
                viewModel.saveLastPosition(0)
                finishLesson()
            }
        }
    }

    private fun enableNextButton(enabled: Boolean) {
        binding.nextButton.isEnabled = enabled

        if (tabDots.isEmpty()) {
            tabDots.addAll(binding.tabs.touchables)
        }

        if (enabled) {
            tabDots.forEach { it.isEnabled = true }
            binding.tabs.touchables.clear()
            binding.tabs.touchables.addAll(tabDots)
        } else {
            binding.tabs.touchables.forEach { it.isEnabled = false }
        }
    }

    private val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            adapter.notifyItemChanged(pagerCurrentItem)
            viewModel.cards.value.let { cards ->
                val card = cards[position]
                val done = if (viewModel.mode.value == LessonMode.REPEAT) {
                    card.passed
                } else {
                    card.learned
                }
                enableNextButton(done)
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
        intent.putExtra("section", Section.LEARN_SYMBOLS)
        intent.putEnumExtra("mode", viewModel.mode.value)
        startActivity(intent)
    }

    private fun saveCard(position: Int) {
        if (position >= 0) {
            val card = viewModel.cards.value[position]
            if (!card.learned) {
                card.learned = true
                viewModel.saveCard(card)
            }
        }
    }
}

