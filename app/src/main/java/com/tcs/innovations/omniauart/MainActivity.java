package com.tcs.innovations.omniauart;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    private String broker = "tcp://163.122.226.62:1883";
    private String url = "";
    //    private String url = "https://google.com";
    MqttAndroidClient client;

    private StringBuilder messageBuffer = new StringBuilder();
    private TextView textView;

    private PeripheralManagerService manager = new PeripheralManagerService();
    private UartDevice uartDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.log_view);

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
    }

    private void connectPaho(){
//        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.shared_prefs), Context.MODE_PRIVATE);
//        String ip = sharedPref.getString(getApplicationContext().getString(R.string.ip_address), "");
//        broker = "tcp://" + ip + ":1883";

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
