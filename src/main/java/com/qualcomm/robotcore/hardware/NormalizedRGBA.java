package com.qualcomm.robotcore.hardware;

import android.graphics.Color;
import com.qualcomm.robotcore.util.Range;

public class NormalizedRGBA {
    public float alpha;
    public float blue;
    public float green;
    public float red;

    public int toColor() {
        return Color.argb(Range.clip((int) (this.alpha * 256.0f), 0, 255), Range.clip((int) (this.red * 256.0f), 0, 255), Range.clip((int) (this.green * 256.0f), 0, 255), Range.clip((int) (this.blue * 256.0f), 0, 255));
    }
}
