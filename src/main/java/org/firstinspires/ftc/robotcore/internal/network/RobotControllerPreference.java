package org.firstinspires.ftc.robotcore.internal.network;

import java.util.Set;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;

public class RobotControllerPreference {
    private Boolean booleanValue = null;
    private Float floatValue = null;
    private Integer intValue = null;
    private Long longValue = null;
    private String prefName;
    private Set<String> stringSetValue = null;
    private String stringValue = null;

    public RobotControllerPreference(String str, Object obj) {
        this.prefName = str;
        if (obj instanceof String) {
            this.stringValue = (String) obj;
        } else if (obj instanceof Boolean) {
            this.booleanValue = (Boolean) obj;
        } else if (obj instanceof Integer) {
            this.intValue = (Integer) obj;
        } else if (obj instanceof Long) {
            this.longValue = (Long) obj;
        } else if (obj instanceof Float) {
            this.floatValue = (Float) obj;
        } else if (obj instanceof Set) {
            this.stringSetValue = (Set) obj;
        }
    }

    public static RobotControllerPreference deserialize(String str) {
        return (RobotControllerPreference) SimpleGson.getInstance().fromJson(str, RobotControllerPreference.class);
    }

    public String serialize() {
        return SimpleGson.getInstance().toJson((Object) this);
    }

    public String getPrefName() {
        return this.prefName;
    }

    public Object getValue() {
        String str = this.stringValue;
        if (str != null) {
            return str;
        }
        Boolean bool = this.booleanValue;
        if (bool != null) {
            return bool;
        }
        Integer num = this.intValue;
        if (num != null) {
            return num;
        }
        Long l = this.longValue;
        if (l != null) {
            return l;
        }
        Float f = this.floatValue;
        if (f != null) {
            return f;
        }
        Set<String> set = this.stringSetValue;
        if (set != null) {
            return set;
        }
        return null;
    }
}
