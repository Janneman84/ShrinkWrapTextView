package com.example.shrinkwrap

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.DynamicLayout
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import shrinkwrap.layout.ShrinkWrap

/**
 * This is an example of a custom view using StaticLayout to render text to canvas in onDraw().
 * Shrink-wrapping is done by using the special ShrinkWrap.buildStaticLayout() function.
 * */
class MyStaticLayoutView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val textPaint = TextPaint()
        textPaint.textSize = 16 * resources.displayMetrics.density;

        val text = "This is a ShrinkWrapped StaticLayout"
        val sl = ShrinkWrap.buildStaticLayout(text, 0, text.length, textPaint, 0, canvas.clipBounds.width(), true) {
            it.setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
        }

        //draw background behind text
        val background = Paint()
        background.color = "#ffd9ff04".toColorInt()
        canvas.drawRect(Rect(0, 0, sl.width, sl.height), background)

        //draws text on canvas
        sl.draw(canvas)

        //adjust view height to fit text
        layoutParams.height = sl.height;
        setLayoutParams(layoutParams)
    }
}

/**
 * This is an example of a custom view using DynamicLayout to render text to canvas in onDraw().
 * Shrink-wrapping is done by using ShrinkWrap.getLayoutRect() and use its returned rect
 * to adjust the translation when drawing to canvas.
 * */
class MyDynamicLayoutView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val textPaint = TextPaint()
        textPaint.textSize = 16 * resources.displayMetrics.density;

        val text = "This is a ShrinkWrapped DynamicLayout"
        val dl = DynamicLayout(text, textPaint, canvas.clipBounds.width(), Layout.Alignment.ALIGN_OPPOSITE, 1.0f, 0f, true)

        val swRect = ShrinkWrap.getLayoutRect(dl, 0f, true)

        //draw background behind text
        val background = Paint()
        background.color = "#ffd9ff04".toColorInt()
        canvas.drawRect(Rect(0, 0, (swRect.right-swRect.left).toInt(), dl.height), background)

        //draws text on canvas with shrink-wrap adjusted offset
        canvas.withTranslation(0 - swRect.left, 0f) {
            dl.draw(this)
        }

        //adjust view height to fit text
        layoutParams.height = dl.height;
        setLayoutParams(layoutParams)
    }
}
