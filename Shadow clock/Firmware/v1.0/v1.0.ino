#include <FastLED.h>
#include <EEPROM.h>
#include <DS3231.h>
#define NUM_LEDS1 60
#define NUM_LEDS2 60
#define DATA_PIN1 10
#define DATA_PIN2 9
int mode = 1;
CRGB poqr[NUM_LEDS1];
CRGB mec[NUM_LEDS2];
DS3231  rtc(SDA, SCL);
Time  t;
//*******************clock variables*********************
int hourarrow = 0;
int minarrow = 0;
int secarrow = 0;
bool secar = true;
//*******************effects variables*******************
void setup() {
  Serial.begin(9600);
  Serial.println("setup pass");
  rtc.begin();
  FastLED.addLeds<WS2812B, DATA_PIN1, GRB>(poqr, NUM_LEDS1);
  FastLED.addLeds<WS2812B, DATA_PIN2, GRB>(mec, NUM_LEDS2);
  //rtc.setTime(12, 34, 00);
  FastLED.setBrightness(30);
  delay(3000);

}

void loop() {
  t = rtc.getTime();
  //***********clock ****************
  if (hourarrow == 0) {
    hourarrow = 1;
  }
  //hour arrow
  if (t.hour != hourarrow) {
    poqr[hourarrow - 1] = CRGB(0, 0, 0);
    hourarrow = t.hour;
    //Serial.print(t.hour);
  }
  else {
    poqr[hourarrow - 1] = CRGB(100, 100, 100);
  }
  //minute arrow
  if (minarrow == 0) {
    minarrow = 1;
  }
  if (t.min != minarrow) {
    poqr[minarrow - 1] = CRGB(0, 0, 0);
    minarrow = t.min;

  }
  else {
    poqr[minarrow - 1] = CRGB(255, 255 , 255);
  }
  //second arrow
  if (secar == true) {
    if (t.sec != secarrow) {
      //Serial.println("sec1");
      poqr[secarrow - 1] = CRGB(0, 0, 0);
      secarrow = t.sec;
    }
    else {
      poqr[secarrow - 1] = CRGB(255, 255 , 255);
      //Serial.println("sec2");
    }
  }
  //modes
  if (mode == 1) {
    mode1();
  }
  else if (mode == 2) {

  }
  else if (mode == 3) {
  }
  else if (mode == 4) {

  }
  else if (mode == 5) {

  }
  else if (mode == 6) {

  }
  else if (mode == 7) {

  }
  else if (mode == 8) {

  }
  FastLED.show();
}

//******************Effects*******************
unsigned long timer;
int i1 = 40;
int i2 = 40;
int row1, row2;
int wobl;
bool direction1 = true;
void mode1() {


  wobl = random(1, 30);
  if (direction1 == true && i1 == i2) {
    i2 = i1 + wobl;
    Serial.println("lol");
  }
  else if (i1 == i2) {
    i2 = i1 - wobl;
    Serial.println("kek");
  }
  if (millis() - timer > 100 && i2 != i1) {


    row1 = i2 / 2;
    row2 = 60 - (i2 / 2);
    for (int i = 0; i >= row1; i++) {
      mec[i] = CRGB(255, 0, 0);
    }
    for (int i = row2; i < 60; i++) {
      mec[i] = CRGB(255, 0, 0);
    }
    for (int i = row1; i < row2; i++) {
      mec[i] = CRGB(0, 0, 0);
    }
  }
  else if (i2 == i1) {
    direction1 = !direction1;
  }



}
