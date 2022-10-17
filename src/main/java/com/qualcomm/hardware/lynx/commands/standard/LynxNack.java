package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.robotcore.util.TypeConversion;
import org.firstinspires.ftc.robotcore.internal.system.Assert;

public class LynxNack extends LynxMessage {
    private int nackReasonCode;

    public interface ReasonCode {
        int getValue();

        boolean isUnsupportedReason();

        String toString();
    }

    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_NACK;
    }

    public boolean isNack() {
        return true;
    }

    public enum StandardReasonCode implements ReasonCode {
        PARAM0(0),
        PARAM1(1),
        PARAM2(2),
        PARAM3(3),
        PARAM4(4),
        PARAM5(5),
        PARAM6(6),
        PARAM7(7),
        PARAM8(8),
        PARAM9(9),
        GPIO_OUT0(10),
        GPIO_OUT1(11),
        GPIO_OUT2(12),
        GPIO_OUT3(13),
        GPIO_OUT4(14),
        GPIO_OUT5(15),
        GPIO_OUT6(16),
        GPIO_OUT7(17),
        GPIO_NO_OUTPUT(18),
        GPIO_IN0(20),
        GPIO_IN1(21),
        GPIO_IN2(22),
        GPIO_IN3(23),
        GPIO_IN4(24),
        GPIO_IN5(25),
        GPIO_IN6(26),
        GPIO_IN7(27),
        GPIO_NO_INPUT(28),
        SERVO_NOT_CONFIG_BEFORE_ENABLED(30),
        BATTERY_TOO_LOW_TO_RUN_SERVO(31),
        I2C_MASTER_BUSY(40),
        I2C_OPERATION_IN_PROGRESS(41),
        I2C_NO_RESULTS_PENDING(42),
        I2C_QUERY_MISMATCH(43),
        I2C_TIMEOUT_SDA_STUCK(44),
        I2C_TIMEOUT_SCK_STUCK(45),
        I2C_TIMEOUT_UNKNOWN_CAUSE(46),
        MOTOR_NOT_CONFIG_BEFORE_ENABLED(50),
        COMMAND_INVALID_FOR_MOTOR_MODE(51),
        BATTERY_TOO_LOW_TO_RUN_MOTOR(52),
        COMMAND_IMPL_PENDING(253),
        COMMAND_ROUTING_ERROR(254),
        PACKET_TYPE_ID_UNKNOWN(255),
        ABANDONED_WAITING_FOR_RESPONSE(256),
        ABANDONED_WAITING_FOR_ACK(257),
        UNRECOGNIZED_REASON_CODE(258);
        
        private int iVal;

        private StandardReasonCode(int i) {
            this.iVal = i;
        }

        public int getValue() {
            return this.iVal;
        }

        public boolean isUnsupportedReason() {
            int i = C07031.f104xe0728a3e[ordinal()];
            return i == 1 || i == 2 || i == 3;
        }
    }

    /* renamed from: com.qualcomm.hardware.lynx.commands.standard.LynxNack$1 */
    static /* synthetic */ class C07031 {

        /* renamed from: $SwitchMap$com$qualcomm$hardware$lynx$commands$standard$LynxNack$StandardReasonCode */
        static final /* synthetic */ int[] f104xe0728a3e;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode[] r0 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f104xe0728a3e = r0
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.COMMAND_IMPL_PENDING     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f104xe0728a3e     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.COMMAND_ROUTING_ERROR     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f104xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.PACKET_TYPE_ID_UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.commands.standard.LynxNack.C07031.<clinit>():void");
        }
    }

    private static class UnrecognizedReasonCode implements ReasonCode {
        private final int value;

        public boolean isUnsupportedReason() {
            return true;
        }

        UnrecognizedReasonCode(int i) {
            this.value = i;
        }

        public String toString() {
            return "Unrecognized NACK code " + this.value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public LynxNack(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxNack(LynxModuleIntf lynxModuleIntf, int i) {
        this(lynxModuleIntf);
        this.nackReasonCode = i;
    }

    public LynxNack(LynxModuleIntf lynxModuleIntf, ReasonCode reasonCode) {
        this(lynxModuleIntf, reasonCode.getValue());
    }

    public ReasonCode getNackReasonCode() {
        if (this.nackReasonCode == StandardReasonCode.I2C_OPERATION_IN_PROGRESS.getValue()) {
            return StandardReasonCode.I2C_OPERATION_IN_PROGRESS;
        }
        if (this.nackReasonCode == StandardReasonCode.I2C_MASTER_BUSY.getValue()) {
            return StandardReasonCode.I2C_MASTER_BUSY;
        }
        for (StandardReasonCode standardReasonCode : StandardReasonCode.values()) {
            if (this.nackReasonCode == standardReasonCode.getValue()) {
                return standardReasonCode;
            }
        }
        return new UnrecognizedReasonCode(this.nackReasonCode);
    }

    public StandardReasonCode getNackReasonCodeAsEnum() {
        ReasonCode nackReasonCode2 = getNackReasonCode();
        if (nackReasonCode2 instanceof StandardReasonCode) {
            return (StandardReasonCode) nackReasonCode2;
        }
        return StandardReasonCode.UNRECOGNIZED_REASON_CODE;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        int i = this.nackReasonCode;
        Assert.assertTrue(((byte) i) == i);
        return new byte[]{(byte) this.nackReasonCode};
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.nackReasonCode = TypeConversion.unsignedByteToInt(bArr[0]);
    }
}
