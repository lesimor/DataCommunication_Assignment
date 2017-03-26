const int ledPin=13;
int blinkRate=0;
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);  //send and receive data at 9600baud speed.
  pinMode(ledPin, OUTPUT);

}
int number = 0;
void loop() {
  // put your main code here, to run repeatedly:
  if(Serial.available()){
    int val = Serial.parseInt(); 
    blinkRate = val * 100;
  }
  blink();
}

void blink(){
  digitalWrite(ledPin,HIGH);
  delay(blinkRate);
  digitalWrite(ledPin, LOW);
  delay(blinkRate);
}

