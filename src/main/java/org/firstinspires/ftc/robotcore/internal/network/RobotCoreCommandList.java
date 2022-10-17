package org.firstinspires.ftc.robotcore.internal.network;

import android.util.Base64;
import com.qualcomm.robotcore.hardware.USBAccessibleLynxModule;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.File;
import java.util.ArrayList;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.internal.p013ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.inspection.InspectionState;

public class RobotCoreCommandList {
    public static final String CMD_CLEAR_REMEMBERED_GROUPS = "CMD_CLEAR_REMEMBERED_GROUPS";
    public static final String CMD_DISABLE_BLUETOOTH = "CMD_DISABLE_BLUETOOTH";
    public static final String CMD_DISCONNECT_FROM_WIFI_DIRECT = "CMD_DISCONNECT_FROM_WIFI_DIRECT";
    public static final String CMD_DISMISS_ALL_DIALOGS = "CMD_DISMISS_ALL_DIALOGS";
    public static final String CMD_DISMISS_DIALOG = "CMD_DISMISS_DIALOG";
    public static final String CMD_DISMISS_PROGRESS = "CMD_DISMISS_PROGRESS";
    public static final String CMD_GAMEPAD_LED_EFFECT = "CMD_GAMEPAD_LED_EFFECT";
    public static final String CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES = "CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES";
    public static final String CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES_RESP = "CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES_RESP";
    public static final String CMD_GET_USB_ACCESSIBLE_LYNX_MODULES = "CMD_GET_USB_ACCESSIBLE_LYNX_MODULES";
    public static final String CMD_GET_USB_ACCESSIBLE_LYNX_MODULES_RESP = "CMD_GET_USB_ACCESSIBLE_LYNX_MODULES_RESP";
    public static final String CMD_LYNX_FIRMWARE_UPDATE = "CMD_LYNX_FIRMWARE_UPDATE";
    public static final String CMD_LYNX_FIRMWARE_UPDATE_RESP = "CMD_LYNX_FIRMWARE_UPDATE_RESP";
    public static final String CMD_NOTIFY_ACTIVE_CONFIGURATION = "CMD_NOTIFY_ACTIVE_CONFIGURATION";
    public static final String CMD_NOTIFY_INIT_OP_MODE = "CMD_NOTIFY_INIT_OP_MODE";
    public static final String CMD_NOTIFY_OP_MODE_LIST = "CMD_NOTIFY_OP_MODE_LIST";
    public static final String CMD_NOTIFY_ROBOT_STATE = "CMD_NOTIFY_ROBOT_STATE";
    public static final String CMD_NOTIFY_RUN_OP_MODE = "CMD_NOTIFY_RUN_OP_MODE";
    public static final String CMD_NOTIFY_USER_DEVICE_LIST = "CMD_NOTIFY_USER_DEVICE_LIST";
    public static final String CMD_NOTIFY_WIFI_DIRECT_REMEMBERED_GROUPS_CHANGED = "CMD_NOTIFY_WIFI_DIRECT_REMEMBERED_GROUPS_CHANGED";
    public static final String CMD_RECEIVE_FRAME_BEGIN = "CMD_RECEIVE_FRAME_BEGIN";
    public static final String CMD_RECEIVE_FRAME_CHUNK = "CMD_RECEIVE_FRAME_CHUNK";
    public static final String CMD_REQUEST_ABOUT_INFO = "CMD_REQUEST_ABOUT_INFO";
    public static final String CMD_REQUEST_ABOUT_INFO_RESP = "CMD_REQUEST_ABOUT_INFO_RESP";
    public static final String CMD_REQUEST_ACTIVE_CONFIG = "CMD_REQUEST_ACTIVE_CONFIG";
    public static final String CMD_REQUEST_FRAME = "CMD_REQUEST_FRAME";
    public static final String CMD_REQUEST_INSPECTION_REPORT = "CMD_REQUEST_INSPECTION_REPORT";
    public static final String CMD_REQUEST_INSPECTION_REPORT_RESP = "CMD_REQUEST_INSPECTION_REPORT_RESP";
    public static final String CMD_REQUEST_OP_MODE_LIST = "CMD_REQUEST_OP_MODE_LIST";
    public static final String CMD_REQUEST_PARTICULAR_CONFIGURATION = "CMD_REQUEST_PARTICULAR_CONFIGURATION";
    public static final String CMD_REQUEST_PARTICULAR_CONFIGURATION_RESP = "CMD_REQUEST_PARTICULAR_CONFIGURATION_RESP";
    public static final String CMD_REQUEST_USER_DEVICE_TYPES = "CMD_REQUEST_USER_DEVICE_TYPES";
    public static final String CMD_ROBOT_CONTROLLER_PREFERENCE = "CMD_ROBOT_CONTROLLER_PREFERENCE";
    public static final String CMD_RUMBLE_GAMEPAD = "CMD_RUMBLE_EFFECT";
    public static final String CMD_SET_TELEMETRY_DISPLAY_FORMAT = "CMD_SET_TELEM_DISPL_FORMAT";
    public static final String CMD_SHOW_DIALOG = "CMD_SHOW_DIALOG";
    public static final String CMD_SHOW_PROGRESS = "CMD_SHOW_PROGRESS";
    public static final String CMD_SHOW_STACKTRACE = "CMD_SHOW_STACKTRACE";
    public static final String CMD_SHOW_TOAST = "CMD_SHOW_TOAST";
    public static final String CMD_STREAM_CHANGE = "CMD_STREAM_CHANGE";
    public static final String CMD_TEXT_TO_SPEECH = "CMD_TEXT_TO_SPEECH";
    public static final String CMD_VISUALLY_CONFIRM_WIFI_BAND_SWITCH = "CMD_VISUALLY_CONFIRM_WIFI_BAND_SWITCH";
    public static final String CMD_VISUALLY_CONFIRM_WIFI_RESET = "CMD_VISUALLY_CONFIRM_WIFI_RESET";

    public static class ShowToast {
        public int duration;
        public String message;

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static ShowToast deserialize(String str) {
            return (ShowToast) SimpleGson.getInstance().fromJson(str, ShowToast.class);
        }
    }

    public static class ShowProgress extends ProgressParameters {
        public String message;

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static ShowProgress deserialize(String str) {
            return (ShowProgress) SimpleGson.getInstance().fromJson(str, ShowProgress.class);
        }
    }

    public static class ShowDialog {
        public String message;
        public String title;
        public String uuidString;

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static ShowDialog deserialize(String str) {
            return (ShowDialog) SimpleGson.getInstance().fromJson(str, ShowDialog.class);
        }
    }

    public static class DismissDialog {
        public String uuidString;

        public DismissDialog(String str) {
            this.uuidString = str;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static DismissDialog deserialize(String str) {
            return (DismissDialog) SimpleGson.getInstance().fromJson(str, DismissDialog.class);
        }
    }

    public static class AboutInfo {
        public String appVersion;
        public String buildTime;
        public String libVersion;
        public String networkConnectionInfo;
        public String networkProtocolVersion;
        public String osVersion;

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static AboutInfo deserialize(String str) {
            return (AboutInfo) SimpleGson.getInstance().fromJson(str, AboutInfo.class);
        }
    }

    public static class FWImage {
        public File file;
        public boolean isAsset;

        public FWImage(File file2, boolean z) {
            this.file = file2;
            this.isAsset = z;
        }

        public String getName() {
            return this.file.getName();
        }
    }

    public static class LynxFirmwareImagesResp {
        public ArrayList<FWImage> firmwareImages = new ArrayList<>();
        public File firstFolder = AppUtil.FIRST_FOLDER;

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static LynxFirmwareImagesResp deserialize(String str) {
            return (LynxFirmwareImagesResp) SimpleGson.getInstance().fromJson(str, LynxFirmwareImagesResp.class);
        }
    }

    public static class USBAccessibleLynxModulesRequest {
        public boolean forFirmwareUpdate = false;

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static USBAccessibleLynxModulesRequest deserialize(String str) {
            return (USBAccessibleLynxModulesRequest) SimpleGson.getInstance().fromJson(str, USBAccessibleLynxModulesRequest.class);
        }
    }

    public static class USBAccessibleLynxModulesResp {
        public ArrayList<USBAccessibleLynxModule> modules = new ArrayList<>();

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static USBAccessibleLynxModulesResp deserialize(String str) {
            return (USBAccessibleLynxModulesResp) SimpleGson.getInstance().fromJson(str, USBAccessibleLynxModulesResp.class);
        }
    }

    public static class LynxFirmwareUpdate {
        public FWImage firmwareImageFile;
        public String originatorId;
        public SerialNumber serialNumber;

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static LynxFirmwareUpdate deserialize(String str) {
            return (LynxFirmwareUpdate) SimpleGson.getInstance().fromJson(str, LynxFirmwareUpdate.class);
        }
    }

    public static class LynxFirmwareUpdateResp {
        public String errorMessage;
        public String originatorId;
        public boolean success;

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static LynxFirmwareUpdateResp deserialize(String str) {
            return (LynxFirmwareUpdateResp) SimpleGson.getInstance().fromJson(str, LynxFirmwareUpdateResp.class);
        }
    }

    public static class CmdStreamChange {
        public boolean available;

        public String serialize() {
            return String.valueOf(this.available);
        }

        public static CmdStreamChange deserialize(String str) {
            CmdStreamChange cmdStreamChange = new CmdStreamChange();
            cmdStreamChange.available = Boolean.parseBoolean(str);
            return cmdStreamChange;
        }
    }

    public static class CmdReceiveFrameBegin {
        private int frameNum;
        private int length;

        public CmdReceiveFrameBegin(int i, int i2) {
            this.frameNum = i;
            this.length = i2;
        }

        public int getFrameNum() {
            return this.frameNum;
        }

        public int getLength() {
            return this.length;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static CmdReceiveFrameBegin deserialize(String str) {
            return (CmdReceiveFrameBegin) SimpleGson.getInstance().fromJson(str, CmdReceiveFrameBegin.class);
        }
    }

    public static class CmdReceiveFrameChunk {
        private int chunkNum;
        private transient byte[] data;
        private String encodedData;
        private int frameNum;

        public CmdReceiveFrameChunk(int i, int i2, byte[] bArr, int i3, int i4) {
            this.frameNum = i;
            this.chunkNum = i2;
            this.data = bArr;
            this.encodedData = Base64.encodeToString(bArr, i3, i4, 0);
        }

        public int getFrameNum() {
            return this.frameNum;
        }

        public int getChunkNum() {
            return this.chunkNum;
        }

        public byte[] getData() {
            return this.data;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static CmdReceiveFrameChunk deserialize(String str) {
            CmdReceiveFrameChunk cmdReceiveFrameChunk = (CmdReceiveFrameChunk) SimpleGson.getInstance().fromJson(str, CmdReceiveFrameChunk.class);
            cmdReceiveFrameChunk.data = Base64.decode(cmdReceiveFrameChunk.encodedData, 0);
            return cmdReceiveFrameChunk;
        }
    }

    public static class TextToSpeech {
        private String countryCode;
        private String languageCode;
        private String text;

        public TextToSpeech(String str, String str2, String str3) {
            this.text = str;
            this.languageCode = str2 == null ? InspectionState.NO_VERSION : str2;
            this.countryCode = str3 == null ? InspectionState.NO_VERSION : str3;
        }

        public String getText() {
            return this.text;
        }

        public String getLanguageCode() {
            return this.languageCode;
        }

        public String getCountryCode() {
            return this.countryCode;
        }

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static TextToSpeech deserialize(String str) {
            return (TextToSpeech) SimpleGson.getInstance().fromJson(str, TextToSpeech.class);
        }
    }
}
