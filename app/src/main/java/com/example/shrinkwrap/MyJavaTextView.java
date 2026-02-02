package com.example.shrinkwrap;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import com.janneman84.shrinkwraptextview.ShrinkWrapTextViewKt;

/**
 * This is an example of a TextView subclass (Java) that does not subclass from ShrinkWrapTextView.
 * Shrink wrapping is still possible by overriding onMeasure() like shown here.
 * This also works for Button subclasses.
 * */
public class MyJavaTextView extends AppCompatTextView {

    public MyJavaTextView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); // Call super first!
        setMeasuredDimension(ShrinkWrapTextViewKt.measureShrinkWrappedWidth(this), getMeasuredHeight());
    }
}
