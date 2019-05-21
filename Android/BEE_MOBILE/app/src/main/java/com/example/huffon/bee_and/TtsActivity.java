package com.example.huffon.bee_and;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class TTSActivity extends AppCompatActivity {
    TextToSpeech tts;
    TextView message;
    TextView speak;
    TextView home;
    ImageButton speakBtn;
    String REQUEST_URL;
    String text;
    String convertedResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        Intent intent = getIntent();
        text = "";
        String result = "상대방의 마음을\n" + "변환 중이에요.";
        REQUEST_URL = intent.getExtras().getString("url");
        System.out.println(REQUEST_URL);
        getJSON();

        message = (TextView)findViewById(R.id.textView);
        speakBtn = (ImageButton)findViewById(R.id.board);
        speak = (TextView)findViewById(R.id.speak);
        home = (TextView)findViewById(R.id.reply);

        message.setText(result);
        message.setTextColor(Color.WHITE);
        message.setTextSize(20);

        speak.setText("음성으로\n듣기");
        speak.setTextColor(Color.WHITE);
        speak.setTextSize(18);

        // 음성 출력을 위한 안드로이드 내 TTS 객체 생성
        tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        // 스피커 버튼을 누를 경우, 읽어들인 문자 정보를 TTS 객체를 이용해 음성 출력
        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = convertedResult;
                Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        // 상대방에게 답장을 하기 위해, 답장 버튼을 누를 경우 STT 액티비티로 이동
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), STTActivity.class);
                startActivity(intent);
            }
        });
    }

    // 쓰레드는 직접 액티비티를 수정할 수 없으므로, Handler를 활용해 텍스트 정보 화면에 표시
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String valuefin = bun.getString("value");
            try {
                convertedResult = convertString(valuefin);
                message.setText("상대방이\n" + convertedResult+ "\n라는 마음을 전했어요!");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    // JSON 데이터 포맷으로 점자 변환 정보를 요청하는 메서드
    public void  getJSON() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(REQUEST_URL);
                    HttpsURLConnection myConnection = (HttpsURLConnection) url.openConnection();

                    if (myConnection.getResponseCode() == 200) {// Success
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader =
                                new InputStreamReader(responseBody, "UTF-8");

                        JsonReader jsonReader = new JsonReader(responseBodyReader);
                        jsonReader.beginObject(); // Start processing the JSON object

                        // JSON 파일을 모두 돌며 로직 수행
                        while (jsonReader.hasNext()) {
                            String key = jsonReader.nextName();
                            // JSON 내 'body' 키를 가진 데이터 셋을 찾음
                            if (key.equals("body")) {
                                text = jsonReader.nextString();
                                // 모바일 화면에 표시해주기 위해 Handler에 텍스트 정보 전달
                                Bundle bun = new Bundle();
                                bun.putString("value", text);
                                Message msg = handler.obtainMessage();
                                msg.setData(bun);
                                handler.sendMessage(msg);
                                break;
                            } else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.close();
                        myConnection.disconnect();
                    } else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String convertString(String input) throws UnsupportedEncodingException {
        String converted = "" + input;
        StringBuilder converter = new StringBuilder();
        char ch;
        for(int i = 0; i < converted.length(); i++) {
            ch = converted.charAt(i);
            if(ch=='\\' && converted.charAt(i+1) == 'u') {
                converter.append((char)Integer.parseInt(converted.substring(i+2, i+6), 16));
                i+=5;
                continue;
            }
            converter.append(ch);
        }
        converted = converter.toString();
        byte[] bt = converted.getBytes("UTF-8");
        converted = new String(bt, "UTF-8");

        return converted;
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}