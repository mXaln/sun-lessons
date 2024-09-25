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
import org.bibletranslationtools.sun.ui.viewmodel.TestSentencesViewModel
import org.bibletranslationtools.sun.utils.Constants
import org.bibletranslationtools.sun.utils.TallyMarkConverter

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

    private lateinit var currentSentence: SentenceWithSymbols
    private var lastAnswerPosition = -1

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

            answersList.layoutManager = LinearLayoutManager(
                this@TestSentencesActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            answersList.adapter = answersAdapter
            answersList.addItemDecoration(
                LinearItemOffsetDecoration(10)
            )

            optionsList.layoutManager = GridLayoutManager(
                this@TestSentencesActivity,
                4
            )
            optionsList.adapter = optionsAdapter
            optionsList.addItemDecoration(GridItemOffsetDecoration(
                spanCount = 4,
                spacing = 20,
                includeEdge = false
            ))

            correctList.layoutManager = LinearLayoutManager(
                this@TestSentencesActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            correctList.adapter = correctAdapter
            correctList.addItemDecoration(
                LinearItemOffsetDecoration(10)
            )

            lifecycleScope.launch {
                viewModel.sentences.collect {
                    if (it.isNotEmpty()) {
                        setNextSentence()
                    }
                }
            }

            nextButton.setOnClickListener {
                if (viewModel.sentenceDone.value) {
                    setNextSentence()
                    viewModel.sentenceDone.value = false
                }
            }

            if (viewModel.isGlobal.value) {
                viewModel.loadAllPassedSentences()
            } else {
                viewModel.loadSentences()
            }
        }
    }

    override fun onSymbolSelected(symbol: Symbol, position: Int) {
        if (!viewModel.sentenceDone.value) {
            symbol.correct = true
            symbol.selected = true
            optionsAdapter.refreshItem(position)

            val answerSymbol = symbol.copy()
            answerSymbol.type = Constants.TYPE_ANSWER

            lastAnswerPosition++
            answerSymbols[lastAnswerPosition] = answerSymbol
            answersAdapter.submitList(answerSymbols)
            answersAdapter.refreshItem(lastAnswerPosition)

            if (lastAnswerPosition >= answersAdapter.itemCount - 1) {
                checkAnswer()
                viewModel.sentenceDone.value = true
                binding.nextButton.isEnabled = true
                lastAnswerPosition = -1
            }
        }
    }

    private fun checkAnswer() {
        val correctSymbols = currentSentence.symbols

        val isSentenceCorrect = correctSymbols.map { it.id } == answerSymbols.map { it.id }

        if (isSentenceCorrect) {
            lifecycleScope.launch(Dispatchers.IO) {
                currentSentence.sentence.passed = true
                currentSentence.sentence.answered = true
                viewModel.updateSentence(currentSentence.sentence)
            }
        }

        answerSymbols.zip(correctSymbols).withIndex().forEach { (index, pair) ->
            pair.first.correct = pair.first.name == pair.second.name
            answersAdapter.refreshItem(index)
        }

        setCorrectSymbols(correctSymbols)

        binding.correctContainer.visibility = View.VISIBLE
        binding.optionsContainer.visibility = View.GONE
    }

    private fun setNextSentence() {
        binding.nextButton.isEnabled = false

        val allSentences = viewModel.sentences.value.toMutableList()
        val inProgressSentences = allSentences.filter { !it.sentence.answered }

        if (inProgressSentences.isEmpty()) {
            finishTest()
            return
        }

        setRandomSentence(inProgressSentences)

        Glide.with(baseContext)
            .load(Uri.parse("file:///android_asset/images/sentences/${currentSentence.sentence.correct}"))
            .fitCenter()
            .into(binding.itemImage)

        lifecycleScope.launch {
            val cards = viewModel.getAllCards()
            val cardSymbols = cards.map { Symbol(id = 0, name = it.symbol, sort = 0) }
            val totalOptions =
                if (currentSentence.symbols.size > OPTIONS_SHORT) OPTIONS_LONG else OPTIONS_SHORT

            val allSymbols = cardSymbols + allSentences
                .map { it.symbols }
                .flatten()

            val incorrectSymbols = allSymbols
                .filter { symbol ->
                    val correctSymbols = currentSentence.symbols
                    correctSymbols.none { it.name == symbol.name }
                }
                .distinctBy { it.name }
                .shuffled()
                .take(totalOptions - currentSentence.symbols.size)

            val options = (currentSentence.symbols + incorrectSymbols).shuffled()
            setOptions(options)

            val answers = currentSentence.symbols.map { it.copy(name = "") }
            setAnswers(answers)

            setCorrectSymbols(listOf())

            binding.correctContainer.visibility = View.GONE
            binding.optionsContainer.visibility = View.VISIBLE
        }
    }

    private fun setRandomSentence(sentences: List<SentenceWithSymbols>) {
        // Try to select a sentence that has not been asked before
        if (this::currentSentence.isInitialized && sentences.size > 1) {
            val oldSentence = currentSentence.copy()
            while (oldSentence == currentSentence) {
                currentSentence = sentences.random()
            }
        } else {
            currentSentence = sentences.random()
        }
    }

    private fun finishTest() {
        if (viewModel.isGlobal.value) {
            val intent = Intent(baseContext, GlobalTestActivity::class.java)
            startActivity(intent)
        } else {
            lifecycleScope.launch {
                val lessons = viewModel.getAllLessons().map { it.id }
                val current = lessons.indexOf(viewModel.lessonId.value)
                var next = 1
                if (current < lessons.size - 1) {
                    next = lessons[current + 1]
                }

                runOnUiThread {
                    val intent = Intent(baseContext, LessonListActivity::class.java)
                    intent.putExtra("selected", next)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.sentenceDone.value = false
    }

    private fun setOptions(symbols: List<Symbol>) {
        symbols.forEach {
            it.selected = false
            it.correct = null
            it.type = Constants.TYPE_OPTION
        }

        optionSymbols.clear()
        optionSymbols.addAll(symbols)
        optionsAdapter.submitList(symbols)
        optionsAdapter.refresh()
    }

    private fun setAnswers(symbols: List<Symbol>) {
        symbols.forEach {
            it.selected = true
            it.correct = null
            it.type = Constants.TYPE_ANSWER
        }

        answerSymbols.clear()
        answerSymbols.addAll(symbols)
        answersAdapter.submitList(symbols)
        answersAdapter.refresh()
    }

    private fun setCorrectSymbols(symbols: List<Symbol>) {
        symbols.forEach {
            it.selected = true
            it.correct = true
            it.type = Constants.TYPE_ANSWER
        }

        correctSymbols.clear()
        correctSymbols.addAll(symbols)
        correctAdapter.submitList(symbols)
        correctAdapter.refresh()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = if (viewModel.isGlobal.value) {
                Intent(baseContext, GlobalTestActivity::class.java)
            } else {
                Intent(baseContext, LessonListActivity::class.java)
            }
            intent.putExtra("selected", viewModel.lessonId.value)
            startActivity(intent)
        }
    }
}