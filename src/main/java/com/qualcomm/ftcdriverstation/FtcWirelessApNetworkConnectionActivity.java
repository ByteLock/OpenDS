package com.qualcomm.ftcdriverstation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.DriverStationAccessPointAssistant;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.internal.p013ui.BaseActivity;

public class FtcWirelessApNetworkConnectionActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "FtcWirelessApNetworkConnection";
    /* access modifiers changed from: private */
    public Heartbeat heartbeat = new Heartbeat();
    private NetworkConnection networkConnection;
    private NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    /* access modifiers changed from: private */
    public RobotState robotState;
    private TextView textViewCurrentAp;
    /* access modifiers changed from: private */
    public TextView textViewWirelessApStatus;

    public String getTag() {
        return TAG;
    }

    public void onClick(View view) {
    }

    /* access modifiers changed from: protected */
    public void setTextView(final TextView textView, final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                textView.setText(str);
            }
        });
    }

    protected class RecvLoopCallback extends RecvLoopRunnable.DegenerateCallback {
        protected RecvLoopCallback() {
        }

        public CallbackResult heartbeatEvent(RobocolDatagram robocolDatagram) {
            try {
                FtcWirelessApNetworkConnectionActivity.this.heartbeat.fromByteArray(robocolDatagram.getData());
                FtcWirelessApNetworkConnectionActivity ftcWirelessApNetworkConnectionActivity = FtcWirelessApNetworkConnectionActivity.this;
                RobotState unused = ftcWirelessApNetworkConnectionActivity.robotState = RobotState.fromByte(ftcWirelessApNetworkConnectionActivity.heartbeat.getRobotState());
                FtcWirelessApNetworkConnectionActivity ftcWirelessApNetworkConnectionActivity2 = FtcWirelessApNetworkConnectionActivity.this;
                ftcWirelessApNetworkConnectionActivity2.setTextView(ftcWirelessApNetworkConnectionActivity2.textViewWirelessApStatus, FtcWirelessApNetworkConnectionActivity.this.robotState.toString());
            } catch (RobotCoreException e) {
                RobotLog.logStackTrace(e);
            }
            return CallbackResult.HANDLED;
        }
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C0648R.C0650id.backbar);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0648R.layout.activity_ftc_wireless_ap_connection);
        this.networkConnection = DriverStationAccessPointAssistant.getDriverStationAccessPointAssistant(getBaseContext());
        this.textViewCurrentAp = (TextView) findViewById(C0648R.C0650id.textViewCurrentAp);
        ((Button) findViewById(C0648R.C0650id.buttonWirelessApSettings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                if (Device.isRevDriverHub()) {
                    intent.putExtra("com.revrobotics.wifiConnectionReason", FtcWirelessApNetworkConnectionActivity.this.getString(C0648R.string.control_hub_connection_reason));
                }
                FtcWirelessApNetworkConnectionActivity.this.startActivity(intent);
            }
        });
    }

    public void onStart() {
        super.onStart();
        this.textViewCurrentAp.setText(this.networkConnection.getConnectionOwnerName());
        this.networkConnection.discoverPotentialConnections();
    }

    public void onStop() {
        super.onStop();
        this.networkConnection.cancelPotentialConnections();
    }
}
