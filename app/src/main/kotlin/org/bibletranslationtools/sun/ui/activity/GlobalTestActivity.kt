package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
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
        setSupportActionBar(binding.topNavBar.toolbar)
        supportActionBar?.title = null

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        binding.topNavBar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.testSymbols.setOnClickListener {
            if (viewModel.cardsCount.value > 0) {
                val intent = Intent(baseContext, TestSymbolsActivity::class.java)
                intent.putExtra("global", true)
                startActivity(intent)
            }
        }

        binding.testSentences.setOnClickListener {
            if (viewModel.sentencesCount.value > 0) {
                val intent = Intent(baseContext, TestSentencesActivity::class.java)
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
                binding.testSentences.isActivated = it > 0
            }
        }

        viewModel.loadAllTestedCardsCount()
        viewModel.loadAllTestedSentencesCount()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(baseContext, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}