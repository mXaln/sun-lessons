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
                itemText.text = sentence.symbols.joinToString(" ") { it.name }

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