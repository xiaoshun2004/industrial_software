package com.scut.industrial_software.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class MachineInfoUtils {

    /**
     *  获取机器的MAC地址
     *
     * @return
     */
    public static String getMachineAddr() {
        String macAddress = null;
        try{
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                //忽略回环接口
                if (networkInterface.isLoopback()|| networkInterface.isUp()) {
                    continue;
                }

                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : mac) {
                        sb.append(String.format("%02X:", b));
                    }
                    macAddress = sb.substring(0, sb.length() - 1);
                    break;
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return macAddress;
    }

    /**
     * 获取CPU序列号
     *
     * @return
     */
    public static String getCPUSerial(){
        return System.getenv("PROCESSOR_IDENTIFIER");
    }

    /**
     * 获取主板序列号
     *
     * @return
     */
    public static String getMainBoardSerial() {
        String mainBoardSerial = null;
        try {
            //使用powershell获取主板序列号
            ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-Command",
                    "Get-CimInstance -ClassName Win32_BaseBoard | Select-Object SerialNumber | Format-List"
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "GBK"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // 提取 SerialNumber 的值
                String result = output.toString().trim();
                if (result.contains("SerialNumber")) {
                    mainBoardSerial = result.split(":")[1].trim();
                }
            } else {
                System.err.println("PowerShell 执行失败，退出码：" + exitCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainBoardSerial;
    }

    /**
     * 获取主机名
     *
     * @return
     */
    public static String getHostName() throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();
        return address.getHostName();
    }
}
