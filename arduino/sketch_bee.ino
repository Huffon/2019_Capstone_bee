#include <SoftwareSerial.h>

SoftwareSerial BEE(2,3);
char buffer[2];
int bufferIndex=0;
int SOL_1 = 4;
int SOL_2 = 5;
int SOL_3 = 6;
int SOL_4 = 7;
int SOL_5 = 8;
int SOL_6 = 9;
int button1 = A0;
int button2 = A1;
int button3 = A2;
int button4 = A3;
int button5 = A4;
int button6 = A5;
int next=12;
//int enter=13;
//int backspace=14;
int b1=0;
int b2=0;
int b3=0;
int b4=0;
int b5=0;
int b6=0;
void setup() {
  BEE.begin(9600);
  Serial.begin(9600);
  pinMode(SOL_1, OUTPUT);
  pinMode(SOL_2, OUTPUT);
  pinMode(SOL_3, OUTPUT);
  pinMode(SOL_4, OUTPUT);
  pinMode(SOL_5, OUTPUT);
  pinMode(SOL_6, OUTPUT);
  pinMode(button1, INPUT_PULLUP);
  pinMode(button2, INPUT_PULLUP);
  pinMode(button3, INPUT_PULLUP);
  pinMode(button4, INPUT_PULLUP);
  pinMode(button5, INPUT_PULLUP);
  pinMode(button6, INPUT_PULLUP);
  pinMode(next, INPUT_PULLUP);
}

void loop() {
    if(BEE.available()&&digitalRead(next)==LOW){
      bufferIndex=0;
      delay(50);
    }
    if(BEE.available()&&bufferIndex==0){
      for(int a=0;a<2;a++) {
      buffer[a] = NULL;
      }
      buffer[0]=BEE.read();
      bufferIndex++;
    }
    if(BEE.available()&&bufferIndex==1){
      buffer[1]=BEE.read();
      bufferIndex++;
    }
    int bt=atoi(buffer);
    int cell[6]={0,0,0,0,0,0};
    
    int i=0;
    while(bt>=1){
      cell[i]=bt%2;
      bt/=2;
      i++;
    }
    if(cell[0]>0)
      digitalWrite(SOL_1, HIGH);
    else
      digitalWrite(SOL_1, LOW);
    if(cell[1]>0)
      digitalWrite(SOL_2, HIGH);
    else
      digitalWrite(SOL_2, LOW);
    if(cell[2]>0)
      digitalWrite(SOL_3, HIGH);
    else
      digitalWrite(SOL_3, LOW);
    if(cell[3]>0)
      digitalWrite(SOL_4, HIGH);
    else
      digitalWrite(SOL_4, LOW);
    if(cell[4]>0)
      digitalWrite(SOL_5, HIGH);
    else
      digitalWrite(SOL_5, LOW);
    if(cell[5]>0)
      digitalWrite(SOL_6, HIGH);
    else
      digitalWrite(SOL_6, LOW);

    b1+=!digitalRead(button1);
    b2+=!digitalRead(button2);
    b3+=!digitalRead(button3);
    b4+=!digitalRead(button4);
    b5+=!digitalRead(button5);
    b6+=!digitalRead(button6);

    if(digitalRead(next)==LOW){
      if(b1>0)
        b1=1;
      if(b2>0)
        b2=1;
      if(b3>0)
        b3=1;
      if(b4>0)
        b4=1;
      if(b5>0)
        b5=1;
      if(b6>0)
        b6=1;
      int sum=b1+(b2*2)+(b3*4)+(b4*8)+(b5*16)+(b6*32);
      //BEE.println(sum);
      BEE.write(sum);
      //Serial.println(sum);
      b1=0;b2=0;b3=0;b4=0;b5=0;b6=0;sum=0;
      delay(500);    
    }
    delay(100);
}
