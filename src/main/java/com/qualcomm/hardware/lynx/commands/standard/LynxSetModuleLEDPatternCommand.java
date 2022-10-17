package com.qualcomm.hardware.lynx.commands.standard;

import android.graphics.Color;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.hardware.Blinker;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class LynxSetModuleLEDPatternCommand extends LynxStandardCommand<LynxAck> {
    public static final int maxStepCount = 16;
    Steps steps;

    public static int cbSerializeStep() {
        return 4;
    }

    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_SET_MODULE_LED_PATTERN;
    }

    public static class Steps implements Iterable<Blinker.Step> {
        ArrayList<Blinker.Step> steps = new ArrayList<>(16);

        public void add(Blinker.Step step) {
            if (this.steps.size() < 16) {
                this.steps.add(step);
            }
        }

        public void add(int i, Blinker.Step step) {
            if (i < 16) {
                this.steps.add(i, step);
            }
        }

        public Iterator<Blinker.Step> iterator() {
            return this.steps.iterator();
        }

        public int size() {
            return this.steps.size();
        }

        public int cbSerialize() {
            int size;
            int cbSerializeStep;
            if (size() == 16) {
                size = this.steps.size();
                cbSerializeStep = LynxSetModuleLEDPatternCommand.cbSerializeStep();
            } else {
                size = this.steps.size() + 1;
                cbSerializeStep = LynxSetModuleLEDPatternCommand.cbSerializeStep();
            }
            return size * cbSerializeStep;
        }
    }

    public LynxSetModuleLEDPatternCommand(LynxModule lynxModule) {
        super(lynxModule);
        this.steps = new Steps();
    }

    public LynxSetModuleLEDPatternCommand(LynxModule lynxModule, Steps steps2) {
        this(lynxModule);
        this.steps = steps2;
    }

    public static void serializeStep(Blinker.Step step, ByteBuffer byteBuffer) {
        int color = step.getColor();
        byteBuffer.put((byte) Math.min(255, (int) Math.round(((double) step.getDurationMs()) / 100.0d)));
        byteBuffer.put((byte) Color.blue(color));
        byteBuffer.put((byte) Color.green(color));
        byteBuffer.put((byte) Color.red(color));
    }

    public static void deserializeStep(Blinker.Step step, ByteBuffer byteBuffer) {
        step.setDuration((long) (byteBuffer.get() * 100), TimeUnit.MILLISECONDS);
        byte b = byteBuffer.get();
        step.setColor(Color.rgb(byteBuffer.get(), byteBuffer.get(), b));
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(this.steps.cbSerialize()).order(LynxDatagram.LYNX_ENDIAN);
        Iterator<Blinker.Step> it = this.steps.iterator();
        while (it.hasNext()) {
            serializeStep(it.next(), order);
        }
        if (this.steps.size() < 16) {
            serializeStep(Blinker.Step.nullStep(), order);
        }
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.steps = new Steps();
        while (order.remaining() >= cbSerializeStep()) {
            Blinker.Step step = new Blinker.Step();
            deserializeStep(step, order);
            this.steps.add(step);
        }
    }
}
