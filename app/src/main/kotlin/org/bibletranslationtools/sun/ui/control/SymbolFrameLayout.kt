package org.bibletranslationtools.sun.ui.control

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.bibletranslationtools.sun.R


class SymbolFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private val STATE_DEFAULT = intArrayOf(R.attr.state_default, 0)
        private val STATE_SELECTED = intArrayOf(R.attr.state_selected, 1)
        private val STATE_CORRECT = intArrayOf(R.attr.state_correct, 2)
        private val STATE_INCORRECT = intArrayOf(R.attr.state_incorrect, 3)
    }

    enum class State {
        DEFAULT,
        SELECTED,
        CORRECT,
        INCORRECT
    }

    private var customState = 0

    private var _state: State = State.DEFAULT
    var state: State
        get() = _state
        set(value) {
            customState = when (value) {
                State.DEFAULT -> 0
                State.SELECTED -> 1
                State.CORRECT -> 2
                State.INCORRECT -> 3
            }
            _state = value
            refreshDrawableState()
        }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.sun, 0, 0).apply {
            try {
                customState = getInt(R.styleable.sun_state_symbol, 0)
            } finally {
                recycle()
            }
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 4)
        when (customState) {
            1 -> mergeDrawableStates(drawableState, STATE_SELECTED)
            2 -> mergeDrawableStates(drawableState, STATE_CORRECT)
            3 -> mergeDrawableStates(drawableState, STATE_INCORRECT)
            else -> mergeDrawableStates(drawableState, STATE_DEFAULT)
        }
        return drawableState
    }
}