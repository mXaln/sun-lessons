package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.ui.adapter.TestSentenceAdapter
import org.bibletranslationtools.sun.data.model.SentenceWithSymbols
import org.bibletranslationtools.sun.data.model.Symbol
import org.bibletranslationtools.sun.databinding.ActivityTestSentencesBinding
import org.bibletranslationtools.sun.ui.adapter.GridItemOffsetDecoration
import org.bibletranslationtools.sun.ui.adapter.LinearItemOffsetDecoration
import org.bibletranslationtools.sun.ui.control.SymbolState
import org.bibletranslationtools.sun.ui.model.LessonMode
import org.bibletranslationtools.sun.ui.viewmodel.TestSentencesViewModel
import org.bibletranslationtools.sun.utils.AnswerType
import org.bibletranslationtools.sun.utils.Section
import org.bibletranslationtools.sun.utils.TallyMarkConverter
import org.bibletranslationtools.sun.utils.putEnumExtra
import kotlin.math.max
import kotlin.math.min

class TestSentencesActivity : AppCompatActivity(), TestSentenceAdapter.OnSymbolSelectedListener {
    private val binding by lazy { ActivityTestSentencesBinding.inflate(layoutInflater) }
    private val viewModel: TestSentencesViewModel by viewModels()

    private val optionsAdapter: TestSentenceAdapter by lazy {
        TestSentenceAdapter(listener = this)
    }
    private val answersAdapter: TestSentenceAdapter by lazy {
        TestSentenceAdapter()
    }
    private val correctAdapter: TestSentenceAdapter by lazy {
        TestSentenceAdapter()
    }
    private val incorrectAdapter: TestSentenceAdapter by lazy {
        TestSentenceAdapter()
    }

    private lateinit var correctSentence: SentenceWithSymbols
    private var lastAnswerPosition = -1
    private var isAnswerCorrect = false
    private var correctAnswerShown = false

    private val optionSymbols = arrayListOf<Symbol>()
    private val answerSymbols = arrayListOf<Symbol>()
    private val correctSymbols = arrayListOf<Symbol>()

    companion object {
        const val OPTIONS_SHORT = 4
        const val OPTIONS_LONG = 8
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            viewModel.lessonId.value = intent.getIntExtra("id", 1)
            viewModel.initializeLessonMode()

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            onBackPressedDispatcher.addCallback(onBackPressedCallback)
            binding.topNavBar.toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            if (viewModel.mode.value == LessonMode.NORMAL) {
                binding.topNavBar.pageTitle.text = getString(R.string.lesson_name, viewModel.lessonId.value)
                binding.topNavBar.tallyNumber.text = TallyMarkConverter.toText(viewModel.lessonId.value)
            } else {
                binding.topNavBar.pageTitle.visibility = View.GONE
                binding.topNavBar.tallyNumber.visibility = View.GONE
            }

            correctList.adapter = correctAdapter
            incorrectList.adapter = incorrectAdapter

            answersList.adapter = answersAdapter
            answersList.addItemDecoration(
                LinearItemOffsetDecoration(10)
            )

            optionsList.adapter = optionsAdapter
            optionsList.addItemDecoration(GridItemOffsetDecoration(4, 20))

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.sentences.collect {
                        if (it.isNotEmpty()) {
                            setNextSentence()
                        }
                    }
                }
            }

            showAnswer.setOnClickListener {
                // If the answer is correct, do nothing
                if (isAnswerCorrect) return@setOnClickListener

                if (it.isActivated) {
                    correctAnswerShown = true
                    correctContainer.visibility = View.VISIBLE
                    incorrectContainer.visibility = View.GONE
                    showAnswer.text = getString(R.string.hide)
                    binding.nextSentence.visibility = View.VISIBLE
                } else {
                    correctContainer.visibility = View.GONE
                    incorrectContainer.visibility = View.VISIBLE
                    showAnswer.text = getString(R.string.show)
                }
                it.isActivated = !it.isActivated
            }

            nextSentence.setOnClickListener {
                if (viewModel.sentenceDone.value) {
                    setNextSentence()
                    viewModel.sentenceDone.value = false
                }
            }

            viewModel.loadSentences()
        }
    }

    private fun setNextSentence() {
        isAnswerCorrect = false
        correctAnswerShown = false

        binding.nextSentence.isEnabled = false
        binding.nextSentence.visibility = View.GONE

        binding.showAnswer.isActivated = true
        binding.showAnswer.text = getString(R.string.show)
        binding.showAnswer.visibility = View.GONE

        binding.correctContainer.visibility = View.GONE
        binding.incorrectContainer.visibility = View.GONE
        binding.optionsContainer.visibility = View.VISIBLE
        binding.answersContainer.visibility = View.VISIBLE

        val allSentences = viewModel.sentences.value.toMutableList()
        val inProgressSentences = allSentences.filter {
            if (viewModel.mode.value == LessonMode.REPEAT) {
                !it.sentence.passed
            } else !it.sentence.tested
        }

        if (inProgressSentences.isEmpty()) {
            finishTest()
            return
        }

        setRandomCorrectSentence(inProgressSentences)

        Glide.with(baseContext)
            .load(Uri.parse("file:///android_asset/images/sentences/${correctSentence.sentence.correct}"))
            .fitCenter()
            .into(binding.itemImage)

        lifecycleScope.launch {
            val cards = viewModel.getAllCards()
            val cardSymbols = cards.map { Symbol(id = 0, name = it.symbol, sort = 0) }
            val totalOptions =
                if (correctSentence.symbols.size > OPTIONS_SHORT) OPTIONS_LONG else OPTIONS_SHORT

            val allSymbols = cardSymbols + allSentences
                .map { it.symbols }
                .flatten()

            val incorrectSymbols = allSymbols
                .filter { symbol ->
                    val correctSymbols = correctSentence.symbols
                    correctSymbols.none { it.name == symbol.name }
                }
                .distinctBy { it.name }
                .shuffled()
                .take(totalOptions - correctSentence.symbols.size)

            val options = (correctSentence.symbols + incorrectSymbols).shuffled()
            setOptions(options)

            val answers = correctSentence.symbols.map { it.copy(name = "") }
            setAnswers(answers)
        }
    }

    override fun onSymbolSelected(symbol: Symbol, position: Int) {
        if (!viewModel.sentenceDone.value) {
            symbol.selected = true
            optionsAdapter.refreshItem(position)

            val answerSymbol = symbol.copy()
            answerSymbol.type = AnswerType.ANSWER
            answerSymbol.selected = true

            lastAnswerPosition++
            answerSymbols[lastAnswerPosition] = answerSymbol
            answersAdapter.submitList(answerSymbols)
            answersAdapter.refreshItem(lastAnswerPosition)

            if (lastAnswerPosition >= answersAdapter.itemCount - 1) {
                checkAnswer()
                viewModel.sentenceDone.value = true
                binding.nextSentence.isEnabled = true
                lastAnswerPosition = -1
            }
        }
    }

    private fun checkAnswer() {
        val correctSymbols = correctSentence.symbols
        val isSentenceCorrect = correctSymbols.map { it.id } == answerSymbols.map { it.id }

        with(binding) {
            setIncorrectSymbols(correctSymbols)
            setCorrectSymbols(correctSymbols)

            optionsContainer.visibility = View.GONE
            answersContainer.visibility = View.GONE

            if (isSentenceCorrect) {
                isAnswerCorrect = true
                lifecycleScope.launch(Dispatchers.IO) {
                    if (viewModel.mode.value == LessonMode.REPEAT) {
                        correctSentence.sentence.passed = true
                    } else {
                        correctSentence.sentence.tested = true
                        viewModel.updateSentence(correctSentence.sentence)
                    }
                }
                correctContainer.visibility = View.VISIBLE
                incorrectContainer.visibility = View.GONE
                showAnswer.visibility = View.GONE
                nextSentence.visibility = View.VISIBLE
            } else {
                showAnswer.visibility = View.VISIBLE
                incorrectContainer.visibility = View.VISIBLE
                correctContainer.visibility = View.GONE
            }
        }
    }

    private fun setRandomCorrectSentence(sentences: List<SentenceWithSymbols>) {
        // Try to select a sentence that has not been asked before
        if (this::correctSentence.isInitialized && sentences.size > 1) {
            val oldSentence = correctSentence.copy()
            while (oldSentence == correctSentence) {
                correctSentence = sentences.random()
            }
        } else {
            correctSentence = sentences.random()
        }
    }

    private fun finishTest() {
        val intent = Intent(baseContext, SectionCompleteActivity::class.java)
        intent.putExtra("id", viewModel.lessonId.value)
        intent.putEnumExtra("section", Section.TEST_SENTENCES)
        intent.putEnumExtra("mode", viewModel.mode.value)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.sentenceDone.value = false
    }

    private fun setOptions(symbols: List<Symbol>) {
        symbols.forEach {
            it.selected = false
            it.correct = null
            it.type = AnswerType.OPTION
        }

        optionSymbols.clear()
        optionSymbols.addAll(symbols)
        optionsAdapter.submitList(symbols)
        optionsAdapter.refresh()
    }

    private fun setAnswers(symbols: List<Symbol>) {
        symbols.forEach {
            it.selected = false
            it.correct = null
            it.type = AnswerType.ANSWER
        }

        answerSymbols.clear()
        answerSymbols.addAll(symbols)
        answersAdapter.submitList(symbols)
        answersAdapter.refresh()
    }

    private fun setIncorrectSymbols(correctSymbols: List<Symbol>) {
        answerSymbols.zip(correctSymbols).forEach { pair ->
            pair.first.selected = false
            pair.first.correct = pair.first.name == pair.second.name
            pair.first.type = AnswerType.RESULT
        }
        val spanCount = min(answerSymbols.size, 5)
        incorrectAdapter.submitList(answerSymbols)
        incorrectAdapter.refresh()
        (binding.incorrectList.layoutManager as GridLayoutManager).spanCount = spanCount
    }

    private fun setCorrectSymbols(symbols: List<Symbol>) {
        symbols.forEach {
            it.selected = false
            it.correct = true
            it.type = AnswerType.RESULT
        }

        correctSymbols.clear()
        correctSymbols.addAll(symbols)
        correctAdapter.submitList(symbols)
        correctAdapter.refresh()

        val spanCount = min(symbols.size, 5)
        (binding.correctList.layoutManager as GridLayoutManager).spanCount = spanCount
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
}