package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.wajahatkarim3.easyflipview.EasyFlipView
import com.wajahatkarim3.easyflipview.EasyFlipView.FlipState
import com.wajahatkarim3.easyflipview.EasyFlipView.OnFlipAnimationListener
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivityLearnSymbolsBinding
import org.bibletranslationtools.sun.ui.adapter.LearnSymbolAdapter
import org.bibletranslationtools.sun.ui.viewmodel.LearnSymbolViewModel
import org.bibletranslationtools.sun.utils.Constants
import org.bibletranslationtools.sun.utils.TallyMarkConverter

class LearnSymbolsActivity : AppCompatActivity(), OnFlipAnimationListener {
    private val binding by lazy { ActivityLearnSymbolsBinding.inflate(layoutInflater) }
    private val adapter by lazy {
        LearnSymbolAdapter(viewModel.flipState)
            .apply { setFlipListener(this@LearnSymbolsActivity) }
    }
    private val viewModel: LearnSymbolViewModel by viewModels()

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

        binding.topNavBar.pageTitle.text = getString(R.string.lesson_name, viewModel.lessonId.value)
        binding.topNavBar.tallyNumber.text = TallyMarkConverter.toText(viewModel.lessonId.value)

        setupCardsView()
        setupButtons()
    }

    override fun onViewFlipCompleted(easyFlipView: EasyFlipView?, newCurrentSide: FlipState?) {
        newCurrentSide?.let {
            viewModel.flipState.value = it
        }
    }

    private fun setupButtons() {
        with(binding) {
            prevButton.visibility = View.GONE
            prevButton.setOnClickListener {
                nextButton.visibility = View.VISIBLE
                val currentItem = viewPager.currentItem
                if (currentItem > 0) {
                    viewModel.flipState.value = FlipState.FRONT_SIDE
                    viewPager.currentItem = currentItem - 1
                    if (viewPager.currentItem == 0) {
                        prevButton.visibility = View.GONE
                    }
                }
            }
            nextButton.setOnClickListener {
                prevButton.visibility = View.VISIBLE
                val currentItem = viewPager.currentItem
                if (currentItem < viewModel.cards.value.size - 1) {
                    viewModel.flipState.value = FlipState.FRONT_SIDE
                    viewPager.currentItem = currentItem + 1
                } else {
                    finishLesson()
                }
            }
            showAnswer.setOnClickListener {
                val currentState = viewModel.flipState.value
                viewModel.flipState.value = when (currentState) {
                    FlipState.FRONT_SIDE -> FlipState.BACK_SIDE
                    FlipState.BACK_SIDE -> FlipState.FRONT_SIDE
                }
            }
            lifecycleScope.launch {
                viewModel.flipState.collect {
                    when (it) {
                        FlipState.FRONT_SIDE -> {
                            binding.showAnswer.text = getString(R.string.see_answer)
                            binding.showAnswer.icon = AppCompatResources.getDrawable(
                                this@LearnSymbolsActivity,
                                R.drawable.ic_eye_open
                            )
                        }
                        FlipState.BACK_SIDE -> {
                            binding.showAnswer.text = getString(R.string.hide_answer)
                            binding.showAnswer.icon = AppCompatResources.getDrawable(
                                this@LearnSymbolsActivity,
                                R.drawable.ic_eye_closed
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupCardsView() {
        with(binding) {
            viewPager.adapter = adapter
            viewPager.registerOnPageChangeCallback(callback)

            TabLayoutMediator(tabs, viewPager) {_,_ ->}.attach()

            lifecycleScope.launch {
                viewModel.cards.collect { cards ->
                    adapter.submitList(cards)
                }
            }

            loadCards()
        }
    }

    private fun loadCards() {
        viewModel.loadCards()
    }

    private val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            viewModel.flipState.value = FlipState.FRONT_SIDE
            viewModel.cards.value.let { cards ->
                val card = cards[position]
                if (!card.learned) {
                    card.learned = true
                    viewModel.saveCard(card)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewPager.unregisterOnPageChangeCallback(callback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(baseContext, LessonListActivity::class.java)
            intent.putExtra("selected", viewModel.lessonId.value)
            startActivity(intent)
        }
    }

    private fun finishLesson() {
        val intent = Intent(baseContext, SectionCompleteActivity::class.java)
        intent.putExtra("id", viewModel.lessonId.value)
        intent.putExtra("type", Constants.LEARN_SYMBOLS)
        startActivity(intent)
    }
}

