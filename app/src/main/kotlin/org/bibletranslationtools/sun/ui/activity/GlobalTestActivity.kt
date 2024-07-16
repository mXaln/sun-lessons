package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.databinding.ActivityGlobalTestBinding
import org.bibletranslationtools.sun.ui.viewmodel.GlobalTestViewModel

class GlobalTestActivity : AppCompatActivity() {
    private val binding by lazy { ActivityGlobalTestBinding.inflate(layoutInflater) }
    private val viewModel: GlobalTestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(baseContext, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.testSymbols.setOnClickListener {
            if (viewModel.cardsCount.value > 0) {
                val intent = Intent(baseContext, SymbolReviewActivity::class.java)
                intent.putExtra("global", true)
                startActivity(intent)
            }
        }

        binding.buildSentences.setOnClickListener {
            if (viewModel.sentencesCount.value > 0) {
                val intent = Intent(baseContext, BuildSentencesActivity::class.java)
                intent.putExtra("global", true)
                startActivity(intent)
            }
        }

        lifecycleScope.launch {
            viewModel.cardsCount.collect {
                binding.testSymbols.isActivated = it > 0
            }
        }

        lifecycleScope.launch {
            viewModel.sentencesCount.collect {
                binding.buildSentences.isActivated = it > 0
            }
        }

        viewModel.loadAllPassedCardsCount()
        viewModel.loadAllPassedSentencesCount()
    }
}