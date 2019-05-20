package com.example.huffon.bee_and;

import android.content.Intent;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class TTSActivity extends AppCompatActivity {
    TextToSpeech tts;
    TextView message;
    TextView speak;
    TextView home;
    ImageButton speakBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        Intent intent = getIntent();
        String text = intent.getExtras().getString("sentence");
        String result = "상대방이\n\"" + text + "\"\n라는 마음을 전했어요.";

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
                String toSpeak = text;
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

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}