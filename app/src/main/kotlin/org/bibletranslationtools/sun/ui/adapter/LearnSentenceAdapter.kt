package org.bibletranslationtools.sun.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wajahatkarim3.easyflipview.EasyFlipView
import com.wajahatkarim3.easyflipview.EasyFlipView.OnFlipAnimationListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.data.model.Sentence
import org.bibletranslationtools.sun.data.model.SentenceWithSymbols
import org.bibletranslationtools.sun.databinding.ItemSentenceLearnBinding


class LearnSentenceAdapter(
    private val flipState: StateFlow<EasyFlipView.FlipState>
) : ListAdapter<SentenceWithSymbols, LearnSentenceAdapter.ViewHolder>(callback) {

    private var flipListener: OnFlipAnimationListener? = null

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

    fun setFlipListener(listener: OnFlipAnimationListener) {
        flipListener = listener
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

                CoroutineScope(Dispatchers.Main).launch {
                    flipState.collect {
                        val currentState = cardViewFlip.currentFlipState
                        if (it != currentState) {
                            cardViewFlip.flipTheView()
                        }
                    }
                }
            }
        }
    }
}