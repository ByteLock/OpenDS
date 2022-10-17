package com.qualcomm.ftcdriverstation;

import android.content.Context;
import com.qualcomm.ftcdriverstation.GamepadTypeOverrideMapper;
import java.util.ArrayList;
import java.util.Iterator;
import org.firstinspires.directgamepadaccess.android.AndroidGamepad;
import org.firstinspires.directgamepadaccess.android.AndroidGamepadManager;

public class FtcGamepadMappingSuggestor implements AndroidGamepadManager.GamepadMappingSuggestor {
    private static FtcGamepadMappingSuggestor theInstance;
    private ArrayList<CachedSuggestion> cachedSuggestions = new ArrayList<>();
    private GamepadTypeOverrideMapper typeOverrideMapper;

    static class CachedSuggestion {
        int pid;
        AndroidGamepad.Type type;
        int vid;

        CachedSuggestion(int i, int i2, AndroidGamepad.Type type2) {
            this.vid = i;
            this.pid = i2;
            this.type = type2;
        }

        /* access modifiers changed from: package-private */
        public boolean matches(AndroidGamepadManager.GamepadPlaceholder gamepadPlaceholder) {
            return gamepadPlaceholder.vid == this.vid && gamepadPlaceholder.pid == this.pid;
        }
    }

    private FtcGamepadMappingSuggestor(Context context) {
        this.typeOverrideMapper = new GamepadTypeOverrideMapper(context);
    }

    public static FtcGamepadMappingSuggestor getInstance(Context context) {
        if (theInstance == null) {
            theInstance = new FtcGamepadMappingSuggestor(context);
        }
        return theInstance;
    }

    public AndroidGamepad.Type suggestMapping(AndroidGamepadManager.GamepadPlaceholder gamepadPlaceholder) {
        GamepadTypeOverrideMapper.GamepadTypeOverrideEntry entryFor = this.typeOverrideMapper.getEntryFor(gamepadPlaceholder.vid, gamepadPlaceholder.pid);
        if (entryFor != null) {
            return entryFor.getAndroidGamepadType();
        }
        if (gamepadPlaceholder.buttonsDepressed.contains(105)) {
            if (!gamepadPlaceholder.buttonsDepressed.contains(98) && !gamepadPlaceholder.buttonsDepressed.contains(97)) {
                return null;
            }
            Iterator<CachedSuggestion> it = this.cachedSuggestions.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                CachedSuggestion next = it.next();
                if (next.matches(gamepadPlaceholder)) {
                    this.cachedSuggestions.remove(next);
                    break;
                }
            }
            this.cachedSuggestions.add(new CachedSuggestion(gamepadPlaceholder.vid, gamepadPlaceholder.pid, AndroidGamepad.Type.SONY_PS4_WITHOUT_KERNEL_SUPPORT));
            return AndroidGamepad.Type.SONY_PS4_WITHOUT_KERNEL_SUPPORT;
        } else if (!gamepadPlaceholder.buttonsDepressed.contains(108)) {
            Iterator<CachedSuggestion> it2 = this.cachedSuggestions.iterator();
            while (it2.hasNext()) {
                CachedSuggestion next2 = it2.next();
                if (next2.matches(gamepadPlaceholder)) {
                    return next2.type;
                }
            }
            return null;
        } else if (!gamepadPlaceholder.buttonsDepressed.contains(96) && !gamepadPlaceholder.buttonsDepressed.contains(97)) {
            return null;
        } else {
            Iterator<CachedSuggestion> it3 = this.cachedSuggestions.iterator();
            while (true) {
                if (!it3.hasNext()) {
                    break;
                }
                CachedSuggestion next3 = it3.next();
                if (next3.matches(gamepadPlaceholder)) {
                    this.cachedSuggestions.remove(next3);
                    break;
                }
            }
            this.cachedSuggestions.add(new CachedSuggestion(gamepadPlaceholder.vid, gamepadPlaceholder.pid, AndroidGamepad.Type.GAMEPAD_SUPPORTED_BY_KERNEL));
            return AndroidGamepad.Type.GAMEPAD_SUPPORTED_BY_KERNEL;
        }
    }
}
