package com.qualcomm.ftccommon.configuration;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import com.google.gson.reflect.TypeToken;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.robotcore.exception.DuplicateNameException;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.ReadXMLFileHandler;
import com.qualcomm.robotcore.hardware.configuration.WriteXMLFileHandler;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.firstinspires.ftc.robotcore.external.Supplier;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.p013ui.UILocation;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Dom2XmlPullBuilder;
import org.firstinspires.inspection.InspectionState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RobotConfigFileManager {
    public static final boolean DEBUG = false;
    public static final String FILE_EXT = ".xml";
    public static final String FILE_LIST_COMMAND_DELIMITER = ";";
    public static final String ROBOT_CONFIG_DESCRIPTION_GENERATE_XSLT = "RobotConfigDescriptionGenerate.xslt";
    public static final String ROBOT_CONFIG_TAXONOMY_XML = "RobotConfigTaxonomy.xml";
    public static final String TAG = "RobotConfigFileManager";
    private static Supplier<Collection<Integer>> xmlResourceIdSupplier;
    private static Supplier<Collection<Integer>> xmlResourceTemplateIdsSupplier;
    /* access modifiers changed from: private */
    public Activity activity;
    private AppUtil appUtil;
    /* access modifiers changed from: private */
    public Context context;
    private final int idActiveConfigHeader;
    /* access modifiers changed from: private */
    public final int idActiveConfigName;
    private NetworkConnectionHandler networkConnectionHandler;
    public final String noConfig;
    private SharedPreferences preferences;
    private Resources resources;
    private WriteXMLFileHandler writer;

    public static String getRobotConfigTemplateAttribute() {
        return "FirstInspires-FTC-template";
    }

    public static String getRobotConfigTypeAttribute() {
        return "FirstInspires-FTC";
    }

    public RobotConfigFileManager(Activity activity2) {
        this.idActiveConfigName = C0470R.C0472id.idActiveConfigName;
        this.idActiveConfigHeader = C0470R.C0472id.idActiveConfigHeader;
        this.networkConnectionHandler = NetworkConnectionHandler.getInstance();
        AppUtil instance = AppUtil.getInstance();
        this.appUtil = instance;
        this.activity = activity2;
        Application application = instance.getApplication();
        this.context = application;
        this.resources = application.getResources();
        this.writer = new WriteXMLFileHandler();
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.noConfig = this.context.getString(C0470R.string.noCurrentConfigFile);
    }

    public RobotConfigFileManager() {
        this((Activity) null);
    }

    public void createConfigFolder() {
        File file = AppUtil.CONFIG_FILES_DIR;
        if (!(!file.exists() ? file.mkdir() : true)) {
            RobotLog.m48ee(TAG, "Can't create the Robot Config Files directory!");
            this.appUtil.showToast(UILocation.BOTH, this.context.getString(C0470R.string.toastCantCreateRobotConfigFilesDir));
        }
    }

    public static void setXmlResourceIdSupplier(Supplier<Collection<Integer>> supplier) {
        xmlResourceIdSupplier = supplier;
    }

    public static void setXmlResourceTemplateIdSupplier(Supplier<Collection<Integer>> supplier) {
        xmlResourceTemplateIdsSupplier = supplier;
    }

    private Collection<Integer> getXmlResourceIds() {
        Supplier<Collection<Integer>> supplier = xmlResourceIdSupplier;
        return supplier != null ? supplier.get() : new ArrayList();
    }

    private Collection<Integer> getXmlResourceTemplateIds() {
        Supplier<Collection<Integer>> supplier = xmlResourceTemplateIdsSupplier;
        return supplier != null ? supplier.get() : new ArrayList();
    }

    public RobotConfigFile getConfigFromString(String str) {
        return RobotConfigFile.fromString(this, str);
    }

    public RobotConfigFile getActiveConfigAndUpdateUI() {
        RobotConfigFile activeConfig = getActiveConfig();
        updateActiveConfigHeader(activeConfig);
        return activeConfig;
    }

    public RobotConfigFile getActiveConfig() {
        String string = this.preferences.getString(this.context.getString(C0470R.string.pref_hardware_config_filename), (String) null);
        if (string == null) {
            return RobotConfigFile.noConfig(this);
        }
        return getConfigFromString(string);
    }

    public void sendActiveConfigToDriverStation() {
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_NOTIFY_ACTIVE_CONFIGURATION, getActiveConfig().toString()));
    }

    public void setActiveConfigAndUpdateUI(boolean z, RobotConfigFile robotConfigFile) {
        setActiveConfig(z, robotConfigFile);
        updateActiveConfigHeader(robotConfigFile);
    }

    public void setActiveConfigAndUpdateUI(RobotConfigFile robotConfigFile) {
        setActiveConfig(robotConfigFile);
        updateActiveConfigHeader(robotConfigFile);
    }

    public void setActiveConfig(boolean z, RobotConfigFile robotConfigFile) {
        if (z) {
            sendRobotControllerActiveConfigAndUpdateUI(robotConfigFile);
            return;
        }
        setActiveConfig(robotConfigFile);
        sendActiveConfigToDriverStation();
    }

    public void setActiveConfig(RobotConfigFile robotConfigFile) {
        String json = SimpleGson.getInstance().toJson((Object) robotConfigFile);
        SharedPreferences.Editor edit = this.preferences.edit();
        edit.putString(this.context.getString(C0470R.string.pref_hardware_config_filename), json);
        edit.apply();
    }

    public void sendRobotControllerActiveConfigAndUpdateUI(RobotConfigFile robotConfigFile) {
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_ACTIVATE_CONFIGURATION, robotConfigFile.toString()));
    }

    public void updateActiveConfigHeader(RobotConfigFile robotConfigFile) {
        updateActiveConfigHeader(robotConfigFile.getName(), robotConfigFile.isDirty());
    }

    public void updateActiveConfigHeader(final String str, final boolean z) {
        if (this.activity != null) {
            this.appUtil.runOnUiThread(new Runnable() {
                public void run() {
                    String trim = RobotConfigFileManager.stripFileNameExtension(str).trim();
                    if (trim.isEmpty()) {
                        trim = RobotConfigFileManager.this.noConfig;
                    }
                    if (z) {
                        trim = String.format(RobotConfigFileManager.this.context.getString(C0470R.string.configDirtyLabel), new Object[]{trim});
                    }
                    TextView textView = (TextView) RobotConfigFileManager.this.activity.findViewById(RobotConfigFileManager.this.idActiveConfigName);
                    if (textView != null) {
                        textView.setText(trim);
                    } else {
                        RobotLog.m49ee(RobotConfigFileManager.TAG, "unable to find header text 0x%08x", Integer.valueOf(RobotConfigFileManager.this.idActiveConfigName));
                    }
                    if (!z && trim.equalsIgnoreCase(RobotConfigFileManager.this.noConfig)) {
                        RobotConfigFileManager.this.changeHeaderBackground(C0470R.C0472id.backgroundLightHolder);
                    } else if (z) {
                        RobotConfigFileManager.this.changeHeaderBackground(C0470R.C0472id.backgroundDarkGrayHolder);
                    } else {
                        RobotConfigFileManager.this.changeHeaderBackground(C0470R.C0472id.backgroundMediumHolder);
                    }
                }
            });
        } else {
            RobotLog.m48ee(TAG, "updateActiveConfigHeader called with null activity");
        }
    }

    public void changeHeaderBackground(int i) {
        Activity activity2 = this.activity;
        if (activity2 != null) {
            View findViewById = activity2.findViewById(i);
            View findViewById2 = this.activity.findViewById(C0470R.C0472id.idActiveConfigHeader);
            if (findViewById != null && findViewById2 != null) {
                findViewById2.setBackground(findViewById.getBackground());
                return;
            }
            return;
        }
        RobotLog.m48ee(TAG, "changeHeaderBackground called with null activity");
    }

    public static class ConfigNameCheckResult {
        public String errorFormat;
        public boolean success;

        public ConfigNameCheckResult(boolean z) {
            this.errorFormat = null;
            this.success = z;
        }

        public ConfigNameCheckResult(String str) {
            this.success = false;
            this.errorFormat = str;
        }
    }

    public ConfigNameCheckResult isPlausibleConfigName(RobotConfigFile robotConfigFile, String str, List<RobotConfigFile> list) {
        if (!str.equals(str.trim())) {
            return new ConfigNameCheckResult(this.context.getString(C0470R.string.configNameWhitespace));
        }
        if (str.length() == 0) {
            return new ConfigNameCheckResult(this.context.getString(C0470R.string.configNameEmpty));
        }
        if (!new File(str).getName().equals(str)) {
            return new ConfigNameCheckResult(this.context.getString(C0470R.string.configNameIllegalCharacters));
        }
        for (char indexOf : str.toCharArray()) {
            if ("?:\"*|/\\<>".indexOf(indexOf) != -1) {
                return new ConfigNameCheckResult(this.context.getString(C0470R.string.configNameIllegalCharacters));
            }
        }
        if (str.equalsIgnoreCase(this.noConfig)) {
            return new ConfigNameCheckResult(this.context.getString(C0470R.string.configNameReserved));
        }
        if (!str.equalsIgnoreCase(robotConfigFile.getName())) {
            for (RobotConfigFile name : list) {
                if (str.equalsIgnoreCase(name.getName())) {
                    return new ConfigNameCheckResult(this.context.getString(C0470R.string.configNameExists));
                }
            }
            return new ConfigNameCheckResult(true);
        } else if (robotConfigFile.isReadOnly()) {
            return new ConfigNameCheckResult(this.context.getString(C0470R.string.configNameReadOnly));
        } else {
            return new ConfigNameCheckResult(true);
        }
    }

    public static String stripFileNameExtension(String str) {
        return str.replaceFirst("[.][^.]+$", InspectionState.NO_VERSION);
    }

    public static File stripFileNameExtension(File file) {
        return new File(file.getParentFile(), stripFileNameExtension(file.getName()));
    }

    public static String withExtension(String str) {
        return stripFileNameExtension(str) + FILE_EXT;
    }

    public static File getFullPath(String str) {
        return new File(AppUtil.CONFIG_FILES_DIR, withExtension(str));
    }

    public ArrayList<RobotConfigFile> getXMLFiles() {
        File[] listFiles = AppUtil.CONFIG_FILES_DIR.listFiles();
        ArrayList<RobotConfigFile> arrayList = new ArrayList<>();
        for (File file : listFiles) {
            if (file.isFile()) {
                String name = file.getName();
                if (Pattern.compile("(?i).xml").matcher(name).find()) {
                    arrayList.add(new RobotConfigFile(this, stripFileNameExtension(name)));
                }
            }
        }
        for (Integer intValue : getXmlResourceIds()) {
            int intValue2 = intValue.intValue();
            RobotConfigFile robotConfigFile = new RobotConfigFile(RobotConfigResFilter.getRootAttribute(this.resources.getXml(intValue2), RobotConfigResFilter.robotConfigRootTag, "name", this.resources.getResourceEntryName(intValue2)), intValue2);
            if (!robotConfigFile.containedIn(arrayList)) {
                arrayList.add(robotConfigFile);
            }
        }
        return arrayList;
    }

    public ArrayList<RobotConfigFile> getXMLTemplates() {
        ArrayList<RobotConfigFile> arrayList = new ArrayList<>();
        for (Integer intValue : getXmlResourceTemplateIds()) {
            int intValue2 = intValue.intValue();
            RobotConfigFile robotConfigFile = new RobotConfigFile(RobotConfigResFilter.getRootAttribute(this.resources.getXml(intValue2), RobotConfigResFilter.robotConfigRootTag, "name", this.resources.getResourceEntryName(intValue2)), intValue2);
            if (!robotConfigFile.containedIn(arrayList)) {
                arrayList.add(robotConfigFile);
            }
        }
        return arrayList;
    }

    public String getRobotConfigDescription(XmlPullParser xmlPullParser) {
        try {
            Source sourceFromPullParser = getSourceFromPullParser(xmlPullParser);
            Source robotConfigDescriptionTransform = getRobotConfigDescriptionTransform();
            StringWriter stringWriter = new StringWriter();
            TransformerFactory.newInstance().newTransformer(robotConfigDescriptionTransform).transform(sourceFromPullParser, new StreamResult(stringWriter));
            return stringWriter.toString().trim();
        } catch (IOException | TransformerException | XmlPullParserException e) {
            RobotLog.logStackTrace(e);
            return this.context.getString(C0470R.string.templateConfigureNoDescriptionAvailable);
        }
    }

    /* access modifiers changed from: protected */
    public Source getRobotConfigDescriptionTransform() throws XmlPullParserException, IOException, TransformerConfigurationException, TransformerException {
        Element parseSubTree = new Dom2XmlPullBuilder().parseSubTree(ReadXMLFileHandler.xmlPullParserFromReader(new InputStreamReader(this.context.getAssets().open(ROBOT_CONFIG_TAXONOMY_XML))));
        parseSubTree.getOwnerDocument();
        DOMSource dOMSource = new DOMSource(parseSubTree);
        StreamSource streamSource = new StreamSource(this.context.getAssets().open(ROBOT_CONFIG_DESCRIPTION_GENERATE_XSLT));
        StringWriter stringWriter = new StringWriter();
        TransformerFactory.newInstance().newTransformer(streamSource).transform(dOMSource, new StreamResult(stringWriter));
        return new StreamSource(new StringReader(stringWriter.toString().trim()));
    }

    /* access modifiers changed from: protected */
    public void addChild(Document document, Element element, String str, String str2) {
        Element createElement = document.createElement(str);
        createElement.setTextContent(str2);
        element.appendChild(createElement);
    }

    /* access modifiers changed from: protected */
    public Source getSourceFromPullParser(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        return new DOMSource(new Dom2XmlPullBuilder().parseSubTree(xmlPullParser));
    }

    public static String serializeXMLConfigList(List<RobotConfigFile> list) {
        return SimpleGson.getInstance().toJson((Object) list);
    }

    public static String serializeConfig(RobotConfigFile robotConfigFile) {
        return SimpleGson.getInstance().toJson((Object) robotConfigFile);
    }

    public static List<RobotConfigFile> deserializeXMLConfigList(String str) {
        return (List) SimpleGson.getInstance().fromJson(str, new TypeToken<Collection<RobotConfigFile>>() {
        }.getType());
    }

    public static RobotConfigFile deserializeConfig(String str) {
        return (RobotConfigFile) SimpleGson.getInstance().fromJson(str, (Type) RobotConfigFile.class);
    }

    public String toXml(Map<SerialNumber, ControllerConfiguration> map) {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(map.values());
        try {
            return this.writer.toXml(arrayList, RobotConfigResFilter.robotConfigRootTypeAttribute, getRobotConfigTypeAttribute());
        } catch (DuplicateNameException e) {
            this.appUtil.showToast(UILocation.BOTH, String.format(this.context.getString(C0470R.string.toastDuplicateName), new Object[]{e.getMessage()}));
            RobotLog.m48ee(TAG, "Found " + e.getMessage());
            return null;
        } catch (RuntimeException e2) {
            RobotLog.m50ee(TAG, (Throwable) e2, "exception while writing XML");
            return null;
        }
    }

    public String toXml(RobotConfigMap robotConfigMap) {
        return toXml(robotConfigMap.map);
    }

    /* access modifiers changed from: package-private */
    public void writeXMLToFile(String str, String str2) throws RobotCoreException, IOException {
        this.writer.writeToFile(str2, AppUtil.CONFIG_FILES_DIR, str);
    }

    /* access modifiers changed from: package-private */
    public void writeToRobotController(RobotConfigFile robotConfigFile, String str) {
        NetworkConnectionHandler networkConnectionHandler2 = this.networkConnectionHandler;
        networkConnectionHandler2.sendCommand(new Command(CommandList.CMD_SAVE_CONFIGURATION, robotConfigFile.toString() + FILE_LIST_COMMAND_DELIMITER + str));
    }

    public void writeToFile(RobotConfigFile robotConfigFile, boolean z, String str) throws RobotCoreException, IOException {
        boolean isDirty = robotConfigFile.isDirty();
        robotConfigFile.markClean();
        if (z) {
            try {
                writeToRobotController(robotConfigFile, str);
            } catch (RobotCoreException | IOException | RuntimeException e) {
                if (isDirty) {
                    robotConfigFile.markDirty();
                }
                throw e;
            }
        } else {
            writeXMLToFile(withExtension(robotConfigFile.getName()), str);
        }
    }
}
