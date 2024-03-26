package com.example.myapplication;// MainActivity.java

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Fix Flashback when it receive Pkg
public class MainActivity extends AppCompatActivity {

    public  BluetoothSocket mmSocket;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private EditText dataDisplay;

    private  LineChart lineChart;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SerialPortService ID

    private Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Button pictureReset = findViewById(R.id.picture_reset);
        dataDisplay = findViewById(R.id.dataDisplay);

        lineChart = findViewById(R.id.lineChart);
        List<Entry> entries = new ArrayList<>();
        LineDataSet dataSet = new LineDataSet(entries, "Real-time Data");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE); // 设置横坐标轴标签颜色为白色

        xAxis.setDrawGridLines(false);
        //xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // 自定义横坐标标签格式

                DecimalFormat decimalFormat = new DecimalFormat("#.##"); // 创建小数点后两位的格式化器
                String valuenew = decimalFormat.format(value); // 将 float 数格式化为字符串

                return valuenew + "V";
            }
        });

        YAxis leftYAxis = lineChart.getAxisLeft();
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setTextColor(Color.WHITE);// 设置纵坐标轴标签颜色为白色

        leftYAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // 自定义纵坐标标签格式
                DecimalFormat decimalFormat = new DecimalFormat("#.##"); // 创建小数点后两位的格式化器
                String valuenew = decimalFormat.format(value); // 将 float 数格式化为字符串
                return valuenew + "A";
            }
        });
        YAxis rightYAxis = lineChart.getAxisRight();
        rightYAxis.setEnabled(false);

        lineChart.setDragEnabled(true); // 允许拖动
        lineChart.setScaleEnabled(true); // 允许缩放

        Description description = new Description();//这六行代码用来调试显示文本
        description.setText("电压/电流");
        description.setEnabled(true);
        description.setTextColor(Color.WHITE);//颜色的代码
        description.setPosition(500f, 20f); // 设置描述文本的位置坐标
        lineChart.setDescription(description);

        dataSet.setDrawValues(false);// 禁止数据点的数值显示

        dataDisplay.append("Hello World!" + "\n");


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
        pictureReset.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("MissingPermission")
            public void onClick(View v) {
                lineChart.clear();
                List<Entry> entries = new ArrayList<>();
                LineDataSet dataSet = new LineDataSet(entries, "Real-time Data");
                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                lineChart.invalidate();
            }
        });
    }

    public void updateChart(float x, float y) {
        LineData lineData = lineChart.getLineData();
        LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(0);

        Entry newEntry = new Entry(x, y);
        dataSet.addEntry(newEntry);
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public float getXVal(byte Neg, byte[] voltage) {
        float ans = 0;
        for (int i = 0; i < voltage.length; i++) {
            float value = voltage[i] & 0xFF; // 将字节转换为无符号整数
            ans += value * Math.pow(16 * 16, i); // 计算每个字节对应的十进制值，并累加到结果中
        }
        if((Neg & 0x10) == 0x10)
            return -1 * ans;

        ans = ans/1000;//把横轴数据缩小1000倍

        return ans;
    }

    public float getYVal(byte Neg, byte[] current) {
        float ans = 0;
        for (int i = 0; i < current.length; i++) {
            float value = current[i] & 0xFF; // 将字节转换为无符号整数
            ans += value * Math.pow(16 * 16, i); // 计算每个字节对应的十进制值，并累加到结果中
        }
        if((Neg & 0x01) == 0x01)
            return -1 * ans;

        ans = ans/1000000;//把uA换成A

        return ans;
    }

    byte[] string2ByteArray(String str) {
        byte[] temp = new byte[str.length() / 2];

        for (int i = 0; i < str.length(); i += 2) {
            String hex = str.substring(i, i + 2);
            temp[i / 2] = (byte) Integer.parseInt(hex, 16);
        }
        return  temp;
    }

    byte[] getSpecByteArray(int index, int len, byte[] src) {
        byte[] byteArray = new byte[len];
        for(int i = index; i < index + len; ++i) {
            byteArray[i - index] = src[i];
        }
        return byteArray;
    }
    public void setData(String bufferLine){
        byte[] src = string2ByteArray(bufferLine);
        byte[] voltage = getSpecByteArray(4, 4, src);
        byte[] current = getSpecByteArray(8, 4, src);
        byte Neg = src[3];
        updateChart(getXVal(Neg, voltage), getYVal(Neg, current));
    }

    @Override
    @SuppressLint("MissingPermission")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                selectBluetoothDevice();
            } else {
                Toast.makeText(this, "未启动蓝牙", Toast.LENGTH_SHORT).show();
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
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            String receivedData = mmBufferedReader.readLine();
                            if (receivedData != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        String tmp = receivedData.replaceAll("\\s", "");
                                        if(tmp.length() != 26)
                                            return;
                                        setData(tmp);
                                        dataDisplay.append(tmp + "\n");
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
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
    @Override
    @SuppressLint("MissingPermission")
    protected void onResume() {
        super.onResume();
        try {
            if(mmSocket != null){
                startListening(mmSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
