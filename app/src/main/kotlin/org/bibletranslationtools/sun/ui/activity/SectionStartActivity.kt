package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivitySectionStartedBinding
import org.bibletranslationtools.sun.utils.Constants
import org.bibletranslationtools.sun.utils.TallyMarkConverter

class SectionStartActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySectionStartedBinding.inflate(layoutInflater) }

    private var id: Int = 1
    private var type: Int = Constants.LEARN_SYMBOLS

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
        type = intent.getIntExtra("type", Constants.LEARN_SYMBOLS)

        when (type) {
            Constants.LEARN_SYMBOLS -> {
                binding.pageTitle.text = getString(R.string.learn_symbols)
                binding.lessonTitle.text = getString(R.string.lesson_name, id)
                binding.image.setImageResource(R.drawable.ic_learn_large)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, LearnSymbolsActivity::class.java)
                    intent.putExtra("id", id)
                    startActivity(intent)
                }
            }
            Constants.TEST_SYMBOLS -> {
                binding.pageTitle.text = getString(R.string.test_symbols)
                binding.lessonTitle.text = getString(R.string.lesson_name, id)
                binding.image.setImageResource(R.drawable.ic_test_large)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, TestSymbolsActivity::class.java)
                    intent.putExtra("id", id)
                    startActivity(intent)
                }
            }
            Constants.LEARN_SENTENCES -> {
                binding.pageTitle.text = getString(R.string.learn_sentences)
                binding.lessonTitle.text = getString(R.string.lesson_name, id)
                binding.image.setImageResource(R.drawable.ic_learn_large)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, LearnSentencesActivity::class.java)
                    intent.putExtra("id", id)
                    startActivity(intent)
                }
            }
            else -> {
                binding.pageTitle.text = getString(R.string.lesson_completed, id)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, TestSentencesActivity::class.java)
                    intent.putExtra("id", id)
                    startActivity(intent)
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
}