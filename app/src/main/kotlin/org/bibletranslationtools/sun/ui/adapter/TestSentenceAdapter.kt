package org.bibletranslationtools.sun.ui.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.R
import org.bibletranslationtools.sun.data.model.Symbol
import org.bibletranslationtools.sun.databinding.ItemSentenceTestBinding
import org.bibletranslationtools.sun.ui.control.SymbolState
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
        private const val MAX_LINE_SYMBOLS = 5
        private val CARD_WIDTH = R.dimen._86dp
        private val CARD_RADIUS = R.dimen._16dp
        private val CARD_PADDING = R.dimen._10dp
        private val CARD_SPACING = R.dimen._10dp
        private val CONTAINER_MARGINS = R.dimen._40dp

        private val callback = object : DiffUtil.ItemCallback<Symbol>() {
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

                when(symbol.correct) {
                    true -> cardFrame.state = SymbolState.CORRECT
                    false -> cardFrame.state = SymbolState.INCORRECT
                    else -> {
                        cardFrame.state = SymbolState.DEFAULT
                    }
                }

                if (symbol.selected) {
                    cardFrame.state = SymbolState.SELECTED
                }

                if (symbol.type == AnswerType.ANSWER) {
                    autoScaleAnswerCard(cardFrame, root)
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

    /**
     * Calculate the size of the item based on the item type
     */
    private fun calculateItemSize(itemView: View, itemViewType: Int): Int {
        val resources = itemView.context.resources
        val displayMetric = resources.displayMetrics
        val margins = resources.getDimension(CONTAINER_MARGINS)
        val normalSize = resources.getDimension(CARD_WIDTH).toInt()

        return when (itemViewType) {
            AnswerType.OPTION.ordinal -> normalSize
            AnswerType.RESULT.ordinal -> {
                val availableWidth = getAvailableWidth(displayMetric, margins, 0f)
                if (itemCount < MAX_LINE_SYMBOLS) {
                    normalSize
                } else {
                    availableWidth / MAX_LINE_SYMBOLS
                }.toInt()
            }
            else -> {
                val spacing = resources.getDimension(CARD_SPACING)
                val availableWidth = getAvailableWidth(displayMetric, margins, spacing)
                if (itemCount < MAX_LINE_SYMBOLS) {
                    normalSize
                } else {
                    availableWidth / itemCount
                }.toInt()
            }
        }
    }

    /**
     * Get the available width of the recycler view,
     * considering the margins and the spacing between items.
     */
    private fun getAvailableWidth(
        displayMetric: DisplayMetrics,
        margins: Float,
        spacing: Float
    ): Float {
        return displayMetric.widthPixels - margins - ((itemCount - 1) * spacing)
    }

    /**
     * Autoscale corner radius and padding of the answer card based on the width of the card.
     */
    private fun autoScaleAnswerCard(card: View, root: View) {
        val cardWidth = root.layoutParams.width
        val normalWidth = root.context.resources.getDimension(CARD_WIDTH)

        val normalRadius = card.context.resources.getDimension(CARD_RADIUS)
        val radiusRatio = normalRadius / normalWidth
        val cardRadius = cardWidth * radiusRatio

        val normalPadding = card.context.resources.getDimension(CARD_PADDING)
        val paddingRatio = normalPadding / normalWidth
        val padding = cardWidth * paddingRatio

        val stateListDrawable = card.background as? StateListDrawable
        stateListDrawable?.let {
            CoroutineScope(Dispatchers.Main).launch {
                val drawable = it.current as? GradientDrawable
                drawable?.cornerRadius = cardRadius
                card.background = it
                card.setPadding(padding.toInt())
                card.refreshDrawableState()
            }
        }
    }
}