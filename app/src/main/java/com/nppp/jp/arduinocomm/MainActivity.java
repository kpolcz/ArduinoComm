package com.nppp.jp.arduinocomm;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;

public class MainActivity extends Activity implements ServerConnection.ServerListener {
    public final String ACTION_USB_PERMISSION = "com.nppp.jp.USB_PERMISSION";
    Button startButton, sendButton, clearButton, stopButton;
    TextView textView;
    EditText editText;
    UsbManager usbManager;
    private OkHttpClient client;
    UsbDevice device;
    private TextView mTextMessage;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    String ndata = null;
    String postData = null;
    int PacketSize = 600;
    private ServerConnection mServerConnection, rServerConnection;
    public String[] dataArray1 = new String[PacketSize];
    public String[] dataArray2 = new String[PacketSize];
    boolean arrFlag = false;//false - we use the 1st arr, true - the 2nd
    int i = 0;
    private class DataUpload extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            i = 0;
            StringBuilder sb = new StringBuilder();

            if (!arrFlag) {
                System.out.println(1);
                System.out.println(Arrays.toString(dataArray1));
                for (int k = 0; k < PacketSize; k++)
                //pack into string, send to server
                { //System.out.println(dataArray1[k]);

                    if ((k + 1) % 6 == 0) {
                        //System.out.println(dataArray1[k]);
                        sb.append(dataArray1[k]);
                        if (k != PacketSize/10 - 1)
                            sb.append("|");
                    } else {
                        sb.append(dataArray1[k]);
                        sb.append(";");
                    }
                }
                postData = sb.toString();
                //System.out.println("flaga ma wartość" + arrFlag);
                System.out.println(postData);
                mServerConnection.sendMessage(postData);
                arrFlag ^= true;
            } else if (arrFlag) {
                System.out.println(2);
                System.out.println(Arrays.toString(dataArray2));
                for (int k = 0; k < PacketSize; k++)
                //pack into string, send to server
                {

                    if ((k + 1) % 6 == 0) {
                        //System.out.println(dataArray1[k]);

                        sb.append(dataArray2[k]);
                        if (k != PacketSize/10 - 1)
                            sb.append("|");
                    } else {
                        sb.append(dataArray2[k]);
                        sb.append(";");
                    }
                }
                postData = sb.toString();
               // System.out.println("flaga ma wartość" + arrFlag);
                mServerConnection.sendMessage(postData);
                arrFlag ^= true;
                 System.out.println(postData);

            }

            return null;
        }

    }
    //Defining a Callback which triggers whenever data is read.
    UsbSerialInterface.UsbReadCallback mCallback = arg0 -> {
        try {
            ndata = new String(arg0, "UTF-8");
            System.out.println(ndata);
            String sep[]=null;
                sep = ndata.split(";");
            System.out.println(Arrays.toString(sep));
            int j = 0;
            System.out.println(Arrays.toString(sep));
            System.out.println("i=" + i);
            if (!arrFlag) {
                while (i < dataArray1.length && j < sep.length)//we still have space for the data
                {
                    if(!Objects.equals(sep[j], ""))
                    {dataArray1[i] = sep[j];
                        j++;
                        i++;}
                    else
                        j++;
//                        System.out.println("j=" + j);
                }
                if (i == dataArray1.length)//array full
                {
                     System.out.println(Arrays.toString(dataArray1));
                    new DataUpload().execute();
                }
            } else if (arrFlag) {
                while (i < dataArray2.length && j < sep.length)//we still have space for the data
                {
                    if(!Objects.equals(sep[j], ""))
                    {dataArray2[i] = sep[j];
                        j++;
                        i++;}
                    else
                        j++;
                }
                if (i == dataArray2.length)//array full
                {

                    new DataUpload().execute();
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            ndata = new String(arg0, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        tvAppend(textView,ndata);
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            tvAppend(textView, "Serial Connection Opened!\n");

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(stopButton);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        startButton = findViewById(R.id.buttonStart);
        sendButton = findViewById(R.id.buttonSend);
        clearButton = findViewById(R.id.buttonClear);
        stopButton = findViewById(R.id.buttonStop);
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);
        setUiEnabled(false);
        mServerConnection = new ServerConnection("ws://pkprojectserver.hopto.org:1337");
        rServerConnection = new ServerConnection("ws://pkprojectserver.hopto.org:1338");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        mServerConnection.connect((ServerConnection.ServerListener) this);
        rServerConnection.connect((ServerConnection.ServerListener) this);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        textView.setEnabled(bool);

    }

    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                System.out.println(deviceVID);
                if (deviceVID == 0x2341||deviceVID ==0x1a86)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                   usbManager.requestPermission(device, pi);
                if (!keep)
                    break;
            }
        }
    }

    public void onClickSend(View view) throws InterruptedException {
        String string = editText.getText().toString();
            serialPort.write(string.getBytes());
        tvAppend(textView, "\nData Sent : " + string + "\n");
    }

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
        tvAppend(textView, "\nSerial Connection Closed! \n");
    }
    public void onClickClear(View view) {
        textView.setText(" ");
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(() -> ftv.append(ftext));
    }


    @Override
    public void onNewMessage(String message) {
        if(message.length()==8)//instrukcje dla arduino
        {
            tvAppend(textView,"od serwera: "+'\n');
            serialPort.write(message.getBytes());
        }
        else
            tvAppend(textView,"rec: "+'\n');

    }

    @Override
    public void onStatusChange(ServerConnection.ConnectionStatus status) {
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
