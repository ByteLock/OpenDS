package com.qualcomm.ftccommon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.qualcomm.ftccommon.configuration.EditActivity;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.USBAccessibleLynxModule;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.network.CallbackResult;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.ui.UILocation;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.Assert;

public class FtcLynxModuleAddressUpdateActivity extends EditActivity {
    public static final String TAG = "FtcLynxModuleAddressUpdateActivity";
    protected BlockingQueue<RobotCoreCommandList.USBAccessibleLynxModulesResp> availableLynxModules = new ArrayBlockingQueue(1);
    protected List<USBAccessibleLynxModule> currentModules = new ArrayList();
    protected DisplayedModuleList displayedModuleList = new DisplayedModuleList();
    DialogInterface.OnClickListener doNothingAndCloseListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialogInterface, int i) {
        }
    };
    protected int msResponseWait = 10000;
    protected NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    protected RecvLoopRunnable.RecvLoopCallback recvLoopCallback = new ReceiveLoopCallback();

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_ftc_lynx_address_update);
        this.networkConnectionHandler.pushReceiveLoopCallback(this.recvLoopCallback);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        AppUtil.getInstance().showWaitCursor(getString(C0470R.string.dialogMessagePleaseWait), new Runnable() {
            public void run() {
                FtcLynxModuleAddressUpdateActivity ftcLynxModuleAddressUpdateActivity = FtcLynxModuleAddressUpdateActivity.this;
                ftcLynxModuleAddressUpdateActivity.currentModules = ftcLynxModuleAddressUpdateActivity.getUSBAccessibleLynxModules();
                Iterator<USBAccessibleLynxModule> it = FtcLynxModuleAddressUpdateActivity.this.currentModules.iterator();
                while (it.hasNext()) {
                    USBAccessibleLynxModule next = it.next();
                    if (!next.isModuleAddressChangeable() && next.getSerialNumber().isEmbedded()) {
                        it.remove();
                    }
                }
            }
        }, new Runnable() {
            public void run() {
                FtcLynxModuleAddressUpdateActivity.this.displayedModuleList.initialize(FtcLynxModuleAddressUpdateActivity.this.currentModules);
                TextView textView = (TextView) FtcLynxModuleAddressUpdateActivity.this.findViewById(C0470R.C0472id.lynxAddressListInstructions);
                if (FtcLynxModuleAddressUpdateActivity.this.currentModules.isEmpty()) {
                    textView.setText(FtcLynxModuleAddressUpdateActivity.this.getString(C0470R.string.lynx_address_instructions_no_devices));
                } else {
                    textView.setText(FtcLynxModuleAddressUpdateActivity.this.getString(C0470R.string.lynx_address_instructions_update));
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.networkConnectionHandler.removeReceiveLoopCallback(this.recvLoopCallback);
    }

    protected class DisplayedModuleList {
        protected AddressConfiguration currentAddressConfiguration;
        protected int lastModuleAddressChoice = 10;
        protected ViewGroup moduleList;

        protected DisplayedModuleList() {
            this.currentAddressConfiguration = new AddressConfiguration();
        }

        public void initialize(List<USBAccessibleLynxModule> list) {
            ViewGroup viewGroup = (ViewGroup) FtcLynxModuleAddressUpdateActivity.this.findViewById(C0470R.C0472id.moduleList);
            this.moduleList = viewGroup;
            viewGroup.removeAllViews();
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i <= this.lastModuleAddressChoice; i++) {
                arrayList.add(Integer.valueOf(i));
            }
            for (USBAccessibleLynxModule next : list) {
                if (!next.isModuleAddressChangeable()) {
                    arrayList.remove(Integer.valueOf(next.getModuleAddress()));
                }
            }
            for (USBAccessibleLynxModule next2 : list) {
                Assert.assertTrue(next2.getModuleAddress() != 0);
                if (size() + 1 >= arrayList.size() - 1) {
                    break;
                }
                add(next2, arrayList);
            }
            this.currentAddressConfiguration = new AddressConfiguration(list);
        }

        /* access modifiers changed from: protected */
        public int size() {
            return this.moduleList.getChildCount();
        }

        /* access modifiers changed from: protected */
        public void add(USBAccessibleLynxModule uSBAccessibleLynxModule, List<Integer> list) {
            View inflate = LayoutInflater.from(FtcLynxModuleAddressUpdateActivity.this.context).inflate(C0470R.layout.lynx_module_configure_address, (ViewGroup) null);
            this.moduleList.addView(inflate);
            new DisplayedModule(inflate).initialize(uSBAccessibleLynxModule, list);
        }

        /* access modifiers changed from: protected */
        public DisplayedModule from(SerialNumber serialNumber) {
            ViewGroup viewGroup = (ViewGroup) FtcLynxModuleAddressUpdateActivity.this.findViewById(C0470R.C0472id.moduleList);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                DisplayedModule displayedModule = new DisplayedModule(viewGroup.getChildAt(i));
                if (displayedModule.getSerialNumber().equals((Object) serialNumber)) {
                    return displayedModule;
                }
            }
            return null;
        }

        public void changeAddress(SerialNumber serialNumber, int i) {
            boolean z = false;
            RobotLog.m61vv(FtcLynxModuleAddressUpdateActivity.TAG, "changeAddress(%s) from:%d to:%d", serialNumber, Integer.valueOf(this.currentAddressConfiguration.getCurrentAddress(serialNumber)), Integer.valueOf(i));
            if (this.currentAddressConfiguration.getCurrentAddress(serialNumber) != i) {
                SerialNumber findByCurrentAddress = this.currentAddressConfiguration.findByCurrentAddress(i);
                this.currentAddressConfiguration.putCurrentAddress(serialNumber, i);
                if (findByCurrentAddress != null) {
                    int findUnusedAddress = findUnusedAddress();
                    RobotLog.m61vv(FtcLynxModuleAddressUpdateActivity.TAG, "conflict with %s: that goes to %d", findByCurrentAddress, Integer.valueOf(findUnusedAddress));
                    if (findUnusedAddress != 0) {
                        z = true;
                    }
                    Assert.assertTrue(z);
                    this.currentAddressConfiguration.putCurrentAddress(findByCurrentAddress, findUnusedAddress);
                    from(findByCurrentAddress).setNewAddress(findUnusedAddress);
                }
            }
        }

        /* access modifiers changed from: protected */
        public int findUnusedAddress() {
            for (int i = 1; i <= this.lastModuleAddressChoice; i++) {
                if (!this.currentAddressConfiguration.containsCurrentAddress(i)) {
                    return i;
                }
            }
            return 0;
        }
    }

    protected class DisplayedModule {
        Spinner spinner;
        View view;

        public DisplayedModule(View view2) {
            this.view = view2;
            this.spinner = (Spinner) view2.findViewById(C0470R.C0472id.spinnerChooseAddress);
        }

        public SerialNumber getSerialNumber() {
            return (SerialNumber) ((TextView) this.view.findViewById(C0470R.C0472id.moduleSerialText)).getTag();
        }

        public void initialize(USBAccessibleLynxModule uSBAccessibleLynxModule, List<Integer> list) {
            TextView textView = (TextView) this.view.findViewById(C0470R.C0472id.moduleSerialText);
            textView.setText(uSBAccessibleLynxModule.getSerialNumber().toString());
            textView.setTag(uSBAccessibleLynxModule.getSerialNumber());
            ((TextView) this.view.findViewById(C0470R.C0472id.moduleAddressText)).setText(FtcLynxModuleAddressUpdateActivity.this.getString(C0470R.string.lynx_address_format_module_address, new Object[]{Integer.valueOf(uSBAccessibleLynxModule.getModuleAddress())}));
            boolean isModuleAddressChangeable = uSBAccessibleLynxModule.isModuleAddressChangeable();
            this.spinner.setEnabled(isModuleAddressChangeable);
            initializeSpinnerList(this.spinner, list, isModuleAddressChangeable);
            this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onNothingSelected(AdapterView<?> adapterView) {
                }

                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                    int i2 = ((AddressAndDisplayName) adapterView.getItemAtPosition(i)).address;
                    if (i2 == DisplayedModule.this.getStartingAddress()) {
                        DisplayedModule.this.selectNoChange();
                    } else if (i2 == 0) {
                        i2 = DisplayedModule.this.getStartingAddress();
                    }
                    FtcLynxModuleAddressUpdateActivity.this.displayedModuleList.changeAddress(DisplayedModule.this.getSerialNumber(), i2);
                }
            });
        }

        public void setNewAddress(int i) {
            RobotLog.m61vv(FtcLynxModuleAddressUpdateActivity.TAG, "setNewAddress(%s)=%d", getSerialNumber(), Integer.valueOf(i));
            if (i == getStartingAddress()) {
                selectNoChange();
                return;
            }
            for (int i2 = 0; i2 < this.spinner.getAdapter().getCount(); i2++) {
                if (getItem(i2).address == i) {
                    this.spinner.setSelection(i2);
                    return;
                }
            }
        }

        /* access modifiers changed from: protected */
        public void selectNoChange() {
            RobotLog.m61vv(FtcLynxModuleAddressUpdateActivity.TAG, "selectNoChange(%s)", getSerialNumber());
            this.spinner.setSelection(0);
        }

        /* access modifiers changed from: protected */
        public AddressAndDisplayName getItem(int i) {
            return (AddressAndDisplayName) this.spinner.getAdapter().getItem(i);
        }

        public int getCurrentAddress() {
            return FtcLynxModuleAddressUpdateActivity.this.displayedModuleList.currentAddressConfiguration.getCurrentAddress(getSerialNumber());
        }

        public int getStartingAddress() {
            return FtcLynxModuleAddressUpdateActivity.this.displayedModuleList.currentAddressConfiguration.getStartingAddress(getSerialNumber());
        }

        /* access modifiers changed from: protected */
        public void initializeSpinnerList(Spinner spinner2, List<Integer> list, boolean z) {
            AddressAndDisplayName[] addressAndDisplayNameArr = new AddressAndDisplayName[list.size()];
            for (int i = 0; i < list.size(); i++) {
                addressAndDisplayNameArr[i] = new AddressAndDisplayName(list.get(i).intValue(), z);
            }
            Arrays.sort(addressAndDisplayNameArr);
            spinner2.setAdapter(new ArrayAdapter(FtcLynxModuleAddressUpdateActivity.this, C0470R.layout.lynx_module_configure_address_spin_item, addressAndDisplayNameArr));
        }
    }

    protected class AddressAndDisplayName implements Comparable<AddressAndDisplayName> {
        public final int address;
        public final String displayName;

        public AddressAndDisplayName(int i, boolean z) {
            String str;
            this.address = i;
            if (i == 0) {
                str = FtcLynxModuleAddressUpdateActivity.this.getString(z ? C0470R.string.lynx_address_format_no_change : C0470R.string.lynx_address_format_not_changeable);
            } else {
                str = FtcLynxModuleAddressUpdateActivity.this.getString(C0470R.string.lynx_address_format_new_module_address, new Object[]{Integer.valueOf(i)});
            }
            this.displayName = str;
        }

        public String toString() {
            return this.displayName;
        }

        public int compareTo(AddressAndDisplayName addressAndDisplayName) {
            return this.address - addressAndDisplayName.address;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDirty() {
        for (USBAccessibleLynxModule serialNumber : this.currentModules) {
            DisplayedModule from = this.displayedModuleList.from(serialNumber.getSerialNumber());
            if (from.getStartingAddress() != from.getCurrentAddress()) {
                return true;
            }
        }
        return false;
    }

    public void onDoneButtonPressed(View view) {
        RobotLog.m60vv(TAG, "onDoneButtonPressed()");
        ArrayList<CommandList.LynxAddressChangeRequest.AddressChange> arrayList = new ArrayList<>();
        for (USBAccessibleLynxModule serialNumber : this.currentModules) {
            DisplayedModule from = this.displayedModuleList.from(serialNumber.getSerialNumber());
            if (from.getStartingAddress() != from.getCurrentAddress()) {
                CommandList.LynxAddressChangeRequest.AddressChange addressChange = new CommandList.LynxAddressChangeRequest.AddressChange();
                addressChange.serialNumber = from.getSerialNumber();
                addressChange.oldAddress = from.getStartingAddress();
                addressChange.newAddress = from.getCurrentAddress();
                arrayList.add(addressChange);
            }
        }
        if (this.currentModules.size() > 0) {
            if (arrayList.size() > 0) {
                CommandList.LynxAddressChangeRequest lynxAddressChangeRequest = new CommandList.LynxAddressChangeRequest();
                lynxAddressChangeRequest.modulesToChange = arrayList;
                sendOrInject(new Command(CommandList.CMD_LYNX_ADDRESS_CHANGE, lynxAddressChangeRequest.serialize()));
            } else {
                AppUtil.getInstance().showToast(UILocation.BOTH, getString(C0470R.string.toastLynxAddressChangeNothingToDo));
            }
        }
        finishOk();
    }

    public void onCancelButtonPressed(View view) {
        RobotLog.m60vv(TAG, "onCancelButtonPressed()");
        doBackOrCancel();
    }

    public void onBackPressed() {
        RobotLog.m60vv(TAG, "onBackPressed()");
        doBackOrCancel();
    }

    /* access modifiers changed from: protected */
    public void doBackOrCancel() {
        if (isDirty()) {
            C04594 r0 = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    FtcLynxModuleAddressUpdateActivity.this.finishCancel();
                }
            };
            AlertDialog.Builder buildBuilder = this.utility.buildBuilder(getString(C0470R.string.saveChangesTitle), getString(C0470R.string.saveChangesMessageScreen));
            buildBuilder.setPositiveButton(C0470R.string.buttonExitWithoutSaving, r0);
            buildBuilder.setNegativeButton(C0470R.string.buttonNameCancel, this.doNothingAndCloseListener);
            buildBuilder.show();
            return;
        }
        finishCancel();
    }

    protected class AddressConfiguration {
        protected Map<SerialNumber, Integer> current = new ConcurrentHashMap();
        protected Map<SerialNumber, Integer> starting = new ConcurrentHashMap();

        public AddressConfiguration() {
        }

        public AddressConfiguration(List<USBAccessibleLynxModule> list) {
            for (USBAccessibleLynxModule next : list) {
                this.starting.put(next.getSerialNumber(), Integer.valueOf(next.getModuleAddress()));
                this.current.put(next.getSerialNumber(), Integer.valueOf(next.getModuleAddress()));
            }
        }

        public int getStartingAddress(SerialNumber serialNumber) {
            return this.starting.get(serialNumber).intValue();
        }

        public boolean containsCurrentAddress(int i) {
            return this.current.values().contains(Integer.valueOf(i));
        }

        public void putCurrentAddress(SerialNumber serialNumber, int i) {
            this.current.put(serialNumber, Integer.valueOf(i));
        }

        public int getCurrentAddress(SerialNumber serialNumber) {
            return this.current.get(serialNumber).intValue();
        }

        public SerialNumber findByCurrentAddress(int i) {
            for (Map.Entry next : this.current.entrySet()) {
                if (((Integer) next.getValue()).equals(Integer.valueOf(i))) {
                    return (SerialNumber) next.getKey();
                }
            }
            return null;
        }
    }

    protected class ReceiveLoopCallback extends RecvLoopRunnable.DegenerateCallback {
        protected ReceiveLoopCallback() {
        }

        public CallbackResult commandEvent(Command command) throws RobotCoreException {
            String name = command.getName();
            name.hashCode();
            if (!name.equals(RobotCoreCommandList.CMD_GET_USB_ACCESSIBLE_LYNX_MODULES_RESP)) {
                return super.commandEvent(command);
            }
            FtcLynxModuleAddressUpdateActivity.this.availableLynxModules.offer(RobotCoreCommandList.USBAccessibleLynxModulesResp.deserialize(command.getExtra()));
            return CallbackResult.HANDLED_CONTINUE;
        }
    }

    /* access modifiers changed from: protected */
    public List<USBAccessibleLynxModule> getUSBAccessibleLynxModules() {
        RobotCoreCommandList.USBAccessibleLynxModulesRequest uSBAccessibleLynxModulesRequest = new RobotCoreCommandList.USBAccessibleLynxModulesRequest();
        RobotCoreCommandList.USBAccessibleLynxModulesResp uSBAccessibleLynxModulesResp = new RobotCoreCommandList.USBAccessibleLynxModulesResp();
        this.availableLynxModules.clear();
        uSBAccessibleLynxModulesRequest.forFirmwareUpdate = true;
        sendOrInject(new Command(RobotCoreCommandList.CMD_GET_USB_ACCESSIBLE_LYNX_MODULES, uSBAccessibleLynxModulesRequest.serialize()));
        RobotCoreCommandList.USBAccessibleLynxModulesResp uSBAccessibleLynxModulesResp2 = (RobotCoreCommandList.USBAccessibleLynxModulesResp) awaitResponse(this.availableLynxModules, uSBAccessibleLynxModulesResp);
        RobotLog.m61vv(TAG, "found %d lynx modules", Integer.valueOf(uSBAccessibleLynxModulesResp2.modules.size()));
        return uSBAccessibleLynxModulesResp2.modules;
    }

    /* access modifiers changed from: protected */
    public <T> T awaitResponse(BlockingQueue<T> blockingQueue, T t) {
        return awaitResponse(blockingQueue, t, (long) this.msResponseWait, TimeUnit.MILLISECONDS);
    }

    /* access modifiers changed from: protected */
    public <T> T awaitResponse(BlockingQueue<T> blockingQueue, T t, long j, TimeUnit timeUnit) {
        try {
            T poll = blockingQueue.poll(j, timeUnit);
            if (poll != null) {
                return poll;
            }
            return t;
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
    }
}
