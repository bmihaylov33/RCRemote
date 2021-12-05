#include <NewPing.h>
#include <Vcc.h>

//motor A connected between A01 and A02
//motor B connected between B01 and B02

#define PWMA 3
#define AIN1 6
#define AIN2 4
#define PWMB 5
#define BIN1 13
#define BIN2 10
#define STBY 11

#define TRIGGER_PIN  8  // Arduino pin tied to trigger pin on the ultrasonic sensor.
#define ECHO_PIN     9   // Arduino pin tied to echo pin on the ultrasonic sensor.
#define MAX_DISTANCE 250 // Maximum distance we want to ping for (in centimeters). Maximum sensor distance is rated at 400-500cm.

int LEDF 7   //front led
int LEDB 12  //back led

char command;
char sending;
String string;
boolean ledon = false;
boolean btConnected = false;

const float VccMin = 0.0;           // Minimum expected Vcc level, in Volts.
const float VccMax = 5.0;           // Maximum expected Vcc level, in Volts.
const float VccCorrection = 1.0/1.0;  // Measured Vcc by multimeter divided by reported Vcc

NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE); // NewPing setup of pins and maximum distance.
Vcc vcc(VccCorrection);

void setup() {

  Serial.begin(9600);
  
  pinMode(LEDF,OUTPUT);  //led front 
  pinMode(LEDB,OUTPUT);  //led back 
  
  pinMode(STBY, OUTPUT);

  pinMode(PWMA, OUTPUT);
  pinMode(AIN1, OUTPUT);
  pinMode(AIN2, OUTPUT);

  pinMode(PWMB, OUTPUT);
  pinMode(BIN1, OUTPUT);
  pinMode(BIN2, OUTPUT);
}

void loop() {

  if(btConnected == false) {
   blinkLed(!btConnected); 
  }
  startUp();

  sendData();
  delay(200);

  if (Serial.available() > 0) {
    string = "";
  }
    
  while(Serial.available() > 0) {
    command = ((byte)Serial.read());
    string += command;
  }
  delay(1);
  Serial.println(string);
  if(string == "c") {
    btConnected = true; 
  }
  
  if(string.lastIndexOf("F") > 0) {
    motorForward();
  } 
  if(string.lastIndexOf("B") > 0) {
    motorBack();
  }
  if(string.lastIndexOf("L") > 0) {
    motorLeft();
  }
  if(string.lastIndexOf("R") > 0) {
    motorRight();
  }
  if(string.lastIndexOf("Q") > 0) {
   applyBrakes();
   delay(300);
  }
  if(string.lastIndexOf("O") > 0) {
   longLedOn();
   ledon = true;
  } 
  if (string.lastIndexOf("o") > 0) {
   shortLedOn();
   ledon = true;  
  }
  if(string.lastIndexOf("s") > 0) {
   ledOff();
   ledon = false;
   Serial.println(string); //debug
  }  
 
  delay(200);
}

void sendBatteryPercentage() {
   //puts # before the values so our app knows what to do with the data
  Serial.print('#');
  float percentage = vcc.Read_Perc(VccMin, VccMax);//gets the percentage value of the battery
  Serial.print(percentage);
  Serial.print('~'); //used as an end of transmission character - used in app for string length
  Serial.println();
//  delay(400);        //added a delay to eliminate missed transmissions
}

void sendDistanceValue() {
  //puts / before the values so our app knows what to do with the data
  Serial.print('/');
  delay(30);                      // Wait 50ms between pings (about 20 pings/sec). 29ms should be the shortest delay between pings.
  unsigned int uS = sonar.ping(); // Send ping, get ping time in microseconds (uS).
  Serial.print(sonar.convert_cm(uS)); // Convert ping time to distance and print result (0 = outside set distance range, no ping echo)
  Serial.print('~'); //used as an end of transmission character - used in app for string length
  Serial.println();
//  delay(400);
}

void sendData() {
  Serial.print('#');
  float percentage = vcc.Read_Perc(VccMin, VccMax);//gets the percentage value of the battery
  Serial.print(percentage);

  Serial.print('/');
  delay(30);                          // Wait 50ms between pings (about 20 pings/sec). 29ms should be the shortest delay between pings.
  unsigned int uS = sonar.ping();     // Send ping, get ping time in microseconds (uS).
  Serial.print(sonar.convert_cm(uS)); // Convert ping time to distance and print result (0 = outside set distance range, no ping echo)
  Serial.print('~');                  //used as an end of transmission character - used in app for string length
  Serial.println();
}

void longLedOn() {
  //if value from bluetooth serial is 'O' turn leds ON
  
  digitalWrite(LEDF,HIGH);  //switch on LED
  digitalWrite(LEDB,HIGH);  //switch on LED
  //delay(10);
}

void shortLedOn() {
  //if value from bluetooth serial is 'o' turn leds ON
  
  digitalWrite(LEDF,100); //switch on LED
  digitalWrite(LEDB,80);  //switch on LED
  //delay(10);
}

void blinkLed(boolean flag) {
  //if value from bluetooth serial is 'c' turn leds ON

  if(flag) {
  shortLedOn();
//  delay(800);
  ledOff();
  }
}

void ledOff() {
  //if value from bluetooth serial is 's' turn leds OFF
  digitalWrite(LEDF,LOW);            //turn off LED
  digitalWrite(LEDB,LOW);            //turn off LED
  //delay(10);
}

void motorForward() {
  digitalWrite (AIN1,HIGH);
  digitalWrite (AIN2,LOW);
  analogWrite  (PWMA,100); 
  Serial.println("forward");
}

void motorBack() {
  digitalWrite (AIN1,LOW);
  digitalWrite (AIN2,HIGH);
  analogWrite  (PWMA,100);
  Serial.println("backward");
}

void motorLeft() {
  digitalWrite (BIN1,HIGH);
  digitalWrite (BIN2,LOW);
  analogWrite  (PWMB,150);
  Serial.println("left");
}

void motorRight() {
  digitalWrite (BIN1,LOW);
  digitalWrite (BIN2,HIGH);
  analogWrite  (PWMB,150);  
  Serial.println("right");
}

void applyBrakes() {
  digitalWrite (AIN1,HIGH);
  digitalWrite (AIN2,HIGH);
  analogWrite  (PWMA,255);
  digitalWrite (BIN1,HIGH);
  digitalWrite (BIN2,HIGH);
  analogWrite  (PWMB,255);  
}

void veerLeft() {
  digitalWrite (AIN1,HIGH);
  digitalWrite (AIN2,LOW);
  analogWrite  (PWMA,190);
  digitalWrite (BIN1,HIGH);
  digitalWrite (BIN2,LOW);
  analogWrite  (PWMB,255);  
}

void veerRight() {
  digitalWrite (AIN1,HIGH);
  digitalWrite (AIN2,LOW);
  analogWrite  (PWMA,255);
  digitalWrite (BIN1,HIGH);
  digitalWrite (BIN2,LOW);
  analogWrite  (PWMB,190);  
}


void startUp() {
  digitalWrite(STBY,HIGH);
}

void shutDown() {
  digitalWrite(STBY,LOW);
}
