package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxSetModuleLEDPatternCommand;
import com.qualcomm.robotcore.hardware.Blinker;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class LynxGetModuleLEDPatternResponse extends LynxStandardResponse {
    LynxSetModuleLEDPatternCommand.Steps steps = new LynxSetModuleLEDPatternCommand.Steps();

    public LynxGetModuleLEDPatternResponse(LynxModule lynxModule) {
        super(lynxModule);
    }

    public static int getStandardCommandNumber() {
        return LynxGetModuleLEDPatternCommand.getStandardCommandNumber() | 32768;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(this.steps.cbSerialize()).order(LynxDatagram.LYNX_ENDIAN);
        Iterator<Blinker.Step> it = this.steps.iterator();
        while (it.hasNext()) {
            LynxSetModuleLEDPatternCommand.serializeStep(it.next(), order);
        }
        if (this.steps.size() < 16) {
            LynxSetModuleLEDPatternCommand.serializeStep(Blinker.Step.nullStep(), order);
        }
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.steps = new LynxSetModuleLEDPatternCommand.Steps();
        while (order.remaining() >= LynxSetModuleLEDPatternCommand.cbSerializeStep()) {
            Blinker.Step step = new Blinker.Step();
            LynxSetModuleLEDPatternCommand.deserializeStep(step, order);
            this.steps.add(step);
        }
    }
}
