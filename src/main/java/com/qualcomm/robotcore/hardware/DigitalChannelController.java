package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.SerialNumber;

public interface DigitalChannelController extends HardwareDevice {
    DigitalChannel.Mode getDigitalChannelMode(int i);

    boolean getDigitalChannelState(int i);

    SerialNumber getSerialNumber();

    void setDigitalChannelMode(int i, DigitalChannel.Mode mode);

    @Deprecated
    void setDigitalChannelMode(int i, Mode mode);

    void setDigitalChannelState(int i, boolean z);

    /* renamed from: com.qualcomm.robotcore.hardware.DigitalChannelController$1 */
    static /* synthetic */ class C07211 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$hardware$DigitalChannelController$Mode */
        static final /* synthetic */ int[] f108x9f25f905;

        static {
            int[] iArr = new int[Mode.values().length];
            f108x9f25f905 = iArr;
            try {
                iArr[Mode.INPUT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    @Deprecated
    public enum Mode {
        INPUT,
        OUTPUT;

        public DigitalChannel.Mode migrate() {
            if (C07211.f108x9f25f905[ordinal()] != 1) {
                return DigitalChannel.Mode.OUTPUT;
            }
            return DigitalChannel.Mode.INPUT;
        }
    }
}
