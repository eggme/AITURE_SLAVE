package com.lsj.aiture_slave;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;


public class MainActivity extends AppCompatActivity {

    // Google Service API AIzaSyCbuC5dqlCLPnyMsKtABimfTMABD8jznak
    private final String KEY = "AIzaSyCbuC5dqlCLPnyMsKtABimfTMABD8jznak";
    private YouTubePlayerView view;
    private YouTubePlayer.OnInitializedListener listener;
    private BluetoothAdapter adapter;
    private BluetoothService service;
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case BluetoothState.BLUETOOTH_MESSAGE_CHANGE :
                    switch (msg.arg1){
                        case BluetoothState.STATE_CONNECTED :
                            Toast.makeText(getApplicationContext(), "연결 성공", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothState.STATE_CONNECTING :
                            Toast.makeText(getApplicationContext(), "연결 중", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothState.STATE_NONE :
                            Log.i("asdasd", "None");
                            break;
                    }
                case BluetoothState.BLUETOOTH_MESSAGE_READ :
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMsg = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(), readMsg, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothState.BLUETOOTH_MESSAGE_DEVICE_NAME :
                    String deviceName = msg.getData().getString(BluetoothState.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), deviceName , Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothState.EXTRA_DEVICE_NUMBER :
                    Message message = (Message)msg.obj;
                    Bundle bundle = message.getData();
                    String address = bundle.getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
                    Toast.makeText(getApplicationContext() , "페어링 된 디바이스 : "+ address, Toast.LENGTH_SHORT).show();
                    BluetoothDevice device = adapter.getRemoteDevice(address);
                    service.connect(device);
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(!adapter.isEnabled()){
            Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(in, BluetoothState.REQUEST_ENABLE_BT);
        }else{
            setUpBT();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = BluetoothAdapter.getDefaultAdapter();

        view = (YouTubePlayerView)findViewById(R.id.view);
        view.initialize(KEY,listener);

        listener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo("vewH-f3fAes&index=7&list=PLRx0vPvlEmdB6sCgj_jubp8KPb1ni0VOC");
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case BluetoothState.REQUEST_ENABLE_BT :
                if(resultCode == Activity.RESULT_OK){
                    setUpBT();
                }
                break;
        }
    }

    private void setUpBT() {
        service = new BluetoothService(adapter, handler);
        if(service != null){
            Log.i("asdasd","service not null");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(service != null){
            if(service.getState() == BluetoothState.STATE_NONE){
                service.start();
            }
        }
    }
}
