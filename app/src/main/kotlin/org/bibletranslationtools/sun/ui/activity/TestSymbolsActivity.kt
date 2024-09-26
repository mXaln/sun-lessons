package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.data.model.Card
import kotlinx.coroutines.*
import org.bibletranslationtools.sun.data.model.Answer
import org.bibletranslationtools.sun.data.model.TestCard
import org.bibletranslationtools.sun.databinding.ActivityTestSymbolsBinding
import org.bibletranslationtools.sun.ui.adapter.TestSymbolAdapter
import org.bibletranslationtools.sun.ui.adapter.GridItemOffsetDecoration
import org.bibletranslationtools.sun.ui.viewmodel.TestSymbolsViewModel
import org.bibletranslationtools.sun.utils.Section
import org.bibletranslationtools.sun.utils.TallyMarkConverter
import org.bibletranslationtools.sun.utils.putEnumExtra

class TestSymbolsActivity : AppCompatActivity(), TestSymbolAdapter.OnCardSelectedListener {

    private val binding by lazy { ActivityTestSymbolsBinding.inflate(layoutInflater) }
    private val viewModel: TestSymbolsViewModel by viewModels()
    private val gridAdapter: TestSymbolAdapter by lazy {
        TestSymbolAdapter(this)
    }

    private lateinit var correctCard: Card
    private val reviewCards = arrayListOf<Card>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            viewModel.lessonId.value = intent.getIntExtra("id", 1)
            viewModel.isGlobal.value = intent.getBooleanExtra("global", false)

            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            onBackPressedDispatcher.addCallback(onBackPressedCallback)
            binding.topNavBar.toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            if (!viewModel.isGlobal.value) {
                binding.topNavBar.pageTitle.text = getString(R.string.lesson_name, viewModel.lessonId.value)
                binding.topNavBar.tallyNumber.text = TallyMarkConverter.toText(viewModel.lessonId.value)
            } else {
                binding.topNavBar.pageTitle.visibility = View.GONE
                binding.topNavBar.tallyNumber.visibility = View.GONE
            }

            answersList.layoutManager = GridLayoutManager(
                this@TestSymbolsActivity,
                2
            )
            answersList.addItemDecoration(
                GridItemOffsetDecoration(
                    2,
                    20,
                    false
                )
            )
            answersList.adapter = gridAdapter

            lifecycleScope.launch {
                viewModel.cards.collect {
                    if (it.isNotEmpty()) {
                        setNextQuestion()
                    }
                }
            }

            nextButton.setOnClickListener {
                if (viewModel.questionDone.value) {
                    setNextQuestion()
                    viewModel.questionDone.value = false
                }
            }

            if (viewModel.isGlobal.value) {
                viewModel.loadAllTestedCards()
            } else {
                viewModel.loadLessonCards()
            }

        }
    }

    override fun onCardSelected(card: Card, position: Int) {
        if (!viewModel.questionDone.value) {
            checkAnswer(reviewCards[position], position)
            viewModel.questionDone.value = true
            binding.nextButton.isEnabled = true
        }
    }

    private fun checkAnswer(selectedCard: Card, position: Int) {
        if (selectedCard.symbol == correctCard.symbol) {
            lifecycleScope.launch(Dispatchers.IO) {
                correctCard.tested = true
                viewModel.updateCard(correctCard)
            }
            correctCard.correct = true

            val answerCards = listOf(
                Answer(true),
                correctCard
            )
            gridAdapter.submitList(answerCards)
            gridAdapter.selectCorrect(correctCard)
        } else {
            correctCard.correct = true
            selectedCard.correct = false

            val answerCards = listOf(
                Answer(false),
                selectedCard,
                Answer(true),
                correctCard
            )
            gridAdapter.submitList(answerCards)
            gridAdapter.selectIncorrect(position)
            gridAdapter.selectCorrect(correctCard)
        }
    }

    private fun setNextQuestion() {
        binding.nextButton.isEnabled = false

        val allCards = viewModel.cards.value.toMutableList()
        allCards.forEach { it.correct = null }

        val inProgressCards = allCards.filter { !it.tested }

        if (inProgressCards.isEmpty()) {
            finishReview()
            return
        }

        setRandomCard(inProgressCards)
        allCards.remove(correctCard)

        val incorrectCards = allCards.shuffled().take(3)

        setAnswers((listOf(correctCard) + incorrectCards).shuffled())

        gridAdapter.submitList(reviewCards as List<TestCard>)

        Glide.with(baseContext)
            .load(Uri.parse("file:///android_asset/images/symbols/${correctCard.secondary}"))
            .fitCenter()
            .into(binding.itemImage)
    }

    private fun setRandomCard(cards: List<Card>) {
        if (this::correctCard.isInitialized && cards.size > 1) {
            val oldCard = correctCard.copy()
            while (oldCard == correctCard) {
                correctCard = cards.random()
            }
        } else {
            correctCard = cards.random()
        }
    }

    private fun finishReview() {
        if (viewModel.isGlobal.value) {
            val intent = Intent(baseContext, GlobalTestActivity::class.java)
            startActivity(intent)
        } else {
            lifecycleScope.launch {
                navigateToNextSection()
            }
        }
    }

    private suspend fun navigateToNextSection() {
        val section = if (viewModel.getSentencesCount() == 0) {
            Section.TEST_SENTENCES
        } else Section.TEST_SYMBOLS

        val intent = Intent(baseContext, SectionCompleteActivity::class.java)
        intent.putExtra("id", viewModel.lessonId.value)
        intent.putEnumExtra("type", section)
        startActivity(intent)
    }

    private fun setAnswers(cards: List<Card>) {
        reviewCards.clear()
        reviewCards.addAll(cards)
        gridAdapter.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.questionDone.value = false
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = if (viewModel.isGlobal.value) {
                Intent(baseContext, GlobalTestActivity::class.java)
            } else {
                Intent(baseContext, HomeActivity::class.java)
            }
            startActivity(intent)
        }
    }
}