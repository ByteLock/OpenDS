package org.firstinspires.ftc.robotcore.internal.p013ui;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import com.qualcomm.hardware.lynx.LynxServoController;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.FilledPolygonDrawable */
public class FilledPolygonDrawable extends PaintedPathDrawable {
    protected int numSides;

    public FilledPolygonDrawable(int i, int i2) {
        super(i);
        this.paint.setStyle(Paint.Style.FILL);
        this.numSides = i2;
    }

    /* access modifiers changed from: protected */
    public void computePath(Rect rect) {
        this.path = new Path();
        this.path.setFillType(Path.FillType.EVEN_ODD);
        this.path.addPath(computePath((double) Math.min(rect.width(), rect.height()), rect.centerX(), rect.centerY()));
    }

    /* access modifiers changed from: protected */
    public Path computePath(double d, int i, int i2) {
        double d2 = 6.283185307179586d / ((double) this.numSides);
        double d3 = d / 2.0d;
        Path path = new Path();
        double d4 = (double) i;
        double d5 = (double) i2;
        path.moveTo((float) ((Math.cos(LynxServoController.apiPositionFirst) * d3) + d4), (float) ((Math.sin(LynxServoController.apiPositionFirst) * d3) + d5));
        for (int i3 = 1; i3 < this.numSides; i3++) {
            double d6 = ((double) i3) * d2;
            path.lineTo((float) ((Math.cos(d6) * d3) + d4), (float) ((Math.sin(d6) * d3) + d5));
        }
        path.close();
        return path;
    }
}
