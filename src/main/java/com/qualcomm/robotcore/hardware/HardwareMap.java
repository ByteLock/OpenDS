package com.qualcomm.robotcore.hardware;

import android.content.Context;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.firstinspires.ftc.robotcore.system.Assert;

public class HardwareMap implements Iterable<HardwareDevice> {
    private static final String LOG_FORMAT = "%-50s %-30s %s";
    private static final String TAG = "HardwareMap";
    private static final Class<I2cDeviceSynchDevice> i2cDriverBaseClass = I2cDeviceSynchDevice.class;
    public DeviceMapping<AccelerationSensor> accelerationSensor = new DeviceMapping<>(AccelerationSensor.class);
    public final List<DeviceMapping<? extends HardwareDevice>> allDeviceMappings;
    protected List<HardwareDevice> allDevicesList = null;
    protected Map<String, List<HardwareDevice>> allDevicesMap = new HashMap();
    public DeviceMapping<AnalogInput> analogInput = new DeviceMapping<>(AnalogInput.class);
    public final Context appContext;
    public DeviceMapping<ColorSensor> colorSensor = new DeviceMapping<>(ColorSensor.class);
    public DeviceMapping<CompassSensor> compassSensor = new DeviceMapping<>(CompassSensor.class);
    public DeviceMapping<CRServo> crservo = new DeviceMapping<>(CRServo.class);
    public DeviceMapping<DcMotor> dcMotor = new DeviceMapping<>(DcMotor.class);
    public DeviceMapping<DcMotorController> dcMotorController = new DeviceMapping<>(DcMotorController.class);
    protected Map<HardwareDevice, Set<String>> deviceNames = new HashMap();
    public DeviceMapping<DigitalChannel> digitalChannel = new DeviceMapping<>(DigitalChannel.class);
    public DeviceMapping<GyroSensor> gyroSensor = new DeviceMapping<>(GyroSensor.class);
    public DeviceMapping<I2cDevice> i2cDevice = new DeviceMapping<>(I2cDevice.class);
    public DeviceMapping<I2cDeviceSynch> i2cDeviceSynch = new DeviceMapping<>(I2cDeviceSynch.class);
    public DeviceMapping<IrSeekerSensor> irSeekerSensor = new DeviceMapping<>(IrSeekerSensor.class);
    public DeviceMapping<LED> led = new DeviceMapping<>(LED.class);
    public DeviceMapping<LightSensor> lightSensor = new DeviceMapping<>(LightSensor.class);
    protected final Object lock = new Object();
    public DeviceMapping<OpticalDistanceSensor> opticalDistanceSensor = new DeviceMapping<>(OpticalDistanceSensor.class);
    public DeviceMapping<PWMOutput> pwmOutput = new DeviceMapping<>(PWMOutput.class);
    protected Map<SerialNumber, HardwareDevice> serialNumberMap = new HashMap();
    public DeviceMapping<Servo> servo = new DeviceMapping<>(Servo.class);
    public DeviceMapping<ServoController> servoController = new DeviceMapping<>(ServoController.class);
    public DeviceMapping<TouchSensor> touchSensor = new DeviceMapping<>(TouchSensor.class);
    public DeviceMapping<TouchSensorMultiplexer> touchSensorMultiplexer = new DeviceMapping<>(TouchSensorMultiplexer.class);
    public DeviceMapping<UltrasonicSensor> ultrasonicSensor = new DeviceMapping<>(UltrasonicSensor.class);
    public DeviceMapping<VoltageSensor> voltageSensor = new DeviceMapping<>(VoltageSensor.class);

    public HardwareMap(Context context) {
        this.appContext = context;
        ArrayList arrayList = new ArrayList(30);
        this.allDeviceMappings = arrayList;
        arrayList.add(this.dcMotorController);
        arrayList.add(this.dcMotor);
        arrayList.add(this.servoController);
        arrayList.add(this.servo);
        arrayList.add(this.crservo);
        arrayList.add(this.touchSensorMultiplexer);
        arrayList.add(this.analogInput);
        arrayList.add(this.digitalChannel);
        arrayList.add(this.opticalDistanceSensor);
        arrayList.add(this.touchSensor);
        arrayList.add(this.pwmOutput);
        arrayList.add(this.i2cDevice);
        arrayList.add(this.i2cDeviceSynch);
        arrayList.add(this.colorSensor);
        arrayList.add(this.led);
        arrayList.add(this.accelerationSensor);
        arrayList.add(this.compassSensor);
        arrayList.add(this.gyroSensor);
        arrayList.add(this.irSeekerSensor);
        arrayList.add(this.lightSensor);
        arrayList.add(this.ultrasonicSensor);
        arrayList.add(this.voltageSensor);
    }

    public <T> T get(Class<? extends T> cls, String str) {
        T tryGet;
        synchronized (this.lock) {
            String trim = str.trim();
            tryGet = tryGet(cls, trim);
            if (tryGet == null) {
                throw new IllegalArgumentException(String.format("Unable to find a hardware device with name \"%s\" and type %s", new Object[]{trim, cls.getSimpleName()}));
            }
        }
        return tryGet;
    }

    public <T> T tryGet(Class<? extends T> cls, String str) {
        T t;
        synchronized (this.lock) {
            List list = this.allDevicesMap.get(str.trim());
            t = null;
            if (list != null) {
                Iterator it = list.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    HardwareDevice hardwareDevice = (HardwareDevice) it.next();
                    if (cls.isInstance(hardwareDevice)) {
                        initializeDeviceIfNecessary(hardwareDevice);
                        t = cls.cast(hardwareDevice);
                        break;
                    }
                }
            }
            if (t == null && (cls.getSimpleName().contains("BNO055") || cls.getSimpleName().contains(LynxConstants.EMBEDDED_BNO055_IMU_XML_TAG))) {
                Iterator<HardwareDevice> it2 = iterator();
                while (true) {
                    if (it2.hasNext()) {
                        if (it2.next().getClass().getSimpleName().contains("BHI260")) {
                            RobotLog.addGlobalWarningMessage("You attempted to use a BNO055 IMU when only a BHI260AP IMU is configured. Most likely, this Control Hub contains a BHI260AP IMU, and you need to migrate your IMU code to the new driver when it becomes available in version 8.1 of the FTC Robot Controller app.");
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return t;
    }

    public <T> T get(Class<? extends T> cls, SerialNumber serialNumber) {
        synchronized (this.lock) {
            HardwareDevice hardwareDevice = this.serialNumberMap.get(serialNumber);
            if (hardwareDevice == null || !cls.isInstance(hardwareDevice)) {
                return null;
            }
            initializeDeviceIfNecessary(hardwareDevice);
            T cast = cls.cast(hardwareDevice);
            return cast;
        }
    }

    public HardwareDevice get(String str) {
        HardwareDevice hardwareDevice;
        synchronized (this.lock) {
            String trim = str.trim();
            List list = this.allDevicesMap.get(trim);
            if (list != null) {
                Iterator it = list.iterator();
                if (it.hasNext()) {
                    hardwareDevice = (HardwareDevice) it.next();
                    initializeDeviceIfNecessary(hardwareDevice);
                }
            }
            throw new IllegalArgumentException(String.format("Unable to find a hardware device with name \"%s\"", new Object[]{trim}));
        }
        return hardwareDevice;
    }

    public <T> List<T> getAll(Class<? extends T> cls) {
        LinkedList linkedList;
        synchronized (this.lock) {
            linkedList = new LinkedList();
            for (HardwareDevice next : unsafeIterable()) {
                if (cls.isInstance(next)) {
                    initializeDeviceIfNecessary(next);
                    linkedList.add(cls.cast(next));
                }
            }
        }
        return linkedList;
    }

    public void put(String str, HardwareDevice hardwareDevice) {
        internalPut((SerialNumber) null, str, hardwareDevice);
    }

    public void put(SerialNumber serialNumber, String str, HardwareDevice hardwareDevice) {
        Assert.assertNotNull(serialNumber);
        internalPut(serialNumber, str, hardwareDevice);
    }

    /* access modifiers changed from: protected */
    public void internalPut(SerialNumber serialNumber, String str, HardwareDevice hardwareDevice) {
        synchronized (this.lock) {
            String trim = str.trim();
            List list = this.allDevicesMap.get(trim);
            if (list == null) {
                list = new ArrayList(1);
                this.allDevicesMap.put(trim, list);
            }
            if (!list.contains(hardwareDevice)) {
                this.allDevicesList = null;
                list.add(hardwareDevice);
            }
            if (serialNumber != null) {
                this.serialNumberMap.put(serialNumber, hardwareDevice);
            }
            rebuildDeviceNamesIfNecessary();
            recordDeviceName(trim, hardwareDevice);
        }
    }

    public boolean remove(String str, HardwareDevice hardwareDevice) {
        return remove((SerialNumber) null, str, hardwareDevice);
    }

    public boolean remove(SerialNumber serialNumber, String str, HardwareDevice hardwareDevice) {
        synchronized (this.lock) {
            String trim = str.trim();
            List list = this.allDevicesMap.get(trim);
            if (list == null) {
                return false;
            }
            list.remove(hardwareDevice);
            if (list.isEmpty()) {
                this.allDevicesMap.remove(trim);
            }
            this.allDevicesList = null;
            this.deviceNames = null;
            if (serialNumber != null) {
                this.serialNumberMap.remove(serialNumber);
            }
            return true;
        }
    }

    public Set<String> getNamesOf(HardwareDevice hardwareDevice) {
        Set<String> set;
        synchronized (this.lock) {
            rebuildDeviceNamesIfNecessary();
            set = this.deviceNames.get(hardwareDevice);
            if (set == null) {
                set = new HashSet<>();
            }
        }
        return set;
    }

    /* access modifiers changed from: protected */
    public void recordDeviceName(String str, HardwareDevice hardwareDevice) {
        String trim = str.trim();
        Set set = this.deviceNames.get(hardwareDevice);
        if (set == null) {
            set = new HashSet();
            this.deviceNames.put(hardwareDevice, set);
        }
        set.add(trim);
    }

    /* access modifiers changed from: protected */
    public void rebuildDeviceNamesIfNecessary() {
        if (this.deviceNames == null) {
            this.deviceNames = new ConcurrentHashMap();
            for (Map.Entry next : this.allDevicesMap.entrySet()) {
                for (HardwareDevice recordDeviceName : (List) next.getValue()) {
                    recordDeviceName((String) next.getKey(), recordDeviceName);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void buildAllDevicesList() {
        if (this.allDevicesList == null) {
            HashSet hashSet = new HashSet();
            for (String str : this.allDevicesMap.keySet()) {
                hashSet.addAll(this.allDevicesMap.get(str));
            }
            this.allDevicesList = new ArrayList(hashSet);
        }
    }

    public int size() {
        int size;
        synchronized (this.lock) {
            buildAllDevicesList();
            size = this.allDevicesList.size();
        }
        return size;
    }

    public Iterator<HardwareDevice> iterator() {
        Iterator<HardwareDevice> it;
        RobotLog.m68ww(TAG, (Throwable) new RuntimeException(), "HardwareMap iterator was used, which blindly initializes all uninitialized devices");
        synchronized (this.lock) {
            buildAllDevicesList();
            initializeMultipleDevicesIfNecessary(this.allDevicesList);
            it = new ArrayList(this.allDevicesList).iterator();
        }
        return it;
    }

    public Iterable<HardwareDevice> unsafeIterable() {
        return new Iterable<HardwareDevice>() {
            public Iterator<HardwareDevice> iterator() {
                Iterator<HardwareDevice> it;
                synchronized (HardwareMap.this.lock) {
                    HardwareMap.this.buildAllDevicesList();
                    it = new ArrayList(HardwareMap.this.allDevicesList).iterator();
                }
                return it;
            }
        };
    }

    /* access modifiers changed from: private */
    public void initializeDeviceIfNecessary(HardwareDevice hardwareDevice) {
        Class<I2cDeviceSynchDevice> cls = i2cDriverBaseClass;
        if (cls.isAssignableFrom(hardwareDevice.getClass())) {
            cls.cast(hardwareDevice).initializeIfNecessary();
        }
    }

    /* access modifiers changed from: private */
    public void initializeMultipleDevicesIfNecessary(Iterable<? extends HardwareDevice> iterable) {
        for (HardwareDevice initializeDeviceIfNecessary : iterable) {
            initializeDeviceIfNecessary(initializeDeviceIfNecessary);
        }
    }

    public class DeviceMapping<DEVICE_TYPE extends HardwareDevice> implements Iterable<DEVICE_TYPE> {
        private final Class<DEVICE_TYPE> deviceTypeClass;
        private final Map<String, DEVICE_TYPE> map = new HashMap();

        public DeviceMapping(Class<DEVICE_TYPE> cls) {
            this.deviceTypeClass = cls;
        }

        public Class<DEVICE_TYPE> getDeviceTypeClass() {
            return this.deviceTypeClass;
        }

        public DEVICE_TYPE cast(Object obj) {
            return (HardwareDevice) this.deviceTypeClass.cast(obj);
        }

        public DEVICE_TYPE get(String str) {
            DEVICE_TYPE device_type;
            synchronized (HardwareMap.this.lock) {
                String trim = str.trim();
                device_type = (HardwareDevice) this.map.get(trim);
                if (device_type != null) {
                    HardwareMap.this.initializeDeviceIfNecessary(device_type);
                } else {
                    throw new IllegalArgumentException(String.format("Unable to find a hardware device with the name \"%s\"", new Object[]{trim}));
                }
            }
            return device_type;
        }

        public void put(String str, DEVICE_TYPE device_type) {
            internalPut((SerialNumber) null, str, device_type);
        }

        public void put(SerialNumber serialNumber, String str, DEVICE_TYPE device_type) {
            internalPut(serialNumber, str, device_type);
        }

        /* access modifiers changed from: protected */
        public void internalPut(SerialNumber serialNumber, String str, DEVICE_TYPE device_type) {
            synchronized (HardwareMap.this.lock) {
                String trim = str.trim();
                remove(serialNumber, trim);
                HardwareMap.this.internalPut(serialNumber, trim, device_type);
                putLocal(trim, device_type);
            }
        }

        public void putLocal(String str, DEVICE_TYPE device_type) {
            synchronized (HardwareMap.this.lock) {
                this.map.put(str.trim(), device_type);
            }
        }

        public boolean contains(String str) {
            boolean containsKey;
            synchronized (HardwareMap.this.lock) {
                containsKey = this.map.containsKey(str.trim());
            }
            return containsKey;
        }

        public boolean remove(String str) {
            return remove((SerialNumber) null, str);
        }

        public boolean remove(SerialNumber serialNumber, String str) {
            synchronized (HardwareMap.this.lock) {
                String trim = str.trim();
                HardwareDevice hardwareDevice = (HardwareDevice) this.map.remove(trim);
                if (hardwareDevice == null) {
                    return false;
                }
                HardwareMap.this.remove(serialNumber, trim, hardwareDevice);
                return true;
            }
        }

        public Iterator<DEVICE_TYPE> iterator() {
            Iterator<DEVICE_TYPE> it;
            synchronized (HardwareMap.this.lock) {
                HardwareMap.this.initializeMultipleDevicesIfNecessary(this.map.values());
                it = new ArrayList(this.map.values()).iterator();
            }
            return it;
        }

        public Set<Map.Entry<String, DEVICE_TYPE>> entrySet() {
            HashSet hashSet;
            synchronized (HardwareMap.this.lock) {
                HardwareMap.this.initializeMultipleDevicesIfNecessary(this.map.values());
                hashSet = new HashSet(this.map.entrySet());
            }
            return hashSet;
        }

        public int size() {
            int size;
            synchronized (HardwareMap.this.lock) {
                size = this.map.size();
            }
            return size;
        }
    }

    public void logDevices() {
        RobotLog.m52i("========= Device Information ===================================================");
        RobotLog.m52i(String.format(LOG_FORMAT, new Object[]{"Type", "Name", "Connection"}));
        for (Map.Entry next : this.allDevicesMap.entrySet()) {
            for (HardwareDevice hardwareDevice : (List) next.getValue()) {
                RobotLog.m52i(String.format(LOG_FORMAT, new Object[]{hardwareDevice.getDeviceName(), (String) next.getKey(), hardwareDevice.getConnectionInfo()}));
            }
        }
    }
}
