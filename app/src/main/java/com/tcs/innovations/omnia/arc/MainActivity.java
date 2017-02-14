package com.tcs.innovations.omnia.arc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import net.glxn.qrgen.android.QRCode;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.util.Strings;
import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.github.rockerhieu.emojicon.EmojiconTextView;
import io.github.rockerhieu.emojicon.emoji.Emojicon;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String clientId = "omnia_arc";

    private static final String topic_control = "omnia/control";
    private static final String topic_text = "omnia/text";
    private static final String topic_expression = "omnia/expression";

    private int qos = 2;
    private String broker = "";
//    private String broker = "tcp://163.122.226.62:1883";
    private String url = "";
    //    private String url = "https://google.com";
    MqttAndroidClient client;

    private static boolean expressionIsVisible = true;

    private PeripheralManagerService manager = new PeripheralManagerService();
    private UartDevice uartDevice;

    private EditText editText;
    private AppCompatButton b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bDot, buttonSubmit, btnBack;
    private ImageView imageView;

    private ImageView qrCodeView;
    private EmojiconTextView expressionView;
    private LinearLayout keyboardView;
    private TextView chatReceived;

    private MultiStateToggleButton multiStateToggleButton;

    public static int white = 0xFFFFFFFF;
    public static int black = 0xFF000000;
    public final static int WIDTH=500;


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
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.ip_address), editText.getText().toString());
                    editor.commit();
                    editor.apply();

                    connectPaho();

//                     Attempt to access the UART device
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

    private ToggleButton.OnValueChangedListener valueChangedListener = new ToggleButton.OnValueChangedListener() {
        @Override
        public void onValueChanged(int value) {
            Log.d(TAG, "Position: " + value);

            switch (value){
                case 0:
                    expressionView.setVisibility(View.VISIBLE);
                    keyboardView.setVisibility(View.INVISIBLE);
                    qrCodeView.setVisibility(View.INVISIBLE);
                    expressionIsVisible = true;
                    break;

                case 1:
                    expressionView.setVisibility(View.INVISIBLE);
                    keyboardView.setVisibility(View.INVISIBLE);
                    qrCodeView.setVisibility(View.VISIBLE);
                    expressionIsVisible = false;

                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.shared_prefs), Context.MODE_PRIVATE);
                    String ip = sharedPref.getString(getApplicationContext().getString(R.string.ip_address), "");

                    try {
                        Bitmap myBitmap = QRCode.from(ip).bitmap();
                        qrCodeView.setImageBitmap(myBitmap);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

//                    try
//                    {
//                        Bitmap bmp =  encodeAsBitmap(ip);
//                        qrCodeView.setImageBitmap(bmp);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    break;

                case 2:
                    expressionView.setVisibility(View.INVISIBLE);
                    keyboardView.setVisibility(View.VISIBLE);
                    qrCodeView.setVisibility(View.INVISIBLE);
                    expressionIsVisible = false;

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

        chatReceived = (TextView) findViewById(R.id.chat_text_view);

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

        qrCodeView = (ImageView) findViewById(R.id.qr_code_view);
        expressionView = (EmojiconTextView) findViewById(R.id.expression_view);
        keyboardView = (LinearLayout) findViewById(R.id.keyboard_view);

        multiStateToggleButton = (MultiStateToggleButton) this.findViewById(R.id.multi_stage_toggle);
        multiStateToggleButton.setOnValueChangedListener(valueChangedListener);
        multiStateToggleButton.setValue(0);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String ip = sharedPref.getString(getApplicationContext().getString(R.string.ip_address), "");
        editText.setText(ip);

        if (!ip.equalsIgnoreCase("")){
            connectPaho();
        }

    }

    private void connectPaho(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String ip = sharedPref.getString(getApplicationContext().getString(R.string.ip_address), "");
        broker = "tcp://m12.cloudmqtt.com:13768";
        Log.i(TAG, broker);

        client =
                new MqttAndroidClient(getApplicationContext(), broker,
                        clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("bdlwjtiv");
        options.setPassword("LXdcdOEP_4CU".toCharArray());

        try {
            IMqttToken token = client.connect(options);
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
            client.publish(topic_expression, message);
            Log.i(TAG, "Message published: " + message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe(){
        try {
            IMqttToken subToken = client.subscribe(topic_control, qos);
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

            IMqttToken subToken1 = client.subscribe(topic_text, qos);
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

            IMqttToken subToken2 = client.subscribe(topic_expression, qos);
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

        switch (topic){
            case topic_control:
                uartDevice.write(message.toString().getBytes(), message.toString().length());

                switch (message.toString()){
                    case "forward_press*":
                        imageView.setImageResource(R.mipmap.ic_button_up);
                        break;

                    case "reverse_press*":
                        imageView.setImageResource(R.mipmap.ic_button_down);
                        break;

                    case "left_press*":
                        imageView.setImageResource(R.mipmap.ic_button_left);
                        break;

                    case "right_press*":
                        imageView.setImageResource(R.mipmap.ic_button_right);
                        break;

                    case "center_press*":
                        imageView.setImageResource(R.mipmap.ic_button_center);
                        break;

                    case "6":
                    case "7":
                    case "8":
                    case "9":
                    case "0":
                        imageView.setImageDrawable(null);
                        break;

                    default:
                        chatReceived.setText(message.getPayload().toString());
                        break;
                }

                break;

            case topic_expression:
                if (expressionIsVisible && !message.toString().equalsIgnoreCase("")){
                    // expression view visible

                    Log.i(TAG, "expression visible: " + String.valueOf(message.getPayload()));

                    expressionView.setText(message.getPayload().toString());
                }else {
                    Log.i(TAG, "expression invisible");
                    // expression not visible
                }
                break;

            case topic_text:
                chatReceived.setText(message.toString());
                break;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void configureUartFrame(UartDevice uart) throws IOException {
        // Configure the UART port
        uart.setBaudrate(9600);
        uart.setDataSize(8);
        uart.setParity(UartDevice.PARITY_NONE);
        uart.setStopBits(1);
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        Bitmap bitmap=null;
        try
        {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);

            int w = result.getWidth();
            int h = result.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = result.get(x, y) ? black:white;
                }
            }
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        } catch (Exception iae) {
            iae.printStackTrace();
            return null;
        }
        return bitmap;
    }
}
