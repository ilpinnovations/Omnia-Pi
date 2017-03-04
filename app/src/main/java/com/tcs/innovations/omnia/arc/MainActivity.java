package com.tcs.innovations.omnia.arc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;
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
import java.net.InetAddress;
import java.util.List;

import io.github.rockerhieu.emojicon.EmojiconTextView;
import io.github.rockerhieu.emojicon.emoji.Emojicon;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    int command = 4;
    private String clientId = "omnia_arc";
    private static final String topic_control = "control";
    private static final String topic_text = "text";
    private static final String topic_expression = "expression";
    private static final String topic_color = "color";
    private static final String BCM23 = "BCM23";
    private static final String BCM25 = "BCM25";
    private static final String BCM19 = "BCM19";
    private static final String BCM26 = "BCM26";
    private static final String PWM0 = "PWM0";
    private static final String PWM1 = "PWM1";
    private Gpio mGpio23;
    private Gpio mGpio25;
    private Gpio mGpio19;
    private Gpio mGpio26;
    private Pwm PWM_0;
    private Pwm PWM_1;

    private static boolean start=false;
    private static boolean end=false;
    public String var = "";

    private Gpio THREAD_GPIO_1;
    private Gpio THREAD_GPIO_2;
    private Pwm THREAD_PWM;
    private boolean THREAD_BOOL_1;
    private boolean THREAD_BOOL_2;
    private Gpio THREAD_GPIO_3;
    private Gpio THREAD_GPIO_4;
    private Pwm THREAD_PWM1;
    private boolean THREAD_BOOL_3;
    private boolean THREAD_BOOL_4;

    private int qos = 2;
    private String broker = "";
    MqttAndroidClient client, client2;

    private static boolean expressionIsVisible = true;
    private UartDevice uartDevice;
    private EditText editText;
    private AppCompatButton b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bDot, buttonSubmit, btnBack, sendColor;
    private ImageView imageView, colorPickerImage;
    private ColorPickerView colorPickerView;
    private ImageView qrCodeView;
    private EmojiconTextView expressionView;
    private LinearLayout keyboardView, colorPickerContainer;
    private TextView chatReceived;

    private MultiStateToggleButton multiStateToggleButton;

    public static int white = 0xFFFFFFFF;
    public static int black = 0xFF000000;
    public final static int WIDTH=500;



    private int colorLatest = 0;

    private View.OnClickListener sendColorListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private OnColorChangedListener colorChangedListener = new OnColorChangedListener() {
        @Override
        public void onColorChanged(int i) {
            Log.d("ColorPicker", "onColorChanged: 0x" + Integer.toHexString(i));
            Log.d("ColorPicker", "onColorChanged: " + i);
            String color = "#" + Integer.toHexString(i).toUpperCase().substring(2,8);
            colorPickerImage.setBackgroundColor(Color.parseColor(color));

            Log.i(TAG, "message published: " + color);
            publishMessage(color);
        }
    };

    private OnColorSelectedListener colorSelectedListener = new OnColorSelectedListener() {
        @Override
        public void onColorSelected(int i) {
            Log.d("ColorPicker", "onColorSelected: 0x" + Integer.toHexString(i).toUpperCase().substring(2,8));
            colorLatest = i;

            String color = "#" + Integer.toHexString(i).toUpperCase().substring(2,8);
            colorPickerImage.setBackgroundColor(Color.parseColor(color));

            Toast.makeText(
                    getApplicationContext(),
                    "selectedColor: " + color,
                    Toast.LENGTH_SHORT).show();

            Log.i(TAG, "message published: " + color);
            publishMessage(color);
        }
    };

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

                    if (!client.isConnected()){
                        connectPaho();
                    }

                    if (!client2.isConnected()){
                        connectPahoColor();
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
                    Log.i(TAG, "at position 0");
                    expressionView.setVisibility(View.VISIBLE);
                    keyboardView.setVisibility(View.GONE);
                    qrCodeView.setVisibility(View.INVISIBLE);
                    colorPickerContainer.setVisibility(View.GONE);
                    chatReceived.setVisibility(View.VISIBLE);

                    expressionIsVisible = true;
                    break;

                case 1:
                    Log.i(TAG, "at position 1");
                    expressionView.setVisibility(View.INVISIBLE);
                    keyboardView.setVisibility(View.GONE);
                    qrCodeView.setVisibility(View.VISIBLE);
                    colorPickerContainer.setVisibility(View.GONE);
                    chatReceived.setVisibility(View.VISIBLE);

                    expressionIsVisible = false;

                    try {
                        Bitmap myBitmap = QRCode.from("https://videortc.herokuapp.com/demo").bitmap();
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
                    Log.i(TAG, "at position 2");
                    expressionView.setVisibility(View.INVISIBLE);
                    keyboardView.setVisibility(View.GONE);
                    qrCodeView.setVisibility(View.INVISIBLE);
                    colorPickerContainer.setVisibility(View.VISIBLE);
                    chatReceived.setVisibility(View.GONE);
                    expressionIsVisible = false;
                    break;


            }
        }
    };


    Thread temp = new Thread(){
        @Override
        public void run() {
            int i=0;
            while (true) {
                if(command==1) {
                    for (i = 0; i <= 60; i=i+2) {
                        if(command==1) {
                            try {
                                Log.i("change", "command 1 = " + i);
                                drive(THREAD_GPIO_1, THREAD_GPIO_2, THREAD_PWM, THREAD_BOOL_1, THREAD_BOOL_2, i);
                                drive(THREAD_GPIO_3, THREAD_GPIO_4, THREAD_PWM1, THREAD_BOOL_3, THREAD_BOOL_4, i);
                                this.sleep(10);
                            } catch (Exception e) {
                                Log.i(TAG, e.getMessage());
                            }
                        }
                        else{break;}
                    }
                    if(command==1)
                        command = 4;
                }
                else if (command == 2)
                {
                    for ( ; i >=0; i=i-2) {
                        if(command==2) {
                            try {
                                Log.i("change", "command 2 = " + i);
                                drive(THREAD_GPIO_1, THREAD_GPIO_2, THREAD_PWM, THREAD_BOOL_1, THREAD_BOOL_2, i);
                                drive(THREAD_GPIO_3, THREAD_GPIO_4, THREAD_PWM1, THREAD_BOOL_3, THREAD_BOOL_4, i);
                                this.sleep(10);
                            } catch (Exception e) {
                                Log.i(TAG, e.getMessage());
                            }
                        }
                        else{break;}
                    }
                    if(command==2)
                        command = 4;
                }
                else if (command == 0)
                {
                    try {
                        drive(THREAD_GPIO_1, THREAD_GPIO_2, THREAD_PWM, THREAD_BOOL_1, THREAD_BOOL_2, 0);
                        drive(THREAD_GPIO_3, THREAD_GPIO_4, THREAD_PWM1, THREAD_BOOL_3, THREAD_BOOL_4, 0);
                        this.sleep(10);
                    } catch (Exception e) {
                        Log.i(TAG, e.getMessage());
                    }

                }
                else if (command == 4)
                {
                    try {
                        this.sleep(10);
                    }
                    catch(Exception e){
                        Log.i(TAG,e.getMessage());
                    }
                }
                /////////////////////////////////////////////////////////////////////////////////

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

        colorPickerView = (ColorPickerView) findViewById(R.id.color_picker_view);
        colorPickerView.addOnColorChangedListener(colorChangedListener);
        colorPickerView.addOnColorSelectedListener(colorSelectedListener);

        colorPickerImage = (ImageView) findViewById(R.id.color_picker_image);
        sendColor = (AppCompatButton) findViewById(R.id.color_send_btn);
        sendColor.setOnClickListener(sendColorListener);

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
        colorPickerContainer = (LinearLayout) findViewById(R.id.color_picker_container);

        multiStateToggleButton = (MultiStateToggleButton) this.findViewById(R.id.multi_stage_toggle);
        multiStateToggleButton.setOnValueChangedListener(valueChangedListener);
        multiStateToggleButton.setValue(0);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String ip = sharedPref.getString(getApplicationContext().getString(R.string.ip_address), "");
        editText.setText(ip);

        connectPaho();
//      connectPahoColor();
        temp.start();


    }

    private void connectPaho(){
        broker = "tcp://m12.cloudmqtt.com:13768";
        Log.i(TAG, broker);

        client =
                new MqttAndroidClient(getApplicationContext(), broker,
                        clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("web-client");
        options.setPassword("web-client".toCharArray());

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

        //                     Attempt to access the UART device
        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            //uartDevice = manager.openUartDevice("UART0");
            PWM_0 = manager.openPwm(PWM0);
            PWM_1 = manager.openPwm(PWM1);
            //List<String> portList = manager.getGpioList();
            mGpio19 = manager.openGpio(BCM19);
            mGpio23 = manager.openGpio(BCM23);
            mGpio25 = manager.openGpio(BCM25);
            mGpio26 = manager.openGpio(BCM26);

        } catch (IOException e) {
            Log.w(TAG, "Unable to access UART device", e);
        }

//        try {
//            configureUartFrame(uartDevice);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void connectPahoColor(){
        String broker = "tcp://m21.cloudmqtt.com:12182";
        Log.i(TAG, broker);

        client2 =
                new MqttAndroidClient(getApplicationContext(), broker,
                        "omnia");

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("omnia");
        options.setPassword("pixelate".toCharArray());

        try {
            IMqttToken token = client2.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");

                    Toast.makeText(getApplicationContext(), "Color MQTT Connection Successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                    Toast.makeText(getApplicationContext(), "Color MQTT Connection Failed", Toast.LENGTH_SHORT).show();
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
            client2.publish(topic_color, message);
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

    public void configureOutput(Gpio gpio,boolean temp) throws IOException {
        // Initialize the pin as a high output
        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        // Low voltage is considered active
        gpio.setActiveType(Gpio.ACTIVE_LOW);
        // Toggle the value to be LOW
        gpio.setValue(temp);
    }

    public void initializePwm(Pwm pwm,int speed) throws IOException {
        pwm.setPwmFrequencyHz(400);
        pwm.setPwmDutyCycle(speed);
        // Enable the PWM signal
        pwm.setEnabled(true);
    }

    public void drive(Gpio gpio1,Gpio gpio2,Pwm gpio3,boolean logic_1,boolean logic_2,int speed) throws IOException
    {
        initializePwm(gpio3,speed);
        configureOutput(gpio1,logic_1);
        configureOutput(gpio2,logic_2);

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
            ///////////////////////

        if (message.toString().equalsIgnoreCase("start")) {
            start = true;
            end = false;
        }

        if (start && !end){
            Log.i("swap", "Topic: " + topic + " | Message: " + message.toString());
            switch (topic){
                case topic_control:
                    //uartDevice.write(message.toString().getBytes(), message.toString().length());
                    switch (message.toString()){
                        case "U":
                        case "forward_press*":
                            var = "U";
//                        drive(mGpio19,mGpio26,PWM_1,true,false,75);
                            THREAD_GPIO_1 = mGpio19;
                            THREAD_GPIO_2 = mGpio26;
                            THREAD_PWM = PWM_1;
                            THREAD_BOOL_1 = false;
                            THREAD_BOOL_2 = true;
                            THREAD_GPIO_3 = mGpio23;
                            THREAD_GPIO_4 = mGpio25;
                            THREAD_PWM1 = PWM_0;
                            THREAD_BOOL_3 = false;
                            THREAD_BOOL_4 = true;
                            command = 1;
                            imageView.setImageResource(R.mipmap.ic_button_up);
                            break;

                        case "D":
                        case "reverse_press*":
                            var = "D";
//                        drive(mGpio19,mGpio26,PWM_1,false,false,0);
                            THREAD_GPIO_1 = mGpio19;
                            THREAD_GPIO_2 = mGpio26;
                            THREAD_PWM = PWM_1;
                            THREAD_BOOL_1 = true;
                            THREAD_BOOL_2 = false;
                            THREAD_GPIO_3 = mGpio23;
                            THREAD_GPIO_4 = mGpio25;
                            THREAD_PWM1 = PWM_0;
                            THREAD_BOOL_3 = true;
                            THREAD_BOOL_4 = false;
                            command = 1;
                            imageView.setImageResource(R.mipmap.ic_button_down);
                            break;

                        case "DL1":
                        case "UL1":
                        case "left_press*":
                            var = "DL1";
                            THREAD_GPIO_1 = mGpio19;
                            THREAD_GPIO_2 = mGpio26;
                            THREAD_PWM = PWM_1;
                            THREAD_BOOL_1 = true;
                            THREAD_BOOL_2 = false;
                            THREAD_GPIO_3 = mGpio23;
                            THREAD_GPIO_4 = mGpio25;
                            THREAD_PWM1 = PWM_0;
                            THREAD_BOOL_3 = false;
                            THREAD_BOOL_4 = true;
                            command = 1;
                            imageView.setImageResource(R.mipmap.ic_button_left);
                            break;

                        case "DR1":
                        case "UR1":
                        case "right_press*":
                            var = "DR1";
                            THREAD_GPIO_1 = mGpio19;
                            THREAD_GPIO_2 = mGpio26;
                            THREAD_PWM = PWM_1;
                            THREAD_BOOL_1 = false;
                            THREAD_BOOL_2 = true;
                            THREAD_GPIO_3 = mGpio23;
                            THREAD_GPIO_4 = mGpio25;
                            THREAD_PWM1 = PWM_0;
                            THREAD_BOOL_3 = true;
                            THREAD_BOOL_4 = false;
                            command = 1;
                            imageView.setImageResource(R.mipmap.ic_button_right);
                            break;

                        case "center_press*":
                            imageView.setImageResource(R.mipmap.ic_button_center);
                            break;

                        default:
//                        chatReceived.setText(message.toString());
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
        if (message.toString().equalsIgnoreCase("end")) {
            Log.i("swap",var + " this");
            switch (var.toString())
            {

                case "U":
//                        drive(mGpio19,mGpio26,PWM_1,false,false,0);
                    THREAD_GPIO_1 = mGpio19;
                    THREAD_GPIO_2 = mGpio26;
                    THREAD_PWM = PWM_1;
                    THREAD_BOOL_1 = false;
                    THREAD_BOOL_2 = true;
                    THREAD_GPIO_3 = mGpio23;
                    THREAD_GPIO_4 = mGpio25;
                    THREAD_PWM1 = PWM_0;
                    THREAD_BOOL_3 = false;
                    THREAD_BOOL_4 = true;
                    command = 2;
                    imageView.setImageResource(R.mipmap.ic_button_up);
                    break;

                case "D":
                    THREAD_GPIO_1 = mGpio19;
                    THREAD_GPIO_2 = mGpio26;
                    THREAD_PWM = PWM_1;
                    THREAD_BOOL_1 = true;
                    THREAD_BOOL_2 = false;
                    THREAD_GPIO_3 = mGpio23;
                    THREAD_GPIO_4 = mGpio25;
                    THREAD_PWM1 = PWM_0;
                    THREAD_BOOL_3 = true;
                    THREAD_BOOL_4 = false;
                    command = 2;
                    imageView.setImageResource(R.mipmap.ic_button_down);
                    break;

                case "DL1":
                    THREAD_GPIO_1 = mGpio19;
                    THREAD_GPIO_2 = mGpio26;
                    THREAD_PWM = PWM_1;
                    THREAD_BOOL_1 = true;
                    THREAD_BOOL_2 = false;
                    THREAD_GPIO_3 = mGpio23;
                    THREAD_GPIO_4 = mGpio25;
                    THREAD_PWM1 = PWM_0;
                    THREAD_BOOL_3 = false;
                    THREAD_BOOL_4 = true;
                    command = 2;
                    imageView.setImageResource(R.mipmap.ic_button_left);
                    break;

                case "DR1":
                    THREAD_GPIO_1 = mGpio19;
                    THREAD_GPIO_2 = mGpio26;
                    THREAD_PWM = PWM_1;
                    THREAD_BOOL_1 = false;
                    THREAD_BOOL_2 = true;
                    THREAD_GPIO_3 = mGpio23;
                    THREAD_GPIO_4 = mGpio25;
                    THREAD_PWM1 = PWM_0;
                    THREAD_BOOL_3 = true;
                    THREAD_BOOL_4 = false;
                    command = 2;
                    imageView.setImageResource(R.mipmap.ic_button_right);
                    break;


                case "center_release*":
                    imageView.setImageResource(R.mipmap.ic_button_center);
                    break;
                default:

            }
            end = true;
            start = false;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGpio19 != null) {
            try {
                mGpio19.close();
                mGpio19 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO", e);
            }
    }
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }
}


