/**
 * @file       smartclock.ino
 * @author     Wonyoung Choi, Hyunjun Park, Somin Lee
 * @date       2016.08.23
 * @version    1.0
 * @brief      smart perfume clock
 * @details    A clock that sprays perfume when it gets signal
 */

//#include <NS_Rainbow.h>
#include <VirtualWire.h>
#include <TFT.h> // Hardware-specific library
#include <SPI.h>
#include <Time.h>
#define N_CELL 8

// Relay variables
const int Relay = 2;

const int ledPin = 4;
const int rfPin = 5;
const int resetPin = 6;
const int dcPin = 7;
const int csPin = 8;

/* Time variables */
unsigned long previousMillis = 0;   
    // LED의 상태가 업데이트된 시간을 기록할 변수  
const long interval = 1000;         
    // LED 상태 변경할 시간 지정(ms단위)  

/* Signal variables */
boolean rfSignalIn = false;     // door bell
boolean btSignalIn = false;     // phone call
byte message[VW_MAX_MESSAGE_LEN];         
    // a buffer to store the incoming messages
byte messageLength = VW_MAX_MESSAGE_LEN;  
    // the size of the message

int dFlag = 0;    
    // pass 'd' for light color change to the door bell color
int zFlag = 1;    
    // pass 'z' for light color change to the original color

/* TFT LCD declaration */
TFT myScreen = TFT(csPin, dcPin, resetPin);

//NS_Rainbow ledStick = NS_Rainbow(N_CELL,ledPin);


void setup() {
    Serial.begin(115200);
    
    myScreen.begin();  
    myScreen.background(0, 0, 0); 
    
    myScreen.stroke(255, 255, 255);
    myScreen.setTextSize(2);
    myScreen.text("Smart", 5, 0);
    
    myScreen.text("Perfume", 5, 20);
    myScreen.text("Clock", 5, 40);
    myScreen.setTextSize(4);
    //myScreen.text("15:00 ",15,70);
  
    
    //ledStick.begin();
    //showNormalLED();
    delay(500);
    vw_set_rx_pin(rfPin);
    vw_setup(2000); 
    vw_rx_start(); 
    pinMode(Relay, OUTPUT);     //Set Pin3 as output
  
    setTime(14, 20, 0, 11, 3, 16);
}


void loop() {
    unsigned long currentMillis = millis();  
  
    // phone ring
    if (Serial.available())  {
        if(Serial.read() == 97) {   // a==97
            btSignalIn = true;
            //Serial.println ("btSignalIn");
        }
    }
  

  
    /* get rf signal */
    /* - Door bell sends '1' when bell rings.
       - When door bell rang, clock send 'd' to the smartphone.
       - When the ring finished, clock send 'z' to the smartphone.
       - dFlag and zFlag were set to send characters once.
       - If characters were send repeatedly, smartphone app overworks.*/
    if (vw_get_message(message, &messageLength)) {  // Non-blocking
        Serial.print("Received: ");
        for (int i = 0; i < messageLength; i++) { 
            Serial.write(message[i]);
            if(message[i] == 49) {      // 49byte == "1" 
                // notify signal of rfSignalIn
                if (dFlag == 0) {
                    Serial.write("d");
                    dFlag = 1;
                    zFlag = 0;
                }
                rfSignalIn = true;
            } else { 
                if (zFlag == 0) {
                    Serial.write ("z");
                    zFlag = 1;
                    dFlag = 0;
                } 
            }
        }
        Serial.println();
    }

  
    /* case: door bell */
    if(rfSignalIn) {
        myScreen.setCursor(10,94);
        myScreen.setTextSize(2);
        myScreen.noStroke(); 
        myScreen.fill(0,0,0);
        myScreen.rect(10,94,myScreen.width(),myScreen.height());
        myScreen.print("Interphone\n Ring!");
        //myScreen.text("Interphone Ring!",10,80);
    
        Serial.println("pump01 is active@!");
        pumpActive(Relay);
        //showLed(1,6);
        rfSignalIn=false;
        //showNormalLED();
        delay(10);
    }
  
    /* case: phone call */
    if(btSignalIn) {
        myScreen.setCursor(10,94);
        myScreen.setTextSize(2);
        myScreen.noStroke(); 
        myScreen.fill(0,0,0);
        myScreen.rect(10,94,myScreen.width(),myScreen.height());
        myScreen.print("SmartPhone\n Ring!");
        //myScreen.text("SmartPhone Ring!",10,94);
        
        Serial.println("pump02 is active@!");
        pumpActive(Relay);
        //showLed(0,6);
        btSignalIn=false;
        //showNormalLED();
        delay(10);
    }

    /* TFT Screen state changes */
    if(currentMillis - previousMillis >= interval) {
        previousMillis = currentMillis;       
        myScreen.setCursor(10, 65);
        myScreen.setTextSize(3);
        digitalClockDisplay(myScreen);
    }
    
    delay(5);
    
}


/**
 * @brief       activate water pump motor
 * @details     turn on and off the target relay module 
 *              so the water pump moter works to spray perfume
 * @param       targetRelay target relay module
 */
void pumpActive(int targetRelay) {
    digitalWrite(targetRelay, HIGH);   // Turn off relay
    delay(120);
    digitalWrite(targetRelay, LOW);    // Turn on relay
}


/**
 * @brief       display clock
 * @details     show hour, minute and second
 * @param       screen TFT screen
 */
void digitalClockDisplay(TFT screen){

    myScreen.noStroke(); 
    myScreen.fill(0, 0, 0);
    screen.rect(10, 65, screen.width(), 22);
    screen.print(hour());
    screen.print(":");
    screen.print(minute());
    screen.print(":");
    
    if(second() < 10) {
        screen.print("0");
        screen.print(second());
    } else {
        screen.print(second());
    }
}


/*
void showLed(int color,int count)
{
  int r=200;
  int g=10;
  int b=20;

  if(color==1){
    r=200;
    g=10;
    b=20;
  }
  
  int bright=190;
  int brightDelay=100;

  for(int i=0;i<count;i++)
  {
        ledStick.clear();
        ledStick.setBrightness(bright); 
        
        ledStick.setColor(0,   r, g, b);
        ledStick.setColor(1,   r, g, b);
        ledStick.setColor(2,   r, g, b);
        ledStick.setColor(3,   r, g, b);
        ledStick.setColor(4,   r, g, b);
        ledStick.setColor(5,   r, g, b);
        ledStick.setColor(6,   r, g, b);
        ledStick.setColor(7,   r, g, b);
        ledStick.show();
        delay(brightDelay);
        ledStick.clear();
        ledStick.show();
        delay(brightDelay);
  }
  
  ledStick.clear();
}

void showNormalLED(){

  int bright=40;
  int r=10;
  int b=10;
  int g=10;
  ledStick.clear();
  ledStick.setBrightness(bright); 
  ledStick.setColor(0,   r, g, b);
  ledStick.setColor(1,   r, g, b);
  ledStick.setColor(2,   r, g, b);
  ledStick.setColor(3,   r, g, b);
  ledStick.setColor(4,   r, g, b);
  ledStick.setColor(5,   r, g, b);
  ledStick.setColor(6,   r, g, b);
  ledStick.setColor(7,   r, g, b);
  ledStick.show();
  
}*/
