package shrinkwrap.layout;

import android.graphics.RectF;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.RequiresApi;
import androidx.core.util.Consumer;

import static java.lang.Math.max;

public class ShrinkWrap {

    /**
     * ShrinkWrap alternative for StaticLayout.Builder.obtain().
     * This returns a StaticLayout with its width reduced to exactly fit its text.
     * Also works if text has mixed alignments.
     * Example:
     * <pre>
     * {@code
     *         // Java
     *         ShrinkWrap.buildStaticLayout(myText, 0, myText.length(), myPaint, 0, 500, true, b -> b
     *             .setAlignment(Layout.Alignment.ALIGN_CENTER)
     *         );
     * }
     * </pre>
     * <pre>
     * {@code
     *         // Kotlin
     *         ShrinkWrap.buildStaticLayout(myText, 0, text.length, myPaint, 0, 500, true) {
     *             it.setAlignment(Layout.Alignment.ALIGN_CENTER)
     *         }
     * }
     * </pre>
     * Do NOT call .build() in the builderConfig callback of this method.
     *
     * @param source        The text to be laid out, optionally with spans
     * @param start         The index of the start of the text
     * @param end           The index + 1 of the end of the text
     * @param paint         The base paint used for layout
     * @param minWidth      The minimum width in pixels
     * @param maxWidth      The maximum width in pixels
     * @param shrinkWrap    Enable/disable shrink-wrapping here
     * @param builderConfig Callback to configure the builder
     * @return StaticLayout object with adjusted size, ready to draw on canvas.
     */

    @RequiresApi(Build.VERSION_CODES.M)
    public static StaticLayout buildStaticLayout(
            CharSequence source,
            int start,
            int end,
            TextPaint paint,
            int minWidth,
            int maxWidth,
            boolean shrinkWrap,
            Consumer<StaticLayout.Builder> builderConfig
    ) {
        StaticLayout.Builder builder = StaticLayout.Builder.obtain(source, start, end, paint, maxWidth);

        if (builderConfig != null) {
            builderConfig.accept(builder);
        }

        StaticLayout sl;
        try {
            sl = builder.build();
        } catch (Exception e) {
            throw new AssertionError("Don't call .build() in the builderConfig callback of the ShrinkWrap.buildStaticLayout() method.");
        }

        if (!shrinkWrap || minWidth >= maxWidth) {
            return sl;
        }

        float layoutWidthF = sl.getWidth();
        float maxCenterWidth = 0f;
        boolean hasLeft = false;
        boolean hasRight = false;
        boolean hasCenter = false;

        float maxLineWidth = 0;
        for (int i = 0; i < sl.getLineCount(); i++) {
//            maxLineWidth = Math.max(maxLineWidth, (int) ceil(sl.getLineMax(i)));

            float left = sl.getLineLeft(i);
            float right = sl.getLineRight(i);

            float lineWidth = sl.getLineMax(i);

            if (lineWidth > maxLineWidth ) {
                maxLineWidth = lineWidth;
            }

            // Check for situations that don't have a clear alignment.
            if (left < 0f || right > layoutWidthF) {
                return sl;
            }
            else {
                int ls = sl.getLineStart(i);
                // If line is wrapped it will have same alignment as previous line, so ignore
                if (i == 0 || ls <= 0 || sl.getText().charAt(ls - 1) == '\n') {

                    Layout.Alignment alignment = sl.getParagraphAlignment(i);
                    int textDirection = sl.getParagraphDirection(i);

                    switch (alignment) {
                        case ALIGN_CENTER: {
                            hasCenter = true;
                            maxCenterWidth = max(maxCenterWidth, lineWidth);
                            break;
                        }
                        case ALIGN_NORMAL:
                            switch (textDirection) {
                                case 1:
                                    hasLeft = true;
                                    break;
                                case -1:
                                    hasRight = true;
                                    break;
                                default:
                                    return sl;
                            }
                            break;
                        case ALIGN_OPPOSITE:
                            switch (textDirection) {
                                case -1:
                                    hasLeft = true;
                                    break;
                                case 1:
                                    hasRight = true;
                                    break;
                                default:
                                    return sl;
                            }
                            break;
                    }
                }
            }
        }

        if ((hasLeft ^ hasRight) && hasCenter) {
            maxLineWidth = max(maxLineWidth, ((maxWidth - maxCenterWidth) * 0.5f) + maxCenterWidth);
        }
        else if (hasLeft && hasRight) {
            maxLineWidth = maxWidth;
        }

        int maxLineWidthInt = (int) Math.ceil(maxLineWidth);

        if (maxLineWidthInt == maxWidth) {
            return sl;
        }

        StaticLayout.Builder builder2 = StaticLayout.Builder.obtain(source, start, end, paint, Math.max(minWidth, maxLineWidthInt));

        if (builderConfig != null) {
            builderConfig.accept(builder2);
        }

        return builder2.build();
    }

    /**
     * Returns shrink-wrapped rect of te supplied layout, i.e. the rect that tightly fits the text.
     * Shrink-wrapping will not work if shrinkWrap is false or if the text has mixed alignments. In that case te full size of the layout will be returned.
     *
     * @param layout     supply StaticLayout or DynamicLayout object
     * @param minWidth   The minimum width in pixels
     * @param shrinkWrap enable/disable shrink-wrapping
     * @return Rect you may use to measure and adjust positioning of the text when drawing.
     *
     */
    public static RectF getLayoutRect(Layout layout, float minWidth, Boolean shrinkWrap) {

        if (!shrinkWrap || minWidth >= layout.getWidth()) {
            return new RectF(0, 0, layout.getWidth(), layout.getHeight());
        }

        float minLeft = 1000000.0f;
        float maxRight = -1000000.0f;

        boolean hasLeft = false;
        boolean hasRight = false;
        boolean hasCenter = false;
        boolean hasUnspecified = false;

        for (int i = 0; i < layout.getLineCount(); i++) {

            int ls = layout.getLineStart(i);
            // If line is wrapped it will have same alignment as previous line, so ignore
            if (i == 0 || ls <= 0 || layout.getText().charAt(ls - 1) == '\n') {

                Layout.Alignment alignment = layout.getParagraphAlignment(i);
                int textDirection = layout.getParagraphDirection(i);

                switch (alignment) {
                    case ALIGN_CENTER: {
                        hasCenter = true;
                        break;
                    }
                    case ALIGN_NORMAL:
                        switch (textDirection) {
                            case 1:
                                hasLeft = true;
                                break;
                            case -1:
                                hasRight = true;
                                break;
                            default:
                                hasUnspecified = true;
                                break;
                        }
                        break;
                    case ALIGN_OPPOSITE:
                        switch (textDirection) {
                            case -1:
                                hasLeft = true;
                                break;
                            case 1:
                                hasRight = true;
                                break;
                            default:
                                hasUnspecified = true;
                                break;
                        }
                        break;
                }

                if (hasUnspecified || (hasLeft && hasRight) || (hasLeft && hasCenter) || (hasCenter && hasRight)) {
                    return new RectF(0, 0, layout.getWidth(), layout.getHeight());
                }
            }
            minLeft = Math.min(minLeft, layout.getLineLeft(i));
            maxRight = Math.max(maxRight, layout.getLineRight(i));
        }

        float extraWidth = Math.max(0, minWidth - (maxRight - minLeft));
        if (extraWidth > 0) {
            if (hasLeft) {
                maxRight = Math.min(maxRight + extraWidth, layout.getWidth());
            } else if (hasRight) {
                minLeft = Math.max(minLeft - extraWidth, 0);
            } else if (hasCenter) {
                minLeft = Math.max(minLeft - (extraWidth * 0.5f), 0);
                maxRight = Math.min(maxRight + (extraWidth * 0.5f), layout.getWidth());
            }
        }

        return new RectF(minLeft, 0, maxRight, layout.getHeight());
    }
}