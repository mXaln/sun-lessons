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
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.databinding.ItemSymbolLearnBinding

class LearnSymbolAdapter(
    private val onFlipListener: OnFlipAnimationListener
): ListAdapter<Card, LearnSymbolAdapter.ViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSymbolLearnBinding.inflate(layoutInflater, parent, false)
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

    inner class ViewHolder(
        val binding: ItemSymbolLearnBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Card) {
            binding.apply {
                itemText.text = card.symbol

                Glide.with(itemImage.context)
                    .load(Uri.parse("file:///android_asset/images/symbols/${card.primary}"))
                    .fitCenter()
                    .into(itemImage)

                cardViewFlip.onFlipListener = onFlipListener

                val currentState = cardViewFlip.currentFlipState
                if (currentState == FlipState.BACK_SIDE) {
                    cardViewFlip.flipTheView()
                }
            }
        }
    }
}