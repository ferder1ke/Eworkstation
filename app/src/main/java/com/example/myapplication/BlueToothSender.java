package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class
BlueToothSender extends AppCompatActivity {
    private EditText InitE, FinalE , Scan_R, Sweep_s, sampling_I, Increse_E;
    private Switch InitPN;

    private EditText Amplitude, Pulse_P, Sampling, Ebrich_T, HighE, LowE, Pulse_W;
    private BluetoothSocket mmSocket;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Map<String, Byte> optionByteMap = new HashMap<>();
    private byte[] Pkg = new byte[23];

    private byte[] setUsignHalfWord2PointFoward(EditText editText) { //小数点后两位
        String curEditTextString = editText.getText().toString();
        double curEditTextVal = Double.parseDouble(curEditTextString);
        if(curEditTextVal > 655.35) {
            editText.setText("655.35");
            curEditTextVal = 655.35;
        }
        else if(curEditTextVal < 0){
            editText.setText("0");
            curEditTextVal = 0;
        }
        curEditTextVal *= 100;
        byte[] res = new byte[2];
        int temp = (int) curEditTextVal;
        res[0] = (byte) ((temp >> 8) & 0xff);
        res[1] = (byte) (temp & 0xff);
        return res;
    }

    private byte[] setSignHalfWord(EditText editText) { //保留整数
        String curEditTextString = editText.getText().toString();
        int curEditTextVal = Integer.parseInt(curEditTextString);
        if(curEditTextVal > 32766){
            editText.setText("32766");
            curEditTextVal = 32766;
        }
        else if(curEditTextVal < -32766){
            editText.setText("-32766");
            curEditTextVal = -32766;
        }
        byte[] res = new byte[2];
        if(curEditTextVal < 0) {
            res[0] |= 0x80;
            curEditTextVal *= -1;
        }
        res[0] |= (byte) ((curEditTextVal >> 8) & 0xff);
        res[1] = (byte) (curEditTextVal & 0xff);
        return res;
    }
    private byte[] setUsignHalfWord1PointFoward(EditText editText) { //小数点后一位
        String curEditTextString = editText.getText().toString();
        double curEditTextVal = Double.parseDouble(curEditTextString);
        if(curEditTextVal > 6553.5) {
            editText.setText("6553.5");
            curEditTextVal = 6553.5;
        }
        else if(curEditTextVal < 0){
            editText.setText("0");
            curEditTextVal = 0;
        }
        curEditTextVal *= 10;
        byte[] res = new byte[2];
        int temp = (int) curEditTextVal;
        res[0] = (byte) ((temp >> 8) & 0xff);//高位
        res[1] = (byte) (temp & 0xff);//低位
        return res;
    }

    private byte[] setUsign5Word(EditText editText) {//保留整数
        String curEditTextString = editText.getText().toString();
        int curEditTextVal = Integer.parseInt(curEditTextString);
        if(curEditTextVal > 655350) {
            editText.setText("655350");
            curEditTextVal = 655350;
        }
        else if(curEditTextVal < 0){
            editText.setText("0");
            curEditTextVal = 0;
        }
        curEditTextVal /= 10;
        byte[] res = new byte[2];
        res[0] = (byte) ((curEditTextVal >> 8) & 0xff);//高位
        res[1] = (byte) (curEditTextVal & 0xff);//低位
        return res;
    }

    private byte[] setUsignHalfWord(EditText editText) {
        String curEditTextString = editText.getText().toString();
        double curEditTextVal = Double.parseDouble(curEditTextString);
        if(curEditTextVal > 65535){
            editText.setText("65535");
            curEditTextVal = 65535;
        }
        else if(curEditTextVal < 0){
            editText.setText("0");
            curEditTextVal = 0;
        }
        byte[] res = new byte[2];
        int temp = (int) curEditTextVal;
        res[0] = (byte) ((temp >> 8) & 0xff);
        res[1] = (byte) (temp & 0xff);
        return res;
    }

    private byte setsignedByte(EditText editText){//检查带符号数据
        String curEditTextString = editText.getText().toString();
        double curEditTextVal = Double.parseDouble(curEditTextString);
        if(curEditTextVal > 12.7)
            editText.setText("12.7");
        else if(curEditTextVal < -12.7){
            editText.setText("-12.7");
        }

        String interStringTmp;
        int interInterTmp;
        interStringTmp = editText.getText().toString();
        interInterTmp = (int) (Double.parseDouble(interStringTmp) * 10);
        if(interInterTmp < 0){
            interInterTmp *= -1;
            byte temp = (byte) interInterTmp;
            temp |= (byte) 0x80;
            return temp;
        }
        return (byte) interInterTmp;
    }
    private byte setUnsignedByte(EditText editText){
        String curEditTextString = editText.getText().toString();
        int curEditTextVal = Integer.parseInt(curEditTextString);
        if(curEditTextVal > 255)
            editText.setText("255");

        String interStringTmp;
        int interInterTmp;
        interStringTmp = editText.getText().toString();
        interInterTmp = Integer.parseInt(interStringTmp);
        return (byte) interInterTmp;
    }
    @SuppressLint("MissingPermission")
    private void sendPkg(BluetoothSocket socket, byte[] pkg){
        try {
            if (socket != null){
                if(!socket.isConnected()){
                    mmSocket.connect();
                }
                OutputStream outputStream = socket.getOutputStream();
                if(outputStream != null){
                    outputStream.write(pkg);
                    outputStream.flush();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    @SuppressLint("MissingPermission")
     protected void onCreate(@Nullable Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
         //界面初始化
         setContentView(R.layout.activity_send_pkg);
         InitE = findViewById(R.id.InitE);
         FinalE = findViewById(R.id.FinalE);
         Scan_R = findViewById(R.id.Scan_R);
         Sweep_s = findViewById(R.id.Sweep_s);
         sampling_I = findViewById(R.id.sampling_I);
         Increse_E = findViewById(R.id.Increse_E);

         InitPN = findViewById(R.id.InitPN);
         Amplitude = findViewById(R.id.Amplitude);
         Pulse_P = findViewById(R.id.Pulse_P);
         Sampling = findViewById(R.id.Sampling);
         Ebrich_T = findViewById(R.id.Ebrich_T);

         HighE = findViewById(R.id.HighE);
         LowE = findViewById(R.id.LowE);
         Pulse_W = findViewById(R.id.Pulse_W);
         Button mButton = findViewById(R.id.button2);
         Spinner spinner = findViewById(R.id.CMD);

         List<String> options = new ArrayList<>();
         options.add("循环伏安法(CV_Rest)");
         options.add("方波脉冲伏安法(CV_Rest)");
         options.add("差分脉冲伏安法(DPV_Rest)");
         options.add("数据流(电压电流)");
        optionByteMap.put("循环伏安法(CV_Rest)", (byte) 0x71);
        optionByteMap.put("方波脉冲伏安法(CV_Rest)", (byte) 0x72);
        optionByteMap.put("差分脉冲伏安法(DPV_Rest)", (byte) 0x73);
        optionByteMap.put("数据流(电压电流)", (byte) 0x81);

         ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
         Intent intent = getIntent();
         //构建mmSocket通道
         String deviceAddress = intent.getStringExtra("device_address");
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        try {
            mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            mmSocket.connect();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            if (mmSocket != null){
                if(!mmSocket.isConnected()){
                    mmSocket.connect();
                }
                OutputStream outputStream = mmSocket.getOutputStream();
                if(outputStream != null){
                    outputStream.write(0);
                    outputStream.flush();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
         mButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 try {
                     Pkg[0] = (byte) 0x5A;
                     Pkg[1] = (byte) 0xA5;
                     //判断控制流
                     String controlFlow = (String) spinner.getSelectedItem();
                     byte controlByte = optionByteMap.get(controlFlow);
                     Pkg[2] = controlByte;           //CMD
                     Pkg[3] = setsignedByte(InitE);  //InitE
                     Pkg[4] = setsignedByte(FinalE); //FinalE

                     byte[] temp;
                     temp = setUsignHalfWord2PointFoward(Scan_R);//Scan_R u16
                     Pkg[5] = temp[0];
                     Pkg[6] = temp[1];

                     Pkg[7] = setUnsignedByte(Sweep_s);//Sweeps
                     Pkg[8] = setUnsignedByte(sampling_I);//sampling_I
                     Pkg[9] = setUnsignedByte(Increse_E);//Increse_E

                     if (InitPN.isChecked())   //InitPN
                        Pkg[10] = 0X00;
                     else
                         Pkg[10] = 0x01;
                     temp = setSignHalfWord(Amplitude);//Ampltitude signed16
                     Pkg[11] = temp[0];
                     Pkg[12] = temp[1];

                     temp = setUsign5Word(Pulse_P);//Pulse_P u16
                     Pkg[13] = temp[0];
                     Pkg[14] = temp[1];

                     temp = setUsignHalfWord1PointFoward(Sampling);
                     Pkg[15] = temp[0];
                     Pkg[16] = temp[1];

                     Pkg[17] = setUnsignedByte(Ebrich_T);//Ebrich_T
                     Pkg[18] = setsignedByte(HighE);//HighE
                     Pkg[19] = setsignedByte(LowE);//LowE

                     temp = setUsignHalfWord(Pulse_W);
                     Pkg[20] = temp[0];
                     Pkg[21] = temp[1];

                     sendPkg(mmSocket, Pkg);
                     Toast.makeText(BlueToothSender.this, "发送成功", Toast.LENGTH_SHORT).show();
                 } catch (NumberFormatException e){
                     Toast.makeText(BlueToothSender.this, "输入的不是有效", Toast.LENGTH_SHORT).show();
                 }
             }
         });
     }
}
