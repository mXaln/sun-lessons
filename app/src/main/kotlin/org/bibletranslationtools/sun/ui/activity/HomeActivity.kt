package org.bibletranslationtools.sun.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ActivityHomeBinding
import org.bibletranslationtools.sun.ui.viewmodel.HomeViewModel
import org.bibletranslationtools.sun.utils.Section
import org.bibletranslationtools.sun.utils.putEnumExtra

class HomeActivity : AppCompatActivity() {
    private val viewModel: HomeViewModel by viewModels()
    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        binding.learnSymbols.setOnClickListener {
            lifecycleScope.launch {
                viewModel.navigateToSection { lastSection, lastLesson, state ->
                    var section = lastSection
                    val cls = when (state) {
                        HomeViewModel.SectionState.NOT_STARTED -> {
                            SectionStartActivity::class.java
                        }
                        HomeViewModel.SectionState.IN_PROGRESS -> {
                            when (lastSection) {
                                Section.LEARN_SYMBOLS -> LearnSymbolsActivity::class.java
                                Section.TEST_SYMBOLS -> TestSymbolsActivity::class.java
                                Section.LEARN_SENTENCES -> LearnSentencesActivity::class.java
                                else -> TestSentencesActivity::class.java
                            }
                        }
                        HomeViewModel.SectionState.COMPLETED -> {
                            var newCls: Class<*> = SectionStartActivity::class.java
                            section = when (lastSection) {
                                Section.LEARN_SYMBOLS -> Section.TEST_SYMBOLS
                                Section.TEST_SYMBOLS -> Section.LEARN_SENTENCES
                                Section.LEARN_SENTENCES -> Section.TEST_SENTENCES
                                else -> {
                                    // When we complete test sentences, we land on completed page
                                    // instead of starting page
                                    newCls = SectionCompleteActivity::class.java
                                    Section.TEST_SENTENCES
                                }
                            }
                            newCls
                        }
                    }

                    val intent = Intent(baseContext, cls)
                    intent.putExtra("id", lastLesson)
                    intent.putEnumExtra("type", section)
                    startActivity(intent)
                }
            }
        }

        binding.testSymbols.setOnClickListener {
            val intent = Intent(baseContext, GlobalTestActivity::class.java)
            startActivity(intent)
        }

        binding.bottomNavBar.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.progress -> {
                    val intent = Intent(baseContext, TrackProgressActivity::class.java)
                    startActivity(intent)
                }
                R.id.lessons -> {
                    val intent = Intent(baseContext, LessonListActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        viewModel.importLessons()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAffinity()
        }
    }

}