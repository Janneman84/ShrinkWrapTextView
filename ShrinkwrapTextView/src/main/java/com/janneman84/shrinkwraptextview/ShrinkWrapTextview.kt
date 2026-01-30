package com.janneman84.shrinkwraptextview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isGone
import kotlin.math.ceil

open class ShrinkWrapTextview : AppCompatTextView {

    private var constructed = false
    /** Set to false to disable shrink wrapping. */
    public var shrinkWrap = true

    constructor(context: Context) : super(context) {
        constructed = true
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        constructed = true
        fetchAttributes(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        constructed = true
        fetchAttributes(context, attrs)
    }

    fun fetchAttributes(context: Context, attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ShrinkWrapTextView,
            0, 0
        )

        try {
            shrinkWrap = attributes.getBoolean(R.styleable.ShrinkWrapTextView_shrinkWrap, true)
        } finally {
            attributes.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // Call super first so you can call lineCount, lineMax and measuredHeight below.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (!constructed || !shrinkWrap || isGone) {
            return
        }

        val lineCount = layout.lineCount

        // No magic needed if there is only 1 line.
        if (lineCount <= 1) {
            return
        }

        // Make sure this view or any of its parents use wrap_content.
        var wrapContentFound = false
        var pv: View? = this

        while (pv != null) {
            if (pv.layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                wrapContentFound = true
                break
            }
            pv = pv.parent as? View
            if (pv == rootView) {
                break
            }
        }

        if (!wrapContentFound) {
            return
        }

        // Get the width of the largest line.
        var lineWidth = 0f
        for (i in 0 until lineCount) {
            lineWidth = lineWidth.coerceAtLeast(layout.getLineMax(i))
        }

        // Set width equal to that of the largest line.
        val width = ceil(lineWidth.toDouble()).toInt() + paddingLeft + paddingRight
        if (width < measuredWidth) {
            // Set largest line width + horizontal padding as width and keep the height the same.
            setMeasuredDimension(width, measuredHeight)

            // Apparently this somehow triggers some layout stuff.
            // Without this right aligned text would get clipped, unless this view is wrapped inside a RelativeLayout.
            setPadding(paddingLeft - 1, paddingTop, paddingRight + 1, paddingBottom)
            setPadding(paddingLeft + 1, paddingTop, paddingRight - 1, paddingBottom)
        }
    }
}

