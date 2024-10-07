package org.bibletranslationtools.sun.ui.control

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.material.button.MaterialButton
import org.bibletranslationtools.sun.R

private object Constants {
    const val NUM_STATES = 4
}

enum class SymbolState(val value: Int) {
    DEFAULT(0),
    SELECTED(1),
    CORRECT(2),
    INCORRECT(3);

    companion object {
        private val map = SymbolState.entries.toTypedArray().associateBy { it.value }
        fun of(value: Int) = map[value] ?: DEFAULT
    }
}

interface SymbolStateDelegate {
    companion object {
        private val STATE_DEFAULT =
            intArrayOf(R.attr.state_default, SymbolState.DEFAULT.value)
        private val STATE_SELECTED =
            intArrayOf(R.attr.state_selected, SymbolState.SELECTED.value)
        private val STATE_CORRECT =
            intArrayOf(R.attr.state_correct, SymbolState.CORRECT.value)
        private val STATE_INCORRECT =
            intArrayOf(R.attr.state_incorrect, SymbolState.INCORRECT.value)
    }

    var state: SymbolState?

    fun initState(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.sun,
            0,
            0
        ).apply {
            try {
                state = SymbolState.of(getInt(R.styleable.sun_state_symbol, 0))
            } finally {
                recycle()
            }
        }
    }

    fun getSymbolDrawableState(): IntArray {
        if (state == null) return STATE_DEFAULT

        return when (state) {
            SymbolState.SELECTED -> STATE_SELECTED
            SymbolState.CORRECT -> STATE_CORRECT
            SymbolState.INCORRECT -> STATE_INCORRECT
            else -> STATE_DEFAULT
        }
    }
}

class SymbolFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), SymbolStateDelegate {
    override var state: SymbolState? = SymbolState.DEFAULT

    init {
        initState(context, attrs)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val baseState = super.onCreateDrawableState(extraSpace + Constants.NUM_STATES)
        return mergeDrawableStates(baseState, getSymbolDrawableState())
    }
}

class SymbolButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr), SymbolStateDelegate {
    override var state: SymbolState? = SymbolState.DEFAULT
        set(value) {
            field = value
            updateIcon()
            refreshDrawableState()
        }

    init {
        initState(context, attrs)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val baseState = super.onCreateDrawableState(extraSpace + Constants.NUM_STATES)
        return mergeDrawableStates(baseState, getSymbolDrawableState())
    }

    override fun refreshDrawableState() {
        super.refreshDrawableState()
        if (icon != null) {
            val currentState = getSymbolDrawableState()
            val changed = icon.setState(currentState)

            // Force the view to draw if icon state has changed.
            if (changed) {
                invalidate()
            }
        }
    }

    private fun updateIcon() {
        // We should manually update button icon on state change
        // because MaterialButton doesn't do that automatically.
        when (state) {
            SymbolState.CORRECT -> setIconResource(R.drawable.ic_check_24)
            SymbolState.INCORRECT -> setIconResource(R.drawable.ic_close_24)
            else -> setIconResource(R.color.transparent)
        }
    }
}