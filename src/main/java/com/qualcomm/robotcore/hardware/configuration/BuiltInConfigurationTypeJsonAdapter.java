package com.qualcomm.robotcore.hardware.configuration;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import java.io.IOException;

public class BuiltInConfigurationTypeJsonAdapter extends TypeAdapter<BuiltInConfigurationType> {
    public BuiltInConfigurationType read(JsonReader jsonReader) throws IOException {
        String str = null;
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            if (jsonReader.nextName().equals("xmlTag")) {
                str = jsonReader.nextString();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return BuiltInConfigurationType.fromXmlTag(str);
    }

    public void write(JsonWriter jsonWriter, BuiltInConfigurationType builtInConfigurationType) throws IOException {
        if (builtInConfigurationType == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.beginObject();
        jsonWriter.name("xmlTag").value(builtInConfigurationType.getXmlTag());
        jsonWriter.name("name").value(builtInConfigurationType.getDisplayName(ConfigurationType.DisplayNameFlavor.Normal));
        jsonWriter.name("flavor").value(ConfigurationType.DeviceFlavor.BUILT_IN.toString());
        jsonWriter.endObject();
    }
}
