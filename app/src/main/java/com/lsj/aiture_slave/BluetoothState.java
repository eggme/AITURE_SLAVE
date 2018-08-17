package com.lsj.aiture_slave;

class BluetoothState {

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public static final int BLUETOOTH_MESSAGE_CHANGE = 1;
    public static final int BLUETOOTH_MESSAGE_READ = 2;
    public static final int BLUETOOTH_MESSAGE_WRITE = 3;
    public static final int BLUETOOTH_MESSAGE_DEVICE_NAME = 4;
    public static final int BLUETOOTH_MESSAGE_TOAST = 5;
    public static final int BLUETOOTH_MESSAGE_WRITE_SUCCESS = 9;

    public static final String DEVICE_NAME = "device_name";
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final String EXTRA_DATA = "device_data";
    public static final int EXTRA_DEVICE_NUMBER = 8;

    public static final int REQUEST_CONNECT_DEVICE = 6;
    public static final int REQUEST_ENABLE_BT = 7;

}


