package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.ArraySet;
import com.qualcomm.robotcore.hardware.Gamepad;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import org.firstinspires.directgamepadaccess.android.AndroidGamepad;

public class GamepadTypeOverrideMapper {
    static final String KEY_GAMEPAD_MAPPING = "GAMEPAD_MAPPING";
    Context context;
    Set<String> serializedEntries;
    SharedPreferences sharedPreferences;

    static class GamepadTypeOverrideEntry {
        Gamepad.Type mappedType;
        int pid;
        int vid;

        GamepadTypeOverrideEntry(int i, int i2, Gamepad.Type type) {
            this.vid = i;
            this.pid = i2;
            this.mappedType = type;
        }

        public boolean equals(GamepadTypeOverrideEntry gamepadTypeOverrideEntry) {
            boolean z = false;
            boolean z2 = (this.vid == gamepadTypeOverrideEntry.vid) & true;
            if (this.pid == gamepadTypeOverrideEntry.pid) {
                z = true;
            }
            return this.mappedType.equals(gamepadTypeOverrideEntry.mappedType) & z2 & z;
        }

        public boolean usbIdsMatch(GamepadTypeOverrideEntry gamepadTypeOverrideEntry) {
            boolean z = false;
            boolean z2 = (this.vid == gamepadTypeOverrideEntry.vid) & true;
            if (this.pid == gamepadTypeOverrideEntry.pid) {
                z = true;
            }
            return z2 & z;
        }

        public boolean usbIdsMatch(int i, int i2) {
            boolean z = false;
            boolean z2 = (this.vid == i) & true;
            if (this.pid == i2) {
                z = true;
            }
            return z2 & z;
        }

        public AndroidGamepad.Type getAndroidGamepadType() {
            int i = C06301.$SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type[this.mappedType.ordinal()];
            if (i == 1) {
                return AndroidGamepad.Type.GAMEPAD_SUPPORTED_BY_KERNEL;
            }
            if (i == 2) {
                return AndroidGamepad.Type.SONY_PS4_WITHOUT_KERNEL_SUPPORT;
            }
            if (i != 3) {
                return null;
            }
            return AndroidGamepad.Type.GAMEPAD_SUPPORTED_BY_KERNEL;
        }

        public String toString() {
            return String.format(Locale.ENGLISH, "%d:%d:%s", new Object[]{Integer.valueOf(this.vid), Integer.valueOf(this.pid), this.mappedType.toString()});
        }

        static GamepadTypeOverrideEntry fromString(String str) {
            String[] split = str.split(":");
            return new GamepadTypeOverrideEntry(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Gamepad.Type.valueOf(split[2]));
        }
    }

    /* renamed from: com.qualcomm.ftcdriverstation.GamepadTypeOverrideMapper$1 */
    static /* synthetic */ class C06301 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.qualcomm.robotcore.hardware.Gamepad$Type[] r0 = com.qualcomm.robotcore.hardware.Gamepad.Type.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type = r0
                com.qualcomm.robotcore.hardware.Gamepad$Type r1 = com.qualcomm.robotcore.hardware.Gamepad.Type.XBOX_360     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.Gamepad$Type r1 = com.qualcomm.robotcore.hardware.Gamepad.Type.SONY_PS4     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.hardware.Gamepad$Type r1 = com.qualcomm.robotcore.hardware.Gamepad.Type.LOGITECH_F310     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftcdriverstation.GamepadTypeOverrideMapper.C06301.<clinit>():void");
        }
    }

    GamepadTypeOverrideMapper(Context context2) {
        this.context = context2;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context2);
        this.sharedPreferences = defaultSharedPreferences;
        this.serializedEntries = defaultSharedPreferences.getStringSet(KEY_GAMEPAD_MAPPING, (Set) null);
    }

    static String checkForClash(Set<String> set, GamepadTypeOverrideEntry gamepadTypeOverrideEntry) {
        for (String next : set) {
            if (GamepadTypeOverrideEntry.fromString(next).usbIdsMatch(gamepadTypeOverrideEntry)) {
                return next;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public synchronized void setEntries(ArrayList<GamepadTypeOverrideEntry> arrayList) {
        Set<String> set = this.serializedEntries;
        if (set != null) {
            set.clear();
        } else {
            this.serializedEntries = new ArraySet();
        }
        if (arrayList.isEmpty()) {
            this.sharedPreferences.edit().remove(KEY_GAMEPAD_MAPPING).commit();
        } else {
            Iterator<GamepadTypeOverrideEntry> it = arrayList.iterator();
            while (it.hasNext()) {
                this.serializedEntries.add(it.next().toString());
            }
            this.sharedPreferences.edit().remove(KEY_GAMEPAD_MAPPING).commit();
            this.sharedPreferences.edit().putStringSet(KEY_GAMEPAD_MAPPING, this.serializedEntries).commit();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void addOrUpdate(GamepadTypeOverrideEntry gamepadTypeOverrideEntry) {
        Set<String> stringSet = this.sharedPreferences.getStringSet(KEY_GAMEPAD_MAPPING, (Set) null);
        this.serializedEntries = stringSet;
        if (stringSet != null) {
            String checkForClash = checkForClash(stringSet, gamepadTypeOverrideEntry);
            if (checkForClash != null) {
                this.serializedEntries.remove(checkForClash);
            }
            this.serializedEntries.add(gamepadTypeOverrideEntry.toString());
        } else {
            ArraySet arraySet = new ArraySet();
            this.serializedEntries = arraySet;
            arraySet.add(gamepadTypeOverrideEntry.toString());
        }
        this.sharedPreferences.edit().putStringSet(KEY_GAMEPAD_MAPPING, this.serializedEntries).commit();
    }

    /* access modifiers changed from: package-private */
    public synchronized GamepadTypeOverrideEntry getEntryFor(int i, int i2) {
        Iterator<GamepadTypeOverrideEntry> it = getEntries().iterator();
        while (it.hasNext()) {
            GamepadTypeOverrideEntry next = it.next();
            if (next.usbIdsMatch(i, i2)) {
                return next;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public synchronized ArrayList<GamepadTypeOverrideEntry> getEntries() {
        Set<String> stringSet = this.sharedPreferences.getStringSet(KEY_GAMEPAD_MAPPING, (Set) null);
        this.serializedEntries = stringSet;
        if (stringSet == null) {
            return new ArrayList<>();
        }
        ArrayList<GamepadTypeOverrideEntry> arrayList = new ArrayList<>();
        for (String fromString : this.serializedEntries) {
            arrayList.add(GamepadTypeOverrideEntry.fromString(fromString));
        }
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete(GamepadTypeOverrideEntry gamepadTypeOverrideEntry) {
        Set<String> stringSet = this.sharedPreferences.getStringSet(KEY_GAMEPAD_MAPPING, (Set) null);
        this.serializedEntries = stringSet;
        if (stringSet != null) {
            boolean z = false;
            for (String equals : stringSet) {
                if (equals.equals(gamepadTypeOverrideEntry.toString())) {
                    z = true;
                }
            }
            if (z) {
                this.serializedEntries.remove(gamepadTypeOverrideEntry.toString());
                this.sharedPreferences.edit().putStringSet(KEY_GAMEPAD_MAPPING, this.serializedEntries).commit();
                return;
            }
            throw new IllegalArgumentException();
        }
    }
}
