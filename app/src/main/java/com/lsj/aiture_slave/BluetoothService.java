package com.lsj.aiture_slave;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {

    private static final String TAG = "asdasd"; // BluetoothService
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter adapter;
    private Handler handler;
    // 연결대기 스레드
    private AcceptThread accpetThread;
    // 연결 스레드
    private ConnectThread connectThread;
    // 연결중 스레드
    private ConnectedThread connectedThread;
    private int state;


    public BluetoothService(BluetoothAdapter adapter, Handler handler){
        state = BluetoothState.STATE_NONE;
        this.adapter = adapter;
        this.handler = handler;
    }

    // 상태 설정
    private synchronized void setState(int state){
        this.state = state;
    }

    //상태값 리턴
    public synchronized int getState(){
        return state;
    }

    // 전송 시작
    public synchronized void start(){
        if(connectedThread != null){
            connectedThread = null;
        }

        if(accpetThread == null){
            accpetThread = new AcceptThread();
            accpetThread.start();
        }
        setState(BluetoothState.STATE_LISTEN);
    }

    // 블루투스 연결
    public synchronized void connect(BluetoothDevice device){
        Log.i("asdasd", "bluetooth connect!");
        if(state == BluetoothState.STATE_CONNECTING){
            if(connectThread != null){connectThread.cancle();connectThread = null;}
        }
        if(connectedThread != null){connectedThread.cancle();connectedThread = null;}

        connectThread = new ConnectThread(device);
        Log.i("asdasd", "connectThread start!");
        connectThread.start();
        setState(BluetoothState.STATE_CONNECTED);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
        Log.i(TAG, "connected run!!");
        if(connectThread != null){connectThread.cancle();connectThread = null;}
        if(connectedThread != null){connectedThread.cancle();connectedThread = null;}
        if(accpetThread != null){accpetThread.cancle();accpetThread = null;}

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        Message msg = handler.obtainMessage(BluetoothState.BLUETOOTH_MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothState.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(BluetoothState.STATE_CONNECTED);
    }

    public synchronized void stop(){
        if(connectThread != null){connectThread.cancle();connectThread = null;}
        if(connectedThread != null){connectedThread.cancle();connectedThread = null;}
        if(accpetThread != null){accpetThread.cancle();accpetThread = null;}

        setState(BluetoothState.STATE_NONE);
    }

    public void write(byte[] out){
        Log.i(TAG, "Thread Write");
        ConnectedThread r;
        synchronized (this){
            if(state != BluetoothState.STATE_CONNECTED) return;
            r= connectedThread;
        }
        r.write(out);
    }

    private void connectionLost(){
        setState(BluetoothState.STATE_LISTEN);
    }

    private void connectionFailed(){
        setState(BluetoothState.STATE_LISTEN);
    }


    private class AcceptThread extends Thread{

        private BluetoothServerSocket serverSocket;

        public AcceptThread(){
        }

        @Override
        public void run() {
            BluetoothSocket socket;
            try{
                Log.i(TAG, "socket 생성 !");
                serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG,MY_UUID);
                Log.i(TAG, "서버소켓 생성 !");
                socket = serverSocket.accept();
                Log.i(TAG, "socket 생성완료 !");
                if(socket != null){
                    Log.i(TAG, "socket not null~~");
                    connected(socket, socket.getRemoteDevice());
                }
            }catch (IOException e){
                Log.i(TAG, "listen 실패");
            }
        }

        public void cancle(){
            try{
                serverSocket.close();
            }catch (IOException e){Log.i(TAG, "AcceptThread close 에러");}
        }
    }

    private class ConnectThread extends Thread{

        private BluetoothDevice device;
        private BluetoothSocket socket;

        private ConnectThread(BluetoothDevice device){
            Log.i("asdasd", "ConnectThread Construct");
            this.device = device;
            BluetoothSocket tmp = null;
            try{
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            }catch (IOException e){Log.i(TAG, "ConnectThread create 에러");}
            socket = tmp;
        }

        @Override
        public void run() {
            adapter.cancelDiscovery();
            Log.i("asdasd", "ConnectThread run!!");
            try{
                if(socket != null){
                    Log.i("asdasd", "socket not null!!");
                }else{
                    Log.i("asdasd", "socket null!!");
                }
                socket.connect();
                Log.i("asdasd", "socket");
            }catch (IOException e){
                Log.i("asdasd", "socket connect exception");
                Log.i("asdasd", e.getMessage());
                connectionFailed();
                try{
                    socket.close();
                }catch (IOException e1){
                    Log.i(TAG, "ConnectThread connection failure 에러");
                }
                BluetoothService.this.start();
                return;
            }
            Log.i("asdasd", "socket connection end");
            synchronized (BluetoothService.this){
                connectThread = null;
            }
            Log.i("asdasd", "connected function start");
            connected(socket, device);
        }

        public void cancle(){
            try{
                socket.close();
            }catch (IOException e){Log.i(TAG, "ConnectThread close 에러입니다");}
        }
    }

    private class ConnectedThread extends Thread{

        private BluetoothSocket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        private ConnectedThread(BluetoothSocket socket){
            Log.i("asdasd", "ConnectedThread Construct!!");
            this.socket = socket;
            try{
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            }catch (IOException e){Log.i(TAG, "ConnectedThread socket not created 에러");}
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes= 0;
            Log.i("asdasd", "ConnectedThread run!!");
            while (true){
                try{
                    bytes = inputStream.read(buffer);
                }catch (IOException e){
                    Log.i(TAG, "disconnected 에러");
                    connectionLost();
                    break;
                }
            }
            handler.obtainMessage(BluetoothState.BLUETOOTH_MESSAGE_READ ,bytes, -1, buffer).sendToTarget();
        }

        public void cancle(){
            try{
                socket.close();
            }catch (IOException e){Log.i(TAG, "ConnectedThread cancle 에러");}
        }

        public void write(byte[] buffer){
            Log.i(TAG, "ConnectedThread Write");
            try{
                outputStream.write(buffer);
                handler.obtainMessage(BluetoothState.BLUETOOTH_MESSAGE_WRITE_SUCCESS, -1, -1, buffer).sendToTarget();
            }catch (IOException e){Log.i(TAG,"write Error 에러");}
        }
    }

}