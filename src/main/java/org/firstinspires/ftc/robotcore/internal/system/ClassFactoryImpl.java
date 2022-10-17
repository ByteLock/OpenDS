package org.firstinspires.ftc.robotcore.internal.system;

import java.lang.ref.WeakReference;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraManager;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.FrameGenerator;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.internal.camera.CameraManagerImpl;
import org.firstinspires.ftc.robotcore.internal.tfod.TFObjectDetectorImpl;
import org.firstinspires.ftc.robotcore.internal.tfod.VuforiaFrameGenerator;
import org.firstinspires.ftc.robotcore.internal.vuforia.VuforiaLocalizerImpl;

public class ClassFactoryImpl extends ClassFactory {
    public static final String TAG = "ClassFactory";
    protected WeakReference<CameraManagerImpl> cameraManagerHolder = new WeakReference<>((Object) null);
    protected final Object lock = new Object();

    public static void onApplicationStart() {
        ClassFactory.InstanceHolder.theInstance = new ClassFactoryImpl();
    }

    public VuforiaLocalizer createVuforia(VuforiaLocalizer.Parameters parameters) {
        return new VuforiaLocalizerImpl(parameters);
    }

    public TFObjectDetector createTFObjectDetector(TFObjectDetector.Parameters parameters, VuforiaLocalizer vuforiaLocalizer) {
        return createTFObjectDetector(parameters, (FrameGenerator) new VuforiaFrameGenerator(vuforiaLocalizer));
    }

    public TFObjectDetector createTFObjectDetector(TFObjectDetector.Parameters parameters, FrameGenerator frameGenerator) {
        return new TFObjectDetectorImpl(parameters, frameGenerator);
    }

    public CameraManager getCameraManager() {
        CameraManagerImpl cameraManagerImpl;
        synchronized (this.lock) {
            cameraManagerImpl = (CameraManagerImpl) this.cameraManagerHolder.get();
            if (cameraManagerImpl == null) {
                cameraManagerImpl = new CameraManagerImpl();
                this.cameraManagerHolder = new WeakReference<>(cameraManagerImpl);
            }
        }
        return cameraManagerImpl;
    }
}
