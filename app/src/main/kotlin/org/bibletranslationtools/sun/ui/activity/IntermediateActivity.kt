package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivityIntermediateBinding
import org.bibletranslationtools.sun.utils.Constants
import org.bibletranslationtools.sun.utils.TallyMarkConverter

class IntermediateActivity : AppCompatActivity() {
    private val binding by lazy { ActivityIntermediateBinding.inflate(layoutInflater) }

    private var id: Int = 1
    private var part: Int = 1
    private var type: Int = Constants.LEARN_SYMBOLS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        id = intent.getIntExtra("id", 1)
        part = intent.getIntExtra("part", 1)
        type = intent.getIntExtra("type", Constants.LEARN_SYMBOLS)

        when (type) {
            Constants.LEARN_SYMBOLS -> {
                binding.pageTitle.text = getString(R.string.learn_symbols)
                binding.image.setImageResource(R.drawable.ic_learn_large)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, SymbolLearnActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("part", part)
                    startActivity(intent)
                }
            }
            Constants.TEST_SYMBOLS -> {
                binding.pageTitle.text = getString(R.string.test_symbols)
                binding.image.setImageResource(R.drawable.ic_test_large)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, SymbolReviewActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("part", part)
                    startActivity(intent)
                }
            }
            else -> {
                binding.pageTitle.text = getString(R.string.build_sentences)
                binding.image.setImageResource(R.drawable.ic_sentences_large)
                binding.startButton.setOnClickListener {
                    val intent = Intent(baseContext, BuildSentencesActivity::class.java)
                    intent.putExtra("id", id)
                    startActivity(intent)
                }
            }
        }

        binding.lessonTitle.text = getString(R.string.lesson_name, id)
        binding.lessonTally.text = TallyMarkConverter.toText(id)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(baseContext, LessonListActivity::class.java)
            intent.putExtra("selected", id)
            startActivity(intent)
        }
    }
}