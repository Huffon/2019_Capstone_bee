#include <SoftwareSerial.h>

SoftwareSerial BEE(2,3);
int SOL[6]={A0,A1,A2,A3,A4,A5};
int next = 4;
int button1 = 5;
int button2 = 6;
int button3 = 7;
int button4 = 8;
int button5 = 9;
int button6 = 10;
int enter = 11;
int backspace =12;
int send_msg=13;
int b1=0;
int b2=0;
int b3=0;
int b4=0;
int b5=0;
int b6=0;
char buffer[2]; // 어플리케이션으로부터 2자리 문자열로 받아오는 정보
int bufferIndex=0;
int bt; // 어플리케이션으로부터 받아온 정보를 저장할 10진수의 정수
int cell[6]; // 점자셀에서 돌출시켜줘야 하는 솔레노이드 구분자
int stat[6]; // 점자셀에서 각각의 돌출 상태
String information; // 어플리케이션으로 전송할 점자정보
String text; // 점자정보에 입력될 한 글자 단위의 정보
char b_info[3]; // (입력시) 철자 단위의 정보

 

int sum;
void setup() {
  BEE.begin(9600);
  Serial.begin(9600);
  bt=0;
  pinMode(SOL[0], OUTPUT);
  pinMode(SOL[1], OUTPUT);
  pinMode(SOL[2], OUTPUT);
  pinMode(SOL[3], OUTPUT);
  pinMode(SOL[4], OUTPUT);
  pinMode(SOL[5], OUTPUT);
  pinMode(button1, INPUT_PULLUP);
  pinMode(button2, INPUT_PULLUP);
  pinMode(button3, INPUT_PULLUP);
  pinMode(button4, INPUT_PULLUP);
  pinMode(button5, INPUT_PULLUP);
  pinMode(button6, INPUT_PULLUP);
  pinMode(next, INPUT_PULLUP);
  pinMode(enter, INPUT_PULLUP);
  pinMode(backspace, INPUT_PULLUP);
  for(int s=0; s<6; s++){
    cell[s]=0;
    stat[s]=0;
    digitalWrite(SOL[s], HIGH);
  }
  
}

void loop() {
  int start=0; // 무한루프를 막아주는 트리거
    // next 버튼을 눌렀을 때
    if(digitalRead(next)==LOW){ //BEE.available()&&
      // 어플리케이션으로부터 받아올 점자정보의 공간을 초기화
      bufferIndex=0;
      for(int a=0;a<2;a++) {
      buffer[a] = NULL;
      }
      delay(50);
      // 어플리케이션으로부터 모든 점자정보를 받아온 경우, 점자셀을 초기화
      if(!(BEE.available())){
        for(int s=0; s<6; s++){
          if(stat[s]==1){
            digitalWrite(SOL[s],LOW);
            delay(50);
            digitalWrite(SOL[s],HIGH);
          }
          stat[s]=0;
          cell[s]=0; 
        }
      }
        
    }

    // 어플리케이션으로부터 점자정보의 첫번째 자리를 받아옴
    if(BEE.available()&&bufferIndex==0){
      buffer[0]=BEE.read();
      bufferIndex++;
    }
    // 어플리케이션으로부터 점자정보의 두번째 자리를 받아옴
    if(BEE.available()&&bufferIndex==1){
      buffer[1]=BEE.read();
      bufferIndex++;

      // 문자열로 받아온 두 개의 점자정보를 정수화 시켜줌
      bt=atoi(buffer);
      for(int s=0; s<6; s++){
        cell[s]=0;
      }
      int i=0;
      // 10진수의 정수가 된 정수를 2진수로 바꿔서 각각의 셀에 삽입
      while(bt>0){
        cell[i]=bt%2;
        bt/=2;
        i++;
      }
      start=1;
      }
    // 새롭게 받아온 점자정보가 있을 때의 동작
    while(start==1){
      for(int s=0; s<6; s++){
        // 점자셀 중 현재의 상태와 표현하고자 하는 부분을 계산하여 동작
        if(cell[s]!=stat[s]){
          digitalWrite(SOL[s],LOW);
          delay(50);
          digitalWrite(SOL[s],HIGH);
          // 현재 상태 수정
          if(stat[s]==0){
            stat[s]=1;
          }
          else{
            stat[s]=0;
          }
        }
      }
      start=0; // 무한루프를 막아줌
    }

    /* 입력부 */
    
    // 점자 입력부에서 입력된 버튼을 저장
    b1+=!digitalRead(button1);
    b2+=!digitalRead(button2);
    b3+=!digitalRead(button3);
    b4+=!digitalRead(button4);
    b5+=!digitalRead(button5);
    b6+=!digitalRead(button6);
    // enter 버튼을 누르는 경우의 동작
    if(digitalRead(enter)==LOW){
      sum=0;
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
      // 눌러진 버튼의 총합을 10진수로 바꿔줌
      sum=b1+(b2*2)+(b3*4)+(b4*8)+(b5*16)+(b6*32);

      // 10진수 형태의 정수를 2자리의 문자열로 바꿔줌
      if(sum<10){
        b_info[0]='0';
        b_info[1]=sum+48;
        
      }
      else
        itoa(sum,b_info,10);
      text.concat(b_info);
      b1=0;b2=0;b3=0;b4=0;b5=0;b6=0;sum=0;
      delay(500);    
    }

    // backspace 버튼을 누를 경우 현재 입력중인 점자를 전부 지움
    if(digitalRead(backspace)==LOW){
      b1=0;b2=0;b3=0;b4=0;b5=0;b6=0;
      delay(50);
    }
    // send 버튼을 누를 때의 동작
    if(digitalRead(send_msg)==LOW){
      // 점자입력 없이 send 버튼을 눌렀을 때
      if(text.length()==0){
        // 현재 입력된 점자를 어플리케이션으로 전송
        BEE.println(information);
        information.remove(0);
        delay(500);
      }
      // 현재 만들어놓은 점자를 송신할 점자 정보에 입력
      else{
        information.concat(text);
        information.concat('!');
        text.remove(0);
        delay(100);
      }
    }
    delay(100);
}
