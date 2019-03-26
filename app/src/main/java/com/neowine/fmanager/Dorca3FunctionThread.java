package com.neowine.fmanager;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class Dorca3FunctionThread extends Thread {
    final public int DR3_FUNC_DORCA_START = 0x10001;
    final public int DR3_FUNC_DORCA_STOP = 0x10002;
    final public int DR3_FUNC_GETSPICLOCK = 0x10011;
    final public int DR3_FUNC_SETSPICLOCK = 0x10012;
    final public int DR3_FUNC_SLEEP_CHECK_LIMIT = 0x10013;
    final public int DR3_FUNC_RX_TX_DEBUG = 0x10014;
    final public int DR3_FUNC_POWERRESET = 0x10015;

    final public int DR3_FUNC_TEST_SHA_1FRAME_TEST = 0x100A1;
    final public int DR3_FUNC_TEST_SHA_2FRAME_TEST = 0x100A2;
    final public int DR3_FUNC_TEST_ECDH_GEN_SESSION_KEY = 0x100A3;

    Handler UiHandler;
    Handler Dr3FunctionHandler;
    private dorca3_function Dr3Functions_in_thread = null;

    Dorca3FunctionThread(Handler handler, dorca3_function dr3_func) {
        UiHandler = handler;
        Dr3Functions_in_thread = dr3_func;
    }

    private void send_string_to_main(String s) {
        Message msg = UiHandler.obtainMessage();
        msg.what = dorca3_activity_message_ids.MESSAGE_SEND_TEST;
        msg.obj = s;
        UiHandler.sendMessage(msg);
    }

    public void Dr3Thread_DorcaStart() {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.what = DR3_FUNC_DORCA_START;
        Dr3FunctionHandler.sendMessage(msg);
    }

    public void Dr3Thread_DorcaStop() {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.what = DR3_FUNC_DORCA_STOP;
        Dr3FunctionHandler.sendMessage(msg);
    }

    public void Dr3Thread_GetSpiClock() {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.what = DR3_FUNC_GETSPICLOCK;
        Dr3FunctionHandler.sendMessage(msg);
    }

    public void Dr3Thread_SetSpiClock(int clock) {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.what = DR3_FUNC_SETSPICLOCK;
        msg.arg1 = clock;
        Dr3FunctionHandler.sendMessage(msg);
    }

    public void Dr3Thread_SleepCheckLimit() {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.what = DR3_FUNC_SLEEP_CHECK_LIMIT;
        Dr3FunctionHandler.sendMessage(msg);
    }

    public void Dr3Thread_RxTxDebug(int onoff) {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.what = DR3_FUNC_RX_TX_DEBUG;
        msg.arg1 = onoff;
        Dr3FunctionHandler.sendMessage(msg);
    }

    public void Dr3Thread_PowerReset() {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.what = DR3_FUNC_POWERRESET;
        Dr3FunctionHandler.sendMessage(msg);
    }

    public void Dr3Thread_SHA_1FRAME_TEST(String strTest) {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.obj = strTest;
        msg.what = DR3_FUNC_TEST_SHA_1FRAME_TEST;
        Dr3FunctionHandler.sendMessage(msg);
    }

    public void Dr3Thread_SHA_2FRAME_TEST(String strTest) {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.obj = strTest;
        msg.what = DR3_FUNC_TEST_SHA_2FRAME_TEST;
        Dr3FunctionHandler.sendMessage(msg);
    }

    public void Dr3Thread_TEST_ECDH_GEN_SESSION_KEY(String strTest) {
        Message msg = Dr3FunctionHandler.obtainMessage();
        msg.obj = strTest;
        msg.what = DR3_FUNC_TEST_ECDH_GEN_SESSION_KEY;
        Dr3FunctionHandler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    public void run() {
        send_string_to_main("Start Dorca3FunctionThread!\n");
        Looper.prepare();
        Dr3FunctionHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DR3_FUNC_DORCA_START: {
                        Dr3Functions_in_thread.dorca_start_no_wakeup();
                        //Dr3Functions_in_thread.dorca_start();
                        break;
                    }

                    case DR3_FUNC_DORCA_STOP: {
                        Dr3Functions_in_thread.dorca_stop();
                        break;
                    }

                    case DR3_FUNC_GETSPICLOCK: {
                        int spiclock = Dr3Functions_in_thread.DorcaGetSpiClock();
                        if (spiclock < 0) {
                            send_string_to_main("Failed to get spiclock\n");
                            return;
                        }
                        send_string_to_main("Get Spi Clock [" + spiclock + "]\n");
                        Message uimsg = UiHandler.obtainMessage();
                        uimsg.what = dorca3_activity_message_ids.MESSAGE_SEND_SHOW_SPI_CLOCK;
                        uimsg.obj = null;
                        uimsg.arg1 = spiclock;
                        UiHandler.sendMessage(uimsg);
                        break;
                    }

                    case DR3_FUNC_SETSPICLOCK: {
                        int clock = msg.arg1;
                        int ret = Dr3Functions_in_thread.DorcaSetSpiClock(clock);
                        if (ret < 0) {
                            send_string_to_main("Failed to set spiclock\n");
                            return;
                        }
                        send_string_to_main("Set Spi Clock [" + clock + "]\n");
                        break;
                    }

                    case DR3_FUNC_SLEEP_CHECK_LIMIT: {
                        Dr3Functions_in_thread.DorcaSleepCheckLimit();
                        break;
                    }

                    case DR3_FUNC_RX_TX_DEBUG: {
                        int onoff = msg.arg1;
                        Dr3Functions_in_thread.DorcaSPI_RX_TX_Debug(onoff);
                        break;
                    }

                    case DR3_FUNC_POWERRESET: {
                        Dr3Functions_in_thread.DorcaPowerReset();
                        Dr3Functions_in_thread.dorca_start_no_wakeup();
                        Dr3Functions_in_thread.DorcaSleepCheckLimit();
                        break;
                    }

                    case DR3_FUNC_TEST_SHA_1FRAME_TEST: {
                        String TestName = (String)msg.obj;
                        Send_Test_Result_to_UI(TestName, Dr3Functions_in_thread.SHA_1Frame_TEST());
                        break;
                    }

                    case DR3_FUNC_TEST_SHA_2FRAME_TEST: {
                        String TestName = (String)msg.obj;
                        Send_Test_Result_to_UI(TestName, Dr3Functions_in_thread.SHA_2Frame_TEST());
                        break;
                    }

                    case DR3_FUNC_TEST_ECDH_GEN_SESSION_KEY: {
                        String TestName = (String)msg.obj;
                        Send_Test_Result_to_UI(TestName, Dr3Functions_in_thread.ecdh_gen_session_key());
                        break;
                    }

                    default:
                        break;
                }
            }
        };
        Looper.loop();
    }

    private void Send_Test_Result_to_UI(String strTest, int Result) {
        Message uimsg = UiHandler.obtainMessage();
        uimsg.what = dorca3_activity_message_ids.MESSAGE_SEND_TEST_MENU_RESULT;
        uimsg.obj = strTest;
        uimsg.arg1 = Result;
        UiHandler.sendMessage(uimsg);
    }
}