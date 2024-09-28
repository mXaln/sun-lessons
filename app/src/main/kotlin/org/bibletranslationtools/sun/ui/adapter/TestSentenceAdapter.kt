package org.bibletranslationtools.sun.ui.adapter

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.data.model.Symbol
import org.bibletranslationtools.sun.databinding.ItemSentenceTestBinding
import org.bibletranslationtools.sun.ui.control.SymbolFrameLayout
import org.bibletranslationtools.sun.utils.AnswerType

class TestSentenceAdapter(
    private val listener: OnSymbolSelectedListener? = null
) : ListAdapter<Symbol, TestSentenceAdapter.ViewHolder>(callback) {

    interface OnSymbolSelectedListener {
        fun onSymbolSelected(symbol: Symbol, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSentenceTestBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val symbol = getItem(position)
        holder.bind(symbol)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    companion object {
        val callback = object : DiffUtil.ItemCallback<Symbol>() {
            override fun areItemsTheSame(oldItem: Symbol, newItem: Symbol): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.name == newItem.name &&
                        oldItem.sort == newItem.sort &&
                        oldItem.correct == newItem.correct &&
                        oldItem.selected == newItem.selected &&
                        oldItem.type == newItem.type
            }

            override fun areContentsTheSame(oldItem: Symbol, newItem: Symbol): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(
        private val binding: ItemSentenceTestBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(symbol: Symbol) {
            binding.apply {
                val itemSize = calculateItemSize(root, itemViewType)
                root.layoutParams.width = itemSize
                root.layoutParams.height = itemSize

                cardText.text = symbol.name

                // Activated - Correct
                // Selected - Incorrect

                when(symbol.correct) {
                    true -> cardFrame.state = SymbolFrameLayout.State.CORRECT
                    false -> cardFrame.state = SymbolFrameLayout.State.INCORRECT
                    else -> {
                        cardFrame.state = SymbolFrameLayout.State.DEFAULT
                    }
                }

                if (symbol.selected) {
                    cardFrame.state = SymbolFrameLayout.State.SELECTED
                }

                root.setOnClickListener {
                    val selectedSymbol = getItem(bindingAdapterPosition)
                    if (!selectedSymbol.selected) {
                        listener?.onSymbolSelected(selectedSymbol, bindingAdapterPosition)
                    }
                }
            }
        }
    }

    fun refreshItem(position: Int) {
        notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }

    private fun calculateItemSize(itemView: View, itemViewType: Int): Int {
        val displayMetric = itemView.context.resources.displayMetrics
        val spacing = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            10f,
            displayMetric
        )
        val availableWidth = displayMetric.widthPixels -
                itemView.context.resources.getDimension(R.dimen._40dp) -
                ((itemCount - 1) * spacing)
        val normalSize = itemView.context.resources.getDimension(R.dimen._86dp).toInt()

        return when (itemViewType) {
            AnswerType.OPTION.ordinal -> normalSize
            else -> {
                if (itemCount <= 4) {
                    normalSize
                } else {
                    availableWidth / itemCount
                }.toInt()
            }
        }
    }
}