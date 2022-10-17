package com.qualcomm.ftccommon.configuration;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.opmode.ClassFilter;
import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaDeterminer;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.xmlpull.v1.XmlPullParserException;

public class RobotConfigResFilter implements ClassFilter {
    public static final String robotConfigRootTag = "Robot";
    public static final String robotConfigRootTypeAttribute = "type";
    protected Resources resources;
    protected String typeAttributeValue;
    protected ArrayList<Integer> xmlIdCollection;

    public void filterAllClassesComplete() {
    }

    public void filterExternalLibrariesClass(Class cls) {
    }

    public void filterExternalLibrariesClassesComplete() {
    }

    public void filterExternalLibrariesClassesStart() {
    }

    public void filterOnBotJavaClassesStart() {
    }

    public RobotConfigResFilter(String str) {
        this(AppUtil.getInstance().getApplication(), str);
    }

    public RobotConfigResFilter(Context context, String str) {
        this.typeAttributeValue = str;
        this.resources = context.getResources();
        this.xmlIdCollection = new ArrayList<>();
        clear();
    }

    /* access modifiers changed from: protected */
    public void clear() {
        this.xmlIdCollection.clear();
    }

    public List<Integer> getXmlIds() {
        return this.xmlIdCollection;
    }

    private boolean isRobotConfiguration(XmlResourceParser xmlResourceParser) {
        return this.typeAttributeValue.equals(getRootAttribute(xmlResourceParser, robotConfigRootTag, robotConfigRootTypeAttribute, (String) null));
    }

    public static String getRootAttribute(XmlResourceParser xmlResourceParser, String str, String str2, String str3) {
        while (xmlResourceParser.getEventType() != 1) {
            try {
                if (xmlResourceParser.getEventType() != 2) {
                    xmlResourceParser.next();
                } else if (!xmlResourceParser.getName().equals(str)) {
                    return null;
                } else {
                    String attributeValue = xmlResourceParser.getAttributeValue((String) null, str2);
                    return attributeValue != null ? attributeValue : str3;
                }
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void filterAllClassesStart() {
        clear();
    }

    public void filterClass(Class cls) {
        if (!OnBotJavaDeterminer.isExternalLibraries(cls) && cls.getName().endsWith("R$xml")) {
            for (Field field : cls.getFields()) {
                try {
                    if (field.getType().equals(Integer.TYPE)) {
                        int i = field.getInt(cls);
                        if (isRobotConfiguration(this.resources.getXml(i))) {
                            this.xmlIdCollection.add(Integer.valueOf(i));
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void filterOnBotJavaClass(Class cls) {
        filterClass(cls);
    }

    public void filterOnBotJavaClassesComplete() {
        filterAllClassesComplete();
    }
}
