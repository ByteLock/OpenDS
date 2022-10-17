package com.qualcomm.ftccommon;

import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
import com.qualcomm.ftccommon.configuration.RobotConfigResFilter;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import java.util.Collection;
import org.firstinspires.ftc.ftccommon.internal.AnnotatedHooksClassFilter;
import org.firstinspires.ftc.robotcore.external.Supplier;
import org.firstinspires.ftc.robotcore.internal.opmode.AnnotatedOpModeClassFilter;
import org.firstinspires.ftc.robotcore.internal.opmode.BlocksClassFilter;
import org.firstinspires.ftc.robotcore.internal.opmode.ClassManager;

public class ClassManagerFactory {
    public static void registerFilters() {
        registerResourceFilters();
        ClassManager instance = ClassManager.getInstance();
        instance.registerFilter(AnnotatedHooksClassFilter.getInstance());
        instance.registerFilter(AnnotatedOpModeClassFilter.getInstance());
        instance.registerFilter(BlocksClassFilter.getInstance());
        instance.registerFilter(ConfigurationTypeManager.getInstance());
    }

    public static void registerResourceFilters() {
        ClassManager instance = ClassManager.getInstance();
        final RobotConfigResFilter robotConfigResFilter = new RobotConfigResFilter(RobotConfigFileManager.getRobotConfigTypeAttribute());
        RobotConfigFileManager.setXmlResourceIdSupplier(new Supplier<Collection<Integer>>() {
            public Collection<Integer> get() {
                return RobotConfigResFilter.this.getXmlIds();
            }
        });
        final RobotConfigResFilter robotConfigResFilter2 = new RobotConfigResFilter(RobotConfigFileManager.getRobotConfigTemplateAttribute());
        RobotConfigFileManager.setXmlResourceTemplateIdSupplier(new Supplier<Collection<Integer>>() {
            public Collection<Integer> get() {
                return RobotConfigResFilter.this.getXmlIds();
            }
        });
        instance.registerFilter(robotConfigResFilter);
        instance.registerFilter(robotConfigResFilter2);
    }

    public static void processAllClasses() {
        ClassManager.getInstance().processAllClasses();
    }
}
