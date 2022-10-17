package com.qualcomm.ftccommon;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import androidx.core.content.FileProvider;
import com.qualcomm.robotcore.util.RobotLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import org.firstinspires.ftc.robotcore.internal.p013ui.ThemedActivity;
import org.firstinspires.inspection.C1275R;
import org.firstinspires.inspection.InspectionState;
import p007fi.iki.elonen.NanoHTTPD;

public class ViewLogsActivity extends ThemedActivity {
    public static final String FILENAME = "org.firstinspires.ftc.ftccommon.logFilename";
    public static final String TAG = "ViewLogsActivity";
    int DEFAULT_NUMBER_OF_LINES = 500;
    int errorColor;
    String filepath = " ";
    /* access modifiers changed from: private */
    public File logFile;
    WebView webViewForLogcat;

    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C1275R.C1277id.backbar);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_view_logs);
        this.errorColor = getResources().getColor(C0470R.color.text_warning);
        WebView webView = (WebView) findViewById(C0470R.C0472id.webView);
        this.webViewForLogcat = webView;
        webView.getSettings().setBuiltInZoomControls(true);
        this.webViewForLogcat.getSettings().setDisplayZoomControls(false);
        this.webViewForLogcat.setBackgroundColor(getResources().getColor(C0470R.color.logviewer_bgcolor));
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Serializable serializableExtra = getIntent().getSerializableExtra("org.firstinspires.ftc.ftccommon.logFilename");
        if (serializableExtra != null) {
            this.filepath = (String) serializableExtra;
        }
        this.logFile = new File(this.filepath);
        try {
            String format = String.format("<span style='white-space: nowrap;'><font face='monospace' color='white'>%s</font></span>", new Object[]{Html.toHtml(colorize(readNLines(this.DEFAULT_NUMBER_OF_LINES)))});
            this.webViewForLogcat.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView webView, String str) {
                    ViewLogsActivity.this.webViewForLogcat.scrollTo(0, 900000000);
                }
            });
            this.webViewForLogcat.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.putExtra("android.intent.extra.SUBJECT", "FTC Robot Log - " + DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
                    ViewLogsActivity viewLogsActivity = ViewLogsActivity.this;
                    intent.putExtra("android.intent.extra.STREAM", FileProvider.getUriForFile(viewLogsActivity, ViewLogsActivity.this.getPackageName() + ".provider", ViewLogsActivity.this.logFile));
                    intent.setType("text/plain");
                    ViewLogsActivity.this.startActivity(intent);
                    return false;
                }
            });
            this.webViewForLogcat.loadData(format, NanoHTTPD.MIME_HTML, "UTF-8");
        } catch (IOException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "Exception loading logcat data");
            this.webViewForLogcat.loadData("<font color='white'>Error loading logcat data</font>", NanoHTTPD.MIME_HTML, "UTF-8");
        }
    }

    public String readNLines(int i) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(this.logFile));
        String[] strArr = new String[i];
        int i2 = 0;
        int i3 = 0;
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine == null) {
                break;
            }
            strArr[i3 % i] = readLine;
            i3++;
        }
        int i4 = i3 - i;
        if (i4 >= 0) {
            i2 = i4;
        }
        String str = InspectionState.NO_VERSION;
        while (i2 < i3) {
            str = str + strArr[i2 % i] + "\n";
            i2++;
        }
        int lastIndexOf = str.lastIndexOf("--------- beginning");
        if (lastIndexOf < 0) {
            return str;
        }
        return str.substring(lastIndexOf);
    }

    private Spannable colorize(String str) {
        SpannableString spannableString = new SpannableString(str);
        int i = 0;
        for (String str2 : str.split("\\n")) {
            if (str2.contains(" E ")) {
                spannableString.setSpan(new ForegroundColorSpan(this.errorColor), i, str2.length() + i, 33);
            }
            i = i + str2.length() + 1;
        }
        return spannableString;
    }
}
