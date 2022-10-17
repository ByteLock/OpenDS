package com.qualcomm.robotcore.hardware;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScannedDevices {
    public static final String TAG = "ScannedDevices";
    @Expose
    protected String errorMessage;
    protected final Object lock;
    @JsonAdapter(MapAdapter.class)
    @Expose
    protected Map<SerialNumber, DeviceManager.UsbDeviceType> map;

    protected static class MapAdapter extends TypeAdapter<Map<SerialNumber, DeviceManager.UsbDeviceType>> {
        protected MapAdapter() {
        }

        public void write(JsonWriter jsonWriter, Map<SerialNumber, DeviceManager.UsbDeviceType> map) throws IOException {
            jsonWriter.beginArray();
            for (Map.Entry next : map.entrySet()) {
                jsonWriter.beginObject();
                jsonWriter.name("key").value(((SerialNumber) next.getKey()).getString());
                jsonWriter.name("value").value(((DeviceManager.UsbDeviceType) next.getValue()).toString());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
        }

        public Map<SerialNumber, DeviceManager.UsbDeviceType> read(JsonReader jsonReader) throws IOException {
            HashMap hashMap = new HashMap();
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                jsonReader.beginObject();
                SerialNumber serialNumber = null;
                DeviceManager.UsbDeviceType usbDeviceType = null;
                while (jsonReader.hasNext()) {
                    String nextName = jsonReader.nextName();
                    nextName.hashCode();
                    if (nextName.equals("key")) {
                        serialNumber = SerialNumber.fromString(jsonReader.nextString());
                    } else if (!nextName.equals("value")) {
                        jsonReader.skipValue();
                    } else {
                        usbDeviceType = DeviceManager.UsbDeviceType.from(jsonReader.nextString());
                    }
                }
                jsonReader.endObject();
                if (!(serialNumber == null || usbDeviceType == null)) {
                    hashMap.put(serialNumber, usbDeviceType);
                }
            }
            jsonReader.endArray();
            return hashMap;
        }
    }

    public ScannedDevices(ScannedDevices scannedDevices) {
        this.lock = new Object();
        this.errorMessage = null;
        HashMap hashMap = new HashMap();
        this.map = hashMap;
        hashMap.clear();
        for (Map.Entry next : scannedDevices.entrySet()) {
            this.map.put((SerialNumber) next.getKey(), (DeviceManager.UsbDeviceType) next.getValue());
        }
        this.errorMessage = scannedDevices.errorMessage;
    }

    public ScannedDevices() {
        this.lock = new Object();
        this.errorMessage = null;
        this.map = new HashMap();
    }

    public void setErrorMessage(String str) {
        synchronized (this.lock) {
            if (TextUtils.isEmpty(this.errorMessage)) {
                this.errorMessage = str;
            }
        }
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int size() {
        int size;
        synchronized (this.lock) {
            size = this.map.size();
        }
        return size;
    }

    public DeviceManager.UsbDeviceType put(SerialNumber serialNumber, DeviceManager.UsbDeviceType usbDeviceType) {
        DeviceManager.UsbDeviceType put;
        synchronized (this.lock) {
            put = this.map.put(serialNumber, usbDeviceType);
        }
        return put;
    }

    public DeviceManager.UsbDeviceType get(SerialNumber serialNumber) {
        DeviceManager.UsbDeviceType usbDeviceType;
        synchronized (this.lock) {
            usbDeviceType = this.map.get(serialNumber);
        }
        return usbDeviceType;
    }

    public boolean containsKey(SerialNumber serialNumber) {
        boolean containsKey;
        synchronized (this.lock) {
            containsKey = this.map.containsKey(serialNumber);
        }
        return containsKey;
    }

    public Set<SerialNumber> keySet() {
        Set<SerialNumber> keySet;
        synchronized (this.lock) {
            keySet = this.map.keySet();
        }
        return keySet;
    }

    public Set<Map.Entry<SerialNumber, DeviceManager.UsbDeviceType>> entrySet() {
        Set<Map.Entry<SerialNumber, DeviceManager.UsbDeviceType>> entrySet;
        synchronized (this.lock) {
            entrySet = this.map.entrySet();
        }
        return entrySet;
    }

    public DeviceManager.UsbDeviceType remove(SerialNumber serialNumber) {
        DeviceManager.UsbDeviceType remove;
        synchronized (this.lock) {
            remove = this.map.remove(serialNumber);
        }
        return remove;
    }

    protected static Gson newGson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public String toSerializationString() {
        return newGson().toJson((Object) this, (Type) ScannedDevices.class);
    }

    public static ScannedDevices fromSerializationString(String str) {
        ScannedDevices scannedDevices = new ScannedDevices();
        String trim = str.trim();
        return trim.length() > 0 ? (ScannedDevices) newGson().fromJson(trim, ScannedDevices.class) : scannedDevices;
    }
}
