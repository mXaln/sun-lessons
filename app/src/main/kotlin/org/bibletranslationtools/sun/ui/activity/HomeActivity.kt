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
                viewModel.navigateToSection { section, lessonId, started ->
                    val cls = if (started) {
                        when (section) {
                            Section.LEARN_SYMBOLS -> LearnSymbolsActivity::class.java
                            Section.TEST_SYMBOLS -> TestSymbolsActivity::class.java
                            Section.LEARN_SENTENCES -> LearnSentencesActivity::class.java
                            else -> TestSentencesActivity::class.java
                        }
                    } else SectionStartActivity::class.java

                    val intent = Intent(this@HomeActivity, cls)
                    intent.putExtra("id", lessonId)
                    intent.putEnumExtra("type", section)
                    startActivity(intent)
                }
            }
        }

        binding.testSymbols.setOnClickListener {
            val intent = Intent(this, GlobalTestActivity::class.java)
            startActivity(intent)
        }

        binding.bottomNavBar.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.progress -> {
                    val intent = Intent(this, TrackProgressActivity::class.java)
                    startActivity(intent)
                }
                R.id.lessons -> {
                    val intent = Intent(this, LessonListActivity::class.java)
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