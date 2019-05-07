package com.example.huffon.bee_and;

import android.content.Intent;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Locale;

public class TtsActivity extends AppCompatActivity {
    TextToSpeech tts;
    TextView message;
    TextView speak;
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

        message.setText(result);
        message.setTextColor(Color.WHITE);
        message.setTextSize(20);

        speak.setText("음성으로\n듣기");
        speak.setTextColor(Color.WHITE);
        speak.setTextSize(18);

        tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = text;
                Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
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