package com.tcs.innovations.omniauart;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String clientId = "OmniaPi";
    private String topic = "MQTT/OMNIA";
    private int qos = 2;
    private String broker = "";
//    private String broker = "tcp://163.122.226.62:1883";
    private String url = "";
    //    private String url = "https://google.com";
    MqttAndroidClient client;

    private StringBuilder messageBuffer = new StringBuilder();
    private TextView textView;

    private PeripheralManagerService manager = new PeripheralManagerService();
    private UartDevice uartDevice;

    private EditText editText;
    private AppCompatButton b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bDot, buttonSubmit, btnBack;
    private ImageView imageView;

    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "in OnClickListener");
            switch (v.getId()){
                case R.id.btn_1:
                    Log.i(TAG, "btn_1");
                    editText.setText(editText.getText() + "1");
                    break;

                case R.id.btn_2:
                    editText.setText(editText.getText() + "2");
                    break;

                case R.id.btn_3:
                    editText.setText(editText.getText() + "3");
                    break;

                case R.id.btn_4:
                    editText.setText(editText.getText() + "4");
                    break;

                case R.id.btn_5:
                    editText.setText(editText.getText() + "5");
                    break;

                case R.id.btn_6:
                    editText.setText(editText.getText() + "6");
                    break;

                case R.id.btn_7:
                    editText.setText(editText.getText() + "7");
                    break;

                case R.id.btn_8:
                    editText.setText(editText.getText() + "8");
                    break;

                case R.id.btn_9:
                    editText.setText(editText.getText() + "9");
                    break;

                case R.id.btn_0:
                    editText.setText(editText.getText() + "0");
                    break;

                case R.id.btn_dot:
                    editText.setText(editText.getText() + ".");
                    break;

                case R.id.btn_back:
                    editText.setText(editText.getText().toString().substring(0, editText.getText().length() - 1));
                    break;

                case R.id.btn_submit:
                    connectPaho();

                    // Attempt to access the UART device
                    try {
                        PeripheralManagerService manager = new PeripheralManagerService();
                        uartDevice = manager.openUartDevice("UART0");
                    } catch (IOException e) {
                        Log.w(TAG, "Unable to access UART device", e);
                    }

                    try {
                        configureUartFrame(uartDevice);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.edit_text);

        b1 = (AppCompatButton) findViewById(R.id.btn_1);
        b2 = (AppCompatButton) findViewById(R.id.btn_2);
        b3 = (AppCompatButton) findViewById(R.id.btn_3);
        b4 = (AppCompatButton) findViewById(R.id.btn_4);
        b5 = (AppCompatButton) findViewById(R.id.btn_5);
        b6 = (AppCompatButton) findViewById(R.id.btn_6);
        b7 = (AppCompatButton) findViewById(R.id.btn_7);
        b8 = (AppCompatButton) findViewById(R.id.btn_8);
        b9 = (AppCompatButton) findViewById(R.id.btn_9);
        b0 = (AppCompatButton) findViewById(R.id.btn_0);
        bDot = (AppCompatButton) findViewById(R.id.btn_dot);
        btnBack = (AppCompatButton) findViewById(R.id.btn_back);
        buttonSubmit = (AppCompatButton) findViewById(R.id.btn_submit);

        imageView = (ImageView) findViewById(R.id.image_show);

        b1.setOnClickListener(btnClickListener);
        b2.setOnClickListener(btnClickListener);
        b3.setOnClickListener(btnClickListener);
        b4.setOnClickListener(btnClickListener);
        b5.setOnClickListener(btnClickListener);
        b6.setOnClickListener(btnClickListener);
        b7.setOnClickListener(btnClickListener);
        b8.setOnClickListener(btnClickListener);
        b9.setOnClickListener(btnClickListener);
        b0.setOnClickListener(btnClickListener);
        bDot.setOnClickListener(btnClickListener);
        btnBack.setOnClickListener(btnClickListener);
        buttonSubmit.setOnClickListener(btnClickListener);

        textView = (TextView) findViewById(R.id.log_view);


    }

    private void connectPaho(){
//        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.shared_prefs), Context.MODE_PRIVATE);
//        String ip = sharedPref.getString(getApplicationContext().getString(R.string.ip_address), "");
//        broker = "tcp://" + ip + ":1883";

        broker = "tcp://" + editText.getText() + ":1883";

        client =
                new MqttAndroidClient(getApplicationContext(), broker,
                        clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");

                    subscribe();
                    Toast.makeText(getApplicationContext(), "MQTT Connection Successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                    Toast.makeText(getApplicationContext(), "MQTT Connection Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishMessage(String payload){
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            Log.i(TAG, "Message published: " + message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe(){
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Log.i(TAG, "Subscribe: onSuccess");
                    client.setCallback(MainActivity.this);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                    Log.i(TAG, "Subscribe: onFailure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(TAG, "Topic: " + topic + " | Message: " + message);
        messageBuffer.append(message);
        messageBuffer.append("\n");

        uartDevice.write(message.toString().getBytes(), message.toString().length());

        switch (message.toString()){
            case "1":
                imageView.setImageResource(R.mipmap.ic_button_up);
                break;

            case "2":
                imageView.setImageResource(R.mipmap.ic_button_down);
                break;

            case "3":
                imageView.setImageResource(R.mipmap.ic_button_left);
                break;

            case "4":
                imageView.setImageResource(R.mipmap.ic_button_right);
                break;

            case "5":
                imageView.setImageResource(R.mipmap.ic_button_center);
                break;

            case "6":
            case "7":
            case "8":
            case "9":
            case "0":
                imageView.setImageDrawable(null);
                break;
        }

        textView.setText(messageBuffer.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void configureUartFrame(UartDevice uart) throws IOException {
        // Configure the UART port
        uart.setBaudrate(115200);
        uart.setDataSize(8);
        uart.setParity(UartDevice.PARITY_NONE);
        uart.setStopBits(1);
    }
}
