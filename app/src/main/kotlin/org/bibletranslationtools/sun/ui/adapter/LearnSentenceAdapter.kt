package org.bibletranslationtools.sun.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wajahatkarim3.easyflipview.EasyFlipView.FlipState
import com.wajahatkarim3.easyflipview.EasyFlipView.OnFlipAnimationListener
import org.bibletranslationtools.sun.data.model.SentenceWithSymbols
import org.bibletranslationtools.sun.databinding.ItemSentenceLearnBinding
import kotlin.math.ceil


class LearnSentenceAdapter(
    private val flipListener: OnFlipAnimationListener
) : ListAdapter<SentenceWithSymbols, LearnSentenceAdapter.ViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSentenceLearnBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sentence = getItem(position)
        holder.bind(sentence)
    }

    companion object {
        val callback = object : DiffUtil.ItemCallback<SentenceWithSymbols>() {
            override fun areItemsTheSame(oldItem: SentenceWithSymbols, newItem: SentenceWithSymbols): Boolean {
                return oldItem.sentence.id == newItem.sentence.id
            }

            override fun areContentsTheSame(oldItem: SentenceWithSymbols, newItem: SentenceWithSymbols): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun flipCard(view: RecyclerView.ViewHolder) {
        (view as ViewHolder).binding.cardViewFlip.flipTheView()
    }

    inner class ViewHolder(
        val binding: ItemSentenceLearnBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sentence: SentenceWithSymbols) {
            binding.apply {
                val symbolsCount = sentence.symbols.size
                val maxSymbols = 5

                val text = if (symbolsCount > maxSymbols) {
                    val symbols = sentence.symbols.map { it.name }
                    val builder = StringBuilder()
                    var counter = 1
                    for (symbol in symbols) {
                        builder.append(symbol)
                        if (counter == maxSymbols) {
                            builder.append("\n")
                            counter = 1
                        } else {
                            builder.append(" ")
                        }
                        counter += symbol.length
                    }
                    builder.toString()
                } else {
                    sentence.symbols.joinToString(" ") { it.name }
                }

                itemText.maxLines = ceil(symbolsCount / maxSymbols.toDouble()).toInt()
                itemText.text = text

                if (itemText.maxLines > 1) {
                    itemText.gravity = android.view.Gravity.CENTER_VERTICAL
                } else {
                    itemText.gravity = android.view.Gravity.CENTER
                }

                Glide.with(itemImage.context)
                    .load(Uri.parse("file:///android_asset/images/sentences/${sentence.sentence.correct}"))
                    .fitCenter()
                    .into(itemImage)

                cardViewFlip.onFlipListener = flipListener

                val currentState = cardViewFlip.currentFlipState
                if (currentState == FlipState.BACK_SIDE) {
                    cardViewFlip.flipTheView()
                }
            }
        }
    }
}