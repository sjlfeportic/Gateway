package com.viatom.a20ftest;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;



/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectFragment extends DialogFragment {

    Intent intent;
    public static int datapr;
    public static int enviopr;
    private UUID notify = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");
    private UUID write = UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb");
    private EditText e1;

    private final String connString = BuildConfig.DeviceConnectionString;;

    private double oxigenio;
    private double batimentos;
    private String msgStr;
    private Message sendMessage;
    private String lastException;

    private DeviceClient client;

    IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    private int msgSentCount = 0;
    private int receiptsConfirmedCount = 0;
    private int sendFailuresCount = 0;
    private int msgReceivedCount = 0;
    private int sendMessagesInterval = 5000;

    private final Handler handler = new Handler();
    private Thread sendThread;

    private static final int METHOD_SUCCESS = 200;
    public static final int METHOD_THROWS = 403;
    private static final int METHOD_NOT_DEFINED = 404;

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.success)
    TextView showSuccess;

    @BindView(R.id.spo2Val)
    TextView spo2Val;

    @BindView(R.id.prVal)
    TextView prVal;


   // @BindView(R.id.logs)
    //ListView logView;

    static TextView ok;
    static TextView ok1;
    public static TextView ok2;


    @OnClick(R.id.disconnect)
    void disconnect() {
        this.dismiss();
        if (!connection.isDisposed()) {
            connection.dispose();
        }
    }


    private Context context;

    RxBleClient rxBleClient;
    RxBleDevice device;
    Disposable connection;

    boolean passed = false;

    private Bluetooth b;

    private ArrayList<String> logs = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    public ConnectFragment() {
        // Required empty public constructor
    }

    public void setB(Bluetooth b) {
        this.b = b;
    }

    private static int spo2;
    private int pr = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.fragment_connect, container, false);
        ButterKnife.bind(this, v);
        iniView();
        final Button button = (Button) v.findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;

    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        connect(b);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showResult();
            }
        }, 10);


    }

    private void stop()
    {
        new Thread(new Runnable() {
            public void run()
            {
                try
                {
                    sendThread.interrupt();
                    client.closeNow();
                    System.out.println("Shutting down...");
                }
                catch (Exception e)
                {
                    lastException = "Exception while closing IoTHub connection: " + e;
                    handler.post(exceptionRunnable);
                }
            }
        }).start();
    }

    private void start()
    {
        sendThread = new Thread(new Runnable() {
            public void run()
            {
                try
                {
                    initClient();
                    for(;;)
                    {
                        sendMessages();
                        Thread.sleep(sendMessagesInterval);
                    }
                }
                catch (InterruptedException e)
                {
                    return;
                }
                catch (Exception e)
                {
                    lastException = "Exception while opening IoTHub connection: " + e;
                    handler.post(exceptionRunnable);
                }
            }
        });

        sendThread.start();
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            //txtLastTempVal.setText(String.format("%.2f",temperature));
            //txtLastHumidityVal.setText(String.format("%.2f",humidity));
            //txtMsgsSentVal.setText(Integer.toString(msgSentCount));
            //txtLastMsgSentVal.setText("[" + new String(sendMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET) + "]");
        }
    };

    final Runnable exceptionRunnable = new Runnable() {
        public void run() {
            // AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            // builder.setMessage(lastException);
            //builder.show();
            //System.out.println(lastException);
            //btnStart.setEnabled(true);
            //btnStop.setEnabled(false);
        }
    };

    final Runnable methodNotificationRunnable = new Runnable() {
        public void run() {
            //Context context = getApplicationContext();
            //CharSequence text = "Set Send Messages Interval to " + sendMessagesInterval + "ms";
            //int duration = Toast.LENGTH_LONG;

            //Toast toast = Toast.makeText(context, text, duration);
            //toast.show();
        }
    };

    private void sendMessages()
    {
        oxigenio=Integer.parseInt(String.valueOf(spo2));
        batimentos = Integer.parseInt(String.valueOf(pr));
        msgStr = "\"Spo2\":" + String.format("%.2f",oxigenio) + ", \"BatimentosCardiacos\":" + String.format("%.2f",batimentos);
        try
        {
            sendMessage = new Message(msgStr);
            sendMessage.setProperty("temperatureAlert", oxigenio > 28 ? "true" : "false");
            sendMessage.setMessageId(java.util.UUID.randomUUID().toString());
            //System.out.println("Message Sent: " + msgStr);
            EventCallback eventCallback = new EventCallback();
            client.sendEventAsync(sendMessage, eventCallback, msgSentCount);
            msgSentCount++;
            handler.post(updateRunnable);
        }
        catch (Exception e)
        {
            System.err.println("Exception while sending event: " + e);
        }
    }

    private void initClient() throws URISyntaxException, IOException
    {
        client = new DeviceClient(connString, protocol);

        try
        {
            client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());
            client.open();
            MessageCallback callback = new MessageCallback();
            client.setMessageCallback(callback, null);
            //client.subscribeToDeviceMethod(new SampleDeviceMethodCallback(), getApplicationContext(), new DeviceMethodStatusCallBack(), null);
        }
        catch (Exception e)
        {
            System.err.println("Exception while opening IoTHub connection: " + e);
            //client.closeNow();
            System.out.println("Shutting down...");
        }
    }

    class EventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            Integer i = context instanceof Integer ? (Integer) context : 0;
            // System.out.println("IoT Hub responded to message " + i.toString()
            //        + " with status " + status.name());

            if((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY))
            {
                //TextView txtReceiptsConfirmedVal = findViewById(R.id.txtReceiptsConfirmedVal);
                receiptsConfirmedCount++;
                //txtReceiptsConfirmedVal.setText(Integer.toString(receiptsConfirmedCount));
            }
            else
            {
                // TextView txtSendFailuresVal = findViewById(R.id.txtSendFailuresVal);
                sendFailuresCount++;
                //  txtSendFailuresVal.setText(Integer.toString(sendFailuresCount));
            }
        }
    }

    class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            //System.out.println(
            //     "Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            msgReceivedCount++;
            //TextView txtMsgsReceivedVal = findViewById(R.id.txtMsgsReceivedVal);
            //txtMsgsReceivedVal.setText(Integer.toString(msgReceivedCount));
            //txtLastMsgReceivedVal.setText("[" + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET) + "]");
            return IotHubMessageResult.COMPLETE;
        }
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            //System.out.println();
            //System.out.println("CONNECTION STATUS UPDATE: " + status);
            //System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            //System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            //System.out.println();

            if (throwable != null)
            {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                //connection was lost, and is not being re-established. Look at provided exception for
                // how to resolve this issue. Cannot send messages until this issue is resolved, and you manually
                // re-open the device client
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                //connection was lost, but is being re-established. Can still send messages, but they won't
                // be sent until the connection is re-established
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                //Connection was successfully re-established. Can send messages.
            }
        }
    }

    private int method_setSendMessagesInterval(Object methodData) throws UnsupportedEncodingException, JSONException
    {
        String payload = new String((byte[])methodData, "UTF-8").replace("\"", "");
        JSONObject obj = new JSONObject(payload);
        sendMessagesInterval = obj.getInt("sendInterval");
        handler.post(methodNotificationRunnable);
        return METHOD_SUCCESS;
    }

    private int method_default(Object data)
    {
        //System.out.println("invoking default method for this device");
        // Insert device specific code here
        return METHOD_NOT_DEFINED;
    }

    protected class DeviceMethodStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            //System.out.println("IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected class SampleDeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
    {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            DeviceMethodData deviceMethodData ;
            try {
                switch (methodName) {
                    case "setSendMessagesInterval": {
                        int status = method_setSendMessagesInterval(methodData);
                        deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                        break;
                    }
                    default: {
                        int status = method_default(methodData);
                        deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                    }
                }
            }
            catch (Exception e)
            {
                int status = METHOD_THROWS;
                deviceMethodData = new DeviceMethodData(status, "Method Throws " + methodName);
            }
            return deviceMethodData;
        }
    }
    void connect(Bluetooth b) {
        device = rxBleClient.getBleDevice(b.getMacAddr());
        connection = device.establishConnection(false) // <-- autoConnect flag
                .subscribe(
                        rxBleConnection -> {
                            rxBleConnection.setupNotification(notify)
                                    .doOnNext(
                                            notificationObservable -> {
                                                addLogs("notify set up");
                                            }
                                    )
                                    .flatMap(notificationObservable -> notificationObservable)
                                    .subscribe(
                                            bytes -> {
                                                onNotifyReceived(bytes);
                                            },
                                            throwable -> {
                                                addLogs(throwable.getMessage());
                                            }
                                    );

                            byte[] bytesToWrite = new byte[5];
                            bytesToWrite[0] = (byte) 0xfe;
                            bytesToWrite[1] = (byte) 0x05;
                            bytesToWrite[2] = (byte) 0x55;
                            bytesToWrite[3] = (byte) 0x00;
                            bytesToWrite[4] = CRCUtils.calCRC8(bytesToWrite);

                            rxBleConnection.writeCharacteristic(write, bytesToWrite)
                                    .subscribe(
                                            bytes -> addLogs(HexString.bytesToHex(bytes)),
                                          throwable -> addLogs(throwable.getMessage())
                                   );
                        },
                        throwable -> {
                            addLogs(throwable.getMessage());
                        }
                );

        device.observeConnectionStateChanges()
                .subscribe(
                        connectionState -> {
                            addLogs(connectionState.toString());
                        },
                        throwable -> {
                            addLogs(throwable.getMessage());
                        }
                );

//         When done... dispose and forget about connection teardown :)
//        disposable.dispose();
    }

    private void onNotifyReceived(byte[] bytes)  {

        addLogs(HexString.bytesToHex(bytes));

        if (bytes.length == 18) {
            if ((bytes[0]&0xff) == 0xfe && (bytes[1]&0xff) == 0x08 && (bytes[2]&0xff) == 0x56) {
                if (((bytes[11] & 0xff) == 0x01) && ((bytes[12]&0xff) == 0xff)) {
                    pr = 0;
                } else {
                    pr = ((bytes[11] & 0xff) << 8) + (bytes[12] & 0xff);
                }
                if (bytes[13] == 0x7f) {
                    spo2 = 0;
                } else {
                    spo2 = bytes[13] & 0xff;
                }

                passed = true;
                showResult();
            } else if ((bytes[0]&0xff) == 0xfe && (bytes[1]&0xff) == 0x0a && (bytes[2]&0xff) == 0x55) {
                if (((bytes[3] & 0xff) == 0x01) && ((bytes[4]&0xff) == 0xff)) {
                    pr = 0;
                } else {
                    pr = ((bytes[3] & 0xff) << 8) + (bytes[4] & 0xff);
                }
                if (bytes[5] == 0x7f) {
                    spo2 = 0;
                } else {
                    spo2 = bytes[5] & 0xff;
                }

                passed = true;
                showResult();
            }
        } else if (bytes.length == 10) {
            if ((bytes[0]&0xff) == 0xfe && (bytes[1]&0xff) == 0x0a && (bytes[2]&0xff) == 0x55) {
                if (((bytes[3] & 0xff) == 0x01) && ((bytes[4]&0xff) == 0xff)) {
                    pr = 0;
                } else {
                    pr = ((bytes[3] & 0xff) << 8) + (bytes[4] & 0xff);
                }
                if (bytes[5] == 0x7f) {
                    spo2 = 0;
                } else {
                    spo2 = bytes[5] & 0xff;
                }

                passed = true;
                showResult();
            }
        }


    }

    void showResult() {

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    spo2Val.setText(String.valueOf(spo2));
                    prVal.setText(String.valueOf(pr));
                    ok=spo2Val;
                    ok1=prVal;



                    if (passed) {
                        showSuccess.setText("Medição efetuada!");
                        showSuccess.setTextColor(Color.GREEN);
                        showSuccess.setVisibility(View.VISIBLE);
                        String message = ConnectFragment.ok.getText().toString();
                        String message2 = ConnectFragment.ok1.getText().toString();
                        String message3 = message+" "+message2;



                        MainActivity2.BackgroundTask b1 = new MainActivity2.BackgroundTask();
                        b1.execute(message3);
                        start();






                    }
                    else {
                        showSuccess.setText("Por favor, não retire o dedo do oxímetro!");
                        showSuccess.setTextColor(Color.BLUE);
                        showSuccess.setVisibility(View.VISIBLE);

                    }
                }
            });
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void addLogs(String s) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = format.format(System.currentTimeMillis());

        if (getActivity() != null) {
            getActivity().runOnUiThread(()->{
                logs.add(0,time + " " + s);
                adapter.notifyDataSetChanged();
            });
        }
    }

    private void iniView() {
        name.setText(b.getName());
        rxBleClient = RxBleClient.create(context);
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, logs);
        //logView.setAdapter(adapter);

    }




}
