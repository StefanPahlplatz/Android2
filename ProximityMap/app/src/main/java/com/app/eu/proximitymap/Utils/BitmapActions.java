package com.app.eu.proximitymap.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

/**
 * Class to preform actions on a bitmap.
 */

class BitmapActions {

    private static final int MARKER_ICON_SIZE = 200;
    private static final int MARKER_ICON_BORDER_SIZE = 28;
    private static final int MARKER_ICON_BORDER_COLOUR = Color.WHITE;

    Bitmap adjust(Bitmap original, boolean crop, boolean circle, boolean border) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width > height) {
            // Horizontal image.
            original = Bitmap.createBitmap(original, (width - height) / 2, 0, original.getHeight(),
                    original.getHeight());
        } else {
            // Vertical image.
            original = Bitmap.createBitmap(original, 0, (height - width) / 2, original.getWidth(),
                    original.getWidth());
        }

        // Crop the bitmap to a 200 by 200 image.
        if (crop) {
            original = Bitmap.createScaledBitmap(original, MARKER_ICON_SIZE, MARKER_ICON_SIZE, true);
        }

        // Get the bitmap as a circle.
        if (circle) {
            original = getCircleBitmap(original, border);
        }

        return original;
    }

    /**
     * Returns the passed bitmap as a circle with a border.
     *
     * @param bitmap to crop.
     * @return new cropped bitmap with a border.
     */
    private Bitmap getCircleBitmap(Bitmap bitmap, boolean border) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        // Create bitmap that has an alpha value.
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);

        // Prepare canvas.
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(width / 2, height / 2, width / 2, paint);

        // Draw bitmap.
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // Draw border.
        if (border) {
            paint.setColor(MARKER_ICON_BORDER_COLOUR);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((float) MARKER_ICON_BORDER_SIZE);
            canvas.drawCircle(width / 2, height / 2, width / 2, paint);
        }

        return output;
    }
}
