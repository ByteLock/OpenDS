package org.firstinspires.ftc.robotcore.internal.network;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import com.qualcomm.robotcore.util.ClassUtil;
import com.qualcomm.robotcore.util.RobotLog;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import org.firstinspires.ftc.robotcore.external.Func;

public class WifiDirectPersistentGroupManager extends WifiStartStoppable {
    public static final String TAG = "WifiDirectPersistentGroupManager";
    public static final String WIFI_P2P_PERSISTENT_GROUPS_CHANGED_ACTION = "android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED";
    protected static Class classPersistentGroupInfoListener;
    protected static Class classWifiP2pGroupList;
    protected static Method methodDeletePersistentGroup;
    protected static Method methodGetGroupList;
    protected static Method methodGetNetworkId;
    protected static Method methodRequestPersistentGroupInfo;

    public interface PersistentGroupInfoListener {
        void onPersistentGroupInfoAvailable(Collection<WifiP2pGroup> collection);
    }

    /* access modifiers changed from: protected */
    public boolean doStart() throws InterruptedException {
        return true;
    }

    /* access modifiers changed from: protected */
    public void doStop() throws InterruptedException {
    }

    public String getTag() {
        return TAG;
    }

    static {
        try {
            classWifiP2pGroupList = Class.forName("android.net.wifi.p2p.WifiP2pGroupList");
            classPersistentGroupInfoListener = Class.forName("android.net.wifi.p2p.WifiP2pManager$PersistentGroupInfoListener");
            methodGetGroupList = ClassUtil.getDeclaredMethod(classWifiP2pGroupList, "getGroupList", new Class[0]);
            methodRequestPersistentGroupInfo = ClassUtil.getDeclaredMethod(WifiP2pManager.class, "requestPersistentGroupInfo", WifiP2pManager.Channel.class, classPersistentGroupInfoListener);
            methodDeletePersistentGroup = ClassUtil.getDeclaredMethod(WifiP2pManager.class, "deletePersistentGroup", WifiP2pManager.Channel.class, Integer.TYPE, WifiP2pManager.ActionListener.class);
            methodGetNetworkId = ClassUtil.getDeclaredMethod(WifiP2pGroup.class, "getNetworkId", new Class[0]);
        } catch (ClassNotFoundException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "exception thrown in static initialization");
        }
    }

    public WifiDirectPersistentGroupManager(WifiDirectAgent wifiDirectAgent) {
        super(wifiDirectAgent);
    }

    public void deletePersistentGroup(int i, WifiP2pManager.ActionListener actionListener) {
        RobotLog.m61vv(TAG, "deletePersistentGroup() netId=%d", Integer.valueOf(i));
        ClassUtil.invoke(this.wifiDirectAgent.getWifiP2pManager(), methodDeletePersistentGroup, this.wifiDirectAgent.getWifiP2pChannel(), Integer.valueOf(i), actionListener);
    }

    public boolean deletePersistentGroup(final int i) {
        return ((Boolean) lockCompletion(false, new Func<Boolean>() {
            public Boolean value() {
                boolean z;
                WifiDirectPersistentGroupManager.this.resetCompletion();
                try {
                    WifiDirectPersistentGroupManager.this.deletePersistentGroup(i, new WifiP2pManager.ActionListener() {
                        public void onSuccess() {
                            WifiDirectPersistentGroupManager.this.releaseCompletion(true);
                        }

                        public void onFailure(int i) {
                            RobotLog.m61vv(WifiDirectPersistentGroupManager.TAG, "failed to delete persistent group: netId=%d", Integer.valueOf(i));
                            WifiDirectPersistentGroupManager.this.releaseCompletion(false);
                        }
                    });
                    z = WifiDirectPersistentGroupManager.this.waitForCompletion();
                } catch (InterruptedException e) {
                    z = WifiDirectPersistentGroupManager.this.receivedCompletionInterrupt(e);
                }
                return Boolean.valueOf(z);
            }
        })).booleanValue();
    }

    public boolean deletePersistentGroup(WifiP2pGroup wifiP2pGroup) {
        return deletePersistentGroup(getNetworkId(wifiP2pGroup));
    }

    public void deleteAllPersistentGroups() {
        for (WifiP2pGroup deletePersistentGroup : getPersistentGroups()) {
            deletePersistentGroup(deletePersistentGroup);
        }
    }

    public int getNetworkId(WifiP2pGroup wifiP2pGroup) {
        return ((Integer) ClassUtil.invoke(wifiP2pGroup, methodGetNetworkId, new Object[0])).intValue();
    }

    /* access modifiers changed from: protected */
    public Object createProxy(final PersistentGroupInfoListener persistentGroupInfoListener) {
        return Proxy.newProxyInstance(classPersistentGroupInfoListener.getClassLoader(), new Class[]{classPersistentGroupInfoListener}, new InvocationHandler() {
            public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
                if (!method.getName().equals("onPersistentGroupInfoAvailable")) {
                    return null;
                }
                persistentGroupInfoListener.onPersistentGroupInfoAvailable((Collection) ClassUtil.invoke(objArr[0], WifiDirectPersistentGroupManager.methodGetGroupList, new Object[0]));
                return null;
            }
        });
    }

    public void requestPersistentGroups(PersistentGroupInfoListener persistentGroupInfoListener) {
        Object createProxy = createProxy(persistentGroupInfoListener);
        ClassUtil.invoke(this.wifiDirectAgent.getWifiP2pManager(), methodRequestPersistentGroupInfo, this.wifiDirectAgent.getWifiP2pChannel(), createProxy);
    }

    public Collection<WifiP2pGroup> getPersistentGroups() {
        final ArrayList arrayList = new ArrayList();
        return (Collection) lockCompletion(arrayList, new Func<Collection<WifiP2pGroup>>() {
            Collection<WifiP2pGroup> result;

            public Collection<WifiP2pGroup> value() {
                this.result = arrayList;
                WifiDirectPersistentGroupManager.this.resetCompletion();
                try {
                    WifiDirectPersistentGroupManager.this.requestPersistentGroups(new PersistentGroupInfoListener() {
                        public void onPersistentGroupInfoAvailable(Collection<WifiP2pGroup> collection) {
                            C11143.this.result = collection;
                            WifiDirectPersistentGroupManager.this.releaseCompletion(true);
                        }
                    });
                    WifiDirectPersistentGroupManager.this.waitForCompletion();
                } catch (InterruptedException e) {
                    WifiDirectPersistentGroupManager.this.receivedCompletionInterrupt(e);
                }
                return this.result;
            }
        });
    }
}
