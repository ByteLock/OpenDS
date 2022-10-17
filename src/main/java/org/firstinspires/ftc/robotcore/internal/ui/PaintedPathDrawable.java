package org.firstinspires.ftc.robotcore.internal.p013ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.PaintedPathDrawable */
public abstract class PaintedPathDrawable extends Drawable {
    protected Paint paint;
    protected Path path;

    /* access modifiers changed from: protected */
    public abstract void computePath(Rect rect);

    public int getOpacity() {
        return -3;
    }

    protected PaintedPathDrawable(int i) {
        Paint paint2 = new Paint();
        this.paint = paint2;
        paint2.setAntiAlias(true);
        this.paint.setColor(i);
    }

    public void setAlpha(int i) {
        this.paint.setAlpha(i);
    }

    public int getAlpha() {
        return this.paint.getAlpha();
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.paint.setColorFilter(colorFilter);
    }

    public ColorFilter getColorFilter() {
        return this.paint.getColorFilter();
    }

    public void draw(Canvas canvas) {
        canvas.drawPath(this.path, this.paint);
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        computePath(rect);
        invalidateSelf();
    }
}
