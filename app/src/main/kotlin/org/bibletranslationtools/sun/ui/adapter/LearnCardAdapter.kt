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
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.databinding.ItemLearnBinding


class LearnCardAdapter(
    private val flipState: StateFlow<EasyFlipView.FlipState>
) : ListAdapter<Card, LearnCardAdapter.ViewHolder>(callback) {

    private var flipListener: OnFlipAnimationListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLearnBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
    }

    companion object {
        val callback = object : DiffUtil.ItemCallback<Card>() {
            override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun setFlipListener(listener: OnFlipAnimationListener) {
        flipListener = listener
    }

    inner class ViewHolder(
        val binding: ItemLearnBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Card) {
            binding.apply {
                itemText.text = card.symbol

                Glide.with(itemImage.context)
                    .load(Uri.parse("file:///android_asset/images/symbols/${card.primary}"))
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