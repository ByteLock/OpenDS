package com.qualcomm.ftccommon.configuration;

import android.content.res.Resources;
import com.google.gson.JsonSyntaxException;
import com.qualcomm.robotcore.util.RobotLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.Collection;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class RobotConfigFile {
    private static final String LOGGER_TAG = "RobotConfigFile";
    private boolean isDirty;
    private FileLocation location;
    private String name;
    private int resourceId;

    public enum FileLocation {
        NONE,
        LOCAL_STORAGE,
        RESOURCE
    }

    public static RobotConfigFile noConfig(RobotConfigFileManager robotConfigFileManager) {
        return new RobotConfigFile(robotConfigFileManager, robotConfigFileManager.noConfig);
    }

    public RobotConfigFile(RobotConfigFileManager robotConfigFileManager, String str) {
        String stripFileNameExtension = RobotConfigFileManager.stripFileNameExtension(str);
        this.name = stripFileNameExtension;
        this.resourceId = 0;
        this.location = stripFileNameExtension.equalsIgnoreCase(robotConfigFileManager.noConfig) ? FileLocation.NONE : FileLocation.LOCAL_STORAGE;
        this.isDirty = false;
    }

    public RobotConfigFile(String str, int i) {
        this.name = str;
        this.resourceId = i;
        this.location = FileLocation.RESOURCE;
        this.isDirty = false;
    }

    public boolean isReadOnly() {
        return this.location == FileLocation.RESOURCE || this.location == FileLocation.NONE;
    }

    public boolean containedIn(Collection<RobotConfigFile> collection) {
        for (RobotConfigFile robotConfigFile : collection) {
            if (robotConfigFile.name.equalsIgnoreCase(this.name)) {
                return true;
            }
        }
        return false;
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public void markClean() {
        this.isDirty = false;
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public String getName() {
        return this.name;
    }

    public File getFullPath() {
        return RobotConfigFileManager.getFullPath(getName());
    }

    public int getResourceId() {
        return this.resourceId;
    }

    public FileLocation getLocation() {
        return this.location;
    }

    /* renamed from: com.qualcomm.ftccommon.configuration.RobotConfigFile$1 */
    static /* synthetic */ class C05301 {

        /* renamed from: $SwitchMap$com$qualcomm$ftccommon$configuration$RobotConfigFile$FileLocation */
        static final /* synthetic */ int[] f68xc2cfb0c5;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.qualcomm.ftccommon.configuration.RobotConfigFile$FileLocation[] r0 = com.qualcomm.ftccommon.configuration.RobotConfigFile.FileLocation.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f68xc2cfb0c5 = r0
                com.qualcomm.ftccommon.configuration.RobotConfigFile$FileLocation r1 = com.qualcomm.ftccommon.configuration.RobotConfigFile.FileLocation.LOCAL_STORAGE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f68xc2cfb0c5     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.ftccommon.configuration.RobotConfigFile$FileLocation r1 = com.qualcomm.ftccommon.configuration.RobotConfigFile.FileLocation.RESOURCE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f68xc2cfb0c5     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.ftccommon.configuration.RobotConfigFile$FileLocation r1 = com.qualcomm.ftccommon.configuration.RobotConfigFile.FileLocation.NONE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.configuration.RobotConfigFile.C05301.<clinit>():void");
        }
    }

    public XmlPullParser getXml() throws FileNotFoundException, XmlPullParserException {
        int i = C05301.f68xc2cfb0c5[this.location.ordinal()];
        if (i == 1) {
            return getXmlLocalStorage();
        }
        if (i == 2) {
            return getXmlResource();
        }
        if (i == 3) {
            return getXmlNone();
        }
        throw new RuntimeException("Unknown type of configuration location: " + this.location);
    }

    /* access modifiers changed from: protected */
    public XmlPullParser getXmlNone() throws XmlPullParserException {
        StringReader stringReader = new StringReader("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n<Robot type=\"FirstInspires-FTC\">\n</Robot>\n");
        XmlPullParserFactory newInstance = XmlPullParserFactory.newInstance();
        newInstance.setNamespaceAware(true);
        XmlPullParser newPullParser = newInstance.newPullParser();
        newPullParser.setInput(stringReader);
        return newPullParser;
    }

    /* access modifiers changed from: protected */
    public XmlPullParser getXmlLocalStorage() throws XmlPullParserException, FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(RobotConfigFileManager.getFullPath(getName()));
        XmlPullParserFactory newInstance = XmlPullParserFactory.newInstance();
        newInstance.setNamespaceAware(true);
        XmlPullParser newPullParser = newInstance.newPullParser();
        newPullParser.setInput(fileInputStream, (String) null);
        return newPullParser;
    }

    /* access modifiers changed from: protected */
    public XmlPullParser getXmlResource() throws FileNotFoundException {
        try {
            return AppUtil.getInstance().getApplication().getResources().getXml(this.resourceId);
        } catch (Resources.NotFoundException unused) {
            throw new FileNotFoundException("XML resource not found");
        }
    }

    public boolean isNoConfig() {
        return this.location == FileLocation.NONE;
    }

    public String toString() {
        return SimpleGson.getInstance().toJson((Object) this);
    }

    public static RobotConfigFile fromString(RobotConfigFileManager robotConfigFileManager, String str) {
        try {
            RobotConfigFile robotConfigFile = (RobotConfigFile) SimpleGson.getInstance().fromJson(str, RobotConfigFile.class);
            return robotConfigFile == null ? noConfig(robotConfigFileManager) : robotConfigFile;
        } catch (JsonSyntaxException unused) {
            RobotLog.m48ee(LOGGER_TAG, "Could not parse the stored config file data from shared settings");
            return noConfig(robotConfigFileManager);
        }
    }
}
