package com.neowine.fmanager;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

import static com.neowine.fmanager.JniBrige.AES_CIPHER;

public class dorca3_function {

    final public static int DORCA3_USB_PID = 4666;
    final public static int DORCA3_USB_VID = 1046;

    final private byte ADDR_NOR_W[] = {0x31};
    final private byte ADDR_NOR_R[] = {0x21};
    final private byte RG_AES_CTRL[] = {0x06, 0x35};
    final private byte RG_SHA_CTRL[] = {0x06, 0x38};
    final private byte RG_ST0_OPMODE[] = {0x06, 0x04};
    final private byte RG_ST1_STDSPI_OPMODE [] = {0x06, 0x06};
    final private byte RG_ACCESS[] = {0x06, 0x1};
    final private byte RG_RNDGEN_USER[] = {0x07, 0x00};
    final private byte RG_RSCREATE_CTRL[] = {0x06, 0x23};

    final private byte RG_SLEEP_TIMER_MSB[] = {0x06, 0x50};
    final private byte RG_SLEEP_TIMER_LSB[] = {0x06, 0x51};

    //final private byte  RG_ST0_OPMODE[2]        = { 0x06, 0x04 };
    final private byte  RG_EET_CTRL[]          = { 0x06, (byte)0xB0 };
    final private byte  RG_EET_OPMODE[]        = { 0x06, (byte)0xB1 };
    final private byte  RG_EET_BYOB_LEN[]      = { 0x06, (byte)0xB2 };
    final private byte  RG_EET_BYOB_ADDR_LSB[] = { 0x06, (byte)0xB3 };
    final private byte  RG_ST1_RND_OPMODE[] = {0x06,0x08};
    //final private byte  RG_AES_CTRL[] = {0x06,0x35};
    final private byte  RG_OKA_CTRL[] = {0x06,0x3C};
    final private byte  RG_ST1_MIDR_OPMODE[] = {0x06,0x0B};
    //final private byte  RG_RNDGEN_USER[] = {0x07,0x00};
    final private byte  RG_SUPER_WIRE_PW0[] = {0x06,0x60};
    final private byte  RG_EE_KEY_AES_CTRL [] = {0x06,0x20};
    final private byte  RG_PERM_GET_CTRL [] = {0x06,0x26};
    final private byte  RG_ST2_SYMCIP_OPMODE  [] = {0x06,0x19};
    final private byte  RG_PERM_GET_CTRL1   [] = {0x06,0x27};
    final private byte  RG_PERM_RELEASE   [] = {0x06,0x28};
    final private byte  RG_ST1_OKA_OPMODE[] = {0x06,0x0A};
    final private byte  RG_ST1_SYMCIP_OPMODE[] = {0x06,0x09};
    final private byte  RG_ST3_SYMCIP_RSCREATE_OPMODE[] = {0x06,0x1D};
    final private byte  RG_ST3_SYMCIP_KEYLOAD_OPMODE[] = {0x06,0x1F};
    final private byte  RG_FFFF[] = {0x0F,(byte)0xFF};
    final private byte  RG_EE_USER_ZONE_SEL[] = {0x06,0x1A};
    final private byte  RG_PERM_GET_EE_RD_PRE_SP[] = {0x06,0x29};
    final private byte  RG_EE_CFG_RD_RG_EEBUF_ST[] = {0x06,0x1C};
    final private byte  RG_MCUAuthResult [] = {0x07,0x20};
    final private byte  RG_ST2_SYMCIP_SHAAuth_CMP_DP [] = {0x07,0x21};
    final private byte  RG_EETEST_BYOB_ADDR_LSB[ ]= {0x06,(byte)0xB3};
    final private byte RG_KL_CTRL[] ={0x06,0x22};
//final private byte  RG_SHA_CTRL[] = {0x06,0x38};
//final private byte  RG_ST1_STDSPI_OPMODE [] = {0x06,0x06};



    final private byte RG_EEBUF100[] = {0x01, 0x00};
    final private byte RG_EEBUF300[] = {0x03, 0x00};
    final private byte RG_EEBUF310[] = {0x03, 0x10};
    final private byte RG_EEBUF320[] = {0x03, 0x20};
    final private byte RG_EEBUF330[] = {0x03, 0x30};
    final private byte RG_EEBUF400[] = {0x04, 0x00};
    final private byte RG_EEBUF410[] = {0x04, 0x10};
    final private byte RG_EEBUF420[] = {0x04, 0x20};
    final private byte RG_EEBUF430[] = {0x04, 0x30};
    final private byte RG_EEBUF500[] = {0x05, 0x00};
    final private byte RG_EEBUF510[] = {0x05, 0x10};

    final private int SPI1_WRITE_DATA = 0x61;
    final private int SPI1_READ_DATA = 0x71;
    final private int Set_ECDH_PrivateKey =	0x11;
    final private int Set_ECDH_PrivateKey_PUF = 0x12;
    final private int Create_ECHD_PublicKey = 0x13;
    final private int Set_ECDH_PublicKey_X = 0x14;
    final private int Set_ECDH_PublicKey_Y = 0x15;
    final private int Get_ECDH_PublicKey_X = 0x16;
    final private int Get_ECDH_PublicKey_Y = 0x17;
    final private int Create_ECHD_KEY = 0x18;
    final private int Get_ECDH_KEY_X = 0x19;
    final private int SET_EEPROM_BY_KEY = 0x1a;
    final private int Get_ECDH_KEY_Y = 0x20;
    final private int Get_ECDH_Result = 0x23;
    final private int Get_RSA_Modulus_n = 0x92;
    final private int Create_RSA_Key = 0x93;
    final private int Encrypt_RSA = 0x94;
    final private int Decrypt_RSA = 0x95;
    final private int SIZE_ECDH_256 = 0xA0;
    final private int SIZE_ECDH_521 = 0xA1;
    final private int SIZE_ECDSA_256 = 0xA2;
    final private int SIZE_ECDSA_521 = 0xA3;
    final private int SIZE_RSA_2048 = 0xA4;
    final private int SAVE_KEY = 0xA6;
    final private int MAKE_RAND = 0xA7;
    final private int Get_RAND = 0xA8;

    private dorca3_usb Dr3USB = null;
    private Handler main_handler;

    final private int   MODE_ECB = 0;
    final private int   MODE_CBC = 1;
    final private int   MODE_OFB = 2;
    final private int   MODE_CTR = 3;
    final private int   MODE_CFB = 4;
    final private int   RG_256 = 0;
    final private int   RG_128 = 1;
    final private int   RG_ARIA = 0;
    final private int   RG_AES = 1;
    final private int   RG_ENC = 0;
    final private int   RG_DEC = 1;
    final private int   MODE256 = 1;
    final private int   MODE128 = 0;
    byte gAES_KEY_SEED[]= {(byte)0xAE,(byte)0xB3,(byte)0x71,(byte)0x7B,(byte)0xDE,(byte)0x83,(byte)0x96,(byte)0x8A,(byte)0x33,(byte)0x7D,(byte)0xCD,(byte)0xA0,(byte)0x68,(byte)0x3E,(byte)0x5A,(byte)0x45,(byte)0x75,(byte)0x4D,(byte)0x7A,(byte)0x47,(byte)0xCE,(byte)0x86,(byte)0x27,(byte)0x17,(byte)0xE9,(byte)0x8B,(byte)0xF8,(byte)0xAC,(byte)0xA8,(byte)0x7A,(byte)0xE8,(byte)0x90};

    boolean IsUsbOpen = false;
    boolean IsUsbUsing = false;

    static private dorca3_function dorca3 = null;
    private dorca3_function(){};

    public void set_handler(Handler hd) {
        main_handler = hd;
    }
    public static dorca3_function getInstance()
    {
        if(dorca3 == null) {
            dorca3 = new dorca3_function();
        }
        return dorca3;

    }


    public dorca3_function(Handler hd) {
        main_handler = hd;
    }
    void memcpy(byte[] dest, byte[] src,int size)
    {
        System.arraycopy(src, 0, dest, 0,  size);
    }
    public void send_string_to_main(String s) {
        Message msg = main_handler.obtainMessage();
        msg.what = dorca3_activity_message_ids.MESSAGE_SEND_TEST;
        msg.obj = s;
        main_handler.sendMessage(msg);
    }

    private void send_byte_dump_to_main(byte[] data) {
        Message msg = main_handler.obtainMessage();
        msg.what = dorca3_activity_message_ids.MESSAGE_SEND_BYTE_DUMP;
        msg.obj = data;
        main_handler.sendMessage(msg);
    }

    private void send_byte_dump_to_main_length(byte[] data, int length) {
        if (data.length < length) {
            send_byte_dump_to_main(data);
            return;
        }
        byte[] slice = Arrays.copyOfRange(data, 0, length);
        Message msg = main_handler.obtainMessage();
        msg.what = dorca3_activity_message_ids.MESSAGE_SEND_BYTE_DUMP;
        msg.obj = slice;
        main_handler.sendMessage(msg);
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        len = (len/2) * 2;
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public boolean byes_equals(byte[] d1, byte[] d2, int length) {
        if (d1 == d2)
            return true;

        if (d1 == null || d2 == null)
            return false;

        for (int i = 0; i < length; i++)
            if (d1[i] != d2[i])
                return false;

        return true;
    }


    public int open(UsbManager manager, UsbDevice device) {
        int ret = 0;
        if (Dr3USB != null && IsUsbOpen == true) {
            send_string_to_main("dorca3_function already opend\n");
            return 0;
        }

        Dr3USB = new dorca3_usb(main_handler);
        if (Dr3USB == null ) {
            send_string_to_main("dorca3_function new fail\n");
            return -301;
        }
        ret = Dr3USB.dorca3_open(manager, device);
        if (ret < 0) {
            send_string_to_main("dorca3_function dorca3_open fail\n");
            Dr3USB = null;
            return ret;
        }
        IsUsbOpen = true;
        IsUsbUsing = false;

        send_string_to_main("dorca3_function open success\n");

        return 0;
    }

    public void close() {
        if (Dr3USB == null )
            return;

        IsUsbOpen = false;
        Dr3USB.dorca3_close();
        Dr3USB = null;
    }

    public void SetCoreUSBDebugPrint(boolean onoff){
        if (Dr3USB == null )
            return;
        Dr3USB.SetDebugPrintOption(onoff);
    }

    public boolean GetCoreUSBDebugPrint() {
        if (Dr3USB == null )
            return false;
        return Dr3USB.GetDebugPrintOption();
    }

    public synchronized boolean AccessibleCheckAndBookingUSB() {
        if (Dr3USB == null) {
            send_string_to_main("AccessibleCheckAndBookingUSB fail, Dr3USB Null\n");
            return false;
        }

        if (IsUsbOpen == false) {
            send_string_to_main("AccessibleCheckAndBookingUSB fail, USB is close\n");
            return false;
        }

        if (IsUsbUsing == true) {
            send_string_to_main("AccessibleCheckAndBookingUSB fail, USB is already use\n");
            return false;
        }

        send_string_to_main("AccessibleCheckAndBookingUSB success\n");
        IsUsbUsing = true;

        return true;
    }

    public synchronized void ReleaseBookingUSB() {
        send_string_to_main("ReleaseBookingUSB\n");
        IsUsbUsing = false;
    }

    public void dorca_start() {
        if (AccessibleCheckAndBookingUSB() == false)
            return;
        send_string_to_main("dorca_start\n");
        Dr3USB.Dorca3_SPI_Init();
        SET_SPI0();
        wake_up();
        ReleaseBookingUSB();
    }

    public void dorca_start_no_wakeup() {
        send_string_to_main("dorca_start_no_wakeup\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return;
        Dr3USB.Dorca3_SPI_Init();
        SET_SPI0();
        ReleaseBookingUSB();
    }

    public void dorca_stop() {
        send_string_to_main("dorca_stop\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return;
        Dr3USB.Dorca3_CM0_Close();
        Dr3USB.Dorca3_Close();
        ReleaseBookingUSB();
    }

    private void SetZero_RG_SLEEP_TIMER() {
        int i;
        byte tx_data[] = new byte[64];
        byte rx_data[] = new byte[64];

        for(i = 0; i < 10; i++) {
            dorca3_interface(ADDR_NOR_W, RG_EEBUF300, tx_data, rx_data, 1);
        }

        tx_data[0] = 0x00;
        dorca3_interface(ADDR_NOR_W, RG_SLEEP_TIMER_MSB, tx_data, rx_data, 1);

        tx_data[0] = 0x00;
        dorca3_interface(ADDR_NOR_W, RG_SLEEP_TIMER_LSB, tx_data, rx_data, 1);

        tx_data[0] = 0x00;
        dorca3_interface(ADDR_NOR_R, RG_SLEEP_TIMER_MSB, tx_data, rx_data, 1);

        tx_data[0] = 0x00;
        dorca3_interface(ADDR_NOR_R, RG_SLEEP_TIMER_LSB, tx_data, rx_data, 1);
    }

    private void wake_up() {
        SetZero_RG_SLEEP_TIMER();
    }

    private int dorca3_interface(byte[] inst, byte[] address, byte[] txdata, byte[] rxdata, int size) {
        int instruct = (int)inst[0];
        int register = (address[0] << 8) | (address[1]);
        if(Dr3USB == null)
            return -1;
        return Dr3USB.dc_usb_transfer(instruct, register, txdata, rxdata, size);
    }

    public void RSSHAReadIdx(byte index, byte[] out){
        int i = 0;
        int j = 0;
        int k = 0;
        int success = 1;
        byte tx_data[] = new byte[64];
        byte rx_data[] = new byte[64];
        byte RS_RD_RND[] = new byte[4];
        byte RS_RND_DATA[] = new byte[4];
        byte Dummy_15BYTE[] = new byte[15];

        byte Trail;
        byte LEN[] = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0xB8};
        Trail = (byte)0x80;

        tx_data[0] = (byte) (0x00 + (index << 4));
        dorca3_interface(ADDR_NOR_W,RG_RSCREATE_CTRL, tx_data, rx_data, 1);
        tx_data[0] = 0x09;
        dorca3_interface(ADDR_NOR_W,RG_ST0_OPMODE, tx_data, rx_data, 1);
        tx_data[0] = 0x08;
        dorca3_interface(ADDR_NOR_W,RG_ST1_SYMCIP_OPMODE, tx_data, rx_data, 1);
        j = 7;
        for(i = 0; i <8; i++)
            tx_data[i] = LEN[j--];
        tx_data[8]	= Trail;
        j = 14;
        for(i = 9; i < 24; i++)
            tx_data[i] = Dummy_15BYTE[j--];
        j = 3;
        for(i = 24; i < 28; i++)
            tx_data[i] = RS_RND_DATA[j--];
        j = 3;
        for(i = 28; i < 32; i++)
            tx_data[i] = RS_RD_RND[j--];

        dorca3_interface(ADDR_NOR_W,RG_EEBUF400, tx_data, rx_data, 32);
        delay_us(100);
        dorca3_interface(ADDR_NOR_R,RG_ACCESS, tx_data, rx_data, 1);
        delay_us(100);
        dorca3_interface(ADDR_NOR_R,RG_EEBUF400, tx_data, rx_data, 32);
        j = 31;
        for( i = 0; i < 32; i++)
            out[i] = rx_data[j--];
        tx_data[0] = 0x01;
        dorca3_interface(ADDR_NOR_W,RG_ST1_SYMCIP_OPMODE,tx_data, rx_data, 1);
        endOP();
        return;
    }

    private void endOP() {

        byte tx_data[] = new byte[64];
        byte rx_data[] = new byte[64];

        tx_data[0] = 0x01;
        dorca3_interface(ADDR_NOR_W, RG_ST0_OPMODE, tx_data, rx_data, 1);

        tx_data[0] = 0x00;
        dorca3_interface(ADDR_NOR_W, RG_ACCESS, tx_data, rx_data, 1);
        try {
            Thread.sleep(10);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void delay_us(int us)
    {
        try {
            Thread.sleep(1);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    private void delay_ms(int ms)
    {
        try {
            Thread.sleep(ms);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    private void SET_SPI0() {
        byte tx_data[] = new byte[64];
        byte rx_data[] = new byte[64];

        tx_data[0] = 0x00;
        dorca3_interface(ADDR_NOR_W, RG_AES_CTRL, tx_data, rx_data, 1);
        dorca3_interface(ADDR_NOR_R, RG_AES_CTRL, tx_data, rx_data, 1);

        tx_data[0] = 1;
        dorca3_interface(ADDR_NOR_W, RG_ST1_STDSPI_OPMODE, tx_data, rx_data, 1);
        endOP();
    }

    private void WRITE_TEST_5() {
        byte tx_data[] = new byte[64];
        byte rx_data[] = new byte[64];

        SetZero_RG_SLEEP_TIMER();

        tx_data[0] = 0x03;
        dorca3_interface(ADDR_NOR_W, RG_AES_CTRL,tx_data, rx_data, 1);
        dorca3_interface(ADDR_NOR_R, RG_AES_CTRL,tx_data, rx_data, 1);

        tx_data[0] = 0x05;
        dorca3_interface(ADDR_NOR_W, RG_ST0_OPMODE, tx_data, rx_data, 1);
        dorca3_interface(ADDR_NOR_R, RG_ST0_OPMODE, tx_data, rx_data, 1);

        tx_data[0] = 0x00;
        dorca3_interface(ADDR_NOR_W, RG_RNDGEN_USER, tx_data, rx_data, 1);
        dorca3_interface(ADDR_NOR_R, RG_RNDGEN_USER, tx_data, rx_data, 1);
    }

    public int DorcaPowerReset() {
        send_string_to_main("DorcaPowerReset\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return -1;
        Dr3USB.DorcaPowerReset();
        ReleaseBookingUSB();
        return 0;
    }

    public int DorcaGetSpiClock() {
        int ret;
        send_string_to_main("DorcaGetSpiClock\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return -1;
        ret = Dr3USB.DorcaGetSpiClock();
        ReleaseBookingUSB();
        return ret;
    }

    public int DorcaSetSpiClock(int clock) {
        int ret;
        send_string_to_main("DorcaSetSpiClock\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return -1;
        ret = Dr3USB.DorcaSetSpiClock(clock);
        ReleaseBookingUSB();
        return ret;
    }

    public int DorcaSPI_RX_TX_Debug(int onoff) {
        int ret;
        send_string_to_main("DorcaSPI_RX_TX_Debug " + onoff + "\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return -1;
        ret = Dr3USB.DorcaSPI_RX_TX_Debug(onoff);
        ReleaseBookingUSB();
        return ret;
    }

    public int DorcaSleepCheckLimit() {
        send_string_to_main("DorcaSleepCheckLimit\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return -1;
        Dr3USB.DorcaSleepCheckLimit();
        ReleaseBookingUSB();
        return 0;
    }

    public int SHA_1Frame_TEST() {
        send_string_to_main("SHA_1Frame_TEST START\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return -1;
        boolean compare_result = false;
        int i,j;
        byte tx_data[] = new byte[64];
        byte rx_data[] = new byte[64];
        byte buf_1FRM[] = new byte[64];
        byte buf_1FRMANS[] = new byte[64];
        byte buf_1FRMANS_REOrderedFRM[] = new byte[64];

        buf_1FRM = hexStringToByteArray("61626380000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000018");
        buf_1FRMANS = hexStringToByteArray("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        tx_data[0] = 0;
        dorca3_interface(ADDR_NOR_W, RG_SHA_CTRL, tx_data, rx_data, 1);

        tx_data[0] = 0x6;
        dorca3_interface(ADDR_NOR_W, RG_ST0_OPMODE, tx_data, rx_data, 1);

        tx_data[0] = 0x4;
        dorca3_interface(ADDR_NOR_W, RG_ST1_STDSPI_OPMODE, tx_data, rx_data, 1);

        j = 63;

        for(i = 0; i < 64; i++) {
            tx_data[i] = buf_1FRM[j--];
        }

        dorca3_interface(ADDR_NOR_W, RG_EEBUF300, tx_data, rx_data, 64);

        dorca3_interface(ADDR_NOR_R, RG_EEBUF400, tx_data, rx_data, 32);
        j = 31;

        for(i = 0; i < 32; i++)
        {
            buf_1FRMANS_REOrderedFRM[i] = rx_data[j--];
        }

        tx_data[0] = 1;
        dorca3_interface(ADDR_NOR_W, RG_ST1_STDSPI_OPMODE, tx_data, rx_data, 1);
        tx_data[0] = 1;
        dorca3_interface(ADDR_NOR_W, RG_ST0_OPMODE, tx_data, rx_data, 1);
        tx_data[0] = 0;
        dorca3_interface(ADDR_NOR_W, RG_ACCESS, tx_data, rx_data, 1);
        endOP();

        send_string_to_main("buf_1FRMANS dump\n");
        send_byte_dump_to_main(buf_1FRMANS);

        send_string_to_main("buf_1FRMANS_REOrderedFRM dump\n");
        send_byte_dump_to_main_length(buf_1FRMANS_REOrderedFRM, buf_1FRMANS.length);

        compare_result = byes_equals(buf_1FRMANS, buf_1FRMANS_REOrderedFRM, buf_1FRMANS.length);

        send_string_to_main("SHA_1Frame_TEST END, Compare Result[" + compare_result + "]\n");

        ReleaseBookingUSB();


        return compare_result ? 0 : -100;
    }

    public int SHA_2Frame_TEST() {
        send_string_to_main("SHA_2Frame_TEST START\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return -1;
        boolean compare_result = false;
        int i,j;
        byte tx_data[] = new byte[64];
        byte rx_data[] = new byte[64];
        byte buf_2_1FRM[] = new byte[64];
        byte buf_2_2FRM[] = new byte[64];
        byte buf2FRMANS[] = new byte[32];
        byte buf_2FRMANS_REOrderedFRM[] = new byte[32];

        buf_2_1FRM = hexStringToByteArray("6162636462636465636465666465666765666768666768696768696a68696a6b696a6b6c6a6b6c6d6b6c6d6e6c6d6e6f6d6e6f706e6f70718000000000000000");
        buf_2_2FRM = hexStringToByteArray("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001c0");
        buf2FRMANS = hexStringToByteArray("248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1");


        tx_data[0] = 2;
        dorca3_interface(ADDR_NOR_W, RG_SHA_CTRL, tx_data, rx_data, 1);

        tx_data[0] = 0x6;
        dorca3_interface(ADDR_NOR_W, RG_ST0_OPMODE, tx_data, rx_data, 1);

        tx_data[0] = 0x4;
        dorca3_interface(ADDR_NOR_W, RG_ST1_STDSPI_OPMODE, tx_data, rx_data, 1);
        j = 63;

        for(i = 0; i < 64; i++)
        {
            tx_data[i] = buf_2_1FRM[j--];
        }

        dorca3_interface(ADDR_NOR_W, RG_EEBUF300, tx_data, rx_data, 64);
        tx_data[0] = 3;
        dorca3_interface(ADDR_NOR_W, RG_SHA_CTRL, tx_data, rx_data, 1);
        j = 63;

        for(i = 0; i < 64; i++)
        {
            tx_data[i] = buf_2_2FRM[j--];
        }

        dorca3_interface(ADDR_NOR_W, RG_EEBUF300, tx_data, rx_data, 64);
        dorca3_interface(ADDR_NOR_R, RG_EEBUF400, tx_data, rx_data, 32);
        j = 31;

        for(i = 0; i < 32; i++)
        {
            buf_2FRMANS_REOrderedFRM[i] = rx_data[j--];
        }

        tx_data[0] = 1;
        dorca3_interface(ADDR_NOR_W, RG_ST1_STDSPI_OPMODE, tx_data, rx_data, 1);
        tx_data[0] = 1;
        dorca3_interface(ADDR_NOR_W, RG_ST0_OPMODE, tx_data, rx_data, 1);
        tx_data[0] = 0;
        dorca3_interface(ADDR_NOR_W, RG_ACCESS, tx_data, rx_data, 1);
        endOP();

        send_string_to_main("buf2FRMANS dump\n");
        send_byte_dump_to_main(buf2FRMANS);

        send_string_to_main("buf_2FRMANS_REOrderedFRM dump\n");
        send_byte_dump_to_main_length(buf_2FRMANS_REOrderedFRM, buf2FRMANS.length);

        compare_result = byes_equals(buf2FRMANS, buf_2FRMANS_REOrderedFRM, buf2FRMANS.length);

        send_string_to_main("SHA_2Frame_TEST END, Compare Result[" + compare_result + "]\n");

        ReleaseBookingUSB();

        return compare_result ? 0 : -100;
    }

    int aes()
    {
        send_string_to_main("AES_TEST START\n");
        if (AccessibleCheckAndBookingUSB() == false) {
            send_string_to_main("AccessibleCheckAndBookingUSB FALSE\n");
            return -1;
        }
        byte[] input = new byte[32];
        byte[] key = new byte[32];
        byte[] output = new byte[32];
        byte[] buf_2_1FRM = {(byte)0xDC,(byte)0x95,(byte)0xC0,(byte)0x78,(byte)0xA2,(byte)0x40,(byte)0x89,(byte)0x89,(byte)0xAD,(byte)0x48,(byte)0xA2,(byte)0x14,(byte)0x92,(byte)0x84,(byte)0x20,(byte)0x87};
        int i = 0;
        for( i = 0; i < 32; i++) {
            input[i] = 0;
            key[i] = 0;
            output[i] = 0;
        }
        Dorca3CipherDecipher((byte)RG_ENC,(byte)1 /*AES*/,key,(byte)32,null,output,input,(byte)16,(byte)MODE_ECB,(byte)1);
        for( i = 0; i < 16; i++)
        {
            if(output[i] == buf_2_1FRM[i] ) {
                send_string_to_main("AES PASS\n");
            }
            else {
                send_string_to_main("AES FAIL\n");
                String testStr = String.format("%02x",output[i]);
                send_string_to_main(testStr);
            }
        }
        send_string_to_main("\nAES AES,AES AES");
        ReleaseBookingUSB();
        return -100;
    }

    void AES_ARIA_Encrypt(byte[] pInput, byte[] pOutput)
    {
        int i;
        int j;
        int inst = 0;
        //unsigned char addr[2];
        byte[] tx_data = new byte[64];
        byte[] rx_data = new byte[64];
        j = 15;
        for(i =  0; i < 16; i++)
        {
            tx_data[i] = pInput[j--];
        }
        send_string_to_main("AES_ARIA_Encrypt Input\n");

        send_byte_dump_to_main(pInput);
        dorca3_interface( ADDR_NOR_W, RG_EEBUF300      , tx_data, rx_data, 16);
        delay_us(2);
        dorca3_interface( ADDR_NOR_R, RG_EEBUF320      , tx_data, rx_data, 16);
        //iEnd = pRSTC->RTTC_RTVR;
        j = 15;
        for(i =  0; i < 16; i++)
        {
            pOutput[i] = rx_data[j--];
        }
        send_string_to_main("AES_ARIA_Encrypt\n");

        send_byte_dump_to_main(pOutput);

        return;
    }
    void AES_ARIA_Decrypt(byte[] pInput, byte[] pOutput)
    {
        int i;
        int j;
        int inst = 0;
        //unsigned char addr[2];
        byte[] tx_data = new byte[64];
        byte[] rx_data = new byte[64];


        j = 15;
        for(i =  0; i < 16; i++)
        {
            tx_data[i] = pInput[j--];
        }

        dorca3_interface( ADDR_NOR_W, RG_EEBUF400      , tx_data, rx_data, 16);
        delay_us(2);
        dorca3_interface( ADDR_NOR_R, RG_EEBUF420      , tx_data, rx_data, 16);
        //iEnd = pRSTC->RTTC_RTVR;
        j = 15;
        for(i =  0; i < 16; i++)
        {
            pOutput[i] = rx_data[j--];
        }

        return;
    }
    void AES_ARIA_CLOSE()
    {

        int i;
        int j;
        int inst = 0;
        //unsigned char addr[2];
        byte[] tx_data = new byte[64];
        byte[] rx_data = new byte[64];
        tx_data[0] = 0x1;
        dorca3_interface( ADDR_NOR_W, RG_ST2_SYMCIP_OPMODE    , tx_data, rx_data, 1);


        tx_data[0] = 0x1;
        dorca3_interface( ADDR_NOR_W, RG_ST1_SYMCIP_OPMODE    , tx_data, rx_data, 1);


        endOP();
        return;
    }
    int KeyLoadIDX(byte KeyAseCtrl,byte TextSel,byte KeySel, byte KeySaveSel, byte[] LoadKEY ,byte mode, byte[] pPrevKey)
    {
        int i;
        int j;
        int inst = 0;
        int pass = 1;
        //unsigned char addr[2];
        byte[] tx_data = new byte[64];
        byte[] rx_data = new byte[64];
        byte temp ;
        int success = 1;

        byte[] cypkey;
        byte[] ciphered_key = new byte[32];
        byte[] LoadKEY_16 = new byte[32];
        byte[] ciphered_key_16 = new byte[32];

        send_string_to_main("LoadKEY\n");
        send_byte_dump_to_main(LoadKEY);
        send_string_to_main("\n pPrevKey\n");
        send_byte_dump_to_main(pPrevKey);

        System.arraycopy(LoadKEY, 16, LoadKEY_16, 0, 16);
        System.arraycopy(ciphered_key, 16, ciphered_key_16, 0, 16);
        if(LoadKEY != null)
        {

            JniBrige.AES_CIPHER(LoadKEY, ciphered_key ,pPrevKey);
            if(mode == MODE256)
                JniBrige.AES_CIPHER(LoadKEY_16,ciphered_key_16,pPrevKey);
        }

        System.arraycopy(ciphered_key_16, 0, ciphered_key, 16, 16);
        send_string_to_main("\n ciphered_key\n");
        send_byte_dump_to_main(ciphered_key);
        delay_ms(10);

        //PRINTLOG("\r\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        //PRINTLOG("\r\n++BEFORE KEY LOAD READ EE_KEY_AES_x");

        //g
        //gPrintOut = 1
        //PRINTLOG("\r\n KeyAseCtrl: %d KeySel:%d  TextSel:%d,KeySaveSel:%d",	KeyAseCtrl,KeySel,TextSel,KeySaveSel);
        tx_data[0] = KeyAseCtrl;// EE_KEY_AES_x0
        dorca3_interface( ADDR_NOR_W, RG_EE_KEY_AES_CTRL  , tx_data, rx_data, 1);

        tx_data[0] =(byte) ( ((byte)KeySaveSel<<4)//KL_KeySaveSel
                |((byte)TextSel<<2)
                |((byte)KeySel) ); //KL_KEYSEL
        //PRINTLOG("RG_KL_CTRL 0x%02x",tx_data[0]);
        dorca3_interface( ADDR_NOR_W, RG_KL_CTRL   , tx_data, rx_data, 1);
	/*
	if(TextSel == 2)
	{
	for( i = 0; i < 64; i++)
	tx_data[i] = i;
	WriteRGEBUF(tx_data);
	}
	*/
        tx_data[0] = 0x01 ;//AES_256
        dorca3_interface( ADDR_NOR_W, RG_AES_CTRL  , tx_data, rx_data, 1);


        tx_data[0] = 0x09;
        dorca3_interface( ADDR_NOR_W, RG_ST0_OPMODE  , tx_data, rx_data, 1);

        tx_data[0] = 0x06;
        dorca3_interface( ADDR_NOR_W, RG_ST1_SYMCIP_OPMODE  , tx_data, rx_data, 1);

        tx_data[0] = 0x03;
        dorca3_interface( ADDR_NOR_W, RG_ST2_SYMCIP_OPMODE   , tx_data, rx_data, 1);
        delay_us(30);

        tx_data[0] = 0x01;
        dorca3_interface( ADDR_NOR_W,  RG_ST2_SYMCIP_OPMODE   , tx_data, rx_data, 1);

        tx_data[0] = 0x09;
        dorca3_interface( ADDR_NOR_W,  RG_ST2_SYMCIP_OPMODE   , tx_data, rx_data, 1);

        tx_data[0] = 0x02;
        dorca3_interface( ADDR_NOR_W,  RG_ST3_SYMCIP_KEYLOAD_OPMODE   , tx_data, rx_data, 1);

        tx_data[0] = 0x00;
        dorca3_interface( ADDR_NOR_W,  RG_ACCESS    , tx_data, rx_data, 1);
        j = 15;

        if(TextSel == 0)
        {
            //		for(i = 0; i < 16; i++)
            //			tx_data[i] = cypkey[j--];


            if(LoadKEY != null)
            {
                j = 31;
                for(i = 0; i < 16; i++)
                    tx_data[i] = ciphered_key[j--];
            }
            dorca3_interface( ADDR_NOR_W, RG_EEBUF400   , tx_data, rx_data, 16);
        }

        //	delay_us(70);
        delay_us(70*2);

        tx_data[0] = 0x03;
        dorca3_interface( ADDR_NOR_W,  	RG_ST3_SYMCIP_KEYLOAD_OPMODE     , tx_data, rx_data, 1);

        tx_data[0] = 0x00;
        dorca3_interface( ADDR_NOR_W,  RG_ACCESS    , tx_data, rx_data, 1);
        if(TextSel == 0)
        {


            if(LoadKEY != null)
            {
                if(mode == MODE256)
                {
                    j = 15;
                    for(i = 0; i < 16; i++)
                        tx_data[i] = ciphered_key[j--];
                    dorca3_interface( ADDR_NOR_W, RG_EEBUF400   , tx_data, rx_data, 16);
                }
            }
            else
            {
                j = 15;
                for(i = 0; i < 16; i++)
                    tx_data[i] = 0;//cypkey[j--];
                dorca3_interface( ADDR_NOR_W, RG_EEBUF400   , tx_data, rx_data, 16);
            }
        }
        //	delay_us(100);
        delay_ms(16);

        if(TextSel == 2)
        {
            tx_data[0] = 0x01;
            dorca3_interface( ADDR_NOR_W, RG_ST1_OKA_OPMODE, tx_data, rx_data, 1);
        }
        tx_data[0] = 0x01;
        dorca3_interface( ADDR_NOR_W,  RG_ST3_SYMCIP_KEYLOAD_OPMODE      , tx_data, rx_data, 1);

        tx_data[0] = 0x01;
        dorca3_interface( ADDR_NOR_W,  RG_ST2_SYMCIP_OPMODE     , tx_data, rx_data, 1);

        tx_data[0] = 0x01;
        dorca3_interface( ADDR_NOR_W, RG_ST1_SYMCIP_OPMODE   , tx_data, rx_data, 1);

        tx_data[0] = 0x01;
        dorca3_interface( ADDR_NOR_W, RG_ST0_OPMODE  , tx_data, rx_data, 1);


        //gPrintOut = 0;
        delay_ms(16);
        endOP();

        return success;
    }
    void KeySetup(byte[] KEY)
    {
        //KeyLoadDemo2(0,0,0,0,KEY,MODE256);
        //byte gAES_KEY_SEED[]= {(byte)0x00,(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x09,(byte)0x0a,(byte)0x0b,(byte)0x0c,(byte)0x0d,(byte)0x0e,(byte)0x0f,(byte)0x10,(byte)0x11,(byte)0x12,(byte)0x13,(byte)0x14,(byte)0x15,(byte)0x16,(byte)0x17,(byte)0x18,(byte)0x19,(byte)0x1a,(byte)0x1b,(byte)0x1c,(byte)0x1d,(byte)0x1e,(byte)0x1f};
        //byte KEY[]= {(byte)0x00,(byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x66,(byte)0x77,(byte)0x88,(byte)0x99,(byte)0xaa,(byte)0xbb,(byte)0xcc,(byte)0xdd,(byte)0xee,(byte)0xff};
        KeyLoadIDX((byte)0,(byte)0,(byte)0,(byte)0,KEY,(byte)MODE256, gAES_KEY_SEED);
        String temp = String.format("KeySetup %d\n",gAES_KEY_SEED.length );

        System.out.println(gAES_KEY_SEED+ "\n" + KEY);
        send_string_to_main(temp);
        return;
    }
    int AES_ARIA_INIT(byte RG_128_256,byte AES_ARIA,byte[] AES_ARIA_KEY,byte RG_TWO_FRAME)
    {
        int i;
        int j;
        int inst = 0;
        //unsigned char addr[2];
        byte[] tx_data = new byte[64];
        byte[] rx_data = new byte[64];
        byte[] key_buffer = new byte[64];
        for(i = 0; i < key_buffer.length;i++ )
            key_buffer[i] = 0;


        send_string_to_main("AES_ARIA_INIT\n");
        String result = String.format("\nRG_128_256 %d,AES_ARIA %d KEY",RG_128_256,AES_ARIA);
        send_string_to_main(result);
        send_byte_dump_to_main(AES_ARIA_KEY);

        if(RG_128_256 == RG_256)
        {
            //memcpy(key_buffer,AES_ARIA_KEY+16,16);
            System.arraycopy(AES_ARIA_KEY, 16, key_buffer, 0, 16);
            //memcpy(key_buffer+16,AES_ARIA_KEY,16);
            System.arraycopy(AES_ARIA_KEY, 0, key_buffer, 16, 16);
            KeySetup(key_buffer);
        }
        else
        {
            // memcpy(key_buffer+16,AES_ARIA_KEY,16);
            System.arraycopy(AES_ARIA_KEY, 0, key_buffer, 16, 16);
            KeySetup(key_buffer);
        }
        tx_data[0] = 0x0;// KEY_0
        dorca3_interface(ADDR_NOR_W, RG_EE_KEY_AES_CTRL,       tx_data, rx_data, 1);
        tx_data[0] =(byte) (
                //	(RG_TWO_FRAME<<3)|
                ((byte)RG_128_256<<1)|
                        (byte)AES_ARIA );
        dorca3_interface(ADDR_NOR_W, RG_AES_CTRL ,      tx_data, rx_data, 1);
        tx_data[0] = 0x9;
        dorca3_interface(ADDR_NOR_W, RG_ST0_OPMODE  ,     tx_data, rx_data, 1);
        tx_data[0] = 0x2;
        dorca3_interface(ADDR_NOR_W, RG_ST1_SYMCIP_OPMODE ,      tx_data, rx_data, 1);
        tx_data[0] = 0x3;
        dorca3_interface(ADDR_NOR_W, RG_ST2_SYMCIP_OPMODE  ,     tx_data, rx_data, 1);
        delay_us(30);
        tx_data[0] = 0x1;
        dorca3_interface(ADDR_NOR_W, RG_ST2_SYMCIP_OPMODE,       tx_data, rx_data, 1);
        tx_data[0] = 0x4;
        dorca3_interface(ADDR_NOR_W, RG_ST2_SYMCIP_OPMODE   ,    tx_data, rx_data, 1);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        return 0;
    }
    public int Dorca3CipherDecipher(byte mode, byte arg_type, byte[] Key, byte key_length, byte[] iv, byte[] out, byte[] in, byte len, byte type,byte last)
    {
        byte dorca3_opmode = 0;
        byte dorca3_enc_dec = 0;
        byte dorca3_keylength = 0;
        byte dorca3_aes_aria = 0;
        byte dorca3_two_frame = 0;
        if(32 == len)
            dorca3_two_frame = 1;

        dorca3_opmode = type;
        dorca3_enc_dec = mode;
        if( 1 == arg_type)
            dorca3_aes_aria = RG_AES;
        else
            dorca3_aes_aria = RG_ARIA;
        if(32 == key_length)
            dorca3_keylength = RG_256;
        else
            dorca3_keylength = RG_128;

        send_string_to_main("Dorca3CipherDecipher\n");
        if(null != Key) {
            if (MODE_ECB == type) {

                AES_ARIA_INIT(dorca3_keylength, dorca3_aes_aria, Key, dorca3_two_frame);
            }
        }
        else {
            Log.e("AES","AES KEY SETTING FAIL");
        }

        if(16 == len) {
            if( RG_ENC == dorca3_enc_dec)
                AES_ARIA_Encrypt(in,out);
            else
                AES_ARIA_Decrypt(in,out);
        }


        if(1 == last){
            AES_ARIA_CLOSE();
        }
        return 0;
    }

    private int ecdh_gen_session_key(byte sk[], byte p1x[], byte p1y[], byte key[])
    {
        byte buffer_ecdh[] = new byte[256];
        byte buffer_receive[] = new byte[256];
        byte XofKey[] = new byte[32];
        int i = 0;

        buffer_ecdh[0] = (byte)SPI1_WRITE_DATA;
        buffer_ecdh[1] = 0;
        buffer_ecdh[2] = (byte)SIZE_ECDH_256;
        buffer_ecdh[3] = 0;
        buffer_ecdh[4] = 0;
        Dr3USB.send_data_arm7(buffer_ecdh, 5);

        buffer_ecdh[0] = (byte)SPI1_WRITE_DATA;
        buffer_ecdh[1] = 0;
        buffer_ecdh[2] = (byte)Set_ECDH_PrivateKey;
        buffer_ecdh[3] = 0;
        buffer_ecdh[4] = 32;
        System.arraycopy(sk, 0, buffer_ecdh, 5, 32);
        Dr3USB.send_data_arm7(buffer_ecdh, 37);

        buffer_ecdh[0] = (byte)SPI1_WRITE_DATA;
        buffer_ecdh[1] = 0;
        buffer_ecdh[2] = (byte)Set_ECDH_PublicKey_X;
        buffer_ecdh[3] = 0;
        buffer_ecdh[4] = 32;
        System.arraycopy(p1x, 0, buffer_ecdh, 5, 32);
        Dr3USB.send_data_arm7(buffer_ecdh, 37);


        buffer_ecdh[0] = (byte)SPI1_WRITE_DATA;
        buffer_ecdh[1] = 0;
        buffer_ecdh[2] = (byte)Set_ECDH_PublicKey_Y;
        buffer_ecdh[3] = 0;
        buffer_ecdh[4] = 32;
        System.arraycopy(p1y, 0, buffer_ecdh, 5, 32);
        Dr3USB.send_data_arm7(buffer_ecdh, 37);

        buffer_ecdh[0] = (byte)SPI1_WRITE_DATA;
        buffer_ecdh[1] = 0;
        buffer_ecdh[2] = (byte)Create_ECHD_KEY;
        buffer_ecdh[3] = 0;
        buffer_ecdh[4] = 0;
        Dr3USB.send_data_arm7(buffer_ecdh, 5);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        buffer_ecdh[0] = (byte)SPI1_READ_DATA;
        buffer_ecdh[1] = 0;
        buffer_ecdh[2] = (byte)Get_ECDH_KEY_X;
        buffer_ecdh[3] = 0;
        buffer_ecdh[4] = 32;
        Dr3USB.read_data_arm7(buffer_ecdh, buffer_receive, 32);
        System.arraycopy(buffer_receive, 0, key, 0, 32);

        buffer_ecdh[0] = (byte)SPI1_WRITE_DATA;
        buffer_ecdh[1] = 0;
        buffer_ecdh[2] = (byte)SET_EEPROM_BY_KEY;
        buffer_ecdh[3] = 0;
        buffer_ecdh[4] = 0;
        Dr3USB.send_data_arm7(buffer_ecdh, 5);

        return 0;
    }

    private int TEST_ECDH_SESSION() {
        boolean compare_result = false;
        byte sk[] = new byte[32];
        byte common_key[] = new byte[32];
        byte p1x[] = new byte[32];
        byte p1y[] = new byte[32];
        byte temp[] = new byte[32];
        int  key_length;

        sk = hexStringToByteArray("c64d654e263cda95d6dc719d3cfd6c3b932b1fea6021b9e2ac36995c4d96ae3d");
        p1y = hexStringToByteArray("fb526fbfae10d2a0d8fab4d4bdcc883bbfadee2a73ea66a1a1fe816c282d2ce9");
        p1x = hexStringToByteArray("764ea0ef1a596b196e8b7316e60de4edccbae87821e767b50f6f36656e7ebe2a");

        key_length = 32;

        ecdh_gen_session_key(sk, p1x, p1y, common_key);

        temp = hexStringToByteArray("9e29727653fe830e9709045ead243fa44acec4efb7322048894c4d06b484ce58");

        send_string_to_main("temp dump\n");
        send_byte_dump_to_main(temp);

        send_string_to_main("common_key dump\n");
        send_byte_dump_to_main(common_key);

        compare_result = byes_equals(temp, common_key, 32);

        send_string_to_main("TEST_ECDH_SESSION END, Compare Result[" + compare_result + "]\n");

        return compare_result ? 0 : -100;
    }

    public int ecdh_gen_session_key() {
        send_string_to_main("ecdh_gen_session_key START\n");
        if (AccessibleCheckAndBookingUSB() == false)
            return -1;

        int ret;

        WRITE_TEST_5();
        Dr3USB.Dorca3_Close();
        Dr3USB.GenINT0();
        Dr3USB.Dorca3_CM0_SPI_Init();
        ret = TEST_ECDH_SESSION();
        Dr3USB.Dorca3_CM0_Close();
        Dr3USB.Dorca3_SPI_Init();
        Dr3USB.GenINT0();
        SET_SPI0();
        ReleaseBookingUSB();

        return ret;
    }
    public native String stringFromJNI();

    //static {
    //    System.loadLibrary("NeowineNative");
    //}

}

