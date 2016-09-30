package com.somin.finalsmarthome;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dfrobot.angelo.blunobasicdemo.BlunoLibrary;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * MyApplicationActivity - The starting point for creating your own Hue App.  
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 * 
 * @author SteveyO
 *
 */
public class MyApplicationActivity extends BlunoLibrary {
    private PHHueSDK phHueSDK;
    private static final int MAX_HUE=65535;
    public static final String TAG = "Smart Home System";


    // Hue variables
    private int originalLightColor = 35000;
    private int phoneCallLightColor = 46920;
    private int doorBellLightColor = 65200;

    private Button hueOnButton;
    private Button hueOffButton;
    private Button hueRandomButton;

    // Bluno variables
    Button blunoConnectionButton;
    private Button buttonSerialSend;
    private EditText serialSendText;
    private TextView serialReceivedText;

    private ImageView logoOnImage,logoOffImage;

    // main function variables
    private Button phoneCallButton;
    private ImageView phoneCallImage;
    private String receivedDoorBellData = "z";
    private int lightChangedFlag = 0;







    private RelativeLayout colorPickLayout;
    private Button colorPickOpenButton, colorPickCloseButton;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onCreateProcess();														//onCreate Process by BlunoLibrary
        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);


        phHueSDK = PHHueSDK.create();
        turnLight("on");



        logoOnImage =  (ImageView) findViewById(R.id.home_on);
        logoOffImage =  (ImageView) findViewById(R.id.home_off);

        colorPickOpenButton=(Button)findViewById(R.id.color_pick_layout_open);
        colorPickCloseButton=(Button)findViewById(R.id.close_color_pick);
        colorPickLayout=(RelativeLayout)findViewById(R.id.color_pick_layout);
        colorPickOpenButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                colorPickLayout.setVisibility(View.VISIBLE);
            }
        });
        colorPickCloseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                colorPickLayout.setVisibility(View.GONE);
            }
        });




        /**
         * bluno
         */

        // set layout
        blunoConnectionButton = (Button) findViewById(R.id.blunoConnection);
        blunoConnectionButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
            }
        });

        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);
        buttonSerialSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
            }
        });

        serialSendText = (EditText) findViewById(R.id.serialSendText);
        serialReceivedText = (TextView) findViewById(R.id.serialReveicedText);


        /**
         * main functions
         */

        // Phone Call
        phoneCallImage = (ImageView) findViewById(R.id.iv_incoming);
        phoneCallButton = (Button) findViewById(R.id.phoneCallButton);		//initial the button for sending the data

        phoneCallButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                final MediaPlayer music;
                music = MediaPlayer.create(MyApplicationActivity.this, R.raw.timeto);

                music.start();
                changeLightColorTo (phoneCallLightColor); // Hue 알림 색상


                final Handler phoneCallHandler3 = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        phoneCallImage.setVisibility(View.GONE);
                        if(music.isPlaying()) {             // 재생중이면 실행될 작업
                            music.stop();                   // stop
                            changeLightColorTo (originalLightColor);
                        }
                    }
                };


                final Handler phoneCallHandler1 = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        phoneCallImage.setVisibility(View.VISIBLE);   // phone call image pop up
                        serialSend("a");                    // most important

                        phoneCallHandler3.sendEmptyMessageDelayed(0, 9000);
                    }
                };


                final Handler phoneCallHandler2 = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        serialSend("a");            //most important

                        phoneCallHandler1.sendEmptyMessageDelayed(0, 500);
                    }
                };

                phoneCallHandler2.sendEmptyMessageDelayed(0, 500);    // ms, 3초후 종료시킴
            }
        });


        /**
         * additional usage of Philips Hue
         */

        // Hue On/Off
        hueOnButton = (Button) findViewById(R.id.hueOn);
        hueOnButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                turnLight("on");
                hueOffButton.setVisibility(View.VISIBLE);
                hueOnButton.setVisibility(View.INVISIBLE);

                logoOnImage.setVisibility(View.VISIBLE);
                logoOffImage.setVisibility(View.INVISIBLE);

            }
        });

        hueOffButton = (Button) findViewById(R.id.hueOff);
        hueOffButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                turnLight("off");
                hueOffButton.setVisibility(View.INVISIBLE);
                hueOnButton.setVisibility(View.VISIBLE);

                logoOnImage.setVisibility(View.INVISIBLE);
                logoOffImage.setVisibility(View.VISIBLE);
            }
        });

        // Random lights
        hueRandomButton = (Button) findViewById(R.id.hueRandom);
        hueRandomButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                randomLights();
            }

        });

    }


    // If you want to handle the response from the bridge, create a PHLightListener object.
    PHLightListener listener = new PHLightListener() {
        
        @Override
        public void onSuccess() {  
        }
        
        @Override
        public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1) {
           Log.w(TAG, "Light has updated");
        }
        
        @Override
        public void onError(int arg0, String arg1) {}

        @Override
        public void onReceivingLightDetails(PHLight arg0) {}

        @Override
        public void onReceivingLights(List<PHBridgeResource> arg0) {}

        @Override
        public void onSearchComplete() {}
    };

    protected void onResume(){
        super.onResume();
        System.out.println("BlUNOActivity onResume");
        onResumeProcess();														//onResume Process by BlunoLibrary
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }
/*
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }

    protected void onStop() {
        super.onStop();
        onStopProcess();														//onStop Process by BlunoLibrary
    }
*/
    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {
            
            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }
            
            phHueSDK.disconnect(bridge);
            super.onDestroy();
            onDestroyProcess();
        }
    }







    // Hue related
    public void turnLight(String mode) {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();

            if(mode == "on") {
                lightState.setHue(originalLightColor);
                lightState.setOn(true);
            } else {
                lightState.setOn(false);
            }

            bridge.updateLightState(light, lightState, listener);
        }
    }

    public void changeLightColorTo(int hueColor) {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setHue(hueColor);

            bridge.updateLightState(light, lightState, listener);
        }

    }

    public void randomLights() {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        Random rand = new Random();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setHue(rand.nextInt(MAX_HUE));

            // To validate your lightstate is valid (before sending to the bridge) you can use:
            // String validState = lightState.validateState();
            bridge.updateLightState(light, lightState, listener);
            //  bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
        }
    }



    // Bluno related
    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                blunoConnectionButton.setText("");
                break;
            case isConnecting:
                blunoConnectionButton.setText("");
                break;
            case isToScan:
                blunoConnectionButton.setText("");
                break;
            case isScanning:
                blunoConnectionButton.setText("");
                break;
            case isDisconnecting:
                blunoConnectionButton.setText("");
                break;
            default:
                break;
        }
    }

    // door bell
    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        // TODO Auto-generated method stub
        //serialReceivedText.append(theString);

        Log.e("///"," the string : "+theString);
        if (theString.equals("d")) {
            changeLightColorTo(doorBellLightColor);
        } else if (theString.equals("z")) {
            changeLightColorTo(originalLightColor);
        }
    }

}
