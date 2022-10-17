package org.disruption.app

import rc.RobotCore.src.main.java.com.qualcomm.robotcore.util.ElapsedTime
import org.disruption.app.view.MainView
import tornadofx.App;

class MainApp: App(MainView::class, Styles::class) {
    companion object {
        val runtime = ElapsedTime()
    }
}