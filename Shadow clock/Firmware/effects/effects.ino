#include <FastLED.h>
#define DATA_PIN2 9
#define NUM_LEDS2 60
CRGB mec[NUM_LEDS2];
int avgh = 30; //avarage height of fire
#define wobl 15 //fire's main line random change amount
//fire
int randsize = 0;
int lastrandsize = 0;
unsigned long seed;
unsigned long timer;
int rando;
int i;
void setup() {
  FastLED.addLeds<WS2812B, DATA_PIN2, GRB>(mec, NUM_LEDS2);
  FastLED.setBrightness(30);
  Serial.begin(9600);
}

void loop() {
  /*
    for (int i = 0; i < 400; i++) {
    seed = 1;
    for (byte j = 0; j < 16; j++) {
      seed *= 4;
      seed += analogRead(A0) & 3;
    }
    //Serial.println(seed);
    }
    randomSeed(seed);
  */
  /*
    mec[1] = CRGB(125, 0, 0);
    FastLED.show();
    delay(500);
    // Now turn the LED off, then pause
    mec[1] = CRGB(0, 0, 0);
    FastLED.show();
    delay(500);
  */


  //randsize = avgh + random(0, wobl);
  //Serial.println(randsize);



  //Serial.print(randsize); Serial.print("  "); Serial.println(lastrandsize);

  //if (randsize >= lastrandsize) {
  /*
    for (int i = 0; i > 60; i++) {
    if (millis() - timer >= 500) {
      timer = millis();
      mec[i] = CRGB(168, 40, 20);
      FastLED.show();
      //Serial.println("k");
    }
    }
  */

  if (millis() - timer >= 100) {
    if (i < 60) {
      timer = millis();
      mec[i] = CRGB(125, 0, 0);
      Serial.println("k");
      FastLED.show();
      i++;
    }
    else if(i > -1){
      timer = millis();
      mec[i] = CRGB(0, 0, 0);
      Serial.println("k");
      FastLED.show();
      i--;
    }

  }


 // lastrandsize = randsize;
  //FastLED.show();

}
