package com.qualcomm.robotcore.util;

import android.text.TextUtils;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.qualcomm.robotcore.C0705R;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import org.firstinspires.ftc.robotcore.internal.system.Misc;
import org.firstinspires.ftc.robotcore.internal.usb.EmbeddedSerialNumber;
import org.firstinspires.ftc.robotcore.internal.usb.FakeSerialNumber;
import org.firstinspires.ftc.robotcore.internal.usb.LynxModuleSerialNumber;
import org.firstinspires.ftc.robotcore.internal.usb.UsbSerialNumber;
import org.firstinspires.ftc.robotcore.internal.usb.VendorProductSerialNumber;

@JsonAdapter(GsonTypeAdapter.class)
public abstract class SerialNumber implements Serializable {
    protected static final HashMap<String, String> deviceDisplayNames = new HashMap<>();
    protected static final String embedded = "(embedded)";
    protected static final String fakePrefix = "FakeUSB:";
    protected static final String lynxModulePrefix = "ExpHub:";
    protected static final String vendorProductPrefix = "VendorProduct:";
    protected final String serialNumberString;

    public SerialNumber getScannableDeviceSerialNumber() {
        return this;
    }

    public boolean isEmbedded() {
        return false;
    }

    public boolean isFake() {
        return false;
    }

    public boolean isUsb() {
        return false;
    }

    public boolean isVendorProduct() {
        return false;
    }

    protected SerialNumber(String str) {
        this.serialNumberString = str;
    }

    public static SerialNumber createFake() {
        return new FakeSerialNumber();
    }

    public static SerialNumber createEmbedded() {
        return new EmbeddedSerialNumber();
    }

    public static SerialNumber fromString(String str) {
        if (FakeSerialNumber.isLegacyFake(str)) {
            return createFake();
        }
        if (str.startsWith(fakePrefix)) {
            return new FakeSerialNumber(str);
        }
        if (str.startsWith(vendorProductPrefix)) {
            return new VendorProductSerialNumber(str);
        }
        if (str.startsWith(lynxModulePrefix)) {
            return new LynxModuleSerialNumber(str);
        }
        if (str.equals(embedded)) {
            return createEmbedded();
        }
        return new UsbSerialNumber(str);
    }

    public static SerialNumber fromStringOrNull(String str) {
        if (!TextUtils.isEmpty(str)) {
            return fromString(str);
        }
        return null;
    }

    public static SerialNumber fromUsbOrNull(String str) {
        if (UsbSerialNumber.isValidUsbSerialNumber(str)) {
            return fromString(str);
        }
        return null;
    }

    public static SerialNumber fromVidPid(int i, int i2, String str) {
        return new VendorProductSerialNumber(i, i2, str);
    }

    static class GsonTypeAdapter extends TypeAdapter<SerialNumber> {
        GsonTypeAdapter() {
        }

        public void write(JsonWriter jsonWriter, SerialNumber serialNumber) throws IOException {
            if (serialNumber == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(serialNumber.getString());
            }
        }

        public SerialNumber read(JsonReader jsonReader) throws IOException {
            return SerialNumber.fromStringOrNull(jsonReader.nextString());
        }
    }

    public String getString() {
        return this.serialNumberString;
    }

    public boolean matches(Object obj) {
        return equals(obj);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof SerialNumber) {
            return this.serialNumberString.equals(((SerialNumber) obj).serialNumberString);
        }
        if (obj instanceof String) {
            return equals((String) obj);
        }
        return false;
    }

    public boolean equals(String str) {
        return this.serialNumberString.equals(str);
    }

    public int hashCode() {
        return this.serialNumberString.hashCode() ^ -1412589453;
    }

    public static void noteSerialNumberType(SerialNumber serialNumber, String str) {
        HashMap<String, String> hashMap = deviceDisplayNames;
        synchronized (hashMap) {
            hashMap.put(serialNumber.getString(), Misc.formatForUser("%s [%s]", str, serialNumber));
        }
    }

    public static String getDeviceDisplayName(SerialNumber serialNumber) {
        String str;
        HashMap<String, String> hashMap = deviceDisplayNames;
        synchronized (hashMap) {
            str = hashMap.get(serialNumber.getString());
            if (str == null) {
                str = Misc.formatForUser(C0705R.string.deviceDisplayNameUnknownUSBDevice, serialNumber);
            }
        }
        return str;
    }
}
