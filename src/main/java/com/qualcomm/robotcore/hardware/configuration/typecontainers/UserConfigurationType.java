/*
Copyright (c) 2016 Robert Atkinson, Noah Andrews

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.qualcomm.robotcore.hardware.configuration.typecontainers;

import org.jetbrains.annotations.NotNull;

import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.util.ClassUtil;

import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaDeterminer;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import static com.qualcomm.robotcore.hardware.ControlSystem.REV_HUB;

/**
 * {@link UserConfigurationType} contains metadata regarding classes which have been declared as
 * user-defined sensor implementations.
 *
 * Subclasses should be either abstract or final.
 */
public abstract class UserConfigurationType implements ConfigurationType, Serializable
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------
    protected @Expose @NotNull     String name = ""; // TODO(Noah): Make private once we get rid of the deprecated annotations
    protected @Expose String         description; // TODO(Noah): Make private once we get rid of the deprecated annotations

    private @Expose @NotNull final DeviceFlavor flavor;
    private @Expose @NotNull       String xmlTag;
    private @Expose                String[] xmlTagAliases;
    private @Expose                boolean builtIn = false;
    private @Expose                boolean isOnBotJava;
    private @Expose                boolean isExternalLibraries;
    private @Expose @NotNull       ControlSystem[] compatibleControlSystems = {REV_HUB};
    private @Expose                boolean isDeprecated;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    public UserConfigurationType(Class clazz, @NotNull DeviceFlavor flavor, @NotNull String xmlTag)
        {
        this.flavor = flavor;
        this.xmlTag = xmlTag;
        this.isOnBotJava = OnBotJavaDeterminer.isOnBotJava(clazz);
        this.isExternalLibraries = OnBotJavaDeterminer.isExternalLibraries(clazz);
        this.isDeprecated = clazz.isAnnotationPresent(Deprecated.class);
        }

    // used by gson deserialization
    protected UserConfigurationType(@NotNull DeviceFlavor flavor)
        {
        this.flavor = flavor;
        this.xmlTag = "";
        }

    public void processAnnotation(@NotNull DeviceProperties deviceProperties)
        {
        description = ClassUtil.decodeStringRes(deviceProperties.description());
        builtIn = deviceProperties.builtIn();
        compatibleControlSystems = deviceProperties.compatibleControlSystems();
        xmlTagAliases = deviceProperties.xmlTagAliases();
        if (!deviceProperties.name().isEmpty())
            {
            name = ClassUtil.decodeStringRes(deviceProperties.name().trim());
            }
        }

    public void finishedAnnotations(Class clazz)
        {
        if (name.isEmpty())
            {
            name = clazz.getSimpleName();
            }

        if (xmlTagAliases == null)
            {
            xmlTagAliases = new String[]{};
            }
        }

    public boolean isCompatibleWith(ControlSystem controlSystem)
        {
        for (ControlSystem compatibleControlSystem : compatibleControlSystems)
            {
            if (controlSystem == compatibleControlSystem)
                {
                return true;
                }
            }
        return false;
        }

    //----------------------------------------------------------------------------------------------
    // Serialization (used in local marshalling during configuration editing)
    //----------------------------------------------------------------------------------------------

    protected static class SerializationProxy implements Serializable
        {
        protected String xmlTag;

        public SerializationProxy(UserConfigurationType userConfigurationType)
            {
            this.xmlTag = userConfigurationType.xmlTag;
            }
        private Object readResolve()
            {
            return ConfigurationTypeManager.getInstance().configurationTypeFromTag(xmlTag);
            }
        }

    private Object writeReplace()
        {
        return new SerializationProxy(this);
        }

    private void readObject(ObjectInputStream in) throws InvalidObjectException
        {
        throw new InvalidObjectException("proxy required"); // attack threat paranoia
        }

    //----------------------------------------------------------------------------------------------
    // Accessing
    //----------------------------------------------------------------------------------------------

    @Override
    public @NotNull DeviceFlavor getDeviceFlavor()
        {
        return this.flavor;
        }

    public @NotNull String getName()
        {
        return this.name;
        }

    public String getDescription()
        {
        return this.description;
        }

    public boolean isOnBotJava()
        {
        return isOnBotJava;
        }

    public boolean isExternalLibraries()
        {
        return isExternalLibraries;
        }

    /**
     * This is about whether the type "comes with" the SDK, not whether it lives in BuiltInConfigurationType
     */
    public boolean isBuiltIn()
        {
        return this.builtIn;
        }


    //----------------------------------------------------------------------------------------------
    // ConfigurationType
    //----------------------------------------------------------------------------------------------

    @Override @NotNull public String getDisplayName(DisplayNameFlavor flavor)
        {
        return this.name;
        }

    @Override @NotNull public String getXmlTag()
        {
        return this.xmlTag;
        }

    @Override @NotNull public String[] getXmlTagAliases()
        {
        return xmlTagAliases;
        }

    @Override @NotNull public DeviceManager.UsbDeviceType toUSBDeviceType()
        {
        return DeviceManager.UsbDeviceType.FTDI_USB_UNKNOWN_DEVICE;
        }

    @Override public boolean isDeviceFlavor(DeviceFlavor flavor)
        {
        return this.flavor == flavor;
        }

    @Override public boolean isDeprecated()
        {
        return isDeprecated;
        }
    }
