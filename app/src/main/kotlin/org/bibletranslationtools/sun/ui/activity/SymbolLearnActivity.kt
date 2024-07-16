package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivityLearnBinding
import org.bibletranslationtools.sun.ui.adapter.LearnCardAdapter
import org.bibletranslationtools.sun.ui.viewmodel.LearnViewModel
import org.bibletranslationtools.sun.utils.Constants
import org.bibletranslationtools.sun.utils.TallyMarkConverter

class SymbolLearnActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLearnBinding.inflate(layoutInflater) }
    private val adapter by lazy { LearnCardAdapter() }
    private val viewModel: LearnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        viewModel.lessonId.value = intent.getIntExtra("id", 1)
        viewModel.part.value = intent.getIntExtra("part", 1)

        binding.lessonTitle.text = getString(R.string.lesson_name, viewModel.lessonId.value)
        binding.lessonTally.text = TallyMarkConverter.toText(viewModel.lessonId.value)

        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(baseContext, LessonListActivity::class.java)
            intent.putExtra("selected", viewModel.lessonId.value)
            startActivity(intent)
        }

        setupCardsView()
        setupButtons()
    }

    private fun setupButtons() {
        with(binding) {
            nextButton.setOnClickListener {
                val currentItem = viewPager.currentItem
                if (currentItem < viewModel.cards.value.size - 1) {
                    viewPager.currentItem = currentItem + 1
                } else {
                    val intent = Intent(baseContext, IntermediateActivity::class.java)
                    intent.putExtra("id", viewModel.lessonId.value)
                    intent.putExtra("part", viewModel.part.value)
                    intent.putExtra("type", Constants.TEST_SYMBOLS)
                    startActivity(intent)
                }
            }

            prevButton.setOnClickListener {
                val currentItem = viewPager.currentItem
                if (currentItem > 0) {
                    viewPager.currentItem = currentItem - 1
                }
            }
        }
    }

    private fun setupCardsView() {
        with(binding) {
            viewPager.adapter = adapter
            viewPager.registerOnPageChangeCallback(callback)

            TabLayoutMediator(tabs, viewPager) { tab, _ ->
                tab.view.isClickable = false
            }.attach()

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
}

