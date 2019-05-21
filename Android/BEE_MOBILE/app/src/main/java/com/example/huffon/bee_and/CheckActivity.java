package com.example.huffon.bee_and;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class CheckActivity extends AppCompatActivity {
    private String REQUEST_URL = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/bee?word=";
    private String convertQuery = null;
    private String value;
    private String valuefin;

    private AlertDialog dialog;
    private TextView message;
    private TextView conv_result;
    private ImageButton ttb;
    private ImageButton retrial;
    private Button send;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        Intent intent = getIntent();
        String text = intent.getExtras().getString("sentence");
        String result = "당신이 전하는 마음\n\"" + text + "\"\n를 점자로 변환하시겠어요?";

        message = (TextView) findViewById(R.id.result);
        conv_result = (TextView) findViewById(R.id.brailleText);
        retrial = (ImageButton) findViewById(R.id.retry);
        ttb = (ImageButton) findViewById(R.id.braille);
        send = (Button) findViewById(R.id.send);
        message.setText(result);

        // 한글 텍스트를 UTF-8로 인코드해서 URL에 추가
        try {
            convertQuery = URLEncoder.encode(text,"UTF-8");
            REQUEST_URL += convertQuery;
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        // 음성인식 재시도 버튼을 누를 경우, STT 액티비티로 재이동
        retrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), STTActivity.class);
                startActivity(intent);
            }
        });

        // 점자 변환 버튼을 누를 경우, URL에 Request를 보내 점자 변환
        ttb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog( CheckActivity.this );
                progressDialog.setMessage("당신의 마음을 변환 중입니다!");
                progressDialog.show();

                getJSON();
            }
        });

        // 전송 버튼을 누를 경우, 반환 받은 점자 정보를 블루투스 액티비티로 보내어 전송
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valuefin == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(CheckActivity.this);
                    dialog = builder.setMessage("점자 변환을 먼저 수행해주세요!")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), BluetoothActivity.class);
                    intent.putExtra("braille", valuefin);
                  startActivity(intent);
                }
            }
        });
    }

    // 쓰레드는 직접 액티비티를 수정할 수 없으므로, Handler를 활용해 점자 정보 화면에 표시
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            valuefin = bun.getString("value");
            conv_result.setText("점자 변환 결과가 이곳에 표시됩니다.\n" + valuefin);
        }
    };


    // JSON 데이터 포맷으로 점자 변환 정보를 요청하는 메서드
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

                        // JSON 파일을 모두 돌며 로직 수행
                        while (jsonReader.hasNext()) {
                            String key = jsonReader.nextName();
                            // JSON 내 'body' 키를 가진 데이터 셋을 찾음
                            if (key.equals("body")) {
                                value = jsonReader.nextString();

                                // 모바일 화면에 표시해주기 위해 Handler에 점자 정보 전달
                                Bundle bun = new Bundle();
                                bun.putString("value", value);
                                Message msg = handler.obtainMessage();
                                msg.setData(bun);
                                handler.sendMessage(msg);

                                progressDialog.dismiss();
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
}