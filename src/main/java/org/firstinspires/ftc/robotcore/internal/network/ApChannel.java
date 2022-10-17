package org.firstinspires.ftc.robotcore.internal.network;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.qualcomm.robotcore.wifi.NetworkType;
import java.io.IOException;
import java.util.EnumSet;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public enum ApChannel {
    UNKNOWN(-1, Band.BAND_2_4_GHZ, false),
    AUTO_2_4_GHZ(0, Band.BAND_2_4_GHZ, false),
    AUTO_5_GHZ(0, Band.BAND_5_GHZ, false),
    CHAN_1(1),
    CHAN_2(2, true),
    CHAN_3(3, true),
    CHAN_4(4, true),
    CHAN_5(5, true),
    CHAN_6(6),
    CHAN_7(7, true),
    CHAN_8(8, true),
    CHAN_9(9, true),
    CHAN_10(10, true),
    CHAN_11(11),
    CHAN_36(36),
    CHAN_40(40),
    CHAN_44(44),
    CHAN_48(48),
    CHAN_149(149),
    CHAN_153(153),
    CHAN_157(157),
    CHAN_161(161),
    CHAN_165(165),
    CHAN_52(52),
    CHAN_56(56),
    CHAN_60(60),
    CHAN_64(64),
    CHAN_100(100),
    CHAN_104(104),
    CHAN_108(108),
    CHAN_112(112),
    CHAN_116(116),
    CHAN_120(120),
    CHAN_124(124),
    CHAN_128(128),
    CHAN_132(132),
    CHAN_136(136),
    CHAN_140(140);
    
    public static final EnumSet<ApChannel> ALL_2_4_GHZ_CHANNELS = null;
    public static final int AP_BAND_2GHZ = 0;
    public static final int AP_BAND_5GHZ = 1;
    private static final int CHANNEL_AUTO_SELECT = 0;
    private static final int LOWEST_5GHZ_CHANNEL = 36;
    public static final EnumSet<ApChannel> NON_DFS_5_GHZ_CHANNELS = null;
    private static final String UNKNOWN_DISPLAY_NAME = "unknown";
    public final Band band;
    public final int channelNum;
    public final boolean overlapsWithOtherChannels;

    static {
        ApChannel apChannel;
        ApChannel apChannel2;
        ApChannel apChannel3;
        ApChannel apChannel4;
        ALL_2_4_GHZ_CHANNELS = EnumSet.range(apChannel, apChannel2);
        NON_DFS_5_GHZ_CHANNELS = EnumSet.range(apChannel3, apChannel4);
    }

    private ApChannel(int i) {
        this(r2, r3, i, false);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    private ApChannel(int i, boolean z) {
        this(r8, r9, i, i < 36 ? Band.BAND_2_4_GHZ : Band.BAND_5_GHZ, z);
    }

    private ApChannel(int i, Band band2, boolean z) {
        this.channelNum = i;
        this.band = band2;
        this.overlapsWithOtherChannels = z;
    }

    public String getDisplayName() {
        if (this == UNKNOWN) {
            return "unknown";
        }
        int i = this.channelNum;
        if (i != 0) {
            return String.valueOf(i);
        }
        if (NetworkConnectionHandler.getNetworkType(AppUtil.getDefContext()) != NetworkType.WIFIDIRECT || !WifiUtil.is5GHzAvailable()) {
            return this.band == Band.BAND_2_4_GHZ ? "auto (2.4 GHz)" : "auto (5 GHz)";
        }
        return "auto (either band)";
    }

    public static ApChannel fromName(String str) {
        ApChannel apChannel = (ApChannel) Enum.valueOf(ApChannel.class, str);
        return apChannel == null ? UNKNOWN : apChannel;
    }

    public static ApChannel fromBandAndChannel(int i, int i2) {
        for (ApChannel apChannel : values()) {
            if (apChannel.band.androidInternalValue == i && apChannel.channelNum == i2) {
                return apChannel;
            }
        }
        return UNKNOWN;
    }

    public enum Band {
        BAND_2_4_GHZ(0),
        BAND_5_GHZ(1);
        
        public final int androidInternalValue;

        private Band(int i) {
            this.androidInternalValue = i;
        }
    }

    public static class GsonTypeAdapter extends TypeAdapter<ApChannel> {
        public void write(JsonWriter jsonWriter, ApChannel apChannel) throws IOException {
            if (apChannel == null) {
                jsonWriter.nullValue();
                return;
            }
            jsonWriter.beginObject();
            jsonWriter.name("name");
            jsonWriter.value(apChannel.name());
            jsonWriter.name("displayName");
            jsonWriter.value(apChannel.getDisplayName());
            jsonWriter.name("band");
            jsonWriter.value(apChannel.band.name());
            jsonWriter.name("overlapsWithOtherChannels");
            jsonWriter.value(apChannel.overlapsWithOtherChannels);
            jsonWriter.endObject();
        }

        public ApChannel read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            ApChannel apChannel = null;
            while (jsonReader.hasNext()) {
                if ("name".equals(jsonReader.nextName())) {
                    apChannel = ApChannel.fromName(jsonReader.nextString());
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return apChannel == null ? ApChannel.UNKNOWN : apChannel;
        }
    }
}
