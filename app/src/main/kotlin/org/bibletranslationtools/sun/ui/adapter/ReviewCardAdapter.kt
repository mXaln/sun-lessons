package org.bibletranslationtools.sun.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.databinding.ItemSymbolBinding

class ReviewCardAdapter(
    private val listener: OnCardSelectedListener? = null
) : ListAdapter<Card, ReviewCardAdapter.ViewHolder>(callback) {

    interface OnCardSelectedListener {
        fun onCardSelected(card: Card, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSymbolBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
    }

    companion object {
        val callback = object : DiffUtil.ItemCallback<Card>() {
            override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.correct == newItem.correct
            }

            override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(
        private val binding: ItemSymbolBinding
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
                    listener?.onCardSelected(selectedCard, bindingAdapterPosition)
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