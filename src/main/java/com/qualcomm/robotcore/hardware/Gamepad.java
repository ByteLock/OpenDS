package com.qualcomm.robotcore.hardware;

import android.os.SystemClock;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import com.qualcomm.robotcore.robocol.RobocolParsableBase;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import org.firstinspires.ftc.robotcore.internal.collections.EvictingBlockingQueue;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.internal.p013ui.GamepadUser;

public class Gamepad extends RobocolParsableBase {
    private static final short BUFFER_SIZE = 65;
    public static final int ID_SYNTHETIC = -2;
    public static final int ID_UNASSOCIATED = -1;
    public static final int LED_DURATION_CONTINUOUS = -1;
    private static final short PAYLOAD_SIZE = 60;
    private static final byte ROBOCOL_GAMEPAD_VERSION = 5;
    public static final int RUMBLE_DURATION_CONTINUOUS = -1;
    private static final long RUMBLE_FINISH_TIME_FLAG_INFINITE = Long.MAX_VALUE;
    private static final long RUMBLE_FINISH_TIME_FLAG_NOT_RUMBLING = -1;

    /* renamed from: a */
    public volatile boolean f109a;

    /* renamed from: b */
    public volatile boolean f110b;
    public volatile boolean back;
    public volatile boolean circle;
    public volatile boolean cross;
    public volatile boolean dpad_down;
    public volatile boolean dpad_left;
    public volatile boolean dpad_right;
    public volatile boolean dpad_up;
    public volatile boolean guide;

    /* renamed from: id */
    public volatile int f111id;
    public EvictingBlockingQueue<LedEffect> ledQueue;
    public volatile boolean left_bumper;
    public volatile boolean left_stick_button;
    public volatile float left_stick_x;
    public volatile float left_stick_y;
    public volatile float left_trigger;
    public long nextRumbleApproxFinishTime;
    public volatile boolean options;

    /* renamed from: ps */
    public volatile boolean f112ps;
    public volatile boolean right_bumper;
    public volatile boolean right_stick_button;
    public volatile float right_stick_x;
    public volatile float right_stick_y;
    public volatile float right_trigger;
    public EvictingBlockingQueue<RumbleEffect> rumbleQueue;
    public volatile boolean share;
    public volatile boolean square;
    public volatile boolean start;
    public volatile long timestamp;
    public volatile boolean touchpad;
    public volatile boolean touchpad_finger_1;
    public volatile float touchpad_finger_1_x;
    public volatile float touchpad_finger_1_y;
    public volatile boolean touchpad_finger_2;
    public volatile float touchpad_finger_2_x;
    public volatile float touchpad_finger_2_y;
    public volatile boolean triangle;
    public volatile Type type;
    protected volatile byte user;
    protected volatile byte userForEffects;

    /* renamed from: x */
    public volatile boolean f113x;

    /* renamed from: y */
    public volatile boolean f114y;

    public enum LegacyType {
        UNKNOWN,
        LOGITECH_F310,
        XBOX_360,
        SONY_PS4
    }

    public enum Type {
        UNKNOWN(LegacyType.UNKNOWN),
        LOGITECH_F310(LegacyType.LOGITECH_F310),
        XBOX_360(LegacyType.XBOX_360),
        SONY_PS4(LegacyType.SONY_PS4),
        SONY_PS4_SUPPORTED_BY_KERNEL(LegacyType.SONY_PS4);
        
        /* access modifiers changed from: private */
        public final LegacyType correspondingLegacyType;

        private Type(LegacyType legacyType) {
            this.correspondingLegacyType = legacyType;
        }
    }

    public GamepadUser getUser() {
        return GamepadUser.from(this.user);
    }

    public void setUser(GamepadUser gamepadUser) {
        this.user = gamepadUser.f280id;
    }

    public void setUserForEffects(byte b) {
        this.userForEffects = b;
    }

    public void setGamepadId(int i) {
        this.f111id = i;
    }

    public int getGamepadId() {
        return this.f111id;
    }

    public void setTimestamp(long j) {
        this.timestamp = j;
    }

    public void refreshTimestamp() {
        setTimestamp(SystemClock.uptimeMillis());
    }

    public Gamepad() {
        this.type = Type.UNKNOWN;
        this.left_stick_x = 0.0f;
        this.left_stick_y = 0.0f;
        this.right_stick_x = 0.0f;
        this.right_stick_y = 0.0f;
        this.dpad_up = false;
        this.dpad_down = false;
        this.dpad_left = false;
        this.dpad_right = false;
        this.f109a = false;
        this.f110b = false;
        this.f113x = false;
        this.f114y = false;
        this.guide = false;
        this.start = false;
        this.back = false;
        this.left_bumper = false;
        this.right_bumper = false;
        this.left_stick_button = false;
        this.right_stick_button = false;
        this.left_trigger = 0.0f;
        this.right_trigger = 0.0f;
        this.circle = false;
        this.cross = false;
        this.triangle = false;
        this.square = false;
        this.share = false;
        this.options = false;
        this.touchpad = false;
        this.f112ps = false;
        this.user = -1;
        this.userForEffects = -1;
        this.f111id = -1;
        this.timestamp = 0;
        this.ledQueue = new EvictingBlockingQueue<>(new ArrayBlockingQueue(1));
        this.rumbleQueue = new EvictingBlockingQueue<>(new ArrayBlockingQueue(1));
        this.nextRumbleApproxFinishTime = -1;
        this.type = type();
    }

    public void copy(Gamepad gamepad) throws RobotCoreException {
        fromByteArray(gamepad.toByteArray());
    }

    public void reset() {
        try {
            copy(new Gamepad());
        } catch (RobotCoreException unused) {
            RobotLog.m46e("Gamepad library in an invalid state");
            throw new IllegalStateException("Gamepad library in an invalid state");
        }
    }

    public RobocolParsable.MsgType getRobocolMsgType() {
        return RobocolParsable.MsgType.GAMEPAD;
    }

    public byte[] toByteArray() throws RobotCoreException {
        ByteBuffer writeBuffer = getWriteBuffer(60);
        try {
            writeBuffer.put(ROBOCOL_GAMEPAD_VERSION);
            writeBuffer.putInt(this.f111id);
            writeBuffer.putLong(this.timestamp).array();
            writeBuffer.putFloat(this.left_stick_x).array();
            writeBuffer.putFloat(this.left_stick_y).array();
            writeBuffer.putFloat(this.right_stick_x).array();
            writeBuffer.putFloat(this.right_stick_y).array();
            writeBuffer.putFloat(this.left_trigger).array();
            writeBuffer.putFloat(this.right_trigger).array();
            int i = 0;
            int i2 = ((((((((((((((((((((((((((((((((((this.touchpad_finger_1 ? 1 : 0) + 0) << 1) + (this.touchpad_finger_2 ? 1 : 0)) << 1) + (this.touchpad ? 1 : 0)) << 1) + (this.left_stick_button ? 1 : 0)) << 1) + (this.right_stick_button ? 1 : 0)) << 1) + (this.dpad_up ? 1 : 0)) << 1) + (this.dpad_down ? 1 : 0)) << 1) + (this.dpad_left ? 1 : 0)) << 1) + (this.dpad_right ? 1 : 0)) << 1) + (this.f109a ? 1 : 0)) << 1) + (this.f110b ? 1 : 0)) << 1) + (this.f113x ? 1 : 0)) << 1) + (this.f114y ? 1 : 0)) << 1) + (this.guide ? 1 : 0)) << 1) + (this.start ? 1 : 0)) << 1) + (this.back ? 1 : 0)) << 1) + (this.left_bumper ? 1 : 0)) << 1;
            if (this.right_bumper) {
                i = 1;
            }
            writeBuffer.putInt(i2 + i);
            writeBuffer.put(this.user);
            writeBuffer.put((byte) legacyType().ordinal());
            writeBuffer.put((byte) this.type.ordinal());
            writeBuffer.putFloat(this.touchpad_finger_1_x);
            writeBuffer.putFloat(this.touchpad_finger_1_y);
            writeBuffer.putFloat(this.touchpad_finger_2_x);
            writeBuffer.putFloat(this.touchpad_finger_2_y);
        } catch (BufferOverflowException e) {
            RobotLog.logStacktrace(e);
        }
        return writeBuffer.array();
    }

    public void fromByteArray(byte[] bArr) throws RobotCoreException {
        byte b;
        if (bArr.length >= 65) {
            ByteBuffer readBuffer = getReadBuffer(bArr);
            byte b2 = readBuffer.get();
            boolean z = true;
            if (b2 >= 1) {
                this.f111id = readBuffer.getInt();
                this.timestamp = readBuffer.getLong();
                this.left_stick_x = readBuffer.getFloat();
                this.left_stick_y = readBuffer.getFloat();
                this.right_stick_x = readBuffer.getFloat();
                this.right_stick_y = readBuffer.getFloat();
                this.left_trigger = readBuffer.getFloat();
                this.right_trigger = readBuffer.getFloat();
                int i = readBuffer.getInt();
                this.touchpad_finger_1 = (131072 & i) != 0;
                this.touchpad_finger_2 = (65536 & i) != 0;
                this.touchpad = (32768 & i) != 0;
                this.left_stick_button = (i & 16384) != 0;
                this.right_stick_button = (i & 8192) != 0;
                this.dpad_up = (i & 4096) != 0;
                this.dpad_down = (i & 2048) != 0;
                this.dpad_left = (i & 1024) != 0;
                this.dpad_right = (i & 512) != 0;
                this.f109a = (i & 256) != 0;
                this.f110b = (i & 128) != 0;
                this.f113x = (i & 64) != 0;
                this.f114y = (i & 32) != 0;
                this.guide = (i & 16) != 0;
                this.start = (i & 8) != 0;
                this.back = (i & 4) != 0;
                this.left_bumper = (i & 2) != 0;
                if ((i & 1) == 0) {
                    z = false;
                }
                this.right_bumper = z;
            }
            if (b2 >= 2) {
                this.user = readBuffer.get();
            }
            if (b2 >= 3) {
                this.type = Type.values()[readBuffer.get()];
            }
            if (b2 >= 4 && (b = readBuffer.get()) < Type.values().length) {
                this.type = Type.values()[b];
            }
            if (b2 >= 5) {
                this.touchpad_finger_1_x = readBuffer.getFloat();
                this.touchpad_finger_1_y = readBuffer.getFloat();
                this.touchpad_finger_2_x = readBuffer.getFloat();
                this.touchpad_finger_2_y = readBuffer.getFloat();
            }
            updateButtonAliases();
            return;
        }
        throw new RobotCoreException("Expected buffer of at least 65 bytes, received " + bArr.length);
    }

    public boolean atRest() {
        return this.left_stick_x == 0.0f && this.left_stick_y == 0.0f && this.right_stick_x == 0.0f && this.right_stick_y == 0.0f && this.left_trigger == 0.0f && this.right_trigger == 0.0f;
    }

    public Type type() {
        return this.type;
    }

    private LegacyType legacyType() {
        return this.type.correspondingLegacyType;
    }

    /* renamed from: com.qualcomm.robotcore.hardware.Gamepad$1 */
    static /* synthetic */ class C07221 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.qualcomm.robotcore.hardware.Gamepad$Type[] r0 = com.qualcomm.robotcore.hardware.Gamepad.Type.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type = r0
                com.qualcomm.robotcore.hardware.Gamepad$Type r1 = com.qualcomm.robotcore.hardware.Gamepad.Type.SONY_PS4     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.Gamepad$Type r1 = com.qualcomm.robotcore.hardware.Gamepad.Type.SONY_PS4_SUPPORTED_BY_KERNEL     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.hardware.Gamepad$Type r1 = com.qualcomm.robotcore.hardware.Gamepad.Type.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.robotcore.hardware.Gamepad$Type r1 = com.qualcomm.robotcore.hardware.Gamepad.Type.LOGITECH_F310     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.robotcore.hardware.Gamepad$Type r1 = com.qualcomm.robotcore.hardware.Gamepad.Type.XBOX_360     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.hardware.Gamepad.C07221.<clinit>():void");
        }
    }

    public String toString() {
        int i = C07221.$SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type[this.type.ordinal()];
        if (i == 1 || i == 2) {
            return ps4ToString();
        }
        return genericToString();
    }

    /* access modifiers changed from: protected */
    public String ps4ToString() {
        String str = new String();
        if (this.dpad_up) {
            str = str + "dpad_up ";
        }
        if (this.dpad_down) {
            str = str + "dpad_down ";
        }
        if (this.dpad_left) {
            str = str + "dpad_left ";
        }
        if (this.dpad_right) {
            str = str + "dpad_right ";
        }
        if (this.cross) {
            str = str + "cross ";
        }
        if (this.circle) {
            str = str + "circle ";
        }
        if (this.square) {
            str = str + "square ";
        }
        if (this.triangle) {
            str = str + "triangle ";
        }
        if (this.f112ps) {
            str = str + "ps ";
        }
        if (this.share) {
            str = str + "share ";
        }
        if (this.options) {
            str = str + "options ";
        }
        if (this.touchpad) {
            str = str + "touchpad ";
        }
        if (this.left_bumper) {
            str = str + "left_bumper ";
        }
        if (this.right_bumper) {
            str = str + "right_bumper ";
        }
        if (this.left_stick_button) {
            str = str + "left stick button ";
        }
        if (this.right_stick_button) {
            str = str + "right stick button ";
        }
        return String.format("ID: %2d user: %2d lx: % 1.2f ly: % 1.2f rx: % 1.2f ry: % 1.2f lt: %1.2f rt: %1.2f %s", new Object[]{Integer.valueOf(this.f111id), Byte.valueOf(this.user), Float.valueOf(this.left_stick_x), Float.valueOf(this.left_stick_y), Float.valueOf(this.right_stick_x), Float.valueOf(this.right_stick_y), Float.valueOf(this.left_trigger), Float.valueOf(this.right_trigger), str});
    }

    /* access modifiers changed from: protected */
    public String genericToString() {
        String str = new String();
        if (this.dpad_up) {
            str = str + "dpad_up ";
        }
        if (this.dpad_down) {
            str = str + "dpad_down ";
        }
        if (this.dpad_left) {
            str = str + "dpad_left ";
        }
        if (this.dpad_right) {
            str = str + "dpad_right ";
        }
        if (this.f109a) {
            str = str + "a ";
        }
        if (this.f110b) {
            str = str + "b ";
        }
        if (this.f113x) {
            str = str + "x ";
        }
        if (this.f114y) {
            str = str + "y ";
        }
        if (this.guide) {
            str = str + "guide ";
        }
        if (this.start) {
            str = str + "start ";
        }
        if (this.back) {
            str = str + "back ";
        }
        if (this.left_bumper) {
            str = str + "left_bumper ";
        }
        if (this.right_bumper) {
            str = str + "right_bumper ";
        }
        if (this.left_stick_button) {
            str = str + "left stick button ";
        }
        if (this.right_stick_button) {
            str = str + "right stick button ";
        }
        return String.format("ID: %2d user: %2d lx: % 1.2f ly: % 1.2f rx: % 1.2f ry: % 1.2f lt: %1.2f rt: %1.2f %s", new Object[]{Integer.valueOf(this.f111id), Byte.valueOf(this.user), Float.valueOf(this.left_stick_x), Float.valueOf(this.left_stick_y), Float.valueOf(this.right_stick_x), Float.valueOf(this.right_stick_y), Float.valueOf(this.left_trigger), Float.valueOf(this.right_trigger), str});
    }

    public static class LedEffect {
        public final boolean repeating;
        public final ArrayList<Step> steps;
        public int user;

        public static class Step {

            /* renamed from: b */
            public int f115b;
            public int duration;

            /* renamed from: g */
            public int f116g;

            /* renamed from: r */
            public int f117r;
        }

        /* synthetic */ LedEffect(ArrayList arrayList, boolean z, C07221 r3) {
            this(arrayList, z);
        }

        private LedEffect(ArrayList<Step> arrayList, boolean z) {
            this.steps = arrayList;
            this.repeating = z;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static LedEffect deserialize(String str) {
            return (LedEffect) SimpleGson.getInstance().fromJson(str, LedEffect.class);
        }

        public static class Builder {
            private boolean repeating;
            private ArrayList<Step> steps = new ArrayList<>();

            public Builder addStep(double d, double d2, double d3, int i) {
                return addStepInternal(d, d2, d3, Math.max(i, 0));
            }

            public Builder setRepeating(boolean z) {
                this.repeating = z;
                return this;
            }

            /* access modifiers changed from: private */
            public Builder addStepInternal(double d, double d2, double d3, int i) {
                double clip = Range.clip(d, (double) LynxServoController.apiPositionFirst, 1.0d);
                double clip2 = Range.clip(d2, (double) LynxServoController.apiPositionFirst, 1.0d);
                double clip3 = Range.clip(d3, (double) LynxServoController.apiPositionFirst, 1.0d);
                Step step = new Step();
                step.f117r = (int) Math.round(Range.scale(clip, LynxServoController.apiPositionFirst, 1.0d, LynxServoController.apiPositionFirst, 255.0d));
                step.f116g = (int) Math.round(Range.scale(clip2, LynxServoController.apiPositionFirst, 1.0d, LynxServoController.apiPositionFirst, 255.0d));
                step.f115b = (int) Math.round(Range.scale(clip3, LynxServoController.apiPositionFirst, 1.0d, LynxServoController.apiPositionFirst, 255.0d));
                step.duration = i;
                this.steps.add(step);
                return this;
            }

            public LedEffect build() {
                return new LedEffect(this.steps, this.repeating, (C07221) null);
            }
        }
    }

    public void setLedColor(double d, double d2, double d3, int i) {
        if (i != -1) {
            i = Math.max(0, i);
        }
        queueEffect(new LedEffect.Builder().addStepInternal(d, d2, d3, i).build());
    }

    public void runLedEffect(LedEffect ledEffect) {
        queueEffect(ledEffect);
    }

    private void queueEffect(LedEffect ledEffect) {
        LedEffect ledEffect2 = new LedEffect(ledEffect.steps, ledEffect.repeating, (C07221) null);
        ledEffect2.user = this.userForEffects;
        this.ledQueue.offer(ledEffect2);
    }

    public static class RumbleEffect {
        public final ArrayList<Step> steps;
        public int user;

        public static class Step {
            public int duration;
            public int large;
            public int small;
        }

        /* synthetic */ RumbleEffect(ArrayList arrayList, C07221 r2) {
            this(arrayList);
        }

        private RumbleEffect(ArrayList<Step> arrayList) {
            this.steps = arrayList;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static RumbleEffect deserialize(String str) {
            return (RumbleEffect) SimpleGson.getInstance().fromJson(str, RumbleEffect.class);
        }

        public static class Builder {
            private ArrayList<Step> steps = new ArrayList<>();

            public Builder addStep(double d, double d2, int i) {
                return addStepInternal(d, d2, Math.max(i, 0));
            }

            /* access modifiers changed from: private */
            public Builder addStepInternal(double d, double d2, int i) {
                double clip = Range.clip(d, (double) LynxServoController.apiPositionFirst, 1.0d);
                double clip2 = Range.clip(d2, (double) LynxServoController.apiPositionFirst, 1.0d);
                Step step = new Step();
                step.large = (int) Math.round(Range.scale(clip, LynxServoController.apiPositionFirst, 1.0d, LynxServoController.apiPositionFirst, 255.0d));
                step.small = (int) Math.round(Range.scale(clip2, LynxServoController.apiPositionFirst, 1.0d, LynxServoController.apiPositionFirst, 255.0d));
                step.duration = i;
                this.steps.add(step);
                return this;
            }

            public RumbleEffect build() {
                return new RumbleEffect(this.steps, (C07221) null);
            }
        }
    }

    public void runRumbleEffect(RumbleEffect rumbleEffect) {
        queueEffect(rumbleEffect);
    }

    public void rumble(int i) {
        if (i != -1) {
            i = Math.max(0, i);
        }
        queueEffect(new RumbleEffect.Builder().addStepInternal(1.0d, LynxServoController.apiPositionFirst, i).build());
    }

    public void rumble(double d, double d2, int i) {
        if (i != -1) {
            i = Math.max(0, i);
        }
        queueEffect(new RumbleEffect.Builder().addStepInternal(d, d2, i).build());
    }

    public void stopRumble() {
        rumble(LynxServoController.apiPositionFirst, LynxServoController.apiPositionFirst, -1);
    }

    public void rumbleBlips(int i) {
        RumbleEffect.Builder builder = new RumbleEffect.Builder();
        for (int i2 = 0; i2 < i; i2++) {
            builder.addStep(1.0d, LynxServoController.apiPositionFirst, SyncdDevice.msAbnormalReopenInterval).addStep(LynxServoController.apiPositionFirst, LynxServoController.apiPositionFirst, 100);
        }
        queueEffect(builder.build());
    }

    private void queueEffect(RumbleEffect rumbleEffect) {
        RumbleEffect rumbleEffect2 = new RumbleEffect(rumbleEffect.steps, (C07221) null);
        rumbleEffect2.user = this.userForEffects;
        this.rumbleQueue.offer(rumbleEffect2);
        this.nextRumbleApproxFinishTime = calcApproxRumbleFinishTime(rumbleEffect2);
    }

    public boolean isRumbling() {
        long j = this.nextRumbleApproxFinishTime;
        if (j == -1) {
            return false;
        }
        if (j == RUMBLE_FINISH_TIME_FLAG_INFINITE) {
            return true;
        }
        if (System.currentTimeMillis() < this.nextRumbleApproxFinishTime) {
            return true;
        }
        return false;
    }

    private long calcApproxRumbleFinishTime(RumbleEffect rumbleEffect) {
        if (rumbleEffect.steps.size() != 1 || rumbleEffect.steps.get(0).duration != -1) {
            long currentTimeMillis = System.currentTimeMillis();
            Iterator<RumbleEffect.Step> it = rumbleEffect.steps.iterator();
            while (it.hasNext()) {
                currentTimeMillis += (long) it.next().duration;
            }
            return currentTimeMillis + 95;
        } else if (rumbleEffect.steps.get(0).large == 0 && rumbleEffect.steps.get(0).small == 0) {
            return -1;
        } else {
            return RUMBLE_FINISH_TIME_FLAG_INFINITE;
        }
    }

    /* access modifiers changed from: protected */
    public void updateButtonAliases() {
        this.circle = this.f110b;
        this.cross = this.f109a;
        this.triangle = this.f114y;
        this.square = this.f113x;
        this.share = this.back;
        this.options = this.start;
        this.f112ps = this.guide;
    }
}
