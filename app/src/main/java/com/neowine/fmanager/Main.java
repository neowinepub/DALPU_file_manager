/*
    Open Manager, an open source file manager for the Android system
    Copyright (C) 2009, 2010, 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.neowine.fmanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v4.view.GravityCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.support.v4.widget.DrawerLayout;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;

public final class Main extends Activity implements View.OnClickListener {
    private File ext_file;
    WordDBHelper mHelper;
    private String neowineDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Neowine";
    private String sphotoEncDir = neowineDir + "/SPhoto_enc";
    private String sphotoDecDir = neowineDir + "/SPhoto_dec";
    public static final String ACTION_WIDGET = "com.neowine.fmanager.Main.ACTION_WIDGET";

    private LinearLayout ll_sdcard, ll_extcard, ll_enc, ll_dec;
    private Button btn_smode, btn_emode, btn_side, btn_photo, btn_movie, btn_music, btn_doc;
    private Button btn_home, btn_local, btn_refresh;
    private TextView tv_sd1_label, tv_sd2_label;
    private TextView tv_sd1_memory, tv_sd2_memory;
    private TextView tv_sd1_total_memory, tv_sd2_total_memory;
    private TextView tv_photo_cnt, tv_movie_cnt, tv_music_cnt, tv_doc_cnt;
    private ProgressBar main_sd1_progressbar, main_sd2_progressbar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private FrameLayout fm;
    private LayoutInflater mInflater;
    private View sidemenu;
    private TextView enc_num, dec_num;
    private static String ENC_DIR = Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc";// + "/Neowine/SPhoto_enc";
    private static String DEC_DIR = Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_dec";// + "/Neowine/SPhoto_dec";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    public Util e4net_util;
    private String LOG_TAG = "E4NET";

    private static final String ACTION_USB_PERMISSION = "com.neowine.dorca3.USB_PERMISSION";

    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private dorca3_function Dr3Functions = null;
    private PendingIntent mPermissionIntent;
    private Handler Dorca3_UI_Handler = null;
    DrContainer container = null;



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbDetachReceiver);
        unregisterReceiver(mUsbAttachReceiver);
        unregisterReceiver(mUsbReceiver);
        //Dr3Functions.close();
    }

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        try {
            Thread.sleep(200);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        checkPermissionREAD_EXTERNAL_STORAGE(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_frame);


        mHelper = new WordDBHelper(this);

        e4net_util = Util.getInstance();

        //Directory Check
/*        if (checkNeowineDir() == false) {
            Toast.makeText(getApplicationContext(), "Can't create directory", Toast.LENGTH_SHORT).show();
            finish();
        }*/

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(8, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[9], navMenuIcons.getResourceId(9, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[10], navMenuIcons.getResourceId(10, -1)));

        // Recycle the typed array
        navMenuIcons.recycle();

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        fm = (FrameLayout) findViewById(R.id.content_frame);
        mInflater = LayoutInflater.from(fm.getContext());
        sidemenu = mInflater.inflate(R.layout.main_view, null);
        fm.addView(sidemenu);

        //Set Button Events
        btn_smode = (Button) sidemenu.findViewById(R.id.btnTopNav1);
        btn_smode.setOnClickListener(this);
        btn_emode = (Button) sidemenu.findViewById(R.id.btnTopNav3);
        btn_emode.setOnClickListener(this);
        btn_side = (Button) sidemenu.findViewById(R.id.btnSideMenu);
        btn_side.setOnClickListener(this);
        btn_photo = (Button) sidemenu.findViewById(R.id.btn_photo);
        btn_photo.setOnClickListener(this);
        btn_movie = (Button) sidemenu.findViewById(R.id.btn_movie);
        btn_movie.setOnClickListener(this);
        btn_music = (Button) sidemenu.findViewById(R.id.btn_music);
        btn_music.setOnClickListener(this);
        btn_doc = (Button) sidemenu.findViewById(R.id.btn_doc);
        btn_doc.setOnClickListener(this);
        //btn_home = (Button) sidemenu.findViewById(R.id.btn_bot_home);
       // btn_home.setOnClickListener(this);
      //  btn_local = (Button) sidemenu.findViewById(R.id.btn_bot_local);
      //  btn_local.setOnClickListener(this);
      //  btn_refresh = (Button) sidemenu.findViewById(R.id.btn_bot_refresh);
      //  btn_refresh.setOnClickListener(this);
        ll_sdcard = (LinearLayout) sidemenu.findViewById(R.id.ll_sdcard);
        ll_sdcard.setOnClickListener(this);
        ll_extcard = (LinearLayout) sidemenu.findViewById(R.id.ll_extcard);
        ll_extcard.setOnClickListener(this);
        ll_enc = (LinearLayout) sidemenu.findViewById(R.id.ll_encfolder);
        ll_enc.setOnClickListener(this);
        ll_dec = (LinearLayout) sidemenu.findViewById(R.id.ll_decfolder);
        ll_dec.setOnClickListener(this);

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        boolean isPresent = true;
        if (!docsFolder.exists()) {
            isPresent = docsFolder.mkdir();
        }

        tv_sd1_label = (TextView) sidemenu.findViewById(R.id.main_tv_sd1_lable);
        tv_sd1_memory = (TextView) sidemenu.findViewById(R.id.main_tv_sd2_memory);
        tv_sd1_total_memory = (TextView) sidemenu.findViewById(R.id.main_tv_sd1_total_memory);
        main_sd1_progressbar = (ProgressBar) sidemenu.findViewById(R.id.main_sd1_progressbar);

        tv_sd2_label = (TextView) sidemenu.findViewById(R.id.main_tv_sd2_lable);
        tv_sd2_memory = (TextView) sidemenu.findViewById(R.id.main_tv_sd1_memory);
        tv_sd2_total_memory = (TextView) sidemenu.findViewById(R.id.main_tv_sd2_total_memory);
        main_sd2_progressbar = (ProgressBar) sidemenu.findViewById(R.id.main_sd2_progressbar);
        String [] FileList = getExternalStorageDirectories();
        for(int i = 0; i < FileList.length; i++)
            Log.d("E4NET","EXTERNAL FILE LIST  :"+FileList[i]);

        File file = Environment.getExternalStorageDirectory();
        tv_sd1_label.setText(file.getName());
        tv_sd1_memory.setText(formatSize(getInternalMemorySize()));
        tv_sd1_total_memory.setText(formatSize(getTotalInternalMemorySize()));
        main_sd1_progressbar.setMax(formatSize2(getTotalInternalMemorySize()));
        main_sd1_progressbar.setProgress(formatSize2(getTotalExternalMemorySize() - getExternalMemorySize()));
        Log.d(e4net_util.e4_tag(), "sdcard0 free : " + formatSize(getTotalExternalMemorySize() - getExternalMemorySize()));
        Log.d(e4net_util.e4_tag(), "sdcard0 total : " + formatSize(getTotalInternalMemorySize()));

//        ext_file = new File(System.getenv("SECONDARY_STORAGE"));
//        File[] ext_file_list = ext_file.listFiles();
//        //File[] ext_file_list = getExternalFilesDirs(null);
//        if ((ext_file_list != null) && (ext_file_list.length > 1)) {
//            tv_sd2_label.setText(ext_file.getName());
//            tv_sd2_memory.setText(formatSize(getTotalExternalMemorySize() - getExternalMemorySize()));
//            tv_sd2_total_memory.setText(formatSize(getTotalExternalMemorySize()));
//            main_sd2_progressbar.setMax(formatSize2(getTotalExternalMemorySize()));
//            main_sd2_progressbar.setProgress(formatSize2(getTotalExternalMemorySize() - getExternalMemorySize()));
//            Log.d(e4net_util.e4_tag(), "sdcard1 free : " + formatSize(getExternalMemorySize()));
//            Log.d(e4net_util.e4_tag(), "sdcard1 total : " + formatSize(getTotalExternalMemorySize()));
//        } else {
//            tv_sd2_label.setText("EMPTY");
//            tv_sd2_memory.setText("0");
//            tv_sd2_total_memory.setText("0");
//
//            AlertDialog.Builder alert = new AlertDialog.Builder(Main.this);
//            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            alert.setMessage("Can't find 'DORCA' in this system!!!");
//            alert.show();
//        }

        if (TRUE) {
            if(FileList[0] != null)
            {
                File file_ext = new File(FileList[0]);
                tv_sd2_label.setText(file_ext.getName());
                tv_sd2_memory.setText(formatSize(getExternalMemorySize(FileList[0])) );
                tv_sd2_total_memory.setText(formatSize(getTotalExternalMemorySize(FileList[0])));
                main_sd2_progressbar.setMax(formatSize2(getTotalExternalMemorySize(FileList[0])));
                main_sd2_progressbar.setProgress(formatSize2(getTotalExternalMemorySize(FileList[0]) - getExternalMemorySize(FileList[0]) ));
                Log.d("E4NET", "sd2 free : " + formatSize(getExternalMemorySize(FileList[0])));
                Log.d("E4NET", "sd2 total : " + formatSize(getTotalExternalMemorySize(FileList[0]) - getExternalMemorySize(FileList[0])  ));
            } else {
                tv_sd2_label.setText("EMPTY");
                tv_sd2_memory.setText("0");
                tv_sd2_total_memory.setText("0");
            }
        } else {
            ext_file = Environment.getExternalStorageDirectory();
            File[] ext_file_list = ext_file.listFiles();
            if ((ext_file_list != null) && (ext_file_list.length > 1)) {
                tv_sd2_label.setText(ext_file.getName());
                tv_sd2_memory.setText(formatSize(getTotalExternalMemorySize() - getExternalMemorySize()));
                tv_sd2_total_memory.setText(formatSize(getTotalExternalMemorySize()));
                main_sd2_progressbar.setMax(formatSize2(getTotalExternalMemorySize()));
                main_sd2_progressbar.setProgress(formatSize2(getTotalExternalMemorySize() - getExternalMemorySize()));
                Log.d(e4net_util.e4_tag(), "sdcard1 free : " + formatSize(formatSize2(getTotalExternalMemorySize() - getExternalMemorySize()))  );
                Log.d(e4net_util.e4_tag(), "sdcard1 total : " + formatSize(getTotalExternalMemorySize()));
            } else {
                tv_sd2_label.setText("EMPTY");
                tv_sd2_memory.setText("0");
                tv_sd2_total_memory.setText("0");
                AlertDialog.Builder alert = new AlertDialog.Builder(Main.this);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.setMessage("Can't find 'DORCA' in this system!!!");
                alert.show();
            }
        }

        //CheckUSB_Device();
        //Set Enc/Dec File Count
        enc_num = (TextView) sidemenu.findViewById(R.id.encrypt_number);
        dec_num = (TextView) sidemenu.findViewById(R.id.decrypt_number);
        //File tmp_num = new File(ENC_DIR);
        //String[] files = tmp_num.list();
       /* if (files.length != 0) {
            enc_num.setText(String.valueOf(files.length));
        }*/
        //tmp_num = new File(DEC_DIR);
        //files = tmp_num.list();
        /*if (files.length != 0) {
            dec_num.setText(String.valueOf(files.length));
        }*/
        //Set Media File Count
        tv_photo_cnt = (TextView) sidemenu.findViewById(R.id.tv_photo_cnt);
        tv_movie_cnt = (TextView) sidemenu.findViewById(R.id.tv_movie_cnt);
        tv_music_cnt = (TextView) sidemenu.findViewById(R.id.tv_music_cnt);
        tv_doc_cnt = (TextView) sidemenu.findViewById(R.id.tv_doc_cnt);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Images.Media._ID};
            int img_cnt = 0;
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    img_cnt++;
                }
                cursor.close();
            }
            tv_photo_cnt.setText(String.valueOf(img_cnt));

            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            projection = new String[]{MediaStore.Audio.Media._ID};
            int audio_cnt = 0;
            cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    audio_cnt++;
                }
                cursor.close();
            }
            tv_music_cnt.setText(String.valueOf(audio_cnt));

            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            projection = new String[]{MediaStore.Video.Media._ID};
            int movie_cnt = 0;
            cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    movie_cnt++;
                }
                cursor.close();
            }
            tv_movie_cnt.setText(String.valueOf(movie_cnt));
            File[] doc_num = docsFolder.listFiles();
            tv_doc_cnt.setText(String.valueOf(doc_num.length));
        }



       // TextView tv = (TextView) findViewById(R.id.sample_text);
      //  tv.setText(stringFromJNI());
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mUsbAttachReceiver , filter);
        filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbDetachReceiver , filter);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        Dorca3_UI_Handler = new ActivityHandler();
        Dr3Functions =  dorca3_function.getInstance();
        Dr3Functions.set_handler(Dorca3_UI_Handler);
        // Example of a call to a native method

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        CheckUSB_Device();

        container = DrContainer.getInstance();
        Dr3Functions.open(mUsbManager, mDevice);

        if(null == mUsbManager)
            Log.d(e4net_util.e4_tag(), "mUsbManager is null");
        if(null == mDevice)
            Log.d(e4net_util.e4_tag(), "mDevice is null");

        container.SetUSB(mUsbManager,mDevice);
        //findViewById(R.id.button).setOnClickListener(
         //       new Button.OnClickListener() {
          //          public void onClick(View v) {
                        //여기에 이벤트를 적어주세요

                        new Thread(new Runnable(){
                            @Override
                            public void run(){
                                int ret;

                                Dr3Functions.dorca_start();
                                Dr3Functions.DorcaSetSpiClock(12000000);
                                Dr3Functions.DorcaPowerReset();
                                Dr3Functions.dorca_start_no_wakeup();
                                Dr3Functions.DorcaSleepCheckLimit();

                                if( Dr3Functions.SHA_1Frame_TEST() == 0)
                                {
                                    runOnUiThread(
                                            new Runnable() {
                                                public void run() { //
                                                    Toast.makeText(getApplicationContext(), "SUCESS", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                }
                                else {
                                    runOnUiThread(
                                            new Runnable() {
                                                public void run() { //
                                                    Toast.makeText(getApplicationContext(), "FAIL", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                }
                                //Dr3Functions.close();
                            }
                        }).start();


                    }
            //    }
        //);



    public String[] getExternalStorageDirectories() {

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = getExternalFilesDirs(null);
            String internalRoot = Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase();

            for (File file : externalDirs) {
                if(file==null) //solved NPE on some Lollipop devices
                    continue;
                String path = file.getPath().split("/Android")[0];

                if(path.toLowerCase().startsWith(internalRoot))
                    continue;

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d(LOG_TAG, results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    Log.d(LOG_TAG, results.get(i)+" might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }
    public void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                Log.d("E4NET","folder :"+fileEntry.getName());
                // listFilesForFolder(fileEntry);
            } else {
                Log.d("E4NET","file :"+fileEntry.getName());
            }
        }
    }
    private long getTotalExternalMemorySize(String Path) {
        //String extFilePath = "/storage/72AD-2013";
        //File path = new File(extFilePath);
        StatFs stat = new StatFs(Path);
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();

        return totalBlocks * blockSize;

    }

    /**
     * 사용가능한 외장 메모리 크기를 가져온다
     */
    private long getExternalMemorySize(String Path) {
        StatFs stat = new StatFs(Path);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }
    /**
     * 전체 내장 메모리 크기를 가져온다
     */
    private long getTotalInternalMemorySize() {
        if (isStorage(true) == true) {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();

            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * 사용가능한 내장 메모리 크기를 가져온다
     */
    private long getInternalMemorySize() {
        if (isStorage(true) == true) {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * 전체 외장 메모리 크기를 가져온다
     */
    private long getTotalExternalMemorySize() {
        if (isStorage(true) == true) {
            //String extFilePath = "/storage/72AD-2013";
            File path = Environment.getExternalStorageDirectory();

            //File path = new File(extFilePath);
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();

            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * 사용가능한 외장 메모리 크기를 가져온다
     */
    private long getExternalMemorySize() {
        if (isStorage(true) == true) {
            //String extFilePath = "/storage/72AD-2013";
            //File path = new File(extFilePath);
            File path = Environment.getExternalStorageDirectory();

            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * 외장메모리 sdcard 사용가능한지에 대한 여부 판단
     */
    private boolean isStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else if (!requireWriteAccess &&
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String  formatSize(long size) {
        String suffix = null;
        float fl_size = size;

        if (fl_size >= 1024) {
            suffix = "KB";
            fl_size /= 1024;
            if (fl_size >= 1024) {
                suffix = "MB";
                fl_size /= 1024;

                if (fl_size >= 1024) {
                    suffix = "GB";
                    fl_size /= 1024;
                }
            }
        }
        StringBuilder resultBuffer = new StringBuilder(String.format("%.2f", fl_size));

        if (suffix != null) {
            resultBuffer.append(suffix);
        }
        return resultBuffer.toString();
    }

    private int formatSize2(long size) {
        String suffix = null;
        float fl_size = size;

        if (fl_size >= 1024) {
            suffix = "KB";
            fl_size /= 1024;
            if (fl_size >= 1024) {
                suffix = "MB";
                fl_size /= 1024;

                suffix = "GB";
                fl_size /= 1024;
            }
        }

        int result = Math.round(fl_size);

        return result;
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        SQLiteDatabase db;
        db = mHelper.getReadableDatabase();
        ContentValues row;
        row = new ContentValues();


        Cursor cursor = db.rawQuery("SELECT pic_dir, mov_dir, mus_dir, doc_dir FROM dic", null);



        String picture_dir = "";
        String movie_dir = "";
        String music_dir = "";
        String docu_dir = "";

        while (cursor.moveToNext()){
            picture_dir = cursor.getString(0);
            movie_dir = cursor.getString(1);
            music_dir = cursor.getString(2);
            docu_dir = cursor.getString(3);
        }

        Log.d("sqasdf", "================================================================================================");
        Log.d("sqasdf", "main testing1 picture_dir " + picture_dir);
        Log.d("sqasdf", "main testing1 movie_dir " + movie_dir);
        Log.d("sqasdf", "main testing1 music_dir " + music_dir);
        Log.d("sqasdf", "main testing1 document_dir " + docu_dir);
        Log.d("sqasdf", "================================================================================================");


        cursor.close();
        mHelper.close();




        switch (v.getId()) {
            case R.id.btnTopNav1:
                e4net_util.intent(Main.this, SmodeActivity.class);
                break;

            case R.id.btnTopNav3:
                e4net_util.intent(Main.this, EmodeActivity.class);
                break;

            case R.id.btnTopNav2:
                e4net_util.intent(Main.this, Main.class);
                break;

            case R.id.btn_photo:
                //e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
                if (picture_dir == null || picture_dir == "") {
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory() + "/DCIM");
                    SmodeActivity.set_pic_dir(Environment.getExternalStorageDirectory() + "/DCIM");
                } else {
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, picture_dir);

                }
                break;

            case R.id.btn_movie:
                //e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString());
                if (movie_dir == null || movie_dir == ""){
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory() + "/Movies");
                    SmodeActivity.set_mov_dir(Environment.getExternalStorageDirectory() + "/Movies");
                } else {
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET,  movie_dir);
                }
                break;

            case R.id.btn_music:
                //e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString());
                if (music_dir == null || music_dir == ""){
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory() + "/Music");
                    SmodeActivity.set_mus_dir(Environment.getExternalStorageDirectory() + "/Music");
                } else {
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, music_dir);
                }
                break;

            case R.id.btn_doc:
                //e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory() + "/Documents");
                if (docu_dir == null || docu_dir == ""){
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory() + "/Documents");
                    SmodeActivity.set_doc_dir(Environment.getExternalStorageDirectory() + "/Documents");
                } else {
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, docu_dir);
                }
                break;

            case R.id.btnSideMenu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.ll_decfolder:
                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, sphotoDecDir);
                break;

            case R.id.ll_encfolder:
                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, sphotoEncDir);
                break;

            case R.id.ll_sdcard:
                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory().toString());
                break;

            case R.id.ll_extcard:
                File ext_file = Environment.getExternalStorageDirectory();
                File[] ext_file_list = ext_file.listFiles();
                if ((ext_file_list != null) && (ext_file_list.length > 1)) {
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, ext_file.toString());
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(Main.this);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.setMessage("Can't find 'DORCA' in this system!!!");
                    alert.show();
                }
                break;

            default:
                break;
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }


    private void selectItem(int position) {
        Intent intent = null;
        String path = null;
        switch (position) {
            case 1:
                //e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory().toString());
                mDrawerLayout.closeDrawer(GravityCompat.START);

                break;
            case 2:
                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getRootDirectory().toString());
                break;
            case 3:
                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                break;
            case 4:
                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory().toString());
                break;
            case 5:
                File ext_file = Environment.getExternalStorageDirectory();
                File[] ext_file_list = ext_file.listFiles();
                if ((ext_file_list != null) && (ext_file_list.length > 1)) {
                    e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, ext_file.toString());
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(Main.this);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.setMessage("Can't find 'DORCA' in this system!!!");
                    alert.show();
                }
                break;
            case 7:
                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
                break;
            case 8:
                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString());
                break;
            case 9:
                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString());
                break;
            case 10:

                e4net_util.intent(Main.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory() + "/Documents");
                break;
        }
    }

    private boolean checkNeowineDir() {

        File neowineDirPath = new File(neowineDir);
        File sPhotoEncDirPath = new File(sphotoEncDir);
        File sPhotoDecDirPath = new File(sphotoDecDir);

        if (!neowineDirPath.isDirectory()) {
            if (neowineDirPath.mkdirs() == false) return false;
            if (sPhotoEncDirPath.mkdirs() == false) return false;
            if (sPhotoDecDirPath.mkdirs() == false) return false;
            return true;
        } else {
            return true;
        }
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }


    public void UpdateDebugLog(CharSequence s, boolean linenumber) {
        //if (linenumber == true) {
        // String update_str = format("%04d: %s", DebugLogTextView.getLineCount(), s);
        //DebugLogTextView.append(update_str);
        //} else {
        //DebugLogTextView.append(s.toString());
        //}

        Log.d("DORCA3", s.toString());
    }

    private void CheckUSB_Device() {
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        UpdateDebugLog("CheckUSB_Device\n", false);
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (!(device.getProductId() == dorca3_function.DORCA3_USB_PID && device.getVendorId() == dorca3_function.DORCA3_USB_VID)) {
                UpdateDebugLog("miss match device" + device.getProductId() + device.getVendorId() + "\n", true);
                continue;
            }
            if (!mUsbManager.hasPermission(device)) {
                mUsbManager.requestPermission(device, mPermissionIntent);
                UpdateDebugLog("request permission\n", true);
                return;
            }

            mDevice = device;
            container = DrContainer.getInstance();
            container.SetUSB(mUsbManager, mDevice);
        }
    }

    BroadcastReceiver mUsbAttachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Toast.makeText(getApplicationContext(), "ACTION_USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
                CheckUSB_Device();
            }
        }
    };
    BroadcastReceiver mUsbDetachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Toast.makeText(getApplicationContext(), "ACTION_USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
            }


            // if (CheckConnectedDevice() == false) {
            //     Toast.makeText(getApplicationContext(), "DORCA3 Device not connected", Toast.LENGTH_SHORT).show();
            //     finishAffinity();
            // }
        }
    };

class ActivityHandler extends Handler {
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case dorca3_activity_message_ids.MESSAGE_SEND_TEST:
                String TestName = (String)msg.obj;
                //Toast.makeText(getApplicationContext(), TestName + " Result " , Toast.LENGTH_SHORT).show();
                Log.d("USB_TEST",TestName);
                break;
            case dorca3_activity_message_ids.MESSAGE_SEND_BYTE_DUMP:
                //byte[] data = (byte[])msg.obj;
                //UpdateDebugLog(hexdump.dumpHexString(data) + "\n", false);
                break;
            case dorca3_activity_message_ids.MESSAGE_SEND_SHOW_SPI_CLOCK:
                //String str_spi_clock = format("%d", msg.arg1);
                //UpdateDebugLog("Get Spi Clock [" + str_spi_clock + "]\n", true);

                break;
            case dorca3_activity_message_ids.MESSAGE_SEND_TEST_MENU_RESULT:
                String TestName2 = (String)msg.obj;
                int Result = msg.arg1;
                Toast.makeText(getApplicationContext(), TestName2 + " Result " + Result, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            UpdateDebugLog("permission access for device\n", true);
                            CheckUSB_Device();
                        }
                    } else {
                        UpdateDebugLog("permission denied for device\n", true);
                    }
                }
            }
        }
    };

    class WordDBHelper extends SQLiteOpenHelper {
        public WordDBHelper(Context context) {
            super(context, "EncFile3.db", null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE dic ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "pic_dir TEXT , mov_dir TEXT , mus_dir TEXT, doc_dir TEXT);");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS dic");
            onCreate(db);
        }

    }

}

