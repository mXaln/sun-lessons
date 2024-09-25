package org.bibletranslationtools.sun.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.data.model.Answer
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.data.model.TestCard
import org.bibletranslationtools.sun.databinding.ItemAnswerBinding
import org.bibletranslationtools.sun.databinding.ItemSymbolTestBinding

class TestSymbolAdapter(
    private val listener: OnCardSelectedListener? = null
) : ListAdapter<TestCard, RecyclerView.ViewHolder>(callback) {

    interface OnCardSelectedListener {
        fun onCardSelected(card: Card, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            ITEM_TYPE_SYMBOL -> {
                val binding = ItemSymbolTestBinding.inflate(layoutInflater, parent, false)
                SymbolViewHolder(binding)
            }
            else -> {
                val binding = ItemAnswerBinding.inflate(layoutInflater, parent, false)
                AnswerViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SymbolViewHolder -> holder.bind(getItem(position) as Card)
            is AnswerViewHolder -> holder.bind(getItem(position) as Answer)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Card -> ITEM_TYPE_SYMBOL
            else -> ITEM_TYPE_ANSWER
        }
    }

    companion object {
        val callback = object : DiffUtil.ItemCallback<TestCard>() {
            override fun areItemsTheSame(oldItem: TestCard, newItem: TestCard): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.correct == newItem.correct
            }

            override fun areContentsTheSame(oldItem: TestCard, newItem: TestCard): Boolean {
                return oldItem == newItem
            }
        }
        private const val ITEM_TYPE_SYMBOL = 0
        private const val ITEM_TYPE_ANSWER = 1
    }

    inner class SymbolViewHolder(
        private val binding: ItemSymbolTestBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Card) {
            with(binding) {
                cardText.text = card.symbol

                when(card.correct) {
                    true -> cardFrame.isActivated = true
                    false -> cardFrame.isSelected = true
                    else -> {
                        cardFrame.isActivated = false
                        cardFrame.isSelected = false
                    }
                }

                root.setOnClickListener {
                    val selectedCard = getItem(bindingAdapterPosition)
                    listener?.onCardSelected(selectedCard as Card, bindingAdapterPosition)
                }
            }
        }
    }

    inner class AnswerViewHolder(
        private val binding: ItemAnswerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Answer) {
            with(binding) {
                if (card.correct == true) {
                    answer.isActivated = true
                    answer.text = root.context.getString(R.string.correct)
                } else {
                    answer.isActivated = false
                    answer.text = root.context.getString(R.string.incorrect)
                }
            }
        }
    }

    fun selectCorrect(position: Int) {
        notifyItemChanged(position)
    }

    fun selectCorrect(item: Card) {
        val position = this.currentList.indexOf(item)
        selectCorrect(position)
    }

    fun selectIncorrect(position: Int) {
        notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }
}