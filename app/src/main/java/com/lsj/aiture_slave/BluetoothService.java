package com.lsj.aiture_slave;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by kyyet on 2018-08-06.
 */

public class BluetoothService {

    private static final String TAG = "BluetoothSerivce";
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private BluetoothAdapter adapter;
    private Handler handler;
    // 연결대기 스레드
    private AcceptThread accpetThread;
    // 연결 스레드
    private ConnectThread connectThread;
    // 연결중 스레드
    private ConnectedThread connectedThread;
    private int state;

    // 상태
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;


    public BluetoothService(BluetoothAdapter adapter, Handler handler){
        state = STATE_NONE;
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
        setState(STATE_LISTEN);
    }

    // 블루투스 연결
    public synchronized void connect(BluetoothDevice device){
        if(state == STATE_CONNECTING){
            if(connectThread != null){connectThread.cancle();connectThread = null;}
        }
        if(connectedThread != null){connectedThread.cancle();connectedThread = null;}

        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTED);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){

        if(connectThread != null){connectThread.cancle();connectThread = null;}
        if(connectedThread != null){connectedThread.cancle();connectedThread = null;}
        if(accpetThread != null){accpetThread.cancle();accpetThread = null;}

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        setState(STATE_CONNECTED);
    }

    public synchronized void stop(){
        if(connectThread != null){connectThread.cancle();connectThread = null;}
        if(connectedThread != null){connectedThread.cancle();connectedThread = null;}
        if(accpetThread != null){accpetThread.cancle();accpetThread = null;}

        setState(STATE_NONE);
    }

    public void write(byte[] out){
        ConnectedThread r;
        synchronized (this){
            if(state != STATE_CONNECTED) return;
            r= connectedThread;
        }
        r.write(out);
    }

    private void connectionLost(){
        setState(STATE_LISTEN);
    }

    private void connectionFailed(){
        setState(STATE_LISTEN);
    }


    private class AcceptThread extends Thread{

        private BluetoothServerSocket serverSocket;

        public AcceptThread(){
        }

        @Override
        public void run() {
            BluetoothSocket socket;
            try{
                serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG,MY_UUID);
                socket = serverSocket.accept();
                if(socket != null){
                    connected(socket, socket.getRemoteDevice());
                }
            }catch (IOException e){
                Log.i(TAG, "listen 실패");
            }
        }

        public void cancle(){
            try{
                serverSocket.close();
            }catch (IOException e){
                Log.i(TAG, "AcceptThread close 에러");}
        }
    }

    private class ConnectThread extends Thread{

        private BluetoothDevice device;
        private BluetoothSocket socket;

        private ConnectThread(BluetoothDevice device){
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

            try{
                socket.connect();
            }catch (IOException e){
                connectionFailed();
                try{
                    socket.close();
                }catch (IOException e1){Log.i(TAG, "ConnectThread connection failure 에러");}

                BluetoothService.this.start();
                return;
            }

            synchronized (BluetoothService.this){
                connectThread = null;
            }

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
            this.socket = socket;
            try{
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            }catch (IOException e){Log.i(TAG, "ConnectedThread socket not created 에러");}
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try{
                    bytes = inputStream.read(buffer);

                    handler.obtainMessage(BluetoothState.BLUETOOTH_MESSAGE_READ ,bytes, -1, buffer).sendToTarget();
                }catch (IOException e){
                    Log.i(TAG, "disconnected 에러");
                    connectionLost();
                    break;
                }
            }
        }

        public void cancle(){
            try{
                socket.close();
            }catch (IOException e){Log.i(TAG, "ConnectedThread cancle 에러");}
        }

        public void write(byte[] buffer){
            try{
                outputStream.write(buffer);
                handler.obtainMessage(BluetoothState.BLUETOOTH_MESSAGE_WRITE, -1, -1, buffer);
            }catch (IOException e){Log.i(TAG,"write Error 에러");}
        }
    }

}
