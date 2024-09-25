package org.bibletranslationtools.sun.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.databinding.ItemLessonBinding
import org.bibletranslationtools.sun.ui.model.LessonModel
import org.bibletranslationtools.sun.utils.Constants
import org.bibletranslationtools.sun.utils.TallyMarkConverter

class LessonListAdapter(
    private val context: Context,
    private val listener: OnLessonSelectedListener? = null
) : ListAdapter<LessonModel, LessonListAdapter.ViewHolder>(callback) {

    interface OnLessonSelectedListener {
        fun onLessonSelected(lesson: LessonModel, position: Int)
        fun onLessonAction(lessonId: Int, action: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemLessonBinding.inflate(inflater, parent, false)
        return ViewHolder(context, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lesson = getItem(position)
        val id = lesson.lesson.id

        with(holder.binding) {
            lessonName.text = context.getString(R.string.lesson_name, id)
            lessonTally.text = TallyMarkConverter.toText(id)

            val cardsLearnedProgress = lesson.cardsLearnedProgress
            val testSymbolsAvailable = cardsLearnedProgress == 100.0
            val cardsPassedProgress = lesson.cardsPassedProgress
            val learnSentencesAvailable = cardsPassedProgress == 100.0
            val sentencesLearnedProgress = lesson.sentencesLearnedProgress
            val sentencesPassedProgress = lesson.sentencesPassedProgress
            val testSentencesAvailable = sentencesLearnedProgress == 100.0
            val hasSentences = lesson.sentences.isNotEmpty()

            setLessonStatus(lesson, holder)

            setLearnSymbols(cardsLearnedProgress, holder)
            setTestSymbols(testSymbolsAvailable, cardsPassedProgress, holder)
            setLearnSentences(learnSentencesAvailable, sentencesLearnedProgress, hasSentences, holder)
            setTestSentences(testSentencesAvailable, sentencesPassedProgress, hasSentences, holder)
        }
    }

    class ViewHolder(
        val context: Context,
        val binding: ItemLessonBinding
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        val callback = object : DiffUtil.ItemCallback<LessonModel>() {
            override fun areItemsTheSame(
                oldItem: LessonModel,
                newItem: LessonModel
            ): Boolean {
                return oldItem.lesson.id == newItem.lesson.id
            }

            override fun areContentsTheSame(
                oldItem: LessonModel,
                newItem: LessonModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    private fun setLessonStatus(
        lesson: LessonModel,
        holder: ViewHolder
    ) {
        with(holder.binding) {
            root.isActivated = lesson.isAvailable

            when {
                lesson.isAvailable && lesson.totalProgress == 100.0 -> {
                    lessonStatus.visibility = View.VISIBLE
                    lessonProgress.visibility = View.GONE
                }

                lesson.isAvailable && lesson.totalProgress < 100.0 -> {
                    lessonStatus.visibility = View.GONE
                    lessonProgress.visibility = View.VISIBLE
                    lessonProgress.progress = lesson.totalProgress.toInt()
                }

                else -> {
                    lessonStatus.visibility = View.VISIBLE
                    lessonProgress.visibility = View.GONE
                }
            }

            updateLessonCardSelection(this, lesson.isSelected)

            if (lesson.isAvailable) {
                root.setOnClickListener {
                    val currentLesson = getItem(holder.bindingAdapterPosition)
                    listener?.onLessonSelected(currentLesson, holder.bindingAdapterPosition)
                }
            }
        }
    }

    fun refreshLesson(position: Int) {
        notifyItemChanged(position)
    }

    private fun setLearnSymbols(
        progress: Double,
        holder: ViewHolder
    ) {
        with(holder.binding) {
            learnSymbols.isActivated = true

            if (progress == 100.0) {
                learnSymbolsStatus.visibility = View.VISIBLE
                learnSymbolsProgress.visibility = View.GONE
            } else {
                learnSymbolsStatus.visibility = View.GONE
                learnSymbolsProgress.visibility = View.VISIBLE
                learnSymbolsProgress.progress = progress.toInt()
            }

            learnSymbols.setOnClickListener {
                val selectedLesson = getItem(holder.bindingAdapterPosition)
                listener?.onLessonAction(selectedLesson.lesson.id, Constants.LEARN_SYMBOLS)
            }
        }
    }

    private fun setTestSymbols(
        available: Boolean,
        progress: Double,
        holder: ViewHolder
    ) {
        with(holder.binding) {
            testSymbols.isActivated = available

            when {
                available && progress == 100.0 -> {
                    testSymbolsStatus.visibility = View.VISIBLE
                    testSymbolsProgress.visibility = View.GONE
                }
                available && progress < 100.0 -> {
                    testSymbolsStatus.visibility = View.GONE
                    testSymbolsProgress.visibility = View.VISIBLE
                    testSymbolsProgress.progress = progress.toInt()
                }
                else -> {
                    testSymbolsStatus.visibility = View.VISIBLE
                    testSymbolsProgress.visibility = View.GONE
                }
            }

            if (available) {
                testSymbols.setOnClickListener {
                    val selectedLesson = getItem(holder.bindingAdapterPosition)
                    listener?.onLessonAction(
                        selectedLesson.lesson.id,
                        Constants.TEST_SYMBOLS
                    )
                }
            }
        }
    }

    private fun setLearnSentences(
        available: Boolean,
        progress: Double,
        hasSentences: Boolean,
        holder: ViewHolder
    ) {
        with(holder.binding) {
            if (hasSentences) {
                learnSentences.visibility = View.VISIBLE
                learnSentences.isActivated = available

                when {
                    available && progress == 100.0 -> {
                        learnSentencesStatus.visibility = View.VISIBLE
                        learnSentencesProgress.visibility = View.GONE
                    }
                    available && progress < 100.0 -> {
                        learnSentencesStatus.visibility = View.GONE
                        learnSentencesProgress.visibility = View.VISIBLE
                        learnSentencesProgress.progress = progress.toInt()
                    }
                    else -> {
                        learnSentencesStatus.visibility = View.VISIBLE
                        learnSentencesProgress.visibility = View.GONE
                    }
                }

                if (available) {
                    learnSentences.setOnClickListener {
                        val selectedLesson = getItem(holder.bindingAdapterPosition)
                        listener?.onLessonAction(
                            selectedLesson.lesson.id,
                            Constants.LEARN_SENTENCES
                        )
                    }
                }
            } else {
                learnSentences.visibility = View.GONE
            }
        }
    }

    private fun setTestSentences(
        available: Boolean,
        progress: Double,
        hasSentences: Boolean,
        holder: ViewHolder
    ) {
        with(holder.binding) {
            if (hasSentences) {
                testSentences.visibility = View.VISIBLE
                testSentences.isActivated = available

                when {
                    available && progress == 100.0 -> {
                        testSentencesStatus.visibility = View.VISIBLE
                        testSentencesProgress.visibility = View.GONE
                    }
                    available && progress < 100.0 -> {
                        testSentencesStatus.visibility = View.GONE
                        testSentencesProgress.visibility = View.VISIBLE
                        testSentencesProgress.progress = progress.toInt()
                    }
                    else -> {
                        testSentencesStatus.visibility = View.VISIBLE
                        testSentencesProgress.visibility = View.GONE
                    }
                }

                if (available) {
                    testSentences.setOnClickListener {
                        val selectedLesson = getItem(holder.bindingAdapterPosition)
                        listener?.onLessonAction(
                            selectedLesson.lesson.id,
                            Constants.TEST_SENTENCES
                        )
                    }
                }
            } else {
                testSentences.visibility = View.GONE
            }
        }
    }

    private fun updateLessonCardSelection(binding: ItemLessonBinding, selected: Boolean) {
        with(binding) {
            if (selected) {
                rooms.visibility = View.VISIBLE
                lessonStatusContainer.visibility = View.GONE
                lessonName.typeface = Typeface.DEFAULT_BOLD

                root.isSelected = true
            } else {
                rooms.visibility = View.GONE
                lessonStatusContainer.visibility = View.VISIBLE
                lessonName.typeface = Typeface.DEFAULT

                root.isSelected = false
            }
        }
    }
}