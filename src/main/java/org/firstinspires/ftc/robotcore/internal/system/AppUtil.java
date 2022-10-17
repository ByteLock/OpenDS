package org.firstinspires.ftc.robotcore.internal.system;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.BuildConfig;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.WeakReferenceSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.firstinspires.ftc.robotcore.external.Predicate;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.function.Continuation;
import org.firstinspires.ftc.robotcore.external.function.ContinuationResult;
import org.firstinspires.ftc.robotcore.internal.collections.MutableReference;
import org.firstinspires.ftc.robotcore.internal.files.MediaTransferProtocolMonitor;
import org.firstinspires.ftc.robotcore.internal.network.CallbackLooper;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.p013ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.internal.p013ui.UILocation;
import org.firstinspires.ftc.robotcore.internal.webserver.websockets.FtcWebSocketMessage;
import org.firstinspires.ftc.robotcore.internal.webserver.websockets.WebSocketManager;
import org.firstinspires.inspection.InspectionState;
import org.threeten.p014bp.Month;
import org.threeten.p014bp.Year;
import org.threeten.p014bp.YearMonth;
import org.threeten.p014bp.format.DateTimeFormatter;
import org.threeten.p014bp.format.DateTimeParseException;

public class AppUtil {
    public static final String BLOCKS_BLK_EXT = ".blk";
    public static final String BLOCKS_JS_EXT = ".js";
    public static final File BLOCKS_SOUNDS_DIR;
    public static final File BLOCK_OPMODES_DIR;
    public static final File CONFIG_FILES_DIR;
    public static final String DISMISS_PROGRESS_MSG = "dismissProgress";
    public static final File FIRST_FOLDER;
    public static final File LOG_FOLDER;
    public static final File LYNX_FIRMWARE_UPDATE_DIR;
    public static final File MATCH_LOG_FOLDER;
    public static final int MAX_MATCH_LOGS_TO_KEEP = 9;
    public static final File OTA_UPDATE_DIR = new File(Environment.getExternalStorageDirectory(), "/OTA-Updates");
    public static final String PROGRESS_NAMESPACE = "progress";
    public static final File RC_APP_UPDATE_DIR;
    public static final File ROBOT_DATA_DIR;
    public static final File ROBOT_SETTINGS;
    public static final File ROOT_FOLDER;
    public static final String SHOW_PROGRESS_MSG = "showProgress";
    public static final File SOUNDS_CACHE;
    public static final File SOUNDS_DIR;
    public static final String TAG = "AppUtil";
    public static final File TFLITE_MODELS_DIR;
    public static final File UPDATES_DIR;
    public static final File WEBCAM_CALIBRATIONS_DIR;
    private Application application;
    /* access modifiers changed from: private */
    public Activity currentActivity;
    /* access modifiers changed from: private */
    public ProgressDialog currentProgressDialog;
    /* access modifiers changed from: private */
    public Map<String, DialogContext> dialogContextMap = new ConcurrentHashMap();
    private final Object dialogLock = new Object();
    private DateTimeFormatter iso8601DateFormat;
    private LifeCycleMonitor lifeCycleMonitor;
    private Random random;
    /* access modifiers changed from: private */
    public final Lock requestPermissionLock = new ReentrantLock();
    /* access modifiers changed from: private */
    public Activity rootActivity;
    private final Object timeLock = new Object();
    private String usbFileSystemRoot;
    /* access modifiers changed from: private */
    public UsbManager usbManager;
    private final WeakReferenceSet<UsbFileSystemRootListener> usbfsListeners = new WeakReferenceSet<>();
    private final Object usbfsRootLock = new Object();
    private WebSocketManager webSocketManager;

    public enum DialogFlavor {
        ALERT,
        CONFIRM,
        PROMPT
    }

    public interface UsbFileSystemRootListener {
        void onUsbFileSystemRootChanged(String str);
    }

    private native boolean nativeSetCurrentTimeMillis(long j);

    static {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        ROOT_FOLDER = externalStorageDirectory;
        File file = new File(externalStorageDirectory + "/FIRST/");
        FIRST_FOLDER = file;
        LOG_FOLDER = externalStorageDirectory;
        MATCH_LOG_FOLDER = new File(file + "/matchlogs/");
        CONFIG_FILES_DIR = file;
        File file2 = new File(file, "/blocks/");
        BLOCK_OPMODES_DIR = file2;
        BLOCKS_SOUNDS_DIR = new File(file2, "/sounds/");
        ROBOT_SETTINGS = new File(file, "/settings/");
        ROBOT_DATA_DIR = new File(file, "/data/");
        File file3 = new File(file, "/updates/");
        UPDATES_DIR = file3;
        RC_APP_UPDATE_DIR = new File(file3, "/Robot Controller Application/");
        LYNX_FIRMWARE_UPDATE_DIR = new File(file3, "/Expansion Hub Firmware/");
        File file4 = new File(file, "sounds");
        SOUNDS_DIR = file4;
        SOUNDS_CACHE = new File(file4, "cache");
        WEBCAM_CALIBRATIONS_DIR = new File(file + "/webcamcalibrations/");
        TFLITE_MODELS_DIR = new File(file + "/tflitemodels/");
        System.loadLibrary(RobotLog.TAG);
    }

    private static class InstanceHolder {
        public static AppUtil theInstance = new AppUtil();

        private InstanceHolder() {
        }
    }

    public static AppUtil getInstance() {
        return InstanceHolder.theInstance;
    }

    public static Application getDefContext() {
        return getInstance().getApplication();
    }

    public static void onApplicationStart(Application application2) {
        getInstance().initialize(application2);
    }

    protected AppUtil() {
    }

    /* access modifiers changed from: protected */
    public void initialize(Application application2) {
        this.usbManager = (UsbManager) application2.getSystemService("usb");
        this.lifeCycleMonitor = new LifeCycleMonitor();
        this.rootActivity = null;
        this.currentActivity = null;
        this.currentProgressDialog = null;
        this.random = new Random();
        application2.registerActivityLifecycleCallbacks(this.lifeCycleMonitor);
        this.application = application2;
        RobotLog.m61vv(TAG, "initializing: getExternalStorageDirectory()=%s", Environment.getExternalStorageDirectory());
        this.usbFileSystemRoot = null;
        getUsbFileSystemRoot();
        AppAliveNotifier.getInstance().onAppStartup();
        AndroidThreeTen.init(application2);
    }

    public File getRelativePath(File file, File file2) {
        File file3 = new File(InspectionState.NO_VERSION);
        while (!file.equals(file2)) {
            File parentFile = file2.getParentFile();
            File file4 = new File(new File(file2.getName()), file3.getPath());
            if (parentFile == null) {
                return file4;
            }
            file2 = parentFile;
            file3 = file4;
        }
        return file3;
    }

    public void ensureDirectoryExists(File file) {
        ensureDirectoryExists(file, true);
    }

    public void ensureDirectoryExists(File file, boolean z) {
        if (!file.isDirectory()) {
            file.delete();
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                ensureDirectoryExists(parentFile, z);
            }
            if (!file.mkdir()) {
                if (!file.isDirectory()) {
                    RobotLog.m49ee(TAG, "failed to create directory %s", file);
                }
                if (z) {
                    MediaTransferProtocolMonitor.renoticeIndicatorFiles(file);
                }
            } else if (z) {
                MediaTransferProtocolMonitor.makeIndicatorFile(file);
            }
        }
    }

    public void deleteChildren(File file) {
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File delete : listFiles) {
                delete(delete);
            }
        }
    }

    public void delete(File file) {
        deleteChildren(file);
        if (!file.delete()) {
            RobotLog.m49ee(TAG, "failed to delete '%s'", file.getAbsolutePath());
        }
    }

    public List<File> filesUnder(File file) {
        Predicate predicate = null;
        return filesUnder(file, (Predicate<File>) null);
    }

    public List<File> filesUnder(File file, Predicate<File> predicate) {
        ArrayList arrayList = new ArrayList();
        if (file.isDirectory()) {
            for (File filesUnder : file.listFiles()) {
                arrayList.addAll(filesUnder(filesUnder, predicate));
            }
        } else if (file.exists() && (predicate == null || predicate.test(file))) {
            arrayList.add(file.getAbsoluteFile());
        }
        return arrayList;
    }

    public List<File> filesUnder(File file, final String str) {
        return filesUnder(file, (Predicate<File>) new Predicate<File>() {
            public boolean test(File file) {
                return file.getName().endsWith(str);
            }
        });
    }

    public List<File> filesIn(File file) {
        Predicate predicate = null;
        return filesIn(file, (Predicate<File>) null);
    }

    public List<File> filesIn(File file, Predicate<File> predicate) {
        ArrayList arrayList = new ArrayList();
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File file2 : listFiles) {
                if (predicate == null || predicate.test(file2)) {
                    arrayList.add(file2.getAbsoluteFile());
                }
            }
        }
        return arrayList;
    }

    public List<File> filesIn(File file, final String str) {
        return filesIn(file, (Predicate<File>) new Predicate<File>() {
            public boolean test(File file) {
                return file.getName().endsWith(str);
            }
        });
    }

    public File getSettingsFile(String str) {
        File file = new File(str);
        if (file.isAbsolute()) {
            return file;
        }
        File file2 = ROBOT_SETTINGS;
        ensureDirectoryExists(file2);
        return new File(file2, str);
    }

    public void copyFile(File file, File file2) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            copyStream((InputStream) fileInputStream, file2);
        } finally {
            fileInputStream.close();
        }
    }

    public void copyStream(InputStream inputStream, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            copyStream(inputStream, (OutputStream) fileOutputStream);
        } finally {
            fileOutputStream.close();
        }
    }

    public void copyStream(File file, OutputStream outputStream) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            copyStream((InputStream) fileInputStream, outputStream);
        } finally {
            fileInputStream.close();
        }
    }

    public void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[Math.min(4096, inputStream.available())];
        while (true) {
            int read = inputStream.read(bArr);
            if (read > 0) {
                outputStream.write(bArr, 0, read);
            } else {
                return;
            }
        }
    }

    public File createTempFile(String str, String str2, File file) throws IOException {
        return File.createTempFile(str, str2, file);
    }

    public File createTempDirectory(String str, String str2, File file) throws IOException {
        File file2;
        if (str.length() >= 3) {
            if (str2 == null) {
                str2 = ".tmp";
            }
            if (file == null) {
                file = new File(System.getProperty("java.io.tmpdir", "."));
            }
            do {
                file2 = new File(file, str + this.random.nextInt() + str2);
            } while (!file2.mkdir());
            return file2;
        }
        throw new IllegalArgumentException("prefix must be at least 3 characters");
    }

    public String getUsbFileSystemRoot() {
        if (this.usbFileSystemRoot == null) {
            synchronized (this.usbfsRootLock) {
                Iterator<String> it = this.usbManager.getDeviceList().keySet().iterator();
                while (true) {
                    if (it.hasNext()) {
                        String usbFileSystemRootFromDeviceName = usbFileSystemRootFromDeviceName(it.next());
                        if (usbFileSystemRootFromDeviceName != null) {
                            setUsbFileSystemRoot(usbFileSystemRootFromDeviceName);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return this.usbFileSystemRoot;
    }

    public String getNonNullUsbFileSystemRoot() {
        String usbFileSystemRoot2 = getUsbFileSystemRoot();
        return usbFileSystemRoot2 == null ? "/dev/bus/usb" : usbFileSystemRoot2;
    }

    public void setUsbFileSystemRoot(UsbDevice usbDevice) {
        setUsbFileSystemRoot(usbFileSystemRootFromDeviceName(usbDevice.getDeviceName()));
    }

    protected static String usbFileSystemRootFromDeviceName(String str) {
        String[] split = TextUtils.isEmpty(str) ? null : str.split("/");
        if (split == null || split.length <= 2) {
            return null;
        }
        StringBuilder sb = new StringBuilder(split[0]);
        for (int i = 1; i < split.length - 2; i++) {
            sb.append("/");
            sb.append(split[i]);
        }
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public void setUsbFileSystemRoot(String str) {
        if (str != null && this.usbFileSystemRoot == null) {
            synchronized (this.usbfsRootLock) {
                if (this.usbFileSystemRoot == null) {
                    RobotLog.m55ii(TAG, "found usbFileSystemRoot: %s", str);
                    this.usbFileSystemRoot = str;
                    notifyUsbListeners(str);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyUsbListeners(String str) {
        ArrayList<UsbFileSystemRootListener> arrayList;
        synchronized (this.usbfsListeners) {
            arrayList = new ArrayList<>(this.usbfsListeners);
        }
        for (UsbFileSystemRootListener onUsbFileSystemRootChanged : arrayList) {
            onUsbFileSystemRootChanged.onUsbFileSystemRootChanged(str);
        }
    }

    public void addUsbfsListener(UsbFileSystemRootListener usbFileSystemRootListener) {
        synchronized (this.usbfsListeners) {
            this.usbfsListeners.add(usbFileSystemRootListener);
        }
    }

    public void removeUsbfsListener(UsbFileSystemRootListener usbFileSystemRootListener) {
        synchronized (this.usbfsListeners) {
            this.usbfsListeners.remove(usbFileSystemRootListener);
        }
    }

    public static String computeMd5(File file) throws NoSuchAlgorithmException, IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            byte[] bArr = new byte[256];
            while (true) {
                int read = fileInputStream.read(bArr);
                if (read < 0) {
                    break;
                }
                instance.update(bArr, 0, read);
            }
            byte[] digest = instance.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                sb.append(String.format(Locale.ROOT, "%02x", new Object[]{Byte.valueOf(digest[i])}));
            }
            return sb.toString();
        } finally {
            fileInputStream.close();
        }
    }

    public void restartApp(int i) {
        RobotLog.m60vv(TAG, "restarting app");
        ((AlarmManager) this.rootActivity.getSystemService(NotificationCompat.CATEGORY_ALARM)).setExact(3, SystemClock.elapsedRealtime() + ((long) 1500), PendingIntent.getActivity(getApplication().getBaseContext(), 0, new Intent(this.rootActivity.getIntent()), this.rootActivity.getIntent().getFlags()));
        System.exit(i);
    }

    public void exitApplication(int i) {
        RobotLog.m63vv(TAG, new RuntimeException(), "exitApplication(%d) was called. Printing stacktrace.", Integer.valueOf(i));
        System.exit(i);
    }

    public void exitApplication() {
        exitApplication(0);
    }

    public Application getApplication() {
        return this.application;
    }

    public String getApplicationId() {
        return getApplication().getPackageName();
    }

    public boolean isRobotController() {
        return getApplicationId().equals(getDefContext().getString(C0705R.string.packageNameRobotController));
    }

    public boolean isDriverStation() {
        return getApplicationId().equals(getDefContext().getString(C0705R.string.packageNameDriverStation));
    }

    public String getAppName() {
        if (isRobotController()) {
            return getDefContext().getString(C0705R.string.appNameRobotController);
        }
        if (isDriverStation()) {
            return getDefContext().getString(C0705R.string.appNameDriverStation);
        }
        return getDefContext().getString(C0705R.string.appNameUnknown);
    }

    public String getRemoteAppName() {
        if (isRobotController()) {
            return getDefContext().getString(C0705R.string.appNameDriverStation);
        }
        if (isDriverStation()) {
            return getDefContext().getString(C0705R.string.appNameRobotController);
        }
        return getDefContext().getString(C0705R.string.appNameUnknown);
    }

    public static int getColor(int i) {
        return getDefContext().getColor(i);
    }

    public void synchronousRunOnUiThread(Runnable runnable) {
        synchronousRunOnUiThread(getActivity(), runnable);
    }

    public void synchronousRunOnUiThread(Activity activity, final Runnable runnable) {
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    runnable.run();
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
    }

    public void runOnUiThread(Runnable runnable) {
        runOnUiThread(getActivity(), runnable);
    }

    public void runOnUiThread(Activity activity, Runnable runnable) {
        activity.runOnUiThread(runnable);
    }

    public void showWaitCursor(String str, Runnable runnable) {
        showWaitCursor(str, runnable, (Runnable) null);
    }

    public void showWaitCursor(final String str, final Runnable runnable, final Runnable runnable2) {
        runOnUiThread(new Runnable() {
            public void run() {
                new AsyncTask<Object, Void, Void>() {
                    ProgressDialog dialog;

                    /* access modifiers changed from: protected */
                    public void onPreExecute() {
                        ProgressDialog progressDialog = new ProgressDialog(AppUtil.this.getActivity());
                        this.dialog = progressDialog;
                        progressDialog.setMessage(str);
                        this.dialog.setIndeterminate(true);
                        this.dialog.setCancelable(false);
                        this.dialog.show();
                    }

                    /* access modifiers changed from: protected */
                    public Void doInBackground(Object... objArr) {
                        runnable.run();
                        return null;
                    }

                    /* access modifiers changed from: protected */
                    public void onPostExecute(Void voidR) {
                        this.dialog.dismiss();
                        if (runnable2 != null) {
                            runnable2.run();
                        }
                    }
                }.execute(new Object[0]);
            }
        });
    }

    public void setWebSocketManager(WebSocketManager webSocketManager2) {
        this.webSocketManager = webSocketManager2;
        webSocketManager2.registerNamespaceAsBroadcastOnly("progress");
    }

    public void showProgress(UILocation uILocation, String str, double d) {
        showProgress(uILocation, str, ProgressParameters.fromFraction(d));
    }

    public void showProgress(UILocation uILocation, String str, double d, int i) {
        showProgress(uILocation, str, ProgressParameters.fromFraction(d, i));
    }

    public void showProgress(UILocation uILocation, String str, ProgressParameters progressParameters) {
        showProgress(uILocation, getActivity(), str, progressParameters);
    }

    public void showProgress(UILocation uILocation, Activity activity, String str, ProgressParameters progressParameters) {
        int i;
        ProgressParameters progressParameters2 = progressParameters;
        final int min = Math.min(progressParameters2.max, 10000);
        if (min == progressParameters2.max) {
            i = progressParameters2.cur;
        } else {
            i = (int) Range.scale((double) progressParameters2.cur, LynxServoController.apiPositionFirst, (double) progressParameters2.max, LynxServoController.apiPositionFirst, (double) min);
        }
        final int i2 = i;
        final Activity activity2 = activity;
        final String str2 = str;
        final ProgressParameters progressParameters3 = progressParameters;
        runOnUiThread(new Runnable() {
            public void run() {
                if (AppUtil.this.currentProgressDialog == null) {
                    ProgressDialog unused = AppUtil.this.currentProgressDialog = new ProgressDialog(activity2);
                    AppUtil.this.currentProgressDialog.setMessage(str2);
                    AppUtil.this.currentProgressDialog.setProgressStyle(1);
                    AppUtil.this.currentProgressDialog.setMax(min);
                    AppUtil.this.currentProgressDialog.setProgress(0);
                    AppUtil.this.currentProgressDialog.setCanceledOnTouchOutside(false);
                    AppUtil.this.currentProgressDialog.show();
                }
                if (progressParameters3.cur == 0) {
                    AppUtil.this.currentProgressDialog.setIndeterminate(true);
                    return;
                }
                AppUtil.this.currentProgressDialog.setIndeterminate(false);
                AppUtil.this.currentProgressDialog.setProgress(i2);
            }
        });
        RobotCoreCommandList.ShowProgress showProgress = new RobotCoreCommandList.ShowProgress();
        showProgress.message = str;
        showProgress.cur = progressParameters2.cur;
        showProgress.max = progressParameters2.max;
        WebSocketManager webSocketManager2 = this.webSocketManager;
        if (webSocketManager2 != null) {
            webSocketManager2.broadcastToNamespace("progress", new FtcWebSocketMessage("progress", SHOW_PROGRESS_MSG, showProgress.serialize()));
        }
        if (uILocation == UILocation.BOTH) {
            NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_SHOW_PROGRESS, showProgress.serialize()));
        }
    }

    public void dismissProgress(UILocation uILocation) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (AppUtil.this.currentProgressDialog != null) {
                    AppUtil.this.currentProgressDialog.dismiss();
                    ProgressDialog unused = AppUtil.this.currentProgressDialog = null;
                }
            }
        });
        WebSocketManager webSocketManager2 = this.webSocketManager;
        if (webSocketManager2 != null) {
            webSocketManager2.broadcastToNamespace("progress", new FtcWebSocketMessage("progress", DISMISS_PROGRESS_MSG));
        }
        if (uILocation == UILocation.BOTH) {
            NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_DISMISS_PROGRESS));
        }
    }

    public void asyncRequestUsbPermission(String str, Context context, UsbDevice usbDevice, Deadline deadline, Handler handler, Consumer<Boolean> consumer) {
        asyncRequestUsbPermission(str, context, usbDevice, deadline, Continuation.create(handler, consumer));
    }

    public void asyncRequestUsbPermission(String str, Context context, UsbDevice usbDevice, Deadline deadline, ExecutorService executorService, Consumer<Boolean> consumer) {
        asyncRequestUsbPermission(str, context, usbDevice, deadline, Continuation.create((Executor) executorService, consumer));
    }

    public void asyncRequestUsbPermission(String str, Context context, UsbDevice usbDevice, Deadline deadline, final Continuation<? extends Consumer<Boolean>> continuation) {
        RobotLog.m60vv(str, "asyncRequestUsbPermission()...");
        try {
            Assert.assertFalse(CallbackLooper.isLooperThread());
            if (this.usbManager.hasPermission(usbDevice)) {
                RobotLog.m43dd(str, "permission already available for %s", usbDevice.getDeviceName());
                continuation.dispatch(new ContinuationResult<Consumer<Boolean>>() {
                    public void handle(Consumer<Boolean> consumer) {
                        consumer.accept(true);
                    }
                });
            } else {
                final MutableReference mutableReference = new MutableReference(false);
                final Deadline deadline2 = deadline;
                final UsbDevice usbDevice2 = usbDevice;
                final MutableReference mutableReference2 = mutableReference;
                final String str2 = str;
                final Context context2 = context;
                final C11788 r1 = new Runnable() {
                    /* JADX WARNING: Can't wrap try/catch for region: R(4:15|16|17|18) */
                    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
                        java.lang.Thread.currentThread().interrupt();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
                        r4 = r7;
                     */
                    /* JADX WARNING: Failed to process nested try/catch */
                    /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0081 */
                    /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x00c7 */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        /*
                            r11 = this;
                            java.lang.String r0 = "USB permission request for %s: result=%s"
                            r1 = 2
                            r2 = 1
                            r3 = 0
                            org.firstinspires.ftc.robotcore.internal.system.Deadline r4 = r3     // Catch:{ InterruptedException -> 0x00c7 }
                            org.firstinspires.ftc.robotcore.internal.system.AppUtil r5 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.this     // Catch:{ InterruptedException -> 0x00c7 }
                            java.util.concurrent.locks.Lock r5 = r5.requestPermissionLock     // Catch:{ InterruptedException -> 0x00c7 }
                            boolean r4 = r4.tryLock(r5)     // Catch:{ InterruptedException -> 0x00c7 }
                            if (r4 == 0) goto L_0x00a6
                            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x009b }
                            r4.<init>()     // Catch:{ all -> 0x009b }
                            java.lang.String r5 = "org.firstinspires.ftc.USB_PERMISSION_REQUEST:"
                            r4.append(r5)     // Catch:{ all -> 0x009b }
                            android.hardware.usb.UsbDevice r5 = r4     // Catch:{ all -> 0x009b }
                            java.lang.String r5 = r5.getDeviceName()     // Catch:{ all -> 0x009b }
                            r4.append(r5)     // Catch:{ all -> 0x009b }
                            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x009b }
                            java.util.concurrent.CountDownLatch r5 = new java.util.concurrent.CountDownLatch     // Catch:{ all -> 0x009b }
                            r5.<init>(r2)     // Catch:{ all -> 0x009b }
                            org.firstinspires.ftc.robotcore.internal.system.AppUtil$8$1 r6 = new org.firstinspires.ftc.robotcore.internal.system.AppUtil$8$1     // Catch:{ all -> 0x009b }
                            r6.<init>(r4, r5)     // Catch:{ all -> 0x009b }
                            android.content.IntentFilter r7 = new android.content.IntentFilter     // Catch:{ all -> 0x009b }
                            r7.<init>(r4)     // Catch:{ all -> 0x009b }
                            android.content.Context r8 = r7     // Catch:{ all -> 0x009b }
                            r9 = 0
                            org.firstinspires.ftc.robotcore.internal.network.CallbackLooper r10 = org.firstinspires.ftc.robotcore.internal.network.CallbackLooper.getDefault()     // Catch:{ all -> 0x009b }
                            android.os.Handler r10 = r10.getHandler()     // Catch:{ all -> 0x009b }
                            r8.registerReceiver(r6, r7, r9, r10)     // Catch:{ all -> 0x009b }
                            android.content.Context r7 = r7     // Catch:{ InterruptedException -> 0x0081 }
                            android.content.Intent r8 = new android.content.Intent     // Catch:{ InterruptedException -> 0x0081 }
                            r8.<init>(r4)     // Catch:{ InterruptedException -> 0x0081 }
                            r4 = 1073741824(0x40000000, float:2.0)
                            android.app.PendingIntent r4 = android.app.PendingIntent.getBroadcast(r7, r3, r8, r4)     // Catch:{ InterruptedException -> 0x0081 }
                            org.firstinspires.ftc.robotcore.internal.system.AppUtil r7 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.this     // Catch:{ InterruptedException -> 0x0081 }
                            android.hardware.usb.UsbManager r7 = r7.usbManager     // Catch:{ InterruptedException -> 0x0081 }
                            android.hardware.usb.UsbDevice r8 = r4     // Catch:{ InterruptedException -> 0x0081 }
                            r7.requestPermission(r8, r4)     // Catch:{ InterruptedException -> 0x0081 }
                            org.firstinspires.ftc.robotcore.internal.system.Deadline r7 = r3     // Catch:{ InterruptedException -> 0x0081 }
                            boolean r5 = r7.await(r5)     // Catch:{ InterruptedException -> 0x0081 }
                            if (r5 == 0) goto L_0x006f
                            java.lang.String r4 = r6     // Catch:{ InterruptedException -> 0x0081 }
                            java.lang.String r5 = "permissionResultAvailable latch awaited"
                            com.qualcomm.robotcore.util.RobotLog.m60vv(r4, r5)     // Catch:{ InterruptedException -> 0x0081 }
                            goto L_0x0079
                        L_0x006f:
                            java.lang.String r5 = r6     // Catch:{ InterruptedException -> 0x0081 }
                            java.lang.String r7 = "requestPermission(): cancelled or timed out waiting for user response"
                            com.qualcomm.robotcore.util.RobotLog.m60vv(r5, r7)     // Catch:{ InterruptedException -> 0x0081 }
                            r4.cancel()     // Catch:{ InterruptedException -> 0x0081 }
                        L_0x0079:
                            android.content.Context r4 = r7     // Catch:{ all -> 0x009b }
                        L_0x007b:
                            r4.unregisterReceiver(r6)     // Catch:{ all -> 0x009b }
                            goto L_0x008b
                        L_0x007f:
                            r4 = move-exception
                            goto L_0x0095
                        L_0x0081:
                            java.lang.Thread r4 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x007f }
                            r4.interrupt()     // Catch:{ all -> 0x007f }
                            android.content.Context r4 = r7     // Catch:{ all -> 0x009b }
                            goto L_0x007b
                        L_0x008b:
                            org.firstinspires.ftc.robotcore.internal.system.AppUtil r4 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.this     // Catch:{ InterruptedException -> 0x00c7 }
                            java.util.concurrent.locks.Lock r4 = r4.requestPermissionLock     // Catch:{ InterruptedException -> 0x00c7 }
                            r4.unlock()     // Catch:{ InterruptedException -> 0x00c7 }
                            goto L_0x00ad
                        L_0x0095:
                            android.content.Context r5 = r7     // Catch:{ all -> 0x009b }
                            r5.unregisterReceiver(r6)     // Catch:{ all -> 0x009b }
                            throw r4     // Catch:{ all -> 0x009b }
                        L_0x009b:
                            r4 = move-exception
                            org.firstinspires.ftc.robotcore.internal.system.AppUtil r5 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.this     // Catch:{ InterruptedException -> 0x00c7 }
                            java.util.concurrent.locks.Lock r5 = r5.requestPermissionLock     // Catch:{ InterruptedException -> 0x00c7 }
                            r5.unlock()     // Catch:{ InterruptedException -> 0x00c7 }
                            throw r4     // Catch:{ InterruptedException -> 0x00c7 }
                        L_0x00a6:
                            java.lang.String r4 = r6     // Catch:{ InterruptedException -> 0x00c7 }
                            java.lang.String r5 = "requestPermission(): requestPermissionLock.tryLock() returned false"
                            com.qualcomm.robotcore.util.RobotLog.m60vv(r4, r5)     // Catch:{ InterruptedException -> 0x00c7 }
                        L_0x00ad:
                            java.lang.String r4 = r6
                            java.lang.Object[] r1 = new java.lang.Object[r1]
                            android.hardware.usb.UsbDevice r5 = r4
                            java.lang.String r5 = r5.getDeviceName()
                            r1[r3] = r5
                            org.firstinspires.ftc.robotcore.internal.collections.MutableReference r3 = r5
                            java.lang.Object r3 = r3.getValue()
                            r1[r2] = r3
                            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r4, (java.lang.String) r0, (java.lang.Object[]) r1)
                            goto L_0x00e5
                        L_0x00c5:
                            r4 = move-exception
                            goto L_0x00e6
                        L_0x00c7:
                            java.lang.Thread r4 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x00c5 }
                            r4.interrupt()     // Catch:{ all -> 0x00c5 }
                            java.lang.String r4 = r6
                            java.lang.Object[] r1 = new java.lang.Object[r1]
                            android.hardware.usb.UsbDevice r5 = r4
                            java.lang.String r5 = r5.getDeviceName()
                            r1[r3] = r5
                            org.firstinspires.ftc.robotcore.internal.collections.MutableReference r3 = r5
                            java.lang.Object r3 = r3.getValue()
                            r1[r2] = r3
                            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r4, (java.lang.String) r0, (java.lang.Object[]) r1)
                        L_0x00e5:
                            return
                        L_0x00e6:
                            java.lang.String r5 = r6
                            java.lang.Object[] r1 = new java.lang.Object[r1]
                            android.hardware.usb.UsbDevice r6 = r4
                            java.lang.String r6 = r6.getDeviceName()
                            r1[r3] = r6
                            org.firstinspires.ftc.robotcore.internal.collections.MutableReference r3 = r5
                            java.lang.Object r3 = r3.getValue()
                            r1[r2] = r3
                            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r5, (java.lang.String) r0, (java.lang.Object[]) r1)
                            throw r4
                        */
                        throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.system.AppUtil.C11788.run():void");
                    }
                };
                if (continuation.isHandler()) {
                    ThreadPool.getDefault().submit(new Runnable() {
                        public void run() {
                            r1.run();
                            continuation.dispatch(new ContinuationResult<Consumer<Boolean>>() {
                                public void handle(Consumer<Boolean> consumer) {
                                    consumer.accept((Boolean) mutableReference.getValue());
                                }
                            });
                        }
                    });
                } else {
                    continuation.createForNewTarget(null).dispatch(new ContinuationResult<Void>() {
                        public void handle(Void voidR) {
                            r1.run();
                            continuation.dispatchHere(new ContinuationResult<Consumer<Boolean>>() {
                                public void handle(Consumer<Boolean> consumer) {
                                    consumer.accept((Boolean) mutableReference.getValue());
                                }
                            });
                        }
                    });
                }
            }
        } finally {
            RobotLog.m60vv(str, "...asyncRequestUsbPermission()");
        }
    }

    public static class DialogContext {
        protected AlertDialog dialog;
        public final CountDownLatch dismissed = new CountDownLatch(1);
        protected EditText input = null;
        protected boolean isArmed = true;
        protected Outcome outcome = Outcome.UNKNOWN;
        protected CharSequence textResult = null;
        protected final String uuidString;

        public enum Outcome {
            UNKNOWN,
            CANCELLED,
            CONFIRMED
        }

        public DialogContext(String str) {
            this.uuidString = str;
        }

        public Outcome getOutcome() {
            return this.outcome;
        }

        public CharSequence getText() {
            return this.textResult;
        }

        public void dismissDialog() {
            AppUtil.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog alertDialog = DialogContext.this.dialog;
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                }
            });
        }
    }

    public static class DialogParams extends MemberwiseCloneable<DialogParams> {
        public Activity activity = AppUtil.getInstance().getActivity();
        public String defaultValue = null;
        public DialogFlavor flavor = DialogFlavor.ALERT;
        public String message;
        public String title;
        public UILocation uiLocation;
        public String uuidString = null;

        public DialogParams(UILocation uILocation, String str, String str2) {
            this.uiLocation = uILocation;
            this.title = str;
            this.message = str2;
        }

        public DialogParams copy() {
            return (DialogParams) super.memberwiseClone();
        }
    }

    public DialogContext showAlertDialog(UILocation uILocation, String str, String str2) {
        return showDialog(new DialogParams(uILocation, str, str2));
    }

    public DialogContext showDialog(DialogParams dialogParams) {
        Continuation continuation = null;
        return showDialog(dialogParams, (Continuation<? extends Consumer<DialogContext>>) null);
    }

    public DialogContext showDialog(DialogParams dialogParams, Consumer<DialogContext> consumer) {
        Handler handler = null;
        return showDialog(dialogParams, (Handler) null, consumer);
    }

    public DialogContext showDialog(DialogParams dialogParams, Handler handler, Consumer<DialogContext> consumer) {
        return showDialog(dialogParams, (Continuation<? extends Consumer<DialogContext>>) consumer != null ? Continuation.create(handler, consumer) : null);
    }

    public DialogContext showDialog(DialogParams dialogParams, Executor executor, Consumer<DialogContext> consumer) {
        return showDialog(dialogParams, (Continuation<? extends Consumer<DialogContext>>) consumer != null ? Continuation.create(executor, consumer) : null);
    }

    public DialogContext showDialog(DialogParams dialogParams, Continuation<? extends Consumer<DialogContext>> continuation) {
        DialogContext dialogContext;
        synchronized (this.dialogLock) {
            DialogParams copy = dialogParams.copy();
            RobotCoreCommandList.ShowDialog showDialog = new RobotCoreCommandList.ShowDialog();
            showDialog.title = copy.title;
            showDialog.message = copy.message;
            showDialog.uuidString = copy.uuidString != null ? copy.uuidString : UUID.randomUUID().toString();
            MutableReference mutableReference = new MutableReference();
            final RobotCoreCommandList.ShowDialog showDialog2 = showDialog;
            final DialogParams dialogParams2 = copy;
            final Continuation<? extends Consumer<DialogContext>> continuation2 = continuation;
            final MutableReference mutableReference2 = mutableReference;
            synchronousRunOnUiThread(new Runnable() {
                /* JADX WARNING: Code restructure failed: missing block: B:5:0x0033, code lost:
                    if (r2 != 3) goto L_0x0089;
                 */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    /*
                        r5 = this;
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogContext r0 = new org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogContext
                        org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList$ShowDialog r1 = r3
                        java.lang.String r1 = r1.uuidString
                        r0.<init>(r1)
                        android.app.AlertDialog$Builder r1 = new android.app.AlertDialog$Builder
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogParams r2 = r4
                        android.app.Activity r2 = r2.activity
                        r1.<init>(r2)
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogParams r2 = r4
                        java.lang.String r2 = r2.title
                        r1.setTitle(r2)
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogParams r2 = r4
                        java.lang.String r2 = r2.message
                        r1.setMessage(r2)
                        int[] r2 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.C117014.f277xb275dcfa
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogParams r3 = r4
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogFlavor r3 = r3.flavor
                        int r3 = r3.ordinal()
                        r2 = r2[r3]
                        r3 = 1
                        if (r2 == r3) goto L_0x007f
                        r4 = 2
                        if (r2 == r4) goto L_0x0036
                        r3 = 3
                        if (r2 == r3) goto L_0x005a
                        goto L_0x0089
                    L_0x0036:
                        android.widget.EditText r2 = new android.widget.EditText
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogParams r4 = r4
                        android.app.Activity r4 = r4.activity
                        r2.<init>(r4)
                        r0.input = r2
                        android.widget.EditText r2 = r0.input
                        r2.setInputType(r3)
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogParams r2 = r4
                        java.lang.String r2 = r2.defaultValue
                        if (r2 == 0) goto L_0x0055
                        android.widget.EditText r2 = r0.input
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogParams r3 = r4
                        java.lang.String r3 = r3.defaultValue
                        r2.setText(r3)
                    L_0x0055:
                        android.widget.EditText r2 = r0.input
                        r1.setView(r2)
                    L_0x005a:
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogParams r2 = r4
                        org.firstinspires.ftc.robotcore.internal.ui.UILocation r2 = r2.uiLocation
                        org.firstinspires.ftc.robotcore.internal.ui.UILocation r3 = org.firstinspires.ftc.robotcore.internal.p013ui.UILocation.ONLY_LOCAL
                        if (r2 != r3) goto L_0x0077
                        int r2 = com.qualcomm.robotcore.C0705R.string.buttonNameOK
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$2 r3 = new org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$2
                        r3.<init>(r0)
                        r1.setPositiveButton(r2, r3)
                        int r2 = com.qualcomm.robotcore.C0705R.string.buttonNameCancel
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$3 r3 = new org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$3
                        r3.<init>(r0)
                        r1.setNegativeButton(r2, r3)
                        goto L_0x0089
                    L_0x0077:
                        java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
                        java.lang.String r1 = "remote confirmation dialogs not yet supported"
                        r0.<init>(r1)
                        throw r0
                    L_0x007f:
                        int r2 = com.qualcomm.robotcore.C0705R.string.buttonNameOK
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$1 r3 = new org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$1
                        r3.<init>(r0)
                        r1.setNeutralButton(r2, r3)
                    L_0x0089:
                        android.app.AlertDialog r1 = r1.create()
                        r0.dialog = r1
                        android.app.AlertDialog r1 = r0.dialog
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$4 r2 = new org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$4
                        r2.<init>(r0)
                        r1.setOnShowListener(r2)
                        android.app.AlertDialog r1 = r0.dialog
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$5 r2 = new org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$5
                        r2.<init>(r0)
                        r1.setOnCancelListener(r2)
                        android.app.AlertDialog r1 = r0.dialog
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$6 r2 = new org.firstinspires.ftc.robotcore.internal.system.AppUtil$11$6
                        r2.<init>(r0)
                        r1.setOnDismissListener(r2)
                        org.firstinspires.ftc.robotcore.internal.system.AppUtil r1 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.this
                        java.util.Map r1 = r1.dialogContextMap
                        java.lang.String r2 = r0.uuidString
                        r1.put(r2, r0)
                        org.firstinspires.ftc.robotcore.internal.collections.MutableReference r1 = r6
                        r1.setValue(r0)
                        android.app.AlertDialog r0 = r0.dialog
                        r0.show()
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.system.AppUtil.C116011.run():void");
                }
            });
            Assert.assertNotNull(mutableReference.getValue());
            if (copy.uiLocation == UILocation.BOTH) {
                NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_SHOW_DIALOG, showDialog.serialize()));
            }
            dialogContext = (DialogContext) mutableReference.getValue();
        }
        return dialogContext;
    }

    /* renamed from: org.firstinspires.ftc.robotcore.internal.system.AppUtil$14 */
    static /* synthetic */ class C117014 {

        /* renamed from: $SwitchMap$org$firstinspires$ftc$robotcore$internal$system$AppUtil$DialogFlavor */
        static final /* synthetic */ int[] f277xb275dcfa;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogFlavor[] r0 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.DialogFlavor.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f277xb275dcfa = r0
                org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogFlavor r1 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.DialogFlavor.ALERT     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f277xb275dcfa     // Catch:{ NoSuchFieldError -> 0x001d }
                org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogFlavor r1 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.DialogFlavor.PROMPT     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f277xb275dcfa     // Catch:{ NoSuchFieldError -> 0x0028 }
                org.firstinspires.ftc.robotcore.internal.system.AppUtil$DialogFlavor r1 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.DialogFlavor.CONFIRM     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.system.AppUtil.C117014.<clinit>():void");
        }
    }

    public void dismissDialog(UILocation uILocation, RobotCoreCommandList.DismissDialog dismissDialog) {
        dismissDialog(dismissDialog.uuidString);
        if (uILocation == UILocation.BOTH) {
            NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_DISMISS_DIALOG, dismissDialog.serialize()));
        }
    }

    /* access modifiers changed from: protected */
    public void dismissDialog(final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                DialogContext dialogContext = (DialogContext) AppUtil.this.dialogContextMap.remove(str);
                if (dialogContext != null) {
                    dialogContext.isArmed = false;
                    dialogContext.dialog.dismiss();
                }
            }
        });
    }

    public void dismissAllDialogs(UILocation uILocation) {
        for (String dismissDialog : new ArrayList(this.dialogContextMap.keySet())) {
            dismissDialog(dismissDialog);
        }
        if (uILocation == UILocation.BOTH) {
            NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_DISMISS_ALL_DIALOGS));
        }
    }

    public void showToast(UILocation uILocation, String str) {
        showToast(uILocation, str, 0);
    }

    public void showToast(UILocation uILocation, final String str, final int i) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                Toast makeText = Toast.makeText(AppUtil.getDefContext(), str, i);
                ((TextView) makeText.getView().findViewById(16908299)).setTextSize(18.0f);
                makeText.show();
            }
        });
        if (uILocation == UILocation.BOTH) {
            RobotCoreCommandList.ShowToast showToast = new RobotCoreCommandList.ShowToast();
            showToast.message = str;
            showToast.duration = i;
            NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_SHOW_TOAST, showToast.serialize()));
        }
    }

    @Deprecated
    public void showToast(UILocation uILocation, Context context, String str) {
        showToast(uILocation, str);
    }

    @Deprecated
    public void showToast(UILocation uILocation, Activity activity, Context context, String str) {
        showToast(uILocation, str);
    }

    @Deprecated
    public void showToast(UILocation uILocation, Activity activity, Context context, String str, int i) {
        showToast(uILocation, str, i);
    }

    public Activity getActivity() {
        return this.currentActivity;
    }

    public Context getModalContext() {
        Activity activity = this.currentActivity;
        return activity != null ? activity : getApplication();
    }

    public Activity getRootActivity() {
        return this.rootActivity;
    }

    /* access modifiers changed from: private */
    public void initializeRootActivityIfNecessary() {
        if (this.rootActivity == null) {
            Activity activity = this.currentActivity;
            this.rootActivity = activity;
            RobotLog.m61vv(TAG, "rootActivity=%s", activity.getClass().getSimpleName());
        }
    }

    private class LifeCycleMonitor implements Application.ActivityLifecycleCallbacks {
        public void onActivityPaused(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        public void onActivityStopped(Activity activity) {
        }

        private LifeCycleMonitor() {
        }

        public void onActivityCreated(Activity activity, Bundle bundle) {
            Activity unused = AppUtil.this.currentActivity = activity;
            AppUtil.this.initializeRootActivityIfNecessary();
        }

        public void onActivityStarted(Activity activity) {
            Activity unused = AppUtil.this.currentActivity = activity;
            AppUtil.this.initializeRootActivityIfNecessary();
        }

        public void onActivityResumed(Activity activity) {
            Activity unused = AppUtil.this.currentActivity = activity;
            AppUtil.this.initializeRootActivityIfNecessary();
        }

        public void onActivityDestroyed(Activity activity) {
            if (activity == AppUtil.this.rootActivity && AppUtil.this.rootActivity != null) {
                RobotLog.m61vv(AppUtil.TAG, "rootActivity=%s destroyed", AppUtil.this.rootActivity.getClass().getSimpleName());
                Activity unused = AppUtil.this.rootActivity = null;
                AppUtil.this.initializeRootActivityIfNecessary();
            }
        }
    }

    public void setBluetoothEnabled(boolean z) {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (z && !defaultAdapter.isEnabled()) {
            defaultAdapter.enable();
        } else if (!z && defaultAdapter.isEnabled()) {
            defaultAdapter.disable();
        }
    }

    public DateTimeFormatter getIso8601DateTimeFormatter() {
        if (this.iso8601DateFormat == null) {
            this.iso8601DateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        }
        return this.iso8601DateFormat;
    }

    public YearMonth getYearMonthFromIso8601(String str) {
        try {
            return YearMonth.parse(str, getIso8601DateTimeFormatter());
        } catch (DateTimeParseException e) {
            RobotLog.m63vv(TAG, e, "Failed to parse the year and month from string", str);
            return YearMonth.m114of(1, 1);
        }
    }

    public YearMonth getLocalSdkBuildMonth() {
        return getYearMonthFromIso8601(BuildConfig.SDK_BUILD_TIME);
    }

    public Year getFtcSeasonYear(YearMonth yearMonth) {
        if (yearMonth.getMonth().getValue() >= Month.SEPTEMBER.getValue()) {
            return Year.m113of(yearMonth.getYear());
        }
        return Year.m113of(yearMonth.getYear() - 1);
    }

    public boolean appIsObsolete(YearMonth yearMonth) {
        YearMonth yearMonth2;
        YearMonth now = YearMonth.now();
        if (now.getMonth().getValue() >= Month.OCTOBER.getValue()) {
            yearMonth2 = YearMonth.m115of(now.getYear(), Month.SEPTEMBER);
        } else {
            yearMonth2 = YearMonth.m115of(now.getYear() - 1, Month.SEPTEMBER);
        }
        return yearMonth.isBefore(yearMonth2);
    }

    public boolean localAppIsObsolete() {
        return appIsObsolete(getLocalSdkBuildMonth());
    }

    public long getWallClockTime() {
        return System.currentTimeMillis();
    }

    public void setWallClockTime(long j) {
        if (Device.isRevControlHub()) {
            nativeSetCurrentTimeMillis(j);
        }
    }

    public void setWallClockIfCurrentlyInsane(long j, String str) {
        synchronized (this.timeLock) {
            boolean isSaneWallClockTime = isSaneWallClockTime(getWallClockTime());
            boolean z = str != null && !str.isEmpty();
            if (!isSaneWallClockTime && isSaneWallClockTime(j) && z) {
                setWallClockTime(j);
                setTimeZone(str);
            }
        }
    }

    public void setTimeZone(String str) {
        if (LynxConstants.isRevControlHub()) {
            TimeZone timeZone = TimeZone.getDefault();
            try {
                ((AlarmManager) getDefContext().getSystemService(NotificationCompat.CATEGORY_ALARM)).setTimeZone(str);
                TimeZone.setDefault((TimeZone) null);
                RobotLog.m61vv(TAG, "attempted to set timezone: before=%s after=%s", timeZone.getID(), TimeZone.getDefault().getID());
            } catch (IllegalArgumentException unused) {
                RobotLog.m49ee(TAG, "Attempted to set invalid timezone: %s", timeZone.getID());
            }
        }
    }

    public boolean isSaneWallClockTime(long j) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(j);
        if (gregorianCalendar.get(1) > 1975) {
            return true;
        }
        return false;
    }

    public String findCaller(String str, int i) {
        StackTraceElement stackTraceElement = new RuntimeException().getStackTrace()[i + 2];
        String className = stackTraceElement.getClassName();
        String substring = className.substring(className.lastIndexOf(46) + 1);
        return Misc.formatInvariant("%s caller=[%s:%d] %s", str, new File(stackTraceElement.getFileName()).getName(), Integer.valueOf(stackTraceElement.getLineNumber()), substring);
    }

    public RuntimeException unreachable() {
        return unreachable(TAG);
    }

    public RuntimeException unreachable(Throwable th) {
        return unreachable(TAG, th);
    }

    public RuntimeException unreachable(String str) {
        return failFast(str, "internal error: this code is unreachable");
    }

    public RuntimeException unreachable(String str, Throwable th) {
        return failFast(str, th, "internal error: this code is unreachable");
    }

    public RuntimeException failFast(String str, String str2, Object... objArr) {
        return failFast(str, String.format(str2, objArr));
    }

    public RuntimeException failFast(String str, String str2) {
        RobotLog.m48ee(str, str2);
        exitApplication(-1);
        return new RuntimeException("keep compiler happy");
    }

    public RuntimeException failFast(String str, Throwable th, String str2, Object... objArr) {
        return failFast(str, th, String.format(str2, objArr));
    }

    public RuntimeException failFast(String str, Throwable th, String str2) {
        RobotLog.m50ee(str, th, str2);
        exitApplication(-1);
        return new RuntimeException("keep compiler happy", th);
    }

    public static ResultReceiver wrapResultReceiverForIpc(ResultReceiver resultReceiver) {
        Parcel obtain = Parcel.obtain();
        resultReceiver.writeToParcel(obtain, 0);
        obtain.setDataPosition(0);
        ResultReceiver resultReceiver2 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(obtain);
        obtain.recycle();
        return resultReceiver2;
    }
}
