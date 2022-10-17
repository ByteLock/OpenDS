package com.qualcomm.ftccommon;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.ArrayList;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;

public class CommandList extends RobotCoreCommandList {
    public static final String CMD_ACTIVATE_CONFIGURATION = "CMD_ACTIVATE_CONFIGURATION";
    public static final String CMD_DELETE_CONFIGURATION = "CMD_DELETE_CONFIGURATION";
    public static final String CMD_DISCOVER_LYNX_MODULES = "CMD_DISCOVER_LYNX_MODULES";
    public static final String CMD_DISCOVER_LYNX_MODULES_RESP = "CMD_DISCOVER_LYNX_MODULES_RESP";
    public static final String CMD_INIT_OP_MODE = "CMD_INIT_OP_MODE";
    public static final String CMD_LYNX_ADDRESS_CHANGE = "CMD_LYNX_ADDRESS_CHANGE";
    public static final String CMD_REQUEST_CONFIGURATIONS = "CMD_REQUEST_CONFIGURATIONS";
    public static final String CMD_REQUEST_CONFIGURATIONS_RESP = "CMD_REQUEST_CONFIGURATIONS_RESP";
    public static final String CMD_REQUEST_CONFIGURATION_TEMPLATES = "CMD_REQUEST_CONFIGURATION_TEMPLATES";
    public static final String CMD_REQUEST_CONFIGURATION_TEMPLATES_RESP = "CMD_REQUEST_CONFIGURATION_TEMPLATES_RESP";
    public static final String CMD_REQUEST_REMEMBERED_GROUPS = "CMD_REQUEST_REMEMBERED_GROUPS";
    public static final String CMD_REQUEST_REMEMBERED_GROUPS_RESP = "CMD_REQUEST_REMEMBERED_GROUPS_RESP";
    public static final String CMD_RESTART_ROBOT = "CMD_RESTART_ROBOT";
    public static final String CMD_RUN_OP_MODE = "CMD_RUN_OP_MODE";
    public static final String CMD_SAVE_CONFIGURATION = "CMD_SAVE_CONFIGURATION";
    public static final String CMD_SCAN = "CMD_SCAN";
    public static final String CMD_SCAN_RESP = "CMD_SCAN_RESP";
    public static final String CMD_SET_MATCH_NUMBER = "CMD_SET_MATCH_NUMBER";
    public static final String CMD_START_DS_PROGRAM_AND_MANAGE = "CMD_START_DS_PROGRAM_AND_MANAGE";
    public static final String CMD_START_DS_PROGRAM_AND_MANAGE_RESP = "CMD_START_DS_PROGRAM_AND_MANAGE_RESP";

    public static class CmdPlaySound {
        public static final String Command = "CMD_PLAY_SOUND";
        public final String hashString;
        public final int loopControl;
        public final long msPresentationTime;
        public final float rate;
        public final float volume;
        public final boolean waitForNonLoopingSoundsToFinish;

        public CmdPlaySound(long j, String str, SoundPlayer.PlaySoundParams playSoundParams) {
            this.msPresentationTime = j;
            this.hashString = str;
            this.waitForNonLoopingSoundsToFinish = playSoundParams.waitForNonLoopingSoundsToFinish;
            this.volume = playSoundParams.volume;
            this.loopControl = playSoundParams.loopControl;
            this.rate = playSoundParams.rate;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static CmdPlaySound deserialize(String str) {
            return (CmdPlaySound) SimpleGson.getInstance().fromJson(str, CmdPlaySound.class);
        }

        public SoundPlayer.PlaySoundParams getParams() {
            SoundPlayer.PlaySoundParams playSoundParams = new SoundPlayer.PlaySoundParams();
            playSoundParams.waitForNonLoopingSoundsToFinish = this.waitForNonLoopingSoundsToFinish;
            playSoundParams.volume = this.volume;
            playSoundParams.loopControl = this.loopControl;
            playSoundParams.rate = this.rate;
            return playSoundParams;
        }
    }

    public static class CmdRequestSound {
        public static final String Command = "CMD_REQUEST_SOUND";
        public final String hashString;
        public final int port;

        public CmdRequestSound(String str, int i) {
            this.hashString = str;
            this.port = i;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static CmdRequestSound deserialize(String str) {
            return (CmdRequestSound) SimpleGson.getInstance().fromJson(str, CmdRequestSound.class);
        }
    }

    public static class CmdStopPlayingSounds {
        public static final String Command = "CMD_STOP_PLAYING_SOUNDS";
        public final SoundPlayer.StopWhat stopWhat;

        public CmdStopPlayingSounds(SoundPlayer.StopWhat stopWhat2) {
            this.stopWhat = stopWhat2;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static CmdStopPlayingSounds deserialize(String str) {
            return (CmdStopPlayingSounds) SimpleGson.getInstance().fromJson(str, CmdStopPlayingSounds.class);
        }
    }

    public static class LynxAddressChangeRequest {
        ArrayList<AddressChange> modulesToChange;

        public static class AddressChange {
            int newAddress;
            int oldAddress;
            SerialNumber serialNumber;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static LynxAddressChangeRequest deserialize(String str) {
            return (LynxAddressChangeRequest) SimpleGson.getInstance().fromJson(str, LynxAddressChangeRequest.class);
        }
    }

    public static class CmdVisuallyIdentify {
        public static final String Command = "CMD_VISUALLY_IDENTIFY";
        public final SerialNumber serialNumber;
        public final boolean shouldIdentify;

        public CmdVisuallyIdentify(SerialNumber serialNumber2, boolean z) {
            this.serialNumber = serialNumber2;
            this.shouldIdentify = z;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static CmdVisuallyIdentify deserialize(String str) {
            return (CmdVisuallyIdentify) SimpleGson.getInstance().fromJson(str, CmdVisuallyIdentify.class);
        }
    }
}
