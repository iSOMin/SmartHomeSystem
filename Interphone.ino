
#include <VirtualWire.h>
 

const int buttonPin = 12;               // Connect sensor to input pin 3 
const int rfPin = 7;
const int piezoPin = 8;
const int hz01=2400;
const int hz02=1900;

void setup() {
  Serial.begin(9600);
  pinMode(buttonPin, INPUT);   
  pinMode(rfPin, OUTPUT);
  pinMode(piezoPin, OUTPUT);
  vw_set_tx_pin(rfPin); 
  vw_setup(2000); 
  
  delay(500);
}

void loop(){
  int val = digitalRead(buttonPin);  
  
  //Serial.println(val);     

  if(val==HIGH){
    
    send("111");
    Serial.println("send message 111");  
    tone(piezoPin, hz01);
    delay(500);
    tone(piezoPin, hz02);
    delay(1000);
    tone(piezoPin, hz01);
    delay(500);
    tone(piezoPin, hz02);
    delay(1100);
    noTone(piezoPin);  
    delay(1000);
    
  }else{

    Serial.println("send message 000");    
    send("000");
    delay(50);
    
  }
  
  delay(10);

}
void send (char *message)
{
  vw_send((uint8_t *)message, strlen(message));
  vw_wait_tx();
}
