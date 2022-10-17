package org.firstinspires.ftc.robotcore.internal.system;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.HashMap;
import java.util.Set;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.inspection.InspectionState;

public class PreferencesHelper {
    protected final SharedPreferences sharedPreferences;
    protected final String tag;

    public PreferencesHelper(String str) {
        this(str, (Context) AppUtil.getDefContext());
    }

    public PreferencesHelper(String str, Context context) {
        this(str, PreferenceManager.getDefaultSharedPreferences(context));
    }

    public PreferencesHelper(String str, SharedPreferences sharedPreferences2) {
        this.tag = str;
        this.sharedPreferences = sharedPreferences2;
    }

    public SharedPreferences getSharedPreferences() {
        return this.sharedPreferences;
    }

    public Object readPref(String str) {
        return this.sharedPreferences.getAll().get(str);
    }

    public boolean writePrefIfDifferent(String str, Object obj) {
        if (obj instanceof String) {
            return writeStringPrefIfDifferent(str, (String) obj);
        }
        if (obj instanceof Boolean) {
            return writeBooleanPrefIfDifferent(str, ((Boolean) obj).booleanValue());
        }
        if (obj instanceof Integer) {
            return writeIntPrefIfDifferent(str, ((Integer) obj).intValue());
        }
        if (obj instanceof Long) {
            return writeLongPrefIfDifferent(str, ((Long) obj).longValue());
        }
        if (obj instanceof Float) {
            return writeFloatPrefIfDifferent(str, ((Float) obj).floatValue());
        }
        if (obj instanceof StringMap) {
            return writeStringMapPrefIfDifferent(str, (StringMap) obj);
        }
        if (obj instanceof Set) {
            return writeStringSetPrefIfDifferent(str, (Set) obj);
        }
        return false;
    }

    public String readString(String str, String str2) {
        return this.sharedPreferences.getString(str, str2);
    }

    public Set<String> readStringSet(String str, Set<String> set) {
        return this.sharedPreferences.getStringSet(str, set);
    }

    public int readInt(String str, int i) {
        return this.sharedPreferences.getInt(str, i);
    }

    public long readLong(String str, long j) {
        return this.sharedPreferences.getLong(str, j);
    }

    public float readFloat(String str, float f) {
        return this.sharedPreferences.getFloat(str, f);
    }

    public boolean readBoolean(String str, boolean z) {
        return this.sharedPreferences.getBoolean(str, z);
    }

    public StringMap readStringMap(String str, StringMap stringMap) {
        StringMap deserialize = StringMap.deserialize(readString(str, (String) null));
        return deserialize != null ? deserialize : stringMap;
    }

    public boolean contains(String str) {
        return this.sharedPreferences.contains(str);
    }

    public void remove(String str) {
        this.sharedPreferences.edit().remove(str).apply();
    }

    public boolean writeStringPrefIfDifferent(String str, String str2) {
        Assert.assertNotNull(str2);
        boolean z = false;
        while (true) {
            if (contains(str) && str2.equals(readString(str, InspectionState.NO_VERSION))) {
                return z;
            }
            logWrite(str, str2);
            this.sharedPreferences.edit().putString(str, str2).apply();
            z = true;
        }
    }

    public boolean writeStringSetPrefIfDifferent(String str, Set<String> set) {
        Assert.assertNotNull(set);
        boolean z = false;
        while (true) {
            if (contains(str)) {
                Set set2 = null;
                if (set.equals(readStringSet(str, (Set<String>) null))) {
                    return z;
                }
            }
            logWrite(str, set);
            this.sharedPreferences.edit().putStringSet(str, set).apply();
            z = true;
        }
    }

    public boolean writeIntPrefIfDifferent(String str, int i) {
        Assert.assertNotNull(Integer.valueOf(i));
        boolean z = false;
        while (true) {
            if (contains(str) && i == readInt(str, 0)) {
                return z;
            }
            logWrite(str, Integer.valueOf(i));
            this.sharedPreferences.edit().putInt(str, i).apply();
            z = true;
        }
    }

    public boolean writeLongPrefIfDifferent(String str, long j) {
        Assert.assertNotNull(Long.valueOf(j));
        boolean z = false;
        while (true) {
            if (contains(str) && j == readLong(str, 0)) {
                return z;
            }
            logWrite(str, Long.valueOf(j));
            this.sharedPreferences.edit().putLong(str, j).apply();
            z = true;
        }
    }

    public boolean writeFloatPrefIfDifferent(String str, float f) {
        Assert.assertNotNull(Float.valueOf(f));
        boolean z = false;
        while (true) {
            if (contains(str) && f == readFloat(str, 0.0f)) {
                return z;
            }
            logWrite(str, Float.valueOf(f));
            this.sharedPreferences.edit().putFloat(str, f).apply();
            z = true;
        }
    }

    public boolean writeBooleanPrefIfDifferent(String str, boolean z) {
        Assert.assertNotNull(Boolean.valueOf(z));
        boolean z2 = false;
        while (true) {
            if (contains(str) && z == readBoolean(str, false)) {
                return z2;
            }
            logWrite(str, Boolean.valueOf(z));
            this.sharedPreferences.edit().putBoolean(str, z).apply();
            z2 = true;
        }
    }

    public boolean writeStringMapPrefIfDifferent(String str, StringMap stringMap) {
        return writeStringPrefIfDifferent(str, stringMap.serialize());
    }

    /* access modifiers changed from: protected */
    public void logWrite(String str, Object obj) {
        RobotLog.m61vv(this.tag, "writing pref name=%s value=%s", str, obj);
    }

    public static class StringMap extends HashMap<String, String> {
        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static StringMap deserialize(String str) {
            if (str == null) {
                return null;
            }
            return (StringMap) SimpleGson.getInstance().fromJson(str, StringMap.class);
        }
    }
}
