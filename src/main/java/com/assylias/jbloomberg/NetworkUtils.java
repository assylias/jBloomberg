package com.assylias.jbloomberg;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

final class NetworkUtils {
    private NetworkUtils () {}

    public static boolean isLocalhost(String host) {
        try {
            return isLocalhost(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    // (C) Kevin Brock @ http://stackoverflow.com/questions/2406341/how-to-check-if-an-ip-address-is-the-local-host-on-a-multi-homed-system
    public static boolean isLocalhost(InetAddress addr) {
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
            return true;
        }

        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
    }
}
