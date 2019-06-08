package com.example.huffon.bee_and;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.TextKeyListener;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Huffon on 5/30/2019.
 */

public class ChatActivity extends AppCompatActivity {
    public static final int LOAD_SUCCESS = 101;
    String TAG = "CHAT";
    TextToSpeech tts;
    private String BTT_URL = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/btt?braille=";
    private String BTT_DEFAULT = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/btt?braille=";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ProgressDialog progressDialog;
    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    public ConnectedTask mConnectedTask = null;
    static boolean isConnectionError = false;
    static BluetoothAdapter mBluetoothAdapter;
    private String recvMessage;
    String text;

    boolean soundFlag = false;
    int chatLog;
    EditText etText;
    Button btnSend;
    Button btnSound;
    String email;
    List<Chat> mChat;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 음성 출력을 위한 안드로이드 내 TTS 객체 생성
        tts = new TextToSpeech(getApplication(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate((float)0.8);
                }
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter.isEnabled()) {
//            showPairedDevicesListDialog();
//        }

        database = FirebaseDatabase.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }

        Intent intent = getIntent();
        String chatId = intent.getStringExtra("friendID");

        etText = (EditText)findViewById(R.id.etText);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSound = (Button) findViewById(R.id.btnSound);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(soundFlag == false) {
                    btnSound.setText("ON");
                    soundFlag = true;
                } else {
                    btnSound.setText("OFF");
                    soundFlag = false;
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stText = etText.getText().toString();

                // 사용자가 메시지를 입력하지 않았을 때,
                if (stText.equals("") || stText.isEmpty()){
                    Toast.makeText(ChatActivity.this, "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                // 사용자가 메시지를 입력했을 때,
                else {
                    Calendar c = Calendar.getInstance();
                    String formattedDate = df.format(c.getTime());
                    DatabaseReference myRef = database.getReference("users").child(chatId).
                            child("chat").child(formattedDate);

                    Hashtable<String, String> chat = new Hashtable<String, String>();
                    chat.put("email", email);
                    chat.put("text", stText);
                    myRef.setValue(chat);
                    etText.setText("");
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mChat = new ArrayList<>();
        mAdapter = new ChatAdapter(mChat, email);
        recyclerView.setAdapter(mAdapter);
        DatabaseReference readRef = database.getReference("users").child(chatId).child("chat");
        readRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                mChat.add(chat);
                recyclerView.scrollToPosition(mChat.size() - 1);
                mAdapter.notifyItemInserted(mChat.size() - 1);

                String sender = chat.getEmail();
                String msg = chat.getText();

                System.out.println("Sent: "+ msg);

                if(!sender.equals(email) && msg.charAt(0) == 'n') {
                    msg = msg.substring(1, msg.length());
                    Log.d( TAG, "상대방으로부터 메시지를 수신했습니다.");
                    if (!soundFlag) {
                        String braille = Ttb.ttb_handler(msg);
                        sendMessage(braille);
                    } else {
                        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
                        tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
             }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;

            //안드로이드 단말기 고유값 설정
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mBluetoothAdapter.cancelDiscovery();
            // 소켓을 이용해 디바이스와 블루투스 연결
            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                }
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            if ( isSucess ) {
                connected(mBluetoothSocket);
            } else{
                isConnectionError = true;
            }
        }
    }

    public void connected( BluetoothSocket socket ) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }

    // 디바이스 연결 성공 시, 화면에 성공 상태를 표시해주기 위한 메서드
    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {
        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket) {
            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
            }
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            byte[] readBuffer = new byte[1024];
            int readBufferPosition = 0;

            while (true) {
                if (isCancelled()) return false;
                try {
                    int bytesAvailable = mInputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mInputStream.read(packetBytes);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == '\n') {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                        encodedBytes.length);
                                recvMessage = new String(encodedBytes, "UTF-8");
                                readBufferPosition = 0;
                                publishProgress(recvMessage);
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {
                    return false;
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... recvMessage) {
            etText.setText(recvMessage[0]);
            String transWord = recvMessage[0];
            BTT_URL += transWord;

            getJSON();
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);
            if (!isSucess) {
                closeSocket();
                isConnectionError = true;
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            closeSocket();
        }

        void closeSocket() {
            try {
                mBluetoothSocket.close();
            } catch (IOException e2) {
            }
        }

        void write(String msg) {
            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
            }
        }
    }

    // 디바이스에서 입력한 점자 정보를 String으로 바꾸기 위해 API 호출
    public void  getJSON() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                String result;
                try {
                    Log.d(TAG, BTT_URL);
                    URL url = new URL(BTT_URL);
                    HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                    Log.d( TAG, "검색 요청 url: " + BTT_URL);

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
                                Log.d( TAG, "검색 결과: " + text);
                                // 모바일 화면에 표시해주기 위해 Handler에 텍스트 정보 전달
                                Bundle bun = new Bundle();
                                bun.putString("value", text);
                                Message msg = handler.obtainMessage();
                                msg.setData(bun);
                                handler.sendMessage(msg);

                                BTT_URL = BTT_DEFAULT;
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
                    result = e.toString();
                }

                BTT_URL = BTT_DEFAULT;
            }
        });
        thread.start();
    }

    // 쓰레드는 직접 액티비티를 수정할 수 없으므로, Handler를 활용해 텍스트 정보 화면에 표시
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String valuefin = bun.getString("value");
            try {
                valuefin = convertString(valuefin);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            valuefin = valuefin.replace("\"", "");
            etText.setText(valuefin);
        }
    };

    // Convert UTF-16 to String
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

    // 블루투스 연결 가능한 기기 display
    public void showPairedDevicesListDialog() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){return;}

        String[] items;
        items = new String[pairedDevices.length];
        for (int i=0;i<pairedDevices.length;i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("연결할 디바이스를 선택해주세요.");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }

    // 점자 정보 전송
    void sendMessage(String msg){
        if ( mConnectedTask != null ) {
            mConnectedTask.write(msg);
        }
    }

    // 블루투스 기능 활성화 여부 확인
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if (resultCode == RESULT_OK){
                //BlueTooth is now Enabled
                showPairedDevicesListDialog();
            }
            if(resultCode == RESULT_CANCELED){
            }
        }
    }

    // 블루투스 연결 해제
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if ( mConnectedTask != null ) {
            mConnectedTask.cancel(true);
        }
        super.onDestroy();
    }
}