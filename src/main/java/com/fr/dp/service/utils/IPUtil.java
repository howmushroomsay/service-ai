package com.fr.dp.service.utils;

import java.net.InetAddress;
import java.util.regex.Pattern;

public class IPUtil {
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final Pattern CIDR_PATTERN = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])"
                    + "/(\\d|[12]\\d|3[0-2])$");

    /**
     * 判断是否为合法的ip
     *
     * @param ip
     * @return
     */
    public static boolean isValidIp(String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 判断是否为合法的cidr
     *
     * @param cidr
     * @return
     */
    public static boolean isValidCidr(String cidr) {
        return CIDR_PATTERN.matcher(cidr).matches();
    }

    /**
     * 判断ip是否在ip段内
     *
     * @param ip   ip地址
     * @param cidr ip段
     * @return
     */
    public static boolean isIpInRange(String ip, String cidr) {
        try {
            String[] cidrParts = cidr.split("/");
            int prefixLength = Integer.parseInt(cidrParts[1]);
            InetAddress addr = InetAddress.getByName(ip);
            byte[] ipBytes = addr.getAddress();
            int ipValue = ((ipBytes[0] & 0xFF) << 24) |
                    ((ipBytes[1] & 0xFF) << 16) |
                    ((ipBytes[2] & 0xFF) << 8) |
                    (ipBytes[3] & 0xFF);
            int netMask = -(1 << (32 - prefixLength));
            InetAddress netMaskAddr = InetAddress.getByName(
                    String.format("%d.%d.%d.%d",
                            (netMask >>> 24) & 0xFF,
                            (netMask >>> 16) & 0xFF,
                            (netMask >>> 8) & 0xFF,
                            netMask & 0xFF));
            byte[] netMaskBytes = netMaskAddr.getAddress();
            int netMaskValue = ((netMaskBytes[0] & 0xFF) << 24) |
                    ((netMaskBytes[1] & 0xFF) << 16) |
                    ((netMaskBytes[2] & 0xFF) << 8) |
                    (netMaskBytes[3] & 0xFF);
            int networkAddress = ipValue & netMaskValue;
            int broadcastAddress = networkAddress | ~netMaskValue;
            int cidrAddress = InetAddress.getByName(cidrParts[0]).hashCode();
            return (ipValue & netMaskValue) == (cidrAddress & netMaskValue)
                    && ipValue != networkAddress
                    && ipValue != broadcastAddress;
        } catch (Exception e) {
            return false;
        }
    }


}
