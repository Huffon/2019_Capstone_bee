package com.example.huffon.bee_and;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class DictionaryFragment extends Fragment {
    TextView result;
    TextView braille;
    EditText searchWord;
    String convertedResult;
    String text;
    String stBraille;
    String TAG = "DICT";

    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    public ConnectedTask mConnectedTask = null;
    static boolean isConnectionError = false;
    static BluetoothAdapter mBluetoothAdapter;
    private String recvMessage;

    private String REQUEST_URL = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/dict?word=";
    private String DEFAULT_URL = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/dict?word=";
    private String BTT_URL = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/btt?braille=";
    private String BTT_DEFAULT = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/btt?braille=";
    private String BRAILLE_URL = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/bee?word=";
    private String BRAILLE_DEFAULT = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/bee?word=";
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dictionary, container, false);
        Button searchBtn = (Button) v.findViewById(R.id.searchBtn);
        searchWord = (EditText) v.findViewById(R.id.wordFind);
        result = (TextView) v.findViewById(R.id.resultText);
        braille = (TextView) v.findViewById(R.id.resultBraille);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            showPairedDevicesListDialog();
        }

        searchBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String word = searchWord.getText().toString();
                if (word.isEmpty() || word.equals("")) {
                    Toast.makeText(getActivity(), "검색어를 입력해주세요.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    String search = searchWord.getText().toString();
                    try {
                        search = URLEncoder.encode(search,"UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    REQUEST_URL += search;
                    braille.setText("");
                    progressDialog = new ProgressDialog( getActivity());
                    progressDialog.setMessage("사전 검색 중입니다!");
                    progressDialog.show();
                    getDict();
                }
            }
        });

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String braille = result.getText().toString();
                try {
                    braille = URLEncoder.encode(braille,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                BRAILLE_URL += braille;
                progressDialog = new ProgressDialog( getActivity());
                progressDialog.setMessage("점자 변환 중입니다!");
                progressDialog.show();
//                braille.set
//                getBraille2();
//                braille.s
            }
        });

        braille.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendMessage = braille.getText().toString();
                if ( sendMessage.length() > 0 ) {
                    sendMessage(sendMessage);
                }
            }
        });

        return v;
    }

    // 쓰레드는 직접 액티비티를 수정할 수 없으므로, Handler를 활용해 텍스트 정보 화면에 표시
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String valuefin = bun.getString("value");
            try {
                if (valuefin.equals("false")) {
                    result.setText("사전에 등재되지 않은 단어입니다.");
                }
                else {
                    convertedResult = convertString(valuefin);
                    convertedResult = convertedResult.replace("\"", "");
                    convertedResult = convertedResult.replace("』", "");
                    convertedResult = convertedResult.replace("」", "");

                    try {
                        int index = convertedResult.indexOf(".");
                        convertedResult = convertedResult.substring(0, index);
                        result.setText(convertedResult);
                    } catch (NullPointerException e) {
                        result.setText(convertedResult);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    public void  getDict() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(REQUEST_URL);
                    HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();

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

                                REQUEST_URL = DEFAULT_URL;
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
                }
            }
        });
        thread.start();
    }

    // 쓰레드는 직접 액티비티를 수정할 수 없으므로, Handler를 활용해 점자 정보 화면에 표시
    Handler handler2 = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            stBraille = bun.getString("value");
            braille.setText(stBraille);
        }
    };

    public void  getBraille2() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(BRAILLE_URL);
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
                                String value = jsonReader.nextString();

                                // 모바일 화면에 표시해주기 위해 Handler에 점자 정보 전달
                                Bundle bun = new Bundle();
                                bun.putString("value", value);
                                Message msg = handler2.obtainMessage();
                                msg.setData(bun);
                                handler2.sendMessage(msg);
                                BRAILLE_URL = BRAILLE_DEFAULT;

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

    public void  getBraille() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(BRAILLE_URL);
                    HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();

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
                                Log.d( TAG, "점자 변환 결과: " + text);
                                // 모바일 화면에 표시해주기 위해 Handler에 텍스트 정보 전달
                                Bundle bun = new Bundle();
                                bun.putString("value", text);
                                Message msg = handler2.obtainMessage();
                                msg.setData(bun);
                                handler2.sendMessage(msg);

                                BRAILLE_URL = BRAILLE_DEFAULT;
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
                }
            }
        });
        thread.start();
    }


    // 사용자의 검색어-점자 변환을 위한 메서드
    public void  getJSON3() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(BTT_URL);
                    HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();

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
                                Message msg = handler3.obtainMessage();
                                msg.setData(bun);
                                handler3.sendMessage(msg);

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
                }

                BTT_URL = BTT_DEFAULT;
            }
        });
        thread.start();
    }

    // 쓰레드는 직접 액티비티를 수정할 수 없으므로, Handler를 활용해 텍스트 정보 화면에 표시
    Handler handler3 = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String valuefin = bun.getString("value");
            try {
                valuefin = convertString(valuefin);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            valuefin = valuefin.replace("\"", "");
            searchWord.setText(valuefin);
        }
    };

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
            searchWord.setText(recvMessage[0]);
            BTT_URL += recvMessage[0];
            getJSON3();
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
            msg += "\n";
            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
            }
        }
    }

    // 연결 가능한 기기 display
    public void showPairedDevicesListDialog() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){return;}

        String[] items;
        items = new String[pairedDevices.length];
        for (int i=0;i<pairedDevices.length;i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    // Bluetooth 기능 활성화 여부 확인
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if (resultCode == RESULT_OK){
                //BlueTooth is now Enabled
                showPairedDevicesListDialog();
            }
            if(resultCode == RESULT_CANCELED){
            }
        }
    }

    // 연결 해제
    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( mConnectedTask != null ) {
            mConnectedTask.cancel(true);
        }
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
}
