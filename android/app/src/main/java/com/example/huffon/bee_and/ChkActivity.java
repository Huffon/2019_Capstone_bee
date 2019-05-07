package com.example.huffon.bee_and;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class ChkActivity extends AppCompatActivity {
    public static final int LOAD_SUCCESS = 101;
    private String REQUEST_URL = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/bee?word=";
    private String convertQuery = null;
    private String value;

    private TextView message;
    private TextView conv_result;
    private ImageButton ttb;
    private ImageButton retrial;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chk);

        Intent intent = getIntent();
        String text = intent.getExtras().getString("sentence");
        String result = "당신이 전하는 마음\n\"" + text + "\"\n를 점자로 변환하시겠어요?";

        message = (TextView) findViewById(R.id.result);
        conv_result = (TextView) findViewById(R.id.brailleText);
        retrial = (ImageButton) findViewById(R.id.retry);
        ttb = (ImageButton) findViewById(R.id.braille);
        message.setText(result);

        try {
            convertQuery = URLEncoder.encode(text,"UTF-8");
            REQUEST_URL += convertQuery;
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        retrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SttActivity.class);
                startActivity(intent);
            }
        });

        ttb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog( ChkActivity.this );
                progressDialog.setMessage("당신의 마음을 변환 중입니다!");
                progressDialog.show();

                getJSON();
            }
        });
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String valuefin = bun.getString("value");
            conv_result.setText("점자 변환 결과가 이곳에 표시됩니다.\n" + valuefin);
        }
    };

    public void  getJSON() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(REQUEST_URL);
                    HttpsURLConnection myConnection = (HttpsURLConnection)url.openConnection();

                    if (myConnection.getResponseCode() == 200) {// Success
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader =
                                new InputStreamReader(responseBody, "UTF-8");

                        JsonReader jsonReader = new JsonReader(responseBodyReader);
                        jsonReader.beginObject(); // Start processing the JSON object

                        while (jsonReader.hasNext()) { // Loop through all keys
                            String key = jsonReader.nextName(); // Fetch the next key
                            if (key.equals("body")) { // Check if desired key
                                value = jsonReader.nextString();

                                Bundle bun = new Bundle();
                                bun.putString("value", value);
                                Message msg = handler.obtainMessage();
                                msg.setData(bun);
                                handler.sendMessage(msg);

                                progressDialog.dismiss();
                                break; // Break out of the loop
                            } else { // Skip values of other keys
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.close();
                        myConnection.disconnect();
                    } else {
                        // Error handling code goes here
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}