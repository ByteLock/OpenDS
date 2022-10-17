package com.qualcomm.robotcore.hardware.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.configuration.annotations.AnalogSensorType;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.DigitalIoDeviceType;
import com.qualcomm.robotcore.hardware.configuration.annotations.ExpansionHubPIDFPositionParams;
import com.qualcomm.robotcore.hardware.configuration.annotations.ExpansionHubPIDFVelocityParams;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import com.qualcomm.robotcore.hardware.configuration.annotations.ServoType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.AnalogSensorConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.DigitalIoDeviceConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.I2cDeviceConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.InstantiableUserConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.UserConfigurationType;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.ClassUtil;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.Util;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.firstinspires.ftc.robotcore.external.Predicate;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.opmode.ClassFilter;

public final class ConfigurationTypeManager implements ClassFilter {
    public static boolean DEBUG = true;
    public static final String TAG = "UserDeviceTypeManager";
    private static String standardServoTypeXmlTag = getXmlTag(Servo.class);
    private static ConfigurationTypeManager theInstance = new ConfigurationTypeManager();
    private static final Class[] typeAnnotationsArray;
    private static final List<Class> typeAnnotationsList;
    private static String unspecifiedMotorTypeXmlTag = getXmlTag(UnspecifiedMotor.class);
    private Map<ConfigurationType.DeviceFlavor, Set<String>> existingTypeDisplayNamesMap = new HashMap();
    private Set<String> existingXmlTags = new HashSet();
    private Gson gson = newGson();
    private Map<String, UserConfigurationType> mapTagToUserType = new HashMap();
    private Comparator<? super ConfigurationType> simpleConfigTypeComparator = new Comparator<ConfigurationType>() {
        public int compare(ConfigurationType configurationType, ConfigurationType configurationType2) {
            return configurationType.getDisplayName(ConfigurationType.DisplayNameFlavor.Normal).compareTo(configurationType2.getDisplayName(ConfigurationType.DisplayNameFlavor.Normal));
        }
    };

    public void filterAllClassesComplete() {
    }

    public void filterExternalLibrariesClassesComplete() {
    }

    public void filterOnBotJavaClassesComplete() {
    }

    public static ConfigurationTypeManager getInstance() {
        return theInstance;
    }

    static {
        Class[] clsArr = {ServoType.class, AnalogSensorType.class, DigitalIoDeviceType.class, I2cDeviceType.class, MotorType.class};
        typeAnnotationsArray = clsArr;
        typeAnnotationsList = Arrays.asList(clsArr);
    }

    public ConfigurationTypeManager() {
        for (ConfigurationType.DeviceFlavor put : ConfigurationType.DeviceFlavor.values()) {
            this.existingTypeDisplayNamesMap.put(put, new HashSet());
        }
        addBuiltinConfigurationTypes();
    }

    public MotorConfigurationType getUnspecifiedMotorType() {
        return (MotorConfigurationType) configurationTypeFromTag(unspecifiedMotorTypeXmlTag);
    }

    public ServoConfigurationType getStandardServoType() {
        return (ServoConfigurationType) configurationTypeFromTag(standardServoTypeXmlTag);
    }

    public ConfigurationType configurationTypeFromTag(String str) {
        BuiltInConfigurationType fromXmlTag = BuiltInConfigurationType.fromXmlTag(str);
        if (fromXmlTag != BuiltInConfigurationType.UNKNOWN) {
            return fromXmlTag;
        }
        ConfigurationType configurationType = this.mapTagToUserType.get(str);
        return configurationType == null ? BuiltInConfigurationType.UNKNOWN : configurationType;
    }

    public UserConfigurationType userTypeFromClass(ConfigurationType.DeviceFlavor deviceFlavor, Class<?> cls) {
        MotorType motorType;
        DeviceProperties deviceProperties = (DeviceProperties) cls.getAnnotation(DeviceProperties.class);
        String xmlTag = deviceProperties != null ? getXmlTag(deviceProperties) : null;
        if (xmlTag == null) {
            int i = C07325.f127x91235fd1[deviceFlavor.ordinal()];
            if (i == 1) {
                I2cSensor i2cSensor = (I2cSensor) cls.getAnnotation(I2cSensor.class);
                if (i2cSensor != null) {
                    xmlTag = getXmlTag(i2cSensor);
                }
            } else if (i == 2 && (motorType = (MotorType) cls.getAnnotation(MotorType.class)) != null) {
                xmlTag = getXmlTag(motorType);
            }
        }
        if (xmlTag == null) {
            return null;
        }
        return (UserConfigurationType) configurationTypeFromTag(xmlTag);
    }

    /* renamed from: com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager$5 */
    static /* synthetic */ class C07325 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$hardware$configuration$ConfigurationType$DeviceFlavor */
        static final /* synthetic */ int[] f127x91235fd1;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.qualcomm.robotcore.hardware.configuration.ConfigurationType$DeviceFlavor[] r0 = com.qualcomm.robotcore.hardware.configuration.ConfigurationType.DeviceFlavor.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f127x91235fd1 = r0
                com.qualcomm.robotcore.hardware.configuration.ConfigurationType$DeviceFlavor r1 = com.qualcomm.robotcore.hardware.configuration.ConfigurationType.DeviceFlavor.I2C     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f127x91235fd1     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.configuration.ConfigurationType$DeviceFlavor r1 = com.qualcomm.robotcore.hardware.configuration.ConfigurationType.DeviceFlavor.MOTOR     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager.C07325.<clinit>():void");
        }
    }

    public List<ConfigurationType> getApplicableConfigTypes(ConfigurationType.DeviceFlavor deviceFlavor, ControlSystem controlSystem, boolean z, int i) {
        LinkedList linkedList = new LinkedList();
        for (UserConfigurationType next : this.mapTagToUserType.values()) {
            if (!linkedList.contains(next) && next.getDeviceFlavor() == deviceFlavor) {
                if ((controlSystem == null || next.isCompatibleWith(controlSystem)) && (next != I2cDeviceConfigurationType.getLynxEmbeddedBNO055ImuType() || controlSystem == null || (controlSystem == ControlSystem.REV_HUB && i == 0))) {
                    if (next != I2cDeviceConfigurationType.getLynxEmbeddedBHI260APImuType() || (z && i == 0)) {
                        linkedList.add(next);
                    }
                }
            }
        }
        linkedList.addAll(getApplicableBuiltInTypes(deviceFlavor, controlSystem));
        Collections.sort(linkedList, this.simpleConfigTypeComparator);
        linkedList.addAll(getDeprecatedConfigTypes(deviceFlavor, controlSystem));
        linkedList.addFirst(BuiltInConfigurationType.NOTHING);
        return linkedList;
    }

    public List<ConfigurationType> getApplicableConfigTypes(ConfigurationType.DeviceFlavor deviceFlavor, ControlSystem controlSystem, boolean z) {
        return getApplicableConfigTypes(deviceFlavor, controlSystem, z, 0);
    }

    private List<BuiltInConfigurationType> getApplicableBuiltInTypes(ConfigurationType.DeviceFlavor deviceFlavor, ControlSystem controlSystem) {
        LinkedList linkedList = new LinkedList();
        if (C07325.f127x91235fd1[deviceFlavor.ordinal()] == 1) {
            linkedList.add(BuiltInConfigurationType.IR_SEEKER_V3);
            linkedList.add(BuiltInConfigurationType.ADAFRUIT_COLOR_SENSOR);
            linkedList.add(BuiltInConfigurationType.COLOR_SENSOR);
            linkedList.add(BuiltInConfigurationType.GYRO);
            if (controlSystem == ControlSystem.REV_HUB) {
                linkedList.add(BuiltInConfigurationType.LYNX_COLOR_SENSOR);
            }
        }
        return linkedList;
    }

    private List<BuiltInConfigurationType> getDeprecatedConfigTypes(ConfigurationType.DeviceFlavor deviceFlavor, ControlSystem controlSystem) {
        return new LinkedList();
    }

    public Gson getGson() {
        return this.gson;
    }

    public void sendUserDeviceTypes() {
        NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_NOTIFY_USER_DEVICE_LIST, serializeUserDeviceTypes()));
    }

    public void deserializeUserDeviceTypes(String str) {
        clearUserTypes();
        for (ConfigurationType configurationType : (ConfigurationType[]) this.gson.fromJson(str, ConfigurationType[].class)) {
            if (!configurationType.isDeviceFlavor(ConfigurationType.DeviceFlavor.BUILT_IN)) {
                add((UserConfigurationType) configurationType);
            }
        }
        if (DEBUG) {
            for (Map.Entry next : this.mapTagToUserType.entrySet()) {
                RobotLog.m61vv(TAG, "deserialized: xmltag=%s name=%s class=%s", ((UserConfigurationType) next.getValue()).getXmlTag(), ((UserConfigurationType) next.getValue()).getName(), ((UserConfigurationType) next.getValue()).getClass().getSimpleName());
            }
        }
    }

    private Gson newGson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(BuiltInConfigurationType.class, new BuiltInConfigurationTypeJsonAdapter()).registerTypeAdapterFactory(RuntimeTypeAdapterFactory.m11of(ConfigurationType.class, "flavor").registerSubtype(BuiltInConfigurationType.class, ConfigurationType.DeviceFlavor.BUILT_IN.toString()).registerSubtype(I2cDeviceConfigurationType.class, ConfigurationType.DeviceFlavor.I2C.toString()).registerSubtype(MotorConfigurationType.class, ConfigurationType.DeviceFlavor.MOTOR.toString()).registerSubtype(ServoConfigurationType.class, ConfigurationType.DeviceFlavor.SERVO.toString()).registerSubtype(AnalogSensorConfigurationType.class, ConfigurationType.DeviceFlavor.ANALOG_SENSOR.toString()).registerSubtype(DigitalIoDeviceConfigurationType.class, ConfigurationType.DeviceFlavor.DIGITAL_IO.toString())).create();
    }

    private String serializeUserDeviceTypes() {
        return this.gson.toJson((Object) this.mapTagToUserType.values());
    }

    private void addBuiltinConfigurationTypes() {
        for (BuiltInConfigurationType builtInConfigurationType : BuiltInConfigurationType.values()) {
            this.existingXmlTags.add(builtInConfigurationType.getXmlTag());
            this.existingTypeDisplayNamesMap.get(builtInConfigurationType.getDeviceFlavor()).add(builtInConfigurationType.getDisplayName(ConfigurationType.DisplayNameFlavor.Normal));
        }
    }

    private void add(UserConfigurationType userConfigurationType) {
        this.mapTagToUserType.put(userConfigurationType.getXmlTag(), userConfigurationType);
        this.existingTypeDisplayNamesMap.get(userConfigurationType.getDeviceFlavor()).add(userConfigurationType.getName());
        this.existingXmlTags.add(userConfigurationType.getXmlTag());
        for (String str : userConfigurationType.getXmlTagAliases()) {
            this.mapTagToUserType.put(str, userConfigurationType);
            this.existingXmlTags.add(str);
        }
    }

    private void clearUserTypes() {
        for (UserConfigurationType userConfigurationType : new ArrayList(this.mapTagToUserType.values())) {
            this.existingTypeDisplayNamesMap.get(userConfigurationType.getDeviceFlavor()).remove(userConfigurationType.getName());
            this.existingXmlTags.remove(userConfigurationType.getXmlTag());
            this.mapTagToUserType.remove(userConfigurationType.getXmlTag());
        }
    }

    private void clearOnBotJavaTypes() {
        for (UserConfigurationType userConfigurationType : new ArrayList(this.mapTagToUserType.values())) {
            if (userConfigurationType.isOnBotJava()) {
                this.existingTypeDisplayNamesMap.get(userConfigurationType.getDeviceFlavor()).remove(userConfigurationType.getName());
                this.existingXmlTags.remove(userConfigurationType.getXmlTag());
                this.mapTagToUserType.remove(userConfigurationType.getXmlTag());
            }
        }
    }

    private void clearExternalLibrariesTypes() {
        for (UserConfigurationType userConfigurationType : new ArrayList(this.mapTagToUserType.values())) {
            if (userConfigurationType.isExternalLibraries()) {
                this.existingTypeDisplayNamesMap.get(userConfigurationType.getDeviceFlavor()).remove(userConfigurationType.getName());
                this.existingXmlTags.remove(userConfigurationType.getXmlTag());
                this.mapTagToUserType.remove(userConfigurationType.getXmlTag());
            }
        }
    }

    public void filterAllClassesStart() {
        clearUserTypes();
    }

    public void filterOnBotJavaClassesStart() {
        clearOnBotJavaTypes();
    }

    public void filterExternalLibrariesClassesStart() {
        clearExternalLibrariesTypes();
    }

    public void filterClass(Class cls) {
        Annotation typeAnnotation;
        if (!addMotorTypeFromDeprecatedAnnotation(cls) && !addI2cTypeFromDeprecatedAnnotation(cls) && (typeAnnotation = getTypeAnnotation(cls)) != null) {
            DeviceProperties deviceProperties = (DeviceProperties) cls.getAnnotation(DeviceProperties.class);
            if (deviceProperties == null) {
                reportConfigurationError("Class " + cls.getSimpleName() + " annotated with " + typeAnnotation + " is missing @DeviceProperties annotation.", new Object[0]);
                return;
            }
            UserConfigurationType createAppropriateConfigurationType = createAppropriateConfigurationType(typeAnnotation, deviceProperties, cls);
            createAppropriateConfigurationType.processAnnotation(deviceProperties);
            createAppropriateConfigurationType.finishedAnnotations(cls);
            if (createAppropriateConfigurationType instanceof InstantiableUserConfigurationType) {
                InstantiableUserConfigurationType instantiableUserConfigurationType = (InstantiableUserConfigurationType) createAppropriateConfigurationType;
                if (instantiableUserConfigurationType.classMustBeInstantiable()) {
                    if (checkInstantiableTypeConstraints(instantiableUserConfigurationType)) {
                        add(createAppropriateConfigurationType);
                        return;
                    }
                    return;
                }
            }
            if (checkAnnotationParameterConstraints(createAppropriateConfigurationType)) {
                add(createAppropriateConfigurationType);
            }
        }
    }

    public void filterOnBotJavaClass(Class cls) {
        filterClass(cls);
    }

    public void filterExternalLibrariesClass(Class cls) {
        filterClass(cls);
    }

    private UserConfigurationType createAppropriateConfigurationType(Annotation annotation, DeviceProperties deviceProperties, Class cls) {
        if (annotation instanceof ServoType) {
            ServoConfigurationType servoConfigurationType = new ServoConfigurationType(cls, getXmlTag(deviceProperties));
            ServoConfigurationType servoConfigurationType2 = servoConfigurationType;
            servoConfigurationType.processAnnotation((ServoType) annotation);
            return servoConfigurationType;
        } else if (annotation instanceof MotorType) {
            MotorConfigurationType motorConfigurationType = new MotorConfigurationType(cls, getXmlTag(deviceProperties));
            MotorConfigurationType motorConfigurationType2 = motorConfigurationType;
            processMotorSupportAnnotations(cls, motorConfigurationType);
            motorConfigurationType.processAnnotation((MotorType) annotation);
            return motorConfigurationType;
        } else if (annotation instanceof AnalogSensorType) {
            return new AnalogSensorConfigurationType(cls, getXmlTag(deviceProperties));
        } else {
            if (annotation instanceof DigitalIoDeviceType) {
                return new DigitalIoDeviceConfigurationType(cls, getXmlTag(deviceProperties));
            }
            if (annotation instanceof I2cDeviceType) {
                return new I2cDeviceConfigurationType(cls, getXmlTag(deviceProperties));
            }
            return null;
        }
    }

    private boolean addMotorTypeFromDeprecatedAnnotation(Class cls) {
        if (!cls.isAnnotationPresent(MotorType.class)) {
            return false;
        }
        MotorType motorType = (MotorType) cls.getAnnotation(MotorType.class);
        MotorConfigurationType motorConfigurationType = new MotorConfigurationType(cls, getXmlTag(motorType));
        motorConfigurationType.processAnnotation(motorType);
        processMotorSupportAnnotations(cls, motorConfigurationType);
        motorConfigurationType.finishedAnnotations(cls);
        if (!checkAnnotationParameterConstraints(motorConfigurationType)) {
            return false;
        }
        add(motorConfigurationType);
        return true;
    }

    private boolean addI2cTypeFromDeprecatedAnnotation(Class cls) {
        if (!isHardwareDevice(cls) || !cls.isAnnotationPresent(I2cSensor.class)) {
            return false;
        }
        I2cSensor i2cSensor = (I2cSensor) cls.getAnnotation(I2cSensor.class);
        I2cDeviceConfigurationType i2cDeviceConfigurationType = new I2cDeviceConfigurationType(cls, getXmlTag(i2cSensor));
        i2cDeviceConfigurationType.processAnnotation(i2cSensor);
        i2cDeviceConfigurationType.finishedAnnotations(cls);
        if (!checkInstantiableTypeConstraints(i2cDeviceConfigurationType)) {
            return false;
        }
        add(i2cDeviceConfigurationType);
        return true;
    }

    private void processMotorSupportAnnotations(Class<?> cls, MotorConfigurationType motorConfigurationType) {
        motorConfigurationType.processAnnotation((DistributorInfo) findAnnotation(cls, DistributorInfo.class));
        processNewOldAnnotations(motorConfigurationType, cls, ExpansionHubPIDFVelocityParams.class, ExpansionHubMotorControllerVelocityParams.class);
        processNewOldAnnotations(motorConfigurationType, cls, ExpansionHubPIDFPositionParams.class, ExpansionHubMotorControllerPositionParams.class);
    }

    /* access modifiers changed from: protected */
    public <NewType extends Annotation, OldType extends Annotation> void processNewOldAnnotations(final MotorConfigurationType motorConfigurationType, final Class<?> cls, final Class<NewType> cls2, final Class<OldType> cls3) {
        if (!ClassUtil.searchInheritance(cls, new Predicate<Class<?>>() {
            public boolean test(Class<?> cls) {
                return ConfigurationTypeManager.this.processAnnotationIfPresent(motorConfigurationType, cls, cls2);
            }
        })) {
            ClassUtil.searchInheritance(cls, new Predicate<Class<?>>() {
                public boolean test(Class<?> cls) {
                    return ConfigurationTypeManager.this.processAnnotationIfPresent(motorConfigurationType, cls, cls3);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public <A extends Annotation> boolean processAnnotationIfPresent(MotorConfigurationType motorConfigurationType, Class<?> cls, Class<A> cls2) {
        Annotation annotation = cls.getAnnotation(cls2);
        if (annotation == null) {
            return false;
        }
        motorConfigurationType.processAnnotation((Object) annotation);
        return true;
    }

    private Annotation getTypeAnnotation(Class cls) {
        for (Annotation annotation : cls.getAnnotations()) {
            if (typeAnnotationsList.contains(annotation.annotationType())) {
                return annotation;
            }
        }
        return null;
    }

    private <A extends Annotation> A findAnnotation(Class<?> cls, final Class<A> cls2) {
        final ArrayList arrayList = new ArrayList(1);
        arrayList.add((Object) null);
        ClassUtil.searchInheritance(cls, new Predicate<Class<?>>() {
            public boolean test(Class<?> cls) {
                Annotation annotation = cls.getAnnotation(cls2);
                if (annotation == null) {
                    return false;
                }
                arrayList.set(0, annotation);
                return true;
            }
        });
        return (Annotation) arrayList.get(0);
    }

    private boolean checkAnnotationParameterConstraints(UserConfigurationType userConfigurationType) {
        if (!isLegalDeviceTypeName(userConfigurationType.getName())) {
            reportConfigurationError("\"%s\" is not a legal device type name", userConfigurationType.getName());
            return false;
        } else if (this.existingTypeDisplayNamesMap.get(userConfigurationType.getDeviceFlavor()).contains(userConfigurationType.getName())) {
            reportConfigurationError("the device type \"%s\" is already defined", userConfigurationType.getName());
            return false;
        } else if (!isLegalXmlTag(userConfigurationType.getXmlTag())) {
            reportConfigurationError("\"%s\" is not a legal XML tag for the device type \"%s\"", userConfigurationType.getXmlTag(), userConfigurationType.getName());
            return false;
        } else if (!this.existingXmlTags.contains(userConfigurationType.getXmlTag())) {
            return true;
        } else {
            reportConfigurationError("the XML tag \"%s\" is already defined", userConfigurationType.getXmlTag());
            return false;
        }
    }

    private boolean checkInstantiableTypeConstraints(InstantiableUserConfigurationType instantiableUserConfigurationType) {
        if (!checkAnnotationParameterConstraints(instantiableUserConfigurationType)) {
            return false;
        }
        if (!isHardwareDevice(instantiableUserConfigurationType.getClazz())) {
            reportConfigurationError("'%s' class doesn't inherit from the class 'HardwareDevice'", instantiableUserConfigurationType.getClazz().getSimpleName());
            return false;
        } else if (!Modifier.isPublic(instantiableUserConfigurationType.getClazz().getModifiers())) {
            reportConfigurationError("'%s' class is not declared 'public'", instantiableUserConfigurationType.getClazz().getSimpleName());
            return false;
        } else if (instantiableUserConfigurationType.hasConstructors()) {
            return true;
        } else {
            reportConfigurationError("'%s' class lacks necessary constructor", instantiableUserConfigurationType.getClazz().getSimpleName());
            return false;
        }
    }

    private boolean isLegalDeviceTypeName(String str) {
        return Util.isGoodString(str);
    }

    public static String getXmlTag(Class cls) {
        return getXmlTag((DeviceProperties) cls.getAnnotation(DeviceProperties.class));
    }

    private void reportConfigurationError(String str, Object... objArr) {
        String format = String.format(str, objArr);
        RobotLog.m48ee(TAG, String.format("configuration error: %s", new Object[]{format}));
        RobotLog.setGlobalErrorMsg(format);
    }

    private boolean isHardwareDevice(Class cls) {
        return ClassUtil.inheritsFrom(cls, HardwareDevice.class);
    }

    private boolean isLegalXmlTag(String str) {
        if (!Util.isGoodString(str)) {
            return false;
        }
        return str.matches("^[" + "\\p{Alpha}_:" + "][" + "\\p{Alpha}_:0-9\\-\\." + "]*$");
    }

    private static String getXmlTag(I2cSensor i2cSensor) {
        return ClassUtil.decodeStringRes(i2cSensor.xmlTag().trim());
    }

    private static String getXmlTag(MotorType motorType) {
        return ClassUtil.decodeStringRes(motorType.xmlTag().trim());
    }

    private static String getXmlTag(DeviceProperties deviceProperties) {
        return ClassUtil.decodeStringRes(deviceProperties.xmlTag().trim());
    }
}
