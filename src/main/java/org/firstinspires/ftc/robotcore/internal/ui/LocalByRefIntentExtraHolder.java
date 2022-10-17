package org.firstinspires.ftc.robotcore.internal.p013ui;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.LocalByRefIntentExtraHolder */
public class LocalByRefIntentExtraHolder implements Parcelable {
    public static final Parcelable.Creator<LocalByRefIntentExtraHolder> CREATOR = new Parcelable.Creator<LocalByRefIntentExtraHolder>() {
        public LocalByRefIntentExtraHolder createFromParcel(Parcel parcel) {
            return new LocalByRefIntentExtraHolder(parcel);
        }

        public LocalByRefIntentExtraHolder[] newArray(int i) {
            return new LocalByRefIntentExtraHolder[i];
        }
    };
    private static final Map<UUID, Object> map = new ConcurrentHashMap();
    protected UUID uuid;

    public int describeContents() {
        return 0;
    }

    public LocalByRefIntentExtraHolder(Object obj) {
        UUID randomUUID = UUID.randomUUID();
        this.uuid = randomUUID;
        map.put(randomUUID, obj);
    }

    private LocalByRefIntentExtraHolder(Parcel parcel) {
        this.uuid = UUID.fromString(parcel.readString());
    }

    public Object getTargetAndForget() {
        Map<UUID, Object> map2 = map;
        Object obj = map2.get(this.uuid);
        map2.remove(this.uuid);
        return obj;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.uuid.toString());
    }
}
