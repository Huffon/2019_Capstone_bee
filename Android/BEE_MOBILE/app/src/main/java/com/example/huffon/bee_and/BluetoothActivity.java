package com.example.huffon.bee_and;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Huffon on 5/8/2019.
 */

public class BluetoothActivity extends AppCompatActivity
{
    private String REQUEST_URL = "https://8k49oi12m2.execute-api.us-east-2.amazonaws.com/beeGet/bee?word=";
    private String convertedResult;
    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    private TextView mConnectionStatus;
    private EditText mInputEditText;
    private Button send;

    private AlertDialog dialog;
    ConnectedTask mConnectedTask = null;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";
    private String recvMessage;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Intent prevIntent = getIntent();
        String text = prevIntent.getExtras().getString("braille");
        text = text.replace("\"", "");

        Button send = (Button) findViewById(R.id.send);
        Button sendButton = (Button)findViewById(R.id.send_button);
        mConnectionStatus = (TextView)findViewById(R.id.connection_status_textview);
        mInputEditText = (EditText)findViewById(R.id.input_string_edittext);
        mInputEditText.setText(text);
        ListView mMessageListview = (ListView) findViewById(R.id.message_listview);

        // TTS 수행
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getJSON(); // 이후 convertedResult 보내기!
                Intent intent = new Intent(getApplicationContext(), TtsActivity.class);
                intent.putExtra("sentence", "테스트");
                startActivity(intent);
            }
        });

        // 아두이노로 데이터 전송
        sendButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String sendMessage = mInputEditText.getText().toString();
                if ( sendMessage.length() > 0 ) {
                    sendMessage(sendMessage);
                }
            }
        });

        mConversationArrayAdapter = new ArrayAdapter<>( this,
                android.R.layout.simple_list_item_1 );
        mMessageListview.setAdapter(mConversationArrayAdapter);

        Log.d( TAG, "블루투스 어댑터를 초기화 중입니다.");

        // 블루투스 초기화 실패 시
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showErrorDialog("이 기기는 블루투스를 지원하지 않습니다.");
            return;
        }

        // 블루투스 초기화 성공 시
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        } else {
            Log.d(TAG, "블루투스가 성공적으로 초기화되었습니다.");
            showPairedDevicesListDialog();
        }
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();
            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d( TAG, "Create Socket for "+mConnectedDeviceName);
            } catch (IOException e) {
                Log.e( TAG, "Socket Create failed " + e.getMessage());
            }

            mConnectionStatus.setText("connecting...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mBluetoothAdapter.cancelDiscovery();
            // 블루투스 소켓을 이용한 연결
            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "Socket close 실패" +
                            " socket during connection failure", e2);
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
                Log.d( TAG,  "Unable to connect device");
                showErrorDialog("Unable to connect device");
            }
        }
    }

    public void connected( BluetoothSocket socket ) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }

    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {
        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket){
            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "소켓 생성에 실패했습니다.", e );
            }
            Log.d( TAG, "Connected to "+mConnectedDeviceName);
            mConnectionStatus.setText( "Connected to "+mConnectedDeviceName);
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            byte [] readBuffer = new byte[1024];
            int readBufferPosition = 0;

            while (true) {
                if ( isCancelled() ) return false;
                try {
                    int bytesAvailable = mInputStream.available();
                    if(bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mInputStream.read(packetBytes);
                        for(int i=0;i<bytesAvailable;i++) {
                            byte b = packetBytes[i];
                            if(b == '\n') {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                        encodedBytes.length);
                                recvMessage = new String(encodedBytes, "UTF-8");
                                readBufferPosition = 0;
                                Log.d(TAG, "recv message: " + recvMessage);
                                publishProgress(recvMessage);
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    return false;
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... recvMessage) {
            mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + recvMessage[0], 0);
            System.out.println("아두이노가 보낸 메시지: " + recvMessage[0]);

            AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothActivity.this);
            dialog = builder.setMessage("상대방으로부터 메시지를 수신 받았습니다.")
                    .setNegativeButton("확인", null)
                    .create();
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);
            if ( !isSucess ) {
                closeSocket();
                Log.d(TAG, "Device connection was lost");
                isConnectionError = true;
                showErrorDialog("Device connection was lost");
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            closeSocket();
        }

        void closeSocket(){
            try {
                mBluetoothSocket.close();
                Log.d(TAG, "close socket()");
            } catch (IOException e2) {
                Log.e(TAG, "unable to close() " +
                        " socket during connection failure", e2);
            }
        }

        void write(String msg){
            msg += "\n";
            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during send", e );
            }
            mInputEditText.setText(" ");
        }
    }


    public void showPairedDevicesListDialog()
    {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){
            showQuitDialog( "No devices have been paired.\n"
                    +"You must pair it with another device.");
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];
        for (int i=0;i<pairedDevices.length;i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select device");
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
            Log.d(TAG, "send message: " + msg);
            mConversationArrayAdapter.insert("Me:  " + msg, 0);
        }
    }

    // Bluetooth 기능 활성화 여부 확인
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if (resultCode == RESULT_OK){
                //BlueTooth is now Enabled
                showPairedDevicesListDialog();
            }
            if(resultCode == RESULT_CANCELED){
                showQuitDialog( "블루투스 기능을 활성화시켜주세요.");
            }
        }
    }

    // 연결 해제
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( mConnectedTask != null ) {
            mConnectedTask.cancel(true);
        }
    }

    // 연결 에러 시 문구 생성
    public void showErrorDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if ( isConnectionError  ) {
                    isConnectionError = false;
                    finish();
                }
            }
        });
        builder.create().show();
    }

    // 대화 강제 종료 시 문구 생성
    public void showQuitDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

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
                                convertedResult = jsonReader.nextString();

                                Bundle bun = new Bundle();
                                bun.putString("value", convertedResult);

                                break; // Break out of the loop
                            } else { // Skip values of other keys
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.close();
                        myConnection.disconnect();
                    } else { // Error handling code goes here
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}