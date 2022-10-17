package com.qualcomm.robotcore.hardware;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.qualcomm.robotcore.util.SerialNumber;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;

public class LynxModuleMetaList implements Iterable<LynxModuleMeta> {
    public LynxModuleMeta[] modules;
    public SerialNumber serialNumber;

    public LynxModuleMetaList(SerialNumber serialNumber2) {
        this(serialNumber2, new LynxModuleMeta[0]);
    }

    public LynxModuleMetaList(SerialNumber serialNumber2, Collection<LynxModuleMeta> collection) {
        this(serialNumber2, metaFromModules(collection));
    }

    private static LynxModuleMeta[] metaFromModules(Collection<LynxModuleMeta> collection) {
        LynxModuleMeta[] lynxModuleMetaArr = new LynxModuleMeta[collection.size()];
        int i = 0;
        for (LynxModuleMeta lynxModuleMeta : collection) {
            lynxModuleMetaArr[i] = lynxModuleMeta;
            i++;
        }
        return lynxModuleMetaArr;
    }

    private LynxModuleMetaList(SerialNumber serialNumber2, LynxModuleMeta[] lynxModuleMetaArr) {
        this.serialNumber = serialNumber2;
        this.modules = lynxModuleMetaArr;
    }

    public Iterator<LynxModuleMeta> iterator() {
        return Arrays.asList(this.modules).iterator();
    }

    public LynxModuleMeta getParent() {
        int i = 0;
        while (true) {
            LynxModuleMeta[] lynxModuleMetaArr = this.modules;
            if (i >= lynxModuleMetaArr.length) {
                return null;
            }
            LynxModuleMeta lynxModuleMeta = lynxModuleMetaArr[i];
            if (lynxModuleMeta.isParent()) {
                return lynxModuleMeta;
            }
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public LynxModuleMetaList flatten() {
        LynxModuleMeta[] lynxModuleMetaArr = new LynxModuleMeta[this.modules.length];
        int i = 0;
        while (true) {
            LynxModuleMeta[] lynxModuleMetaArr2 = this.modules;
            if (i >= lynxModuleMetaArr2.length) {
                return new LynxModuleMetaList(this.serialNumber, lynxModuleMetaArr);
            }
            lynxModuleMetaArr[i] = new LynxModuleMeta(lynxModuleMetaArr2[i]);
            i++;
        }
    }

    public String toSerializationString() {
        return SimpleGson.getInstance().toJson((Object) flatten());
    }

    public static LynxModuleMetaList fromSerializationString(String str) {
        return (LynxModuleMetaList) new GsonBuilder().registerTypeAdapter(RobotCoreLynxModule.class, new JsonDeserializer() {
            public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                return jsonDeserializationContext.deserialize(jsonElement, LynxModuleMeta.class);
            }
        }).create().fromJson(str, LynxModuleMetaList.class);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        LynxModuleMeta[] lynxModuleMetaArr = this.modules;
        int length = lynxModuleMetaArr.length;
        int i = 0;
        boolean z = true;
        while (i < length) {
            LynxModuleMeta lynxModuleMeta = lynxModuleMetaArr[i];
            if (!z) {
                sb.append(" ");
            }
            sb.append(String.format(Locale.getDefault(), "%d(%s, ImuType.%s)", new Object[]{Integer.valueOf(lynxModuleMeta.getModuleAddress()), Boolean.valueOf(lynxModuleMeta.isParent()), lynxModuleMeta.imuType()}));
            i++;
            z = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
