package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
    private val testCards = arrayListOf<Card>()

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

            binding.topNavBar.pageTitle.text = getString(R.string.lesson_name, viewModel.lessonId.value)
            binding.topNavBar.tallyNumber.text = TallyMarkConverter.toText(viewModel.lessonId.value)

            answersList.layoutManager = GridLayoutManager(
                this@TestSymbolsActivity,
                2
            )
            answersList.addItemDecoration(
                GridItemOffsetDecoration(
                    2,
                    40,
                    false
                )
            )
            answersList.adapter = gridAdapter

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.cards.collect {
                        if (it.isNotEmpty()) {
                            setNextQuestion()
                        }
                    }
                }
            }

            nextButton.setOnClickListener {
                if (viewModel.questionDone.value) {
                    setNextQuestion()
                    viewModel.questionDone.value = false
                }
            }

            viewModel.loadLessonCards()
        }
    }

    private fun setNextQuestion() {
        binding.nextButton.isEnabled = false

        val allCards = viewModel.cards.value.toMutableList()
        allCards.forEach { it.correct = null }

        val inProgressCards = allCards.filter {
            if (viewModel.isGlobal.value) !it.passed else !it.tested
        }

        if (inProgressCards.isEmpty()) {
            finishTest()
            return
        }

        setRandomCorrectCard(inProgressCards)
        allCards.remove(correctCard)

        val incorrectCards = allCards.shuffled().take(3)

        setAnswers((listOf(correctCard) + incorrectCards).shuffled())

        gridAdapter.submitList(testCards as List<TestCard>)

        Glide.with(baseContext)
            .load(Uri.parse("file:///android_asset/images/symbols/${correctCard.secondary}"))
            .fitCenter()
            .into(binding.itemImage)
    }

    override fun onCardSelected(card: Card, position: Int) {
        if (!viewModel.questionDone.value) {
            checkAnswer(testCards[position], position)
            viewModel.questionDone.value = true
            binding.nextButton.isEnabled = true
        }
    }

    private fun checkAnswer(selectedCard: Card, position: Int) {
        if (selectedCard.symbol == correctCard.symbol) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (viewModel.isGlobal.value) {
                    correctCard.passed = true
                } else {
                    correctCard.tested = true
                    viewModel.updateCard(correctCard)
                }
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

    private fun setRandomCorrectCard(cards: List<Card>) {
        if (this::correctCard.isInitialized && cards.size > 1) {
            val oldCard = correctCard.copy()
            while (oldCard == correctCard) {
                correctCard = cards.random()
            }
        } else {
            correctCard = cards.random()
        }
    }

    private fun finishTest() {
        navigateToNextSection()
    }

    private fun navigateToNextSection() {
        lifecycleScope.launch {
            val section = if (viewModel.getSentencesCount() == 0) {
                Section.TEST_SENTENCES
            } else Section.TEST_SYMBOLS

            val intent = Intent(baseContext, SectionCompleteActivity::class.java)
            intent.putExtra("id", viewModel.lessonId.value)
            intent.putEnumExtra("type", section)
            intent.putExtra("global", viewModel.isGlobal.value)
            startActivity(intent)
        }
    }

    private fun setAnswers(cards: List<Card>) {
        testCards.clear()
        testCards.addAll(cards)
        gridAdapter.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.questionDone.value = false
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (viewModel.isGlobal.value) {
                val intent = Intent(baseContext, LessonListActivity::class.java)
                intent.putExtra("selected", viewModel.lessonId.value)
                startActivity(intent)
            } else {
                val intent = Intent(baseContext, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }
}