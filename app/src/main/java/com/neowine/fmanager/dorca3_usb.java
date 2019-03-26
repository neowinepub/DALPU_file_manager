package com.neowine.fmanager;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

class dc_protocol {
    public int sequence_id;
    public int target;
    public int special_cmd;
    public int spi_inst;
    public int dc_register;
    public int data_size;
    public int result;

    public int get_member_size() {
        return Integer.BYTES * 7;
    }

    public byte[] get_member_to_bytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(get_member_size());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(sequence_id);
        intBuffer.put(target);
        intBuffer.put(special_cmd);
        intBuffer.put(spi_inst);
        intBuffer.put(dc_register);
        intBuffer.put(data_size);
        intBuffer.put(result);
        return byteBuffer.array();
    }

    public int set_member_to_bytes(byte[] data) {
        if (data.length < get_member_size()) {
            return -1;
        }

        byte[] allmember = Arrays.copyOf(data, get_member_size());
        ByteBuffer byteBuffer = ByteBuffer.wrap(allmember);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        sequence_id = intBuffer.get();
        target = intBuffer.get();
        special_cmd = intBuffer.get();
        spi_inst = intBuffer.get();
        dc_register = intBuffer.get();
        data_size = intBuffer.get();
        result = intBuffer.get();

        return 0;
    }
}

public class dorca3_usb {
    private int TARGET_ID_SPI0 = 1;
    private int TARGET_ID_SPI1 = 2;
    private int TARGET_ID_CMD = 3;

    private int SET_SPI0_CS0_INIT = 0xF1;
    private int SET_SPI0_CS0_DEINIT = 0xF2;
    private int SET_SPI0_CS1_INIT = 0xF3;
    private int SET_SPI0_CS1_DEINIT = 0xF4;
    private int SET_DORCA_INT_TRIGGER = 0xF5;
    private int SET_DORCA_POWER_RESET = 0xF6;
    private int SET_DORCA_SLEEP_CHECK_LIMIT = 0xF8;
    private int SET_DORCA_DEBUG_SPI_TX_RX_ENABLE = 0xF9;
    private int SET_DORCA_DEBUG_SPI_TX_RX_DISABLE = 0xFA;
    private int SET_DORCA_SPI_CLOCK = 0xFB;
    private int GET_DORCA_SPI_CLOCK = 0xFC;
    private int SET_DORCA_GPIO_LEVEL = 0xFD;

    private Handler main_handler;

    private boolean forceClaim = true;
    private UsbDevice mDevice;
    private UsbManager mUsbManager;
    private UsbDeviceConnection mConnection;
    private UsbInterface mIntf;
    private UsbEndpoint out_endpoint;
    private UsbEndpoint in_endpoint;

    private boolean show_debug;

    private int seq_number;
    private byte[] txbuffer = new byte[512];
    private byte[] rxbuffer = new byte[512];

    private boolean isconnected;

    public dorca3_usb(Handler hd) {
        main_handler = hd;
        seq_number = 0;
        isconnected = false;
        show_debug = false;
    }

    public void SetDebugPrintOption(boolean onoff) {
        show_debug = onoff;
    }

    public boolean GetDebugPrintOption() {
        return show_debug;
    }

    public int dorca3_open(UsbManager manager, UsbDevice device) {
        if (manager == null || device == null) {
            send_string_to_main("Dorca3 USB manager and device null\n");
            return -10;
        }

        mUsbManager = manager;
        mDevice = device;
        mIntf = mDevice.getInterface(0);
        mConnection = mUsbManager.openDevice(mDevice);
        if (mConnection == null) {
            send_string_to_main("Dorca3 USB openDevice failed\n");
            return -1;
        }

        if (!mConnection.claimInterface(mIntf, true)) {
            send_string_to_main("Dorca3 USB failed to claim interface\n");
            return -2;
        }

        in_endpoint = mIntf.getEndpoint(0);
        if (in_endpoint == null) {
            send_string_to_main("Dorca3 USB fail to get in endpoint\n");
            return -3;
        }
        out_endpoint = mIntf.getEndpoint(1);
        if (out_endpoint == null) {
            send_string_to_main("Dorca3 USB fail to get out endpoint\n");
            return -4;
        }

        isconnected = true;

        send_string_to_main("Dorca3 USB open success!\n");
        return 0;
    }

    public boolean dorca3_is_connect() {
        return isconnected;
    }

    public void dorca3_close() {
        if (mConnection == null || mConnection == null || mUsbManager == null)
            return;

        mConnection.releaseInterface(mIntf);
        mConnection.close();
        //mDevice = null;
        mConnection = null;
        //mUsbManager = null;
    }

    private void send_string_to_main(String s) {
        if (show_debug == false)
            return;
        Message msg = main_handler.obtainMessage();
        msg.what = dorca3_activity_message_ids.MESSAGE_SEND_TEST;
        msg.obj = s;
        main_handler.sendMessage(msg);
    }

    private void send_byte_dump_to_main(byte[] data) {
        if (show_debug == false)
            return;
        Message msg = main_handler.obtainMessage();
        msg.what = dorca3_activity_message_ids.MESSAGE_SEND_BYTE_DUMP;
        msg.obj = data;
        main_handler.sendMessage(msg);
    }

    private int get_seq_number() {
        seq_number++;
        if (seq_number > 0x6fffffff) {
            seq_number = 0;
        }
        return seq_number;
    }

    public int dc_usb_transfer(int instruct, int register, byte[] txdata, byte[] rxdata, int size) {
        int tx_size;
        int rx_size;
        int txed_size;
        int rxed_size;

        if (dorca3_is_connect() == false)
            return -1;

        dc_protocol dp = new dc_protocol();
        dc_protocol rdp = new dc_protocol();

        Arrays.fill(txbuffer, (byte)0);
        Arrays.fill(rxbuffer, (byte)0);

        if (instruct == 0x31 || instruct == 0x30) {
            dp.sequence_id = get_seq_number();
            dp.target = TARGET_ID_SPI0;
            dp.special_cmd = 0;
            dp.spi_inst = instruct;
            dp.dc_register = register;
            dp.data_size = size;
            dp.result = 0;

            byte[] dpbytes = dp.get_member_to_bytes();
            System.arraycopy(dpbytes, 0, txbuffer, 0, dp.get_member_size());
            System.arraycopy(txdata, 0, txbuffer, dp.get_member_size(), size);

            tx_size = dp.get_member_size() + size;
            txed_size = mConnection.bulkTransfer(out_endpoint, txbuffer, tx_size, 1000);
            if (txed_size < 0) {
                send_string_to_main("dc_usb_transfer: ailed to write bulkTransfer" + txed_size + "\n");
                return -1;
            }

            rx_size = rdp.get_member_size();
            rxed_size = mConnection.bulkTransfer(in_endpoint, rxbuffer, rx_size, 1000);
            if (rxed_size < 0) {
                send_string_to_main("dc_usb_transfer: failed to read bulkTransfer" + rxed_size + "\n");
                return -1;
            }

            rdp.set_member_to_bytes(rxbuffer);
            if(dp.sequence_id == rdp.result && dp.sequence_id == rdp.sequence_id) {
                send_string_to_main("dc_usb_transfer: dc_usb_transfer success transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]" + "\n");
            } else {
                send_string_to_main("dc_usb_transfer: dc_usb_transfer fail transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]" + "\n");
            }
        } else {
            dp.sequence_id = get_seq_number();
            dp.target = TARGET_ID_SPI0;
            dp.special_cmd = 0;
            dp.spi_inst = instruct;
            dp.dc_register = register;
            dp.data_size = size;
            dp.result = 0;

            byte[] dpbytes = dp.get_member_to_bytes();
            System.arraycopy(dpbytes, 0, txbuffer, 0, dp.get_member_size());
            tx_size = dp.get_member_size();
            txed_size = mConnection.bulkTransfer(out_endpoint, txbuffer, tx_size, 1000);
            if (txed_size < 0) {
                send_string_to_main("dc_usb_transfer: ailed to write bulkTransfer" + txed_size + "\n");
                return -1;
            }

            rx_size = rdp.get_member_size() + size;
            rxed_size = mConnection.bulkTransfer(in_endpoint, rxbuffer, rx_size, 1000);
            if (rxed_size < 0) {
                send_string_to_main("dc_usb_transfer: failed to read bulkTransfer" + rxed_size + "\n");
                return -1;
            }
            rdp.set_member_to_bytes(rxbuffer);
            if(dp.sequence_id == rdp.result && dp.sequence_id == rdp.sequence_id) {
                System.arraycopy(rxbuffer, rdp.get_member_size(), rxdata, 0, size);
                send_string_to_main("dc_usb_transfer: dc_usb_transfer success transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]" + "\n");
            } else {
                send_string_to_main("dc_usb_transfer: dc_usb_transfer fail transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]" + "\n");
            }
        }
        return 0;
    }

    public int dc_usb_transfer_CM0(int instruct, byte[] txdata, byte[] rxdata, int size) {
        int tx_size;
        int rx_size;
        int txed_size;
        int rxed_size;

        if (dorca3_is_connect() == false)
            return -1;

        dc_protocol dp = new dc_protocol();
        dc_protocol rdp = new dc_protocol();

        Arrays.fill(txbuffer, (byte)0);
        Arrays.fill(rxbuffer, (byte)0);

        if (instruct == 0xabcd1) {
            dp.sequence_id = get_seq_number();
            dp.target = TARGET_ID_SPI1;
            dp.special_cmd = 0;
            dp.spi_inst = instruct;
            dp.dc_register = 0;
            dp.data_size = size;
            dp.result = 0;

            byte[] dpbytes = dp.get_member_to_bytes();
            System.arraycopy(dpbytes, 0, txbuffer, 0, dp.get_member_size());
            System.arraycopy(txdata, 0, txbuffer, dp.get_member_size(), size);

            tx_size = dp.get_member_size() + size;
            txed_size = mConnection.bulkTransfer(out_endpoint, txbuffer, tx_size, 1000);
            if (txed_size < 0) {
                send_string_to_main("dc_usb_transfer_CM0: ailed to write bulkTransfer" + txed_size + "\n");
                return -1;
            }

            rx_size = rdp.get_member_size();
            rxed_size = mConnection.bulkTransfer(in_endpoint, rxbuffer, rx_size, 1000);
            if (rxed_size < 0) {
                send_string_to_main("dc_usb_transfer_CM0: failed to read bulkTransfer" + rxed_size + "\n");
                return -1;
            }

            rdp.set_member_to_bytes(rxbuffer);
            if(dp.sequence_id == rdp.result && dp.sequence_id == rdp.sequence_id) {
                send_string_to_main("dc_usb_transfer_CM0: dc_usb_transfer success transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]" + "\n");
            } else {
                send_string_to_main("dc_usb_transfer_CM0: dc_usb_transfer fail transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]" + "\n");
            }
        } else {
            dp.sequence_id = get_seq_number();
            dp.target = TARGET_ID_SPI1;
            dp.special_cmd = 0;
            dp.spi_inst = instruct;
            dp.dc_register = 0;
            dp.data_size = size;
            dp.result = 0;

            byte[] dpbytes = dp.get_member_to_bytes();
            System.arraycopy(dpbytes, 0, txbuffer, 0, dp.get_member_size());
            tx_size = dp.get_member_size();
            txed_size = mConnection.bulkTransfer(out_endpoint, txbuffer, tx_size, 1000);
            if (txed_size < 0) {
                send_string_to_main("dc_usb_transfer_CM0: ailed to write bulkTransfer" + txed_size + "\n");
                return -1;
            }

            rx_size = rdp.get_member_size() + size;
            rxed_size = mConnection.bulkTransfer(in_endpoint, rxbuffer, rx_size, 1000);
            if (rxed_size < 0) {
                send_string_to_main("dc_usb_transfer_CM0: failed to read bulkTransfer" + rxed_size + "\n");
                return -1;
            }
            rdp.set_member_to_bytes(rxbuffer);
            if(dp.sequence_id == rdp.result && dp.sequence_id == rdp.sequence_id) {
                System.arraycopy(rxbuffer, rdp.get_member_size(), rxdata, 0, size);
                send_string_to_main("dc_usb_transfer_CM0: dc_usb_transfer success transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]" + "\n");
            } else {
                send_string_to_main("dc_usb_transfer_CM0: dc_usb_transfer fail transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]" + "\n");
            }
        }
        return 0;
    }

    private int dc_usb_control(int command, byte[] txdata, byte[] rxdata, int data_size) {

        int tx_size;
        int rx_size;
        int txed_size;
        int rxed_size;

        if (dorca3_is_connect() == false)
            return -1;

        dc_protocol dp = new dc_protocol();
        dc_protocol rdp = new dc_protocol();

        if (in_endpoint == null || out_endpoint == null) {
            send_string_to_main("Can't find the endpoints" + "\n");
            return -1;
        }

        Arrays.fill(txbuffer, (byte)0);
        Arrays.fill(rxbuffer, (byte)0);

        dp.sequence_id = get_seq_number();
        dp.target = TARGET_ID_CMD;
        dp.special_cmd = command;
        dp.spi_inst = 0;
        dp.dc_register = 0;
        dp.data_size = data_size;
        dp.result = 0;

        tx_size = dp.get_member_size();
        rx_size = rdp.get_member_size();

        byte[] dpbytes = dp.get_member_to_bytes();
        System.arraycopy(dpbytes, 0, txbuffer, 0, dp.get_member_size());

        if (txdata != null) {
            System.arraycopy(txdata, 0, txbuffer, dp.get_member_size(), data_size);
            tx_size = dp.get_member_size() + data_size;
        }

        send_string_to_main("dc_usb_control write " + tx_size + "bytes" + "\n");
        txed_size = mConnection.bulkTransfer(out_endpoint, txbuffer, tx_size, 1000);
        if (txed_size < 0) {
            send_string_to_main("failed to write bulkTransfer" + txed_size + "\n");
            return -1;
        }

        if (tx_size != txed_size) {
            send_string_to_main("mismatch write transfer size " + tx_size + " " + txed_size + "\n");
            return -1;
        }

        if (rxdata != null) {
            rx_size = rx_size + data_size;
        }

        rxed_size = mConnection.bulkTransfer(in_endpoint, rxbuffer, rx_size, 1000);
        if (rxed_size < 0) {
            send_string_to_main("failed to read bulkTransfer" + rxed_size + "\n");
            return -1;
        }

        if (rx_size != rxed_size) {
            send_string_to_main("mismatch read transfer size " + rx_size + " " + rxed_size + "\n");
            return -1;
        }

        send_string_to_main("dc_usb_control read " + rxed_size + " bytes" + "\n");

        rdp.set_member_to_bytes(rxbuffer);
        if (rxdata != null) {
            System.arraycopy(rxbuffer, rdp.get_member_size(), rxdata, 0, data_size);
            send_byte_dump_to_main(rxdata);
        }

        if(dp.sequence_id == rdp.result && dp.sequence_id == rdp.sequence_id) {
            send_string_to_main("dc_usb_control success transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]"+ "\n");
        } else {
            send_string_to_main("dc_usb_control fail transfer id[" + dp.sequence_id + "] result [" + rdp.result + "]" + "\n");
        }

        return 0;
    }

    public void Dorca3_SPI_Init() {

        if (dorca3_is_connect() == false)
            return;

        dc_usb_control(SET_SPI0_CS0_INIT, null, null, 0);
    }

    public void Dorca3_Close() {
        if (dorca3_is_connect() == false)
            return;
        dc_usb_control(SET_SPI0_CS0_DEINIT, null, null, 0);
    }

    public void Dorca3_CM0_SPI_Init() {
        if (dorca3_is_connect() == false)
            return;
        dc_usb_control(SET_SPI0_CS1_INIT, null, null, 0);
    }

    public void Dorca3_CM0_Close() {
        if (dorca3_is_connect() == false)
            return;
        dc_usb_control(SET_SPI0_CS1_DEINIT, null, null, 0);
    }

    public void GenINT0() {
        if (dorca3_is_connect() == false)
            return;
        dc_usb_control(SET_DORCA_INT_TRIGGER, null, null, 0);
    }

    public void DorcaPowerReset() {
        if (dorca3_is_connect() == false)
            return;
        dc_usb_control(SET_DORCA_POWER_RESET, null, null, 0);
    }

    public int DorcaGetSpiClock() {
        int iclock = 0;

        if (dorca3_is_connect() == false)
            return -1;

        byte[] byteclock = new byte[4];
        dc_usb_control(GET_DORCA_SPI_CLOCK, null, byteclock, 4);
        iclock = byteclock[0] | (byteclock[1] & 0xFF) << 8 | (byteclock[2] & 0xFF) << 16 | (byteclock[3] & 0xFF) << 24;
        return iclock;
    }

    public int DorcaSetSpiClock(int clock) {
        if (dorca3_is_connect() == false)
            return -1;
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(clock);
        dc_usb_control(SET_DORCA_SPI_CLOCK, byteBuffer.array(), null, 4);
        return 0;
    }

    public int DorcaSetGpio(int gpio, int highlow) {
        if (dorca3_is_connect() == false)
            return -1;
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(gpio);
        byteBuffer.putInt(highlow);
        byteBuffer.array();
        dc_usb_control(SET_DORCA_GPIO_LEVEL, byteBuffer.array(), null, 8);
        return 0;
    }

    public int DorcaSPI_RX_TX_Debug(int onoff) {
        if (dorca3_is_connect() == false)
            return -1;
        if (onoff == 1)
            return dc_usb_control(SET_DORCA_DEBUG_SPI_TX_RX_ENABLE, null, null, 0);
        else
            return dc_usb_control(SET_DORCA_DEBUG_SPI_TX_RX_DISABLE, null, null, 0);
    }

    public void DorcaSleepCheckLimit() {
        if (dorca3_is_connect() == false)
            return;
        dc_usb_control(SET_DORCA_SLEEP_CHECK_LIMIT, null, null, 0);
    }

    public int Dorca_spi_write_read_using_usb(int instruct, int register, byte[] txdata, byte[] rxdata, int size) {
        if (dorca3_is_connect() == false)
            return -1;
        return dc_usb_transfer(instruct, register, txdata, rxdata, size);
    }

    public void send_data_arm7(byte[] buffer, int size) {
        if (dorca3_is_connect() == false)
            return;
        dc_usb_transfer_CM0(0xabcd1, buffer, null, size);
    }

    public void read_data_arm7(byte[] txbuffer, byte[] rxbuffer, int size) {
        if (dorca3_is_connect() == false)
            return;
        dc_usb_transfer_CM0(0xabcd1, txbuffer, null, 5);
        try {
            Thread.sleep(50);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        dc_usb_transfer_CM0(0xabcd2, null, rxbuffer, size);
    }


}
