package com.qualcomm.robotcore.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class Network {
    public static InetAddress getLoopbackAddress() {
        try {
            return InetAddress.getByAddress(new byte[]{Byte.MAX_VALUE, 0, 0, 1});
        } catch (UnknownHostException unused) {
            return null;
        }
    }

    public static ArrayList<InetAddress> getLocalIpAddresses() {
        ArrayList<InetAddress> arrayList = new ArrayList<>();
        try {
            Iterator<T> it = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();
            while (it.hasNext()) {
                arrayList.addAll(Collections.list(((NetworkInterface) it.next()).getInetAddresses()));
            }
        } catch (SocketException unused) {
        }
        return arrayList;
    }

    public static ArrayList<InetAddress> getLocalIpAddress(String str) {
        ArrayList<InetAddress> arrayList = new ArrayList<>();
        try {
            Iterator<T> it = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();
            while (it.hasNext()) {
                NetworkInterface networkInterface = (NetworkInterface) it.next();
                if (networkInterface.getName() == str) {
                    arrayList.addAll(Collections.list(networkInterface.getInetAddresses()));
                }
            }
        } catch (SocketException unused) {
        }
        return arrayList;
    }

    public static ArrayList<InetAddress> removeIPv6Addresses(Collection<InetAddress> collection) {
        ArrayList<InetAddress> arrayList = new ArrayList<>();
        for (InetAddress next : collection) {
            if (next instanceof Inet4Address) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public static ArrayList<InetAddress> removeIPv4Addresses(Collection<InetAddress> collection) {
        ArrayList<InetAddress> arrayList = new ArrayList<>();
        for (InetAddress next : collection) {
            if (next instanceof Inet6Address) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public static ArrayList<InetAddress> removeLoopbackAddresses(Collection<InetAddress> collection) {
        ArrayList<InetAddress> arrayList = new ArrayList<>();
        for (InetAddress next : collection) {
            if (!next.isLoopbackAddress()) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public static ArrayList<String> getHostAddresses(Collection<InetAddress> collection) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (InetAddress hostAddress : collection) {
            String hostAddress2 = hostAddress.getHostAddress();
            if (hostAddress2.contains("%")) {
                hostAddress2 = hostAddress2.substring(0, hostAddress2.indexOf(37));
            }
            arrayList.add(hostAddress2);
        }
        return arrayList;
    }
}
