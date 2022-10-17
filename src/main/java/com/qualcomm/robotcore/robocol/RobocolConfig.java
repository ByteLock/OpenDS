package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.util.Network;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class RobocolConfig {
    public static final int MAX_MAX_PACKET_SIZE = 65520;
    public static final int MS_RECEIVE_TIMEOUT = 300;
    public static final int PORT_NUMBER = 20884;
    public static final int ROBOCOL_VERSION = 123;
    public static final int SLASH_24_SUBNET_MASK = -256;
    public static final int TIMEOUT = 1000;
    public static final int TTL = 3;

    public static InetAddress determineBindAddress(InetAddress inetAddress) {
        ArrayList<InetAddress> removeIPv6Addresses = Network.removeIPv6Addresses(Network.removeLoopbackAddresses(Network.getLocalIpAddresses()));
        Iterator<InetAddress> it = removeIPv6Addresses.iterator();
        while (it.hasNext()) {
            InetAddress next = it.next();
            try {
                Enumeration<InetAddress> inetAddresses = NetworkInterface.getByInetAddress(next).getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress nextElement = inetAddresses.nextElement();
                    if (nextElement.equals(inetAddress)) {
                        return nextElement;
                    }
                }
                continue;
            } catch (SocketException unused) {
                RobotLog.m58v(String.format("socket exception while trying to get network interface of %s", new Object[]{next.getHostAddress()}));
            }
        }
        return determineBindAddressBasedOnSubnet(removeIPv6Addresses, inetAddress);
    }

    public static InetAddress determineBindAddressBasedOnSubnet(ArrayList<InetAddress> arrayList, InetAddress inetAddress) {
        int byteArrayToInt = TypeConversion.byteArrayToInt(inetAddress.getAddress());
        Iterator<InetAddress> it = arrayList.iterator();
        while (it.hasNext()) {
            InetAddress next = it.next();
            if ((TypeConversion.byteArrayToInt(next.getAddress()) & -256) == (byteArrayToInt & -256)) {
                return next;
            }
        }
        return Network.getLoopbackAddress();
    }

    public static InetAddress determineBindAddressBasedOnIsReachable(ArrayList<InetAddress> arrayList, InetAddress inetAddress) {
        Iterator<InetAddress> it = arrayList.iterator();
        while (it.hasNext()) {
            InetAddress next = it.next();
            try {
                if (next.isReachable(NetworkInterface.getByInetAddress(next), 3, 1000)) {
                    return next;
                }
            } catch (SocketException unused) {
                RobotLog.m58v(String.format("socket exception while trying to get network interface of %s", new Object[]{next.getHostAddress()}));
            } catch (IOException unused2) {
                RobotLog.m58v(String.format("IO exception while trying to determine if %s is reachable via %s", new Object[]{inetAddress.getHostAddress(), next.getHostAddress()}));
            }
        }
        return Network.getLoopbackAddress();
    }
}
