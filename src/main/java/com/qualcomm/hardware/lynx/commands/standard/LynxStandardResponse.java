package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.LynxResponse;

public abstract class LynxStandardResponse extends LynxResponse {
    public LynxStandardResponse(LynxModule lynxModule) {
        super(lynxModule);
    }
}
