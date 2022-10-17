package org.firstinspires.ftc.robotcore.internal.system;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.ArrayList;
import java.util.List;

public abstract class PermissionValidatorActivity extends Activity {
    private static final String LIFECYCLE_TAG = "Lifecycle ";
    public static final String PERMS_VALID_KEY = "org.firstinspires.ftc.robotcore.PERMS_VALID_KEY";
    private static final String TAG = "PermissionValidatorActivity";
    private final String instanceId = Integer.toHexString(System.identityHashCode(this));
    /* access modifiers changed from: private */
    public TextView instructions;
    /* access modifiers changed from: private */
    public TextView permDenied;
    List<String> permanentDenials = new ArrayList();
    /* access modifiers changed from: private */
    public PermissionValidator permissionValidator;
    protected List<String> permissions;

    public abstract String mapPermissionToExplanation(String str);

    /* access modifiers changed from: protected */
    public abstract Class onStartApplication();

    private class PermissionListenerImpl implements PermissionListener {
        private PermissionListenerImpl() {
        }

        public void onPermissionDenied(String str) {
            PermissionValidatorActivity.this.permissionValidator.explain(PermissionValidatorActivity.this.permissions.get(0));
        }

        public void onPermissionGranted(String str) {
            PermissionValidatorActivity.this.permissions.remove(str);
            if (PermissionValidatorActivity.this.permissions.isEmpty()) {
                ServiceController.onApplicationStart();
                PermissionValidatorActivity.this.startRobotController();
                return;
            }
            PermissionValidatorActivity.this.permissionValidator.checkPermission(PermissionValidatorActivity.this.permissions.get(0));
        }

        public void onPermissionPermanentlyDenied(String str) {
            RobotLog.m48ee(PermissionValidatorActivity.TAG, "Permission permanently denied for " + str);
            RobotLog.m48ee(PermissionValidatorActivity.TAG, "Robot Controller will not run");
            PermissionValidatorActivity.this.permDenied.setText(String.format(Misc.formatForUser(C0705R.string.permPermanentlyDenied), new Object[]{AppUtil.getInstance().isRobotController() ? "Robot Controller" : "Driver Station"}));
            PermissionValidatorActivity.this.permDenied.setVisibility(0);
            PermissionValidatorActivity.this.instructions.setText(String.format(Misc.formatForUser(C0705R.string.permPermanentlyDeniedRecovery), new Object[]{AppUtil.getInstance().isRobotController() ? "Storage, Location, and Camera" : "Storage and Location"}));
            PermissionValidatorActivity.this.instructions.setVisibility(0);
            PermissionValidatorActivity.this.instructions.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", PermissionValidatorActivity.this.getPackageName(), (String) null));
                    PermissionValidatorActivity.this.startActivity(intent);
                }
            });
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        this.permissionValidator.onRequestPermissionsResult(i, strArr, iArr);
    }

    /* access modifiers changed from: protected */
    public void startRobotController() {
        if (!this.permanentDenials.isEmpty()) {
            this.permDenied.setVisibility(0);
            this.instructions.setVisibility(0);
            return;
        }
        RobotLog.m54ii(TAG, "All permissions validated.  Starting RobotController");
        startActivity(new Intent(AppUtil.getDefContext(), onStartApplication()));
        finish();
    }

    /* access modifiers changed from: protected */
    public void enforcePermissions() {
        if (this.permissions.isEmpty()) {
            startRobotController();
        } else {
            this.permissionValidator.checkPermission(this.permissions.get(0));
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        RobotLog.m54ii(TAG, "Lifecycle onCreate : " + this.instanceId);
        setContentView(C0705R.layout.activity_permissions_validator);
        TextView textView = (TextView) findViewById(C0705R.C0707id.permDeniedText);
        this.permDenied = textView;
        textView.setVisibility(4);
        TextView textView2 = (TextView) findViewById(C0705R.C0707id.explanationText);
        this.instructions = textView2;
        textView2.setVisibility(4);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        RobotLog.m54ii(TAG, "Lifecycle onStart : " + this.instanceId);
        this.permissionValidator = new PermissionValidator(this, new PermissionListenerImpl());
        enforcePermissions();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        RobotLog.m54ii(TAG, "Lifecycle onResume : " + this.instanceId);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        RobotLog.m54ii(TAG, "Lifecycle onDestroy : " + this.instanceId);
    }
}
