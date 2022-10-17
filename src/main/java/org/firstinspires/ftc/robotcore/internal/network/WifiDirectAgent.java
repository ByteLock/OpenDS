package org.firstinspires.ftc.robotcore.internal.network;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.ClassUtil;
import com.qualcomm.robotcore.util.RobotLog;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.internal.network.WifiDirectPersistentGroupManager;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.system.CallbackRegistrar;

public class WifiDirectAgent extends WifiStartStoppable {
    public static final String TAG = "NetDiscover_wifiDirectAgent";
    protected static WifiDirectAgent theInstance;
    protected static StartResult theInstanceStartResult = new StartResult();
    protected final CallbackRegistrar<Callback> callbacks = new CallbackRegistrar<>();
    protected final ChannelListener channelListener;
    protected final Context context;
    protected IntentFilter intentFilter;
    protected boolean isWifiP2pEnabled = false;
    protected final CallbackLooper looper;
    protected final WifiBroadcastReceiver wifiBroadcastReceiver;
    protected final WifiP2pManager.Channel wifiP2pChannel;
    protected final WifiP2pManager wifiP2pManager;
    protected NetworkInfo.State wifiP2pState = NetworkInfo.State.UNKNOWN;
    protected WifiState wifiState = WifiState.UNKNOWN;

    public interface Callback {
        void onReceive(Context context, Intent intent);
    }

    public String getTag() {
        return TAG;
    }

    static {
        WifiDirectAgent wifiDirectAgent = new WifiDirectAgent();
        theInstance = wifiDirectAgent;
        wifiDirectAgent.start(theInstanceStartResult);
    }

    public static WifiDirectAgent getInstance() {
        return theInstance;
    }

    public WifiDirectAgent() {
        super(0);
        Application application = AppUtil.getInstance().getApplication();
        this.context = application;
        CallbackLooper callbackLooper = CallbackLooper.getDefault();
        this.looper = callbackLooper;
        ChannelListener channelListener2 = new ChannelListener();
        this.channelListener = channelListener2;
        WifiP2pManager wifiP2pManager2 = (WifiP2pManager) application.getSystemService("wifip2p");
        this.wifiP2pManager = wifiP2pManager2;
        this.wifiP2pChannel = wifiP2pManager2.initialize(application, callbackLooper.getLooper(), channelListener2);
        this.wifiBroadcastReceiver = new WifiBroadcastReceiver();
        IntentFilter intentFilter2 = new IntentFilter();
        this.intentFilter = intentFilter2;
        intentFilter2.addAction("android.net.wifi.p2p.STATE_CHANGED");
        this.intentFilter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        this.intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.intentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        this.intentFilter.addAction("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
        this.intentFilter.addAction(WifiDirectPersistentGroupManager.WIFI_P2P_PERSISTENT_GROUPS_CHANGED_ACTION);
        this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
    }

    /* access modifiers changed from: protected */
    public boolean doStart() {
        doListen();
        new WifiDirectPersistentGroupManager(this).requestPersistentGroups(new WifiDirectPersistentGroupManager.PersistentGroupInfoListener() {
            public void onPersistentGroupInfoAvailable(Collection<WifiP2pGroup> collection) {
                for (WifiP2pGroup networkName : collection) {
                    RobotLog.m61vv(WifiDirectAgent.TAG, "found persistent group: %s", networkName.getNetworkName());
                }
            }
        });
        return true;
    }

    /* access modifiers changed from: protected */
    public void doStop() {
        doNotListen();
    }

    public void doNotListen() {
        this.context.unregisterReceiver(this.wifiBroadcastReceiver);
    }

    public void doListen() {
        this.context.registerReceiver(this.wifiBroadcastReceiver, this.intentFilter, (String) null, this.looper.getHandler());
    }

    public WifiP2pManager getWifiP2pManager() {
        return this.wifiP2pManager;
    }

    public WifiP2pManager.Channel getWifiP2pChannel() {
        return this.wifiP2pChannel;
    }

    public CallbackLooper getLooper() {
        return this.looper;
    }

    public boolean isLooperThread() {
        Assert.assertNotNull(this.looper);
        return CallbackLooper.isLooperThread();
    }

    public boolean isWifiDirectEnabled() {
        return this.isWifiP2pEnabled;
    }

    public WifiState getWifiState() {
        return this.wifiState;
    }

    public NetworkInfo.State getWifiDirectState() {
        return this.wifiP2pState;
    }

    @Deprecated
    public boolean isAirplaneModeOn() {
        return WifiUtil.isAirplaneModeOn();
    }

    @Deprecated
    public boolean isBluetoothOn() {
        return WifiUtil.isBluetoothOn();
    }

    @Deprecated
    public boolean isWifiEnabled() {
        return WifiUtil.isWifiEnabled();
    }

    @Deprecated
    public boolean isWifiConnected() {
        return WifiUtil.isWifiConnected();
    }

    public boolean isWifiDirectConnected() {
        NetworkInfo.State wifiDirectState = getWifiDirectState();
        return wifiDirectState == NetworkInfo.State.CONNECTED || wifiDirectState == NetworkInfo.State.CONNECTING;
    }

    public boolean disconnectFromWifiDirect() {
        return ((Boolean) lockCompletion(false, new Func<Boolean>() {
            public Boolean value() {
                boolean resetCompletion = WifiDirectAgent.this.resetCompletion();
                if (resetCompletion) {
                    try {
                        WifiDirectAgent.this.wifiP2pManager.requestGroupInfo(WifiDirectAgent.this.wifiP2pChannel, new WifiP2pManager.GroupInfoListener() {
                            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                                if (wifiP2pGroup == null || !wifiP2pGroup.isGroupOwner()) {
                                    WifiDirectAgent.this.releaseCompletion(false);
                                } else {
                                    WifiDirectAgent.this.wifiP2pManager.removeGroup(WifiDirectAgent.this.wifiP2pChannel, new WifiP2pManager.ActionListener() {
                                        public void onSuccess() {
                                            WifiDirectAgent.this.releaseCompletion(true);
                                        }

                                        public void onFailure(int i) {
                                            WifiDirectAgent.this.releaseCompletion(false);
                                        }
                                    });
                                }
                            }
                        });
                        resetCompletion = WifiDirectAgent.this.waitForCompletion();
                    } catch (InterruptedException e) {
                        resetCompletion = WifiDirectAgent.this.receivedCompletionInterrupt(e);
                    }
                }
                return Boolean.valueOf(resetCompletion);
            }
        })).booleanValue();
    }

    public void setWifiP2pChannels(int i, int i2, WifiP2pManager.ActionListener actionListener) {
        Method declaredMethod = ClassUtil.getDeclaredMethod(getWifiP2pManager().getClass(), "setWifiP2pChannels", WifiP2pManager.Channel.class, Integer.TYPE, Integer.TYPE, WifiP2pManager.ActionListener.class);
        if (declaredMethod != null) {
            ClassUtil.invoke(getWifiP2pManager(), declaredMethod, getWifiP2pChannel(), Integer.valueOf(i), Integer.valueOf(i2), actionListener);
            return;
        }
        throw new RuntimeException("setWifiP2pChannels() is not supported on this device");
    }

    public void registerCallback(Callback callback) {
        this.callbacks.registerCallback(callback);
    }

    public void unregisterCallback(Callback callback) {
        this.callbacks.unregisterCallback(callback);
    }

    protected class WifiBroadcastReceiver extends BroadcastReceiver {
        protected static final String TAG = "NetDiscover_wifiDirectAgent_bcast";

        protected WifiBroadcastReceiver() {
        }

        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            action.hashCode();
            char c = 65535;
            switch (action.hashCode()) {
                case -1875733435:
                    if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                        c = 0;
                        break;
                    }
                    break;
                case -1772632330:
                    if (action.equals("android.net.wifi.p2p.CONNECTION_STATE_CHANGE")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1566767901:
                    if (action.equals("android.net.wifi.p2p.THIS_DEVICE_CHANGED")) {
                        c = 2;
                        break;
                    }
                    break;
                case -1394739139:
                    if (action.equals("android.net.wifi.p2p.PEERS_CHANGED")) {
                        c = 3;
                        break;
                    }
                    break;
                case -1331207498:
                    if (action.equals("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE")) {
                        c = 4;
                        break;
                    }
                    break;
                case 315025416:
                    if (action.equals(WifiDirectPersistentGroupManager.WIFI_P2P_PERSISTENT_GROUPS_CHANGED_ACTION)) {
                        c = 5;
                        break;
                    }
                    break;
                case 1695662461:
                    if (action.equals("android.net.wifi.p2p.STATE_CHANGED")) {
                        c = 6;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    WifiDirectAgent.this.wifiState = WifiState.from(intent.getIntExtra("wifi_state", 0));
                    RobotLog.m61vv(TAG, "wifiState=%s", WifiDirectAgent.this.wifiState);
                    break;
                case 1:
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    WifiDirectAgent.this.wifiP2pState = networkInfo.getState();
                    RobotLog.m43dd(TAG, "connection changed: networkInfo.state=%s", networkInfo.getState());
                    dump(networkInfo);
                    dump((WifiP2pInfo) intent.getParcelableExtra("wifiP2pInfo"));
                    dump((WifiP2pGroup) intent.getParcelableExtra("p2pGroupInfo"));
                    break;
                case 2:
                    dump((WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice"));
                    break;
                case 3:
                    dump((WifiP2pDeviceList) intent.getParcelableExtra("wifiP2pDeviceList"));
                    break;
                case 4:
                    RobotLog.m61vv(TAG, "p2p discoverPeers()=%s", Boolean.valueOf(intent.getIntExtra("discoveryState", 0) == 2));
                    break;
                case 5:
                    RobotLog.m60vv(TAG, "Wi-Fi Direct remembered groups cleared");
                    NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_NOTIFY_WIFI_DIRECT_REMEMBERED_GROUPS_CHANGED));
                    break;
                case 6:
                    WifiDirectAgent.this.isWifiP2pEnabled = intent.getIntExtra("wifi_p2p_state", 0) == 2;
                    RobotLog.m61vv(TAG, "wifiP2pEnabled=%s", Boolean.valueOf(WifiDirectAgent.this.isWifiP2pEnabled));
                    break;
            }
            WifiDirectAgent.this.callbacks.callbacksDo(new Consumer<Callback>() {
                public void accept(Callback callback) {
                    callback.onReceive(context, intent);
                }
            });
        }

        /* access modifiers changed from: protected */
        public void dump(WifiP2pDevice wifiP2pDevice) {
            RobotLog.m61vv(TAG, "this device changed: %s", WifiDirectAgent.format(wifiP2pDevice));
        }

        /* access modifiers changed from: protected */
        public void dump(WifiP2pDeviceList wifiP2pDeviceList) {
            ArrayList<WifiP2pDevice> arrayList = new ArrayList<>(wifiP2pDeviceList.getDeviceList());
            RobotLog.m60vv(TAG, "peers found: count=" + arrayList.size());
            for (WifiP2pDevice wifiP2pDevice : arrayList) {
                RobotLog.m60vv(TAG, "    peer: " + wifiP2pDevice.deviceAddress + " " + wifiP2pDevice.deviceName);
            }
        }

        /* access modifiers changed from: protected */
        public void dump(NetworkInfo networkInfo) {
            Assert.assertNotNull(networkInfo);
            RobotLog.m61vv(TAG, "NetworkInfo: %s", networkInfo.toString());
        }

        /* access modifiers changed from: protected */
        public void dump(WifiP2pInfo wifiP2pInfo) {
            Assert.assertNotNull(wifiP2pInfo);
            RobotLog.m61vv(TAG, "WifiP2pInfo: %s", wifiP2pInfo.toString());
        }

        /* access modifiers changed from: protected */
        public void dump(WifiP2pGroup wifiP2pGroup) {
            RobotLog.m61vv(TAG, "WifiP2pGroup: %s", wifiP2pGroup != null ? wifiP2pGroup.toString().replace("\n ", ", ") : "none");
        }
    }

    protected static String format(WifiP2pDevice wifiP2pDevice) {
        return wifiP2pDevice.toString().replace(": ", "=").replace("\n ", " ");
    }

    protected class ChannelListener implements WifiP2pManager.ChannelListener {
        public void onChannelDisconnected() {
        }

        protected ChannelListener() {
        }
    }
}
