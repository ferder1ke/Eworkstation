package com.example.myapplication;// MainActivity.java

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public  BluetoothSocket mmSocket;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private EditText dataDisplay;

    private  LineChart lineChart;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SerialPortService ID

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        Button connectButton = findViewById(R.id.connectButton);
        dataDisplay = findViewById(R.id.dataDisplay);
        Log.d("MyApp", "onCreate executed");

        lineChart = findViewById(R.id.lineChart);

        dataDisplay.append("Hello World!" + "\n");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("MissingPermission")
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    selectBluetoothDevice();
                }
            }
        });
    }

    public void setData(){
        float datas[] = {14f,15f,16f,17f,16f,16f};
        //在MPAndroidChart一般都是通过List<Entry>对象来装数据的
        List<Entry> entries = new ArrayList<Entry>();
        //循环取出数据
        for(int i = 0; i < datas.length; i++){
            entries.add(new Entry(i,datas[i]));
        }
        //一个LineDataSet对象就是一条曲线
        LineDataSet lineDataSet = new LineDataSet(entries,"第一条数据");
        //LineData才是正真给LineChart的数据
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
    }


    @Override
    @SuppressLint("MissingPermission")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                selectBluetoothDevice();
            } else {
                Toast.makeText(this, "为启动蓝牙", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            String address = data.getStringExtra("address");
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            try {
                mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                mmSocket.connect();
                startListening(mmSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startListening(BluetoothSocket socket) {
        try {
            Toast.makeText(this, "蓝牙链接成功", Toast.LENGTH_SHORT).show();
            InputStream mmInputStream = socket.getInputStream();
            BufferedReader mmBufferedReader = new BufferedReader(new InputStreamReader(mmInputStream));
            Thread listenThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            String receivedData = mmBufferedReader.readLine();
                            if (receivedData != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        float datas[] = {14f,15f,16f,17f,16f,16f};
                                        //在MPAndroidChart一般都是通过List<Entry>对象来装数据的
                                        List<Entry> entries = new ArrayList<Entry>();
                                        //循环取出数据
                                        for(int i = 0; i < datas.length; i++){
                                            entries.add(new Entry(i,datas[i]));
                                        }
                                        //一个LineDataSet对象就是一条曲线
                                        LineDataSet lineDataSet = new LineDataSet(entries,"第一条数据");
                                        //LineData才是正真给LineChart的数据
                                        LineData lineData = new LineData(lineDataSet);
                                        lineChart.setData(lineData);
                                        lineChart.invalidate(); // 刷新图表
                                        dataDisplay.append(receivedData + "\n");
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            listenThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    private void selectBluetoothDevice() {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, 2);
    }

    //蓝牙搜寻模式 这里就是mainActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.pkg_send){
            Intent intent = new Intent(this, BlueToothSender.class);
            if(mmSocket != null){
                String deviceAddress = this.mmSocket.getRemoteDevice().getAddress();
                intent.putExtra("device_address", deviceAddress);
            }
            startActivity(intent);
            return true;
        }else if(item.getItemId() == R.id.bluetooth_search){
            selectBluetoothDevice();
            return true;
        }
        return true;
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
