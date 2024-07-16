package org.bibletranslationtools.sun.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.bibletranslationtools.sun.databinding.GridLessonBinding
import org.bibletranslationtools.sun.ui.model.LessonModel
import org.bibletranslationtools.sun.utils.TallyMarkConverter

class LessonGridAdapter(
    private val context: Context
) : ListAdapter<LessonModel, LessonGridAdapter.ViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = GridLessonBinding.inflate(inflater, parent, false)
        return ViewHolder(context, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lesson = getItem(position)
        val id = lesson.lesson.id

        with(holder.binding) {
            lessonName.text = id.toString()
            lessonTally.text = TallyMarkConverter.toText(id)

            val completed = lesson.totalProgress == 100.0

            root.isSelected = completed

            if (completed) {
                lock.visibility = View.GONE
                lessonName.visibility = View.VISIBLE
                lessonTally.visibility = View.VISIBLE
            } else {
                lock.visibility = View.VISIBLE
                lessonName.visibility = View.GONE
                lessonTally.visibility = View.GONE
            }
        }
    }

    class ViewHolder(
        val context: Context,
        val binding: GridLessonBinding
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
}