package org.firstinspires.ftc.robotcore.internal.p013ui;

import android.hardware.input.InputManager;
import android.os.Handler;
import android.view.InputDevice;
import android.view.InputEvent;
import com.qualcomm.robotcore.util.ClassUtil;
import java.lang.reflect.Method;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.InputManager */
public class InputManager {
    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1;
    protected static InputManager theInstance = new InputManager();
    protected android.hardware.input.InputManager inputManager = ((android.hardware.input.InputManager) AppUtil.getInstance().getActivity().getSystemService("input"));
    protected Method methodInjectInputEvent;

    public static InputManager getInstance() {
        return theInstance;
    }

    protected InputManager() {
        Class<android.hardware.input.InputManager> cls = android.hardware.input.InputManager.class;
        try {
            this.methodInjectInputEvent = cls.getMethod("injectInputEvent", new Class[]{InputEvent.class, Integer.TYPE});
        } catch (NoSuchMethodException unused) {
        }
    }

    public boolean injectInputEvent(InputEvent inputEvent, int i) {
        return ((Boolean) ClassUtil.invoke(this.inputManager, this.methodInjectInputEvent, inputEvent, Integer.valueOf(i))).booleanValue();
    }

    public InputDevice getInputDevice(int i) {
        return this.inputManager.getInputDevice(i);
    }

    public int[] getInputDeviceIds() {
        return this.inputManager.getInputDeviceIds();
    }

    public void registerInputDeviceListener(InputManager.InputDeviceListener inputDeviceListener, Handler handler) {
        this.inputManager.registerInputDeviceListener(inputDeviceListener, handler);
    }

    public void unregisterInputDeviceListener(InputManager.InputDeviceListener inputDeviceListener) {
        this.inputManager.unregisterInputDeviceListener(inputDeviceListener);
    }
}
