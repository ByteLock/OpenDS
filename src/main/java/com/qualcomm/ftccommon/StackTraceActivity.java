package com.qualcomm.ftccommon;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import p007fi.iki.elonen.NanoHTTPD;

public class StackTraceActivity extends Activity {
    public static final String KEY_STACK_TRACE = "KEY_STACK_TRACE";
    WebView webView;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_stacktrace);
        String string = getIntent().getExtras().getString(KEY_STACK_TRACE);
        ((TextView) findViewById(C0470R.C0472id.header_error_message)).setText(string.substring(0, string.indexOf("\n")));
        WebView webView2 = (WebView) findViewById(C0470R.C0472id.webView);
        this.webView = webView2;
        webView2.getSettings().setBuiltInZoomControls(true);
        this.webView.getSettings().setDisplayZoomControls(false);
        this.webView.setBackgroundColor(getResources().getColor(C0470R.color.logviewer_bgcolor));
        this.webView.loadData(String.format("<span style='white-space: nowrap;'><font face='monospace' color='white'><pre>%s</pre></font></span>", new Object[]{string.replace("\n", "<br>").replace("\t", "    ")}), NanoHTTPD.MIME_HTML, "UTF-8");
    }

    public void onAccept(View view) {
        finish();
    }

    public void onZoomIn(View view) {
        this.webView.zoomIn();
    }

    public void onZoomOut(View view) {
        this.webView.zoomOut();
    }
}
