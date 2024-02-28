package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothManager {
    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @SuppressLint("MissingPermission")
    public static void sendHexData(String address, String hexData) {
        try {
            // 获取BluetoothAdapter
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                // 设备不支持蓝牙
                return;
            }
            // 获取远程蓝牙设备
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            // 创建RFCOMM通道
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
            // 连接蓝牙设备
            socket.connect();
            // 获取输出流
            OutputStream outputStream = socket.getOutputStream();

            // 将十六进制字符串转换为字节数组
            byte[] byteArray = new byte[hexData.length() / 2];
            for (int i = 0; i < hexData.length(); i += 2) {
                byteArray[i / 2] = (byte) Integer.parseInt(hexData.substring(i, i + 2), 16);
            }
            // 发送字节数组
            outputStream.write(byteArray);
            // 关闭Socket等资源
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            // 处理异常
            e.printStackTrace();
        }
    }
}
