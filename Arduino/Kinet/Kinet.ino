#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

#include <Adafruit_NeoPixel.h>
#ifdef __AVR__
#include <avr/power.h>
#endif

// Which pin on the Arduino is connected to the NeoPixels?
// On a Trinket or Gemma we suggest changing this to 1
#define PIN            2

// How many NeoPixels are attached to the Arduino?
#define NUMPIXELS      150

// When we setup the NeoPixel library, we tell it how many pixels, and which pin to use to send signals.
// Note that for older NeoPixel strips you might need to change the third parameter--see the strandtest
// example for more information on possible values.
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUMPIXELS, PIN, NEO_GRB + NEO_KHZ800);

char ssid[] = "your-ssid";          // your network SSID (name)
char pass[] = "your-password";    // your network password

// A UDP instance to let us send and receive packets over UDP
WiFiUDP Udp;
const unsigned int outPort = 9999;          // remote port (not needed for receive)
const unsigned int localPort = 6038;        // local port to listen for UDP packets (here's where we send the packets)

int headerSize = 21;

void setup() {
  Serial.begin(115200);

  // Connect to WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");

  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  Serial.println("Starting UDP");
  Udp.begin(localPort);
  Serial.print("Local port: ");
  Serial.println(Udp.localPort());


  pixels.begin(); // This initializes the NeoPixel library.
}

void loop() {
  int size = Udp.parsePacket();
//  int diff = size - NUMPIXELS;
  uint8_t buf[3];
  if (size > 0) {
    // Get the header
    for (int i = 0; i < headerSize; i++) {
      int x = Udp.read();
    }
    // Get all the data
    for (int i=0; i<NUMPIXELS; i++) {
      for (int j=0; j<3; j++) {
        buf[j] = Udp.read();
        if (j == 2) {
          pixels.setPixelColor(i, pixels.Color(buf[0], buf[1], buf[2]));
        }
      }
    }
    // Dump the rest of the useless UDP data
    Udp.flush();
    // Turn on pixels
    pixels.show();
  }
}
