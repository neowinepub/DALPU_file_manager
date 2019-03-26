package com.neowine.fmanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

import static android.widget.Toast.LENGTH_SHORT;
import static com.neowine.fmanager.JniBrige.AES_CIPHER;
import static com.neowine.fmanager.JniBrige.OPENSSLENC;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;

/**
 * Created by e4net on 16. 2. 5..
 */
public class SmodeActivity extends Activity implements View.OnClickListener {

    private static String LOG_TAG = "NEOWINE_SPI_LOG";
    private int finish_flag = 0;
    private Button btn_emode, btn_main, btn_side, btnHome;
    WordDBHelper mHelper;
    private Button btn_home, btn_local, btn_sort;
    private static String ENC_DIR = Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc";
    private static String DEC_DIR = Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_dec";
    private static String pic_dir = Environment.getExternalStorageDirectory() + "/DCIM";
    private static String mov_dir = Environment.getExternalStorageDirectory() + "/Movies";
    private static String mus_dir = Environment.getExternalStorageDirectory() + "/Music";
    private static String doc_dir = Environment.getExternalStorageDirectory() + "/Documents";
    private boolean cyperEntered = false;
    private boolean workEntered = false;
    private String workFlag;
    private int drop_flag = 0;
    private static String INPUT_KEY = "1234567890123456";
    private static int HYPERSTEP = 32;
    public static final String ACTION_WIDGET = "com.neowine.fmanager.Main.ACTION_WIDGET";
    private static final String PREFS_NAME = "ManagerPrefsFile";    //user preference file name
    private static final String PREFS_HIDDEN = "hidden";
    private static final String PREFS_COLOR = "color";
    private static final String PREFS_THUMBNAIL = "thumbnail";
    private static final String PREFS_SORT = "sort";
    private static final String PREFS_STORAGE = "sdcard space";
    private static final int MENU_PASTE = 0x00;            //option menu id
    private static final int MENU_QUIT = 0x01;            //option menu id
    private static final int D_MENU_DELETE = 0x05;            //context menu id
    private static final int D_MENU_RENAME = 0x06;            //context menu id
    private static final int D_MENU_COPY = 0x07;            //context menu id
    private static final int D_MENU_PASTE = 0x08;            //context menu id
    private static final int D_MENU_ZIP = 0x0e;            //context menu id
    private static final int D_MENU_UNZIP = 0x0f;            //context menu id
    private static final int D_MENU_MOVE = 0x30;            //context menu id
    private static final int F_ENCRYPT_FW = 0x20;            //context menu id
    private static final int F_MENU_DELETE = 0x0a;            //context menu id
    private static final int F_DECRYPT_FW = 0x0b;            //context menu id
    private static final int F_ENCRYPT_SWXOR = 0x0c;            //context menu id
    private static final int F_DECRYPT_SWXOR = 0x0d;            //context menu id
    private static final int F_ENCRYPT_SWAES = 0x1e;            //context menu id
    private static final int F_DECRYPT_SWAES = 0x1f;            //context menu id
    private static final int F_IO_TEST = 0x1d;            //context menu id
    private static final int SETTING_REQ = 0x10;            //request code for intent

    private static final int FROM_GRID = 1;
    private static final int FROM_CYPER = 2;
    private int FROM = 0;
    final private int RG_ENC = 0;
    final private int MODE_ECB = 0;
    private TextView DebugLogTextView;
    byte[] output = new byte[32];
    byte[] test_output = new byte[32];

    private FileManager mFileMag;
    private EventHandler mHandler;
    private EventHandler.TableRow mTable;
    private SharedPreferences mSettings;
    private boolean mReturnIntent = false;
    private boolean mHoldingFile = false;
    private boolean mHoldingZip = false;
    private boolean mUseBackKey = true;
    private MyAdapter m_myAdapter;
    private String mCopiedTarget;
    private String mZippedTarget;
    private String mSelectedListItem;                //item from context menu
    private TextView mPathLabel, mDetailLabel;
    private TextView workCopy, workMove, workDelete, workRename;
    private int workCopyX, workMoveX, workDeleteX, workRenameX, workEventX;
    private GridView grid;
    private GridView grid_chiper;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private FrameLayout fm;
    private LayoutInflater mInflater;
    private View sidemenu;
    private LinearLayout cypherZone, workZone;
    private TextView txTitle;
    private ImageView enc_icon, enc_arrow, dec_icon, drag_drop_jpeg, drag_drop_data;
    private int start = 0;
    public Util e4net_util;
    private File file1;
    private File file2;
    private MyData m_myData;
    ArrayList<Integer> ar_int;
    ArrayList<String> ar_str;
    ArrayList<String> ar_cihper;
    private String m_CurrentFile;
    private dorca3_function dorca3_fx;
    final Handler cwjHandler = new Handler();
    ExecutorService eService = Executors.newSingleThreadExecutor();
    public ProgressDialog mypDialog;
    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private dorca3_function
            Dr3Functions = null;
    private PendingIntent mPermissionIntent;
    private Handler Dorca3_UI_Handler = null;
    private DrContainer container = null;
    private int start_grid = 0;
    private int start_cypher = 0;
    private static final String ACTION_USB_PERMISSION = "com.neowine.dorca3.USB_PERMISSION";

    public static String get_pic_dir(){return pic_dir;}
    public static String get_mov_dir(){return mov_dir;}
    public static String get_mus_dir(){return mus_dir;}
    public static String get_doc_dir(){return doc_dir;}
    public static void set_pic_dir(String dir){pic_dir = dir;}
    public static void set_mov_dir(String dir){mov_dir = dir;}
    public static void set_mus_dir(String dir){mus_dir = dir;}
    public static void set_doc_dir(String dir){doc_dir = dir;}


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SQLiteDatabase db;
        ContentValues row;
        Log.d("E4NET", "ContentValues row;");
        db = mHelper.getWritableDatabase();
        unregisterReceiver(mUsbDetachReceiver);
        unregisterReceiver(mUsbAttachReceiver);
        unregisterReceiver(mUsbReceiver);
        Dr3Functions.close();
        Log.d("E4NET", "Dr3Functions.close();");
        //db.execSQL("DELETE FROM dic;");
        // insert 메서드로 삽입
        /*
        row = new ContentValues();
        for( int i = 0; i < ar_cihper.size(); i++)
        row.put("file", ar_cihper.get(i));
        Log.d("E4NET","row.put(file, ar_cihper.get(i));");
        db.insert("dic", null, row);
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM dic", null);

        String Result = "";
        while (cursor.moveToNext()) {
            String eng = cursor.getString(0);
            Log.d("E4NET",eng);
        }
	*/
        mHelper.close();
    }

    public String testSHA256(String str) {

        String SHA = "";

        try {

            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.reset();
            sh.update(str.getBytes());

            byte byteData[] = sh.digest();

            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < byteData.length; i++) {

                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));

            }

            SHA = sb.toString();


        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();

            SHA = null;

        }

        return SHA;

    }

    public void UpdateDebugLog(CharSequence s, boolean linenumber) {
        if (linenumber == true) {
            String update_str = format("%04d: %s", DebugLogTextView.getLineCount(), s);
            DebugLogTextView.append(update_str);
        } else {
            DebugLogTextView.append(s.toString());
        }

        Log.d("DORCA3", s.toString());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_frame);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHelper = new WordDBHelper(this);
//////////////////////////////////////////////////////////////////////////////////////
        SQLiteDatabase db;
        ContentValues row;
        m_CurrentFile = new String();
        Log.d("E4NET", "ContentValues row;");
        row = new ContentValues();

//        db = mHelper.getWritableDatabase();
//
//       // db.delete("dic", null, null);
//        Log.d("E4NET","DELETE FROM dic;");
//        // insert 메서드로 삽입
//
//        for( int i = 0; i < 10; i++) {
//            row = new ContentValues();
//            row.put("file", "ar_cihper.get(i):   " + i);
//            db.insert("dic", null, row);
//            Log.d("E4NET","ar_cihper.get(i):   " + i);
//        }
//
//        Log.d("E4NET","row.put(file, ar_cihper.get(i));");
//
//        Cursor cursor;
//        cursor = db.rawQuery("SELECT * FROM dic", null);
//
//        String Result = "";
//        while (cursor.moveToNext()) {
//            String eng = cursor.getString(0);
//            Log.d("E4NET",eng);
//        }
//        mHelper.close();


//////////////////////////////////////////////////////////////////////////////////////
        ar_int = new ArrayList<Integer>();
        ar_str = new ArrayList<String>();
        ar_cihper = new ArrayList<String>();
        //Util Load
        m_myData = new MyData(ar_int, ar_str);

        e4net_util = Util.getInstance();
        m_myAdapter = new MyAdapter(this, R.layout.row, m_myData);
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
        sidemenu = mInflater.inflate(R.layout.smode_view, null);
        fm.addView(sidemenu);

        //Set Button Events
        btn_main = (Button) sidemenu.findViewById(R.id.btnTopNav2);
        btn_main.setOnClickListener(this);
        btn_emode = (Button) sidemenu.findViewById(R.id.btnTopNav3);
        btn_emode.setOnClickListener(this);
        btn_side = (Button) sidemenu.findViewById(R.id.btnSideMenu);
        btn_side.setOnClickListener(this);
        btn_home = (Button) sidemenu.findViewById(R.id.btnHome);
        btn_home.setOnClickListener(this);

        //btn_home = (Button) sidemenu.findViewById(R.id.btn_smode_bot_home);
        //btn_home.setOnClickListener(this);
        //btn_local = (Button) sidemenu.findViewById(R.id.btn_smode_bot_local);
        //btn_local.setOnClickListener(this);
        //btn_sort = (Button) sidemenu.findViewById(R.id.btn_smode_bot_sort);
        //btn_sort.setOnClickListener(this);
        //enc_icon = (ImageView) sidemenu.findViewById(R.id.smodeEncIcon);
        //enc_arrow = (ImageView) sidemenu.findViewById(R.id.smodeEncArrow);
        //dec_icon = (ImageView) sidemenu.findViewById(R.id.smodeDecIcon);

        cypherZone = (LinearLayout) sidemenu.findViewById(R.id.smodeCypher);
        workZone = (LinearLayout) sidemenu.findViewById(R.id.smodeMiddleWorks);
        grid = (GridView) findViewById(R.id.grid);
        grid_chiper = (GridView) findViewById(R.id.grid_cipher);
        Log.d("LinearLayout", "findViewById");

        workDelete = (TextView) findViewById(R.id.smode_delete);
        workRename = (TextView) findViewById(R.id.smode_rename);
        DebugLogTextView = (TextView) findViewById(R.id.debuglog);
        DebugLogTextView.setMovementMethod(new ScrollingMovementMethod());
        DebugLogTextView.setTextSize(14);
        DebugLogTextView.setHorizontallyScrolling(true);
        DebugLogTextView.setEllipsize(null);
        Log.d("LinearLayout", "DebugLogTextView");
        //  drag_drop_jpeg = (ImageView)sidemenu.findViewById(R.id.drag_drop_jpeg);
        //drag_drop_data = (ImageView)sidemenu.findViewById(R.id.drag_drop_data);

        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mUsbAttachReceiver, filter);
        filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbDetachReceiver, filter);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        //int open_result = Dr3Functions.open(container.mUsbManager, container.mDevice);

        cypherZone.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {

                // 이벤트 시작
                switch (event.getAction()) {

                    // 이미지를 드래그 시작될때
                    case DragEvent.ACTION_DRAG_STARTED:

                        Log.d("DragClickListener", "   cypherZone ACTION_DRAG_STARTED  start_grid: " + start_grid);
                        Log.d("DragClickListener", "AFTER   cypherZone ACTION_DRAG_STARTED  : start_cypher " + start_cypher);
                        break;
                    // 드래그한 이미지를 옮길려는 지역으로 들어왔을때
                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d("DragClickListener", "cypherZone ACTION_DRAG_ENTERED");
                        // 이미지가 들어왔다는 것을 알려주기 위해 배경이미지 변경
                        //v.setBackground(targetShape);
                        break;

                    // 드래그한 이미지가 영역을 빠져 나갈때
                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d("DragClickListener", "cypherZone ACTION_DRAG_EXITED");
                        //v.setBackground(normalShape);
                        break;

                    // 이미지를 드래그해서 드랍시켰을때
                    case DragEvent.ACTION_DROP:
                        Log.d("DragClickListener", "cypherZone ACTION_DROP");
                        int open_result;


                        if (start_cypher == 1) {
                            Log.d("DragClickListener", "if(start_cypher == 1)");
                            start_cypher = 0;
                            start_grid = 0;
                            return TRUE;
                        }
                        if (v == findViewById(R.id.smodeCypher)) {

                            Log.d("DragClickListener", "cypherZone ACTION_DROP OK OK ");
                        } else {
                            Log.d("DragClickListener", "cypherZone NOT v == findViewById(R.id.smodeCypher) ");
                            return TRUE;
                        }


                        //container = DrContainer.getInstance();
                        open_result = Dr3Functions.open(container.mUsbManager, container.mDevice);
                        //Toast.makeText(getApplicationContext(), open_result, Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Dr3Functions.open test[" + open_result + "]", Toast.LENGTH_SHORT).show();

//                if (open_result < 0) {
//                    runOnUiThread(
//                            new Runnable() {
//                                public void run() {
//                                    Toast.makeText(getApplicationContext(), "USB NOT CONNECTED", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                //    break;
//                }


                        ClipboardManager cm1 = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData cd = cm1.getPrimaryClip();
                        ClipData.Item item = cd.getItemAt(0);

                        m_myData.imgs.add(R.drawable.btn_document);
                        m_myData.strs.add(item.getText().toString());
                        ar_cihper.add(item.getText().toString());


                        String temp = item.getText().toString();
                        String filename = temp.substring(temp.lastIndexOf("/") + 1);
                        String filepath = temp.substring(0, temp.lastIndexOf("/"));
                        boolean result;
                        File f = new File("/storage/emulated/0/Download/ENC"); // 최 하위 디렉토리에 대해서만 생성을 함. // 최 하위 디렉토리의 바루 상위 디렉토리가 존재하지 않을 경우, // 디렉토리가 생성되지 못하고, false를 리턴함
                        result = f.mkdirs();
                        if (result)
                            Log.d("DragClickListener", "directory create success");
                        else
                            Log.d("DragClickListener", "directory create fail");
                        Log.d("DragClickListener", "file path  " + filepath);
                        Log.d("DragClickListener", "file name  " + GetOutFileENCName(filename));
                        m_CurrentFile = temp;

                        NeowineTask obj = new NeowineTask(item.getText().toString(), filepath + "/" + GetOutFileENCName(filename), "0000000000000000", Dorcatype.EncSSL, 0);

                        //NeowineTask obj = new NeowineTask(item.getText().toString(),  Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc" + "/"+GetOutFileENCName(filename),"0000000000000000",Dorcatype.EncSSL, 0);

                        obj.execute();


                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.d("DragClickListener", "cypherZone  ACTION_DRAG_ENDED");
                        FROM = 0;
                        start_cypher = 0;
                        start_grid = 0;
                        Log.d("DragClickListener", "cypherZone  start_cypher = 0;");
                        Log.d("DragClickListener", "cypherZone  start_grid = 0;");
                        //v.setBackground(normalShape); // go back to normal shape

                    default:
                        break;
                }
                return true;
            }
        });


        workZone.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                // 이벤트 시작
                switch (event.getAction()) {
                    // 이미지를 드래그 시작될때
                    case DragEvent.ACTION_DRAG_STARTED:
                        workEntered = false;
                        Log.d(e4net_util.e4_tag(), "DragClickListener ACTION_DRAG_STARTED");
                        break;

                    // 드래그한 이미지를 옮길려는 지역으로 들어왔을때
                    case DragEvent.ACTION_DROP:
                        Log.d(e4net_util.e4_tag(), "DragClickListener ACTION_DRAG_ENTERED");
                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData cd = cm.getPrimaryClip();

                        if (cd != null) {
                            ClipData.Item item = cd.getItemAt(0);

                            File f = new File(item.getText().toString());
                            mSelectedListItem = f.getName();
                            Log.d(e4net_util.e4_tag(), "Select [" + mSelectedListItem + "]");
                            workEventX = (int) event.getX();
                            workDeleteX = (int) workDelete.getX();
                            workRenameX = (int) workRename.getX();
                            Log.d(e4net_util.e4_tag(), "workEventX  :" + workEventX);
                            Log.d(e4net_util.e4_tag(), "workDeleteX  :" + workDeleteX);
                            Log.d(e4net_util.e4_tag(), "workRenameX  :" + workRenameX);
                            workDeleteX = (int) workDelete.getLeft();
                            workRenameX = (int) workRename.getLeft();
                            Log.d(e4net_util.e4_tag(), "****************************************************");
                            Log.d(e4net_util.e4_tag(), "workDeleteX  :" + workDeleteX);
                            Log.d(e4net_util.e4_tag(), "workRenameX  :" + workRenameX);
                            workFlag = "";
                            int[] oldFolderCellPosition = new int[2];
                            v.getLocationOnScreen(oldFolderCellPosition);
                            Log.d(e4net_util.e4_tag(), "View X  :" + oldFolderCellPosition[0]);
                            Log.d(e4net_util.e4_tag(), "View  X  :" + oldFolderCellPosition[1]);
                            //if (!f.isDirectory()) {
                            if (workEventX < workRenameX) {
                                Log.d(e4net_util.e4_tag(), "Delete [" + mSelectedListItem + "]");
                                workFlag = "DELETE";
                            } else if (workEventX >= workRenameX) {
                                Log.d(e4net_util.e4_tag(), "Rename [" + mSelectedListItem + "]");
                                workFlag = "RENAME";
                            } else {
                                workFlag = "NONE";
                                Log.d(e4net_util.e4_tag(), "Error selected [" + mSelectedListItem + "]");
                            }
                            //}
                        }
                        workEntered = true;
                        break;

                    // 드래그한 이미지가 영역을 빠져 나갈때
                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d("DragClickListener", "ACTION_DRAG_EXITED");
                        workEntered = false;
                        break;

                    // 이미지를 드래그해서 드랍시켰을때
                    //case DragEvent.ACTION_DROP:
                    //   Log.d("DragClickListener", "ACTION_DROP");
                    //   break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.d(e4net_util.e4_tag(), "DragClickListener ACTION_DRAG_ENDED : " + workEntered);
                        if (workEntered) {
                            ClipboardManager cm1 = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData cd1 = cm1.getPrimaryClip();
                            Log.d(e4net_util.e4_tag(), "workFlag" + workFlag);
                            if (cd1 != null) {
                                if (workFlag.equals("DELETE")) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(SmodeActivity.this);
                                    builder.setTitle("Warning ");
                                    builder.setIcon(R.drawable.warning);
                                    builder.setMessage("Deleting " + mSelectedListItem +
                                            " cannot be undone. Are you sure you want to delete?");
                                    builder.setCancelable(false);

                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            mHandler.deleteFile(mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                                        }
                                    });

                                    AlertDialog alert_d = builder.create();
                                    alert_d.show();
                                } else if (workFlag.equals("RENAME")) {
                                    showDialog(D_MENU_RENAME);
                                }

                            }
                        }
                        workZone.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });


        /*read settings*/
        mSettings = getSharedPreferences(PREFS_NAME, 0);
        boolean hide = mSettings.getBoolean(PREFS_HIDDEN, false);
        boolean thumb = mSettings.getBoolean(PREFS_THUMBNAIL, true);
        int space = mSettings.getInt(PREFS_STORAGE, View.VISIBLE);
        int color = mSettings.getInt(PREFS_COLOR, -1);
        int sort = mSettings.getInt(PREFS_SORT, 4);

        mFileMag = new FileManager();
        mFileMag.setShowHiddenFiles(hide);
        mFileMag.setSortType(sort);

        if (savedInstanceState != null)
            mHandler = new EventHandler(SmodeActivity.this, mFileMag, savedInstanceState.getString("location"));
        else
            mHandler = new EventHandler(SmodeActivity.this, mFileMag);

        mHandler.setTextColor(Color.BLACK);
        mHandler.setShowThumbnails(thumb);
        mTable = mHandler.new TableRow();
        mHandler.SetEncAdapters(m_myData, m_myAdapter, ar_cihper);
        /*sets the ListAdapter for our ListActivity and
         *gives our EventHandler class the same adapter
         */
        mHandler.setListAdapter(mTable);
        grid.setAdapter(mTable);
        grid_chiper.setAdapter(m_myAdapter);

        mDetailLabel = (TextView) findViewById(R.id.smode_detail_label1);
        mPathLabel = (TextView) findViewById(R.id.smode_path_label1);
        mPathLabel.setText("path: /sdcard");
        mHandler.setUpdateLabels(mPathLabel, mDetailLabel);
        txTitle = (TextView) findViewById(R.id.smodeTopText);
        txTitle.setText("sdcard");

        e4net_util.create_path_view("/storage/emulated/0", getBaseContext(), (LinearLayout) findViewById(R.id.smode_path_layout));

        Intent intent = getIntent();
        if (intent.getAction() == null) {
            Log.d(e4net_util.e4_tag(), "Smode Activity");
        } else if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
            mReturnIntent = true;

        } else if (intent.getAction().equals(ACTION_WIDGET)) {
            Log.d(e4net_util.e4_tag(), "Widget action, string = " + intent.getExtras().getString("folder"));

            e4net_util.create_path_view(intent.getExtras().getString("folder"), getBaseContext(), (LinearLayout) findViewById(R.id.smode_path_layout));
            String[] tmp = intent.getExtras().getString("folder").split("/");
            txTitle.setText(tmp[tmp.length - 1]);
            mHandler.updateDirectory(mFileMag.getNextDir(intent.getExtras().getString("folder"), true));
        }
        /*
        ArrayList<String> files = mFileMag.getNextDir(intent.getExtras().getString("folder"), true);
		for(String path : files )
		{
			 String encFile = new String();
			 int index = path.lastIndexOf('.')-3;
			 if(index < 0)
			     continue;
			  encFile = path.substring(index,3);
            Log.d(e4net_util.e4_tag(), "ENC_DEC " + encFile);
			if(encFile.equals("ENC") ){
            int fileIndex = path.lastIndexOf("/");
            String filename = path.substring(fileIndex,path.length());
              	  	m_myData.strs.add(filename);
	                ar_cihper.add(filename);
            m_myAdapter.notifyDataSetChanged();
				Log.d(e4net_util.e4_tag(), "ENC_FILE " + path);	  				
				}
			else{

				Log.d(e4net_util.e4_tag(), "NORMAL_FILE " + path);	  				
				}
			
		}
		*/
        createDorcaPath();

        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                workZone.setVisibility(View.VISIBLE);
                workZone.bringToFront();
                view.setBackgroundColor(Color.parseColor("#3a84ff"));
                TextView filepath = (TextView) view.findViewById(R.id.filePath);
                Log.d(e4net_util.e4_tag(), "File Path : " + filepath.getText());
                ClipboardManager clipmgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData.Item item = new ClipData.Item(filepath.getText());
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData data = new ClipData(filepath.getText(), mimeTypes, item);
                clipmgr.setPrimaryClip(data);
                view.setBackgroundColor(Color.parseColor("#ffffff"));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, // data to be dragged
                        shadowBuilder, // drag shadow
                        view, // 드래그 드랍할  Vew
                        0 // 필요없은 플래그
                );
                view.setBackgroundColor(Color.parseColor("#ffffff"));
                // view.setVisibility(View.INVISIBLE) ;
                Log.d("DragClickListener", "------------------------------------- LONG CLICK grid -------------------------------------");
                start_grid = 1;
                start_cypher = 0;
                return true;
            }
        });

        grid_chiper.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //workZone.setVisibility(View.VISIBLE);
                //TextView filepath = (TextView) view.findViewById(R.id.filePath);
                //Log.d(e4net_util.e4_tag(), "File Path : " + filepath.getText());
                ClipboardManager clipmgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData.Item item = new ClipData.Item(ar_cihper.get(position));
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData data = new ClipData(ar_cihper.get(position), mimeTypes, item);
                clipmgr.setPrimaryClip(data);
                view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, // data to be dragged
                        shadowBuilder, // drag shadow
                        view, // 드래그 드랍할  Vew
                        0 // 필요없은 플래그
                );
                Log.d("DragClickListener", "-------------------------------------LONG CLICK grid_chiper -------------------------------------");
                view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                start_grid = 0;
                start_cypher = 1;
                return true;
            }
        });


        grid.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {

                // 이벤트 시작
                switch (event.getAction()) {

                    // 이미지를 드래그 시작될때
                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.d("DragClickListener", "grid    ACTION_DRAG_STARTED  start_cypher :" + start_cypher);
                        Log.d("DragClickListener", "grid    ACTION_DRAG_STARTED  start_grid :" + start_grid);
                        break;

                    // 드래그한 이미지를 옮길려는 지역으로 들어왔을때
                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d("DragClickListener", "grid ACTION_DRAG_ENTERED");
                        // 이미지가 들어왔다는 것을 알려주기 위해 배경이미지 변경
                        //v.setBackground(targetShape);
                        break;

                    // 드래그한 이미지가 영역을 빠져 나갈때
                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d("DragClickListener", "grid ACTION_DRAG_EXITED");
                        //v.setBackground(normalShape);
                        break;

                    // 이미지를 드래그해서 드랍시켰을때
                    case DragEvent.ACTION_DROP:
                        Log.d("DragClickListener", "grid ACTION_DROP");

                        int open_result;
                        if (start_grid == 1) {
                            Log.d("DragClickListener", "if(start_grid == 1) { ");
                            start_grid = 0;
                            start_cypher = 0;
                            return TRUE;
                        }
//                        if (v == findViewById(R.id.grid_cipher)) {
//
//                            Log.d("DragClickListener", "grid ACTION_DROP OK OK ");
//                        }
//                        else {
//                            Log.d("DragClickListener", "grid NOT v == findViewById(R.id.grid_cipher) ");
//                            return TRUE;
//                        }
                        //container = DrContainer.getInstance();
                        open_result = Dr3Functions.open(container.mUsbManager, container.mDevice);
                        Toast.makeText(getApplicationContext(), "Dr3Functions.open test[" + open_result + "]", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(), open_result, Toast.LENGTH_SHORT).show();
//                        if (open_result < 0) {
//                            runOnUiThread(
//                                    new Runnable() {
//                                        public void run() {
//                                            Toast.makeText(getApplicationContext(), "USB NOT CONNECTED", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//                            break;
//                        }


                        ClipboardManager cm1 = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData cd = cm1.getPrimaryClip();
                        ClipData.Item item = cd.getItemAt(0);
                        Toast.makeText(getApplicationContext(), item.getText().toString(), Toast.LENGTH_SHORT).show();
                        Log.d("DragClickListener", item.getText().toString());
                        for (int i = 0; i < m_myData.strs.size(); i++) {
                            String temp = item.getText().toString();

                            String str0 = temp.replaceAll("(^\\p{Z}+|\\p{Z}+$)", "");
                            String str1 = m_myData.strs.get(i).replaceAll("(^\\p{Z}+|\\p{Z}+$)", "");

                            Log.d("DragClickListener", i + "  " + str0.length() + str0);
                            Log.d("DragClickListener", i + "  " + str1.length() + str1);

                            if (str0.equals(str1)) {
                                m_myData.strs.remove(i);
                                m_myData.imgs.remove(i);
                                ar_cihper.remove(i);

                                String temp2 = str0;
                                String filename = temp.substring(temp2.lastIndexOf("/") + 1);
                                String filepath = temp.substring(0, temp2.lastIndexOf("/"));
                                boolean result;
                                File f = new File("/storage/emulated/0/Download/ENC"); // 최 하위 디렉토리에 대해서만 생성을 함. // 최 하위 디렉토리의 바루 상위 디렉토리가 존재하지 않을 경우, // 디렉토리가 생성되지 못하고, false를 리턴함
                                result = f.mkdirs();
                                if (result)
                                    Log.d("DragClickListener", "directory create success");
                                else
                                    Log.d("DragClickListener", "directory create fail");
                                Log.d("DragClickListener", "file path  " + filepath);
                                Log.d("DragClickListener", "file name  " + GetOutFileENCName(filename));
                                //NeowineTask obj = new NeowineTask(Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc" + "/" +GetOutFileENCName(filename),  mFileMag.getCurrentDir() + "/"+GetOutFileDECName(filename),"0000000000000000",Dorcatype.DecSSL, 0);
                                m_CurrentFile = str0;


                                Log.d("DragClickListener", "m_CurrentFile  " + m_CurrentFile);
                                NeowineTask obj = new NeowineTask(temp, filepath + "/" + GetOutFileDECName(filename), "0000000000000000", Dorcatype.DecSSL, 0);

                                //System.out.println("filepath: ");
                                obj.execute();

                                Log.d("DragClickListener", "Remove Data success");
                                //m_myAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                        Log.d("DragClickListener", "End");

                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        start_cypher = 0;
                        start_grid = 0;
                        Log.d("DragClickListener", "grid ACTION_DRAG_ENDED  start_grid = 0;");
                        Log.d("DragClickListener", "grid ACTION_DRAG_ENDED  start_cypher = 0;");
                        //v.setBackground(normalShape); // go back to normal shape

                    default:
                        break;
                }
                return true;
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String item = mHandler.getData(position);
                boolean multiSelect = FALSE;
                Log.d("FILE_MGR   ", item);
                File file = new File(mFileMag.getCurrentDir() + "/" + item);
                if (file.isDirectory()) {
                    mFileMag.ListAllsubForder(mFileMag.getCurrentDir() + "/" + item);
                }
                Log.d("FILE_MGR_PATH", mFileMag.GetPathList());
                if (file.isDirectory())
                    txTitle.setText(item);

                String item_ext = null;

                try {
                    item_ext = item.substring(item.lastIndexOf("."), item.length());

                } catch (IndexOutOfBoundsException e) {
                    item_ext = "";
                }

                /*
                 * If the user has multi-select on, we just need to record the file
                 * not make an intent for it.
                 */
                if (multiSelect) {
                    mTable.addMultiPosition(position, file.getPath());

                } else {
                    if (file.isDirectory()) {
                        if (file.canRead()) {
                            mHandler.updateDirectory(mFileMag.getNextDir(item, false));
//                            mPathLabel.setText(mFileMag.getCurrentDir());
                            e4net_util.create_path_view(mFileMag.getCurrentDir(), getBaseContext(), (LinearLayout) findViewById(R.id.smode_path_layout));

                            /*set back button switch to true
                             * (this will be better implemented later)
                             */
                            if (!mUseBackKey)
                                mUseBackKey = true;

                        } else {
                            Toast.makeText(SmodeActivity.this, "Can't read folder due to permissions", LENGTH_SHORT).show();
                        }
                    }

                    /*music file selected--add more audio formats*/
                    else if (item_ext.equalsIgnoreCase(".mp3") ||
                            item_ext.equalsIgnoreCase(".m4a")) {

                        if (mReturnIntent) {
                            returnIntentResults(file);
                        } else {
                            /*Intent i = new Intent();
                            i.setAction(android.content.Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(file), "audio/*");
                            startActivity(i);*/
                            Intent audIntent = new Intent();
                            Uri audURI = FileProvider.getUriForFile(SmodeActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                            audIntent.putExtra(MediaStore.EXTRA_OUTPUT, audURI);
                            audIntent.setAction(android.content.Intent.ACTION_VIEW);
                            audIntent.setDataAndType(audURI, "audio/*");
                            audIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(audIntent);
                        }
                    }

                    /*photo file selected*/
                    else if (item_ext.equalsIgnoreCase(".jpeg") ||
                            item_ext.equalsIgnoreCase(".jpg") ||
                            item_ext.equalsIgnoreCase(".png") ||
                            item_ext.equalsIgnoreCase(".gif") ||
                            item_ext.equalsIgnoreCase(".tiff")) {

                        if (file.exists()) {
                            if (mReturnIntent) {
                                returnIntentResults(file);

                            } else {
                                Intent picIntent = new Intent();
                                Uri picURI = FileProvider.getUriForFile(SmodeActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                                //Uri picURI = FileProvider.getUriForFile(getApplicationContext(), "com.mydomain.fileprovider", file);
                                picIntent.putExtra(MediaStore.EXTRA_OUTPUT, picURI);
                                picIntent.setAction(android.content.Intent.ACTION_VIEW);
                                picIntent.setDataAndType(picURI, "image/*");
                                picIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(picIntent);
                            }
                        }
                    }

                    /*video file selected--add more video formats*/
                    else if (item_ext.equalsIgnoreCase(".m4v") ||
                            item_ext.equalsIgnoreCase(".3gp") ||
                            item_ext.equalsIgnoreCase(".wmv") ||
                            item_ext.equalsIgnoreCase(".mp4") ||
                            item_ext.equalsIgnoreCase(".ogg") ||
                            item_ext.equalsIgnoreCase(".wav")) {

                        if (file.exists()) {
                            if (mReturnIntent) {
                                returnIntentResults(file);

                            } else {
                                Intent vidIntent = new Intent();
                                Uri vidURI = FileProvider.getUriForFile(SmodeActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                                vidIntent.putExtra(MediaStore.EXTRA_OUTPUT, vidURI);
                                vidIntent.setAction(android.content.Intent.ACTION_VIEW);
                                vidIntent.setDataAndType(vidURI, "video/*");
                                vidIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(vidIntent);
                            }
                        }
                    }

                    /*zip file */
                    else if (item_ext.equalsIgnoreCase(".zip")) {

                        if (mReturnIntent) {
                            returnIntentResults(file);

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SmodeActivity.this);
                            AlertDialog alert;
                            mZippedTarget = mFileMag.getCurrentDir() + "/" + item;
                            CharSequence[] option = {"Extract here", "Extract to..."};

                            builder.setTitle("Extract");
                            builder.setItems(option, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            String dir = mFileMag.getCurrentDir();
                                            mHandler.unZipFile(item, dir + "/");
                                            break;

                                        case 1:
                                            mDetailLabel.setText("Holding " + item + " to extract");
                                            mHoldingZip = true;
                                            break;
                                    }
                                }
                            });

                            alert = builder.create();
                            alert.show();
                        }
                    }

                    /* gzip files, this will be implemented later */
                    else if (item_ext.equalsIgnoreCase(".gzip") ||
                            item_ext.equalsIgnoreCase(".gz")) {

                        if (mReturnIntent) {
                            returnIntentResults(file);

                        } else {
                            //TODO:
                        }
                    }

                    /*pdf file selected*/
                    else if (item_ext.equalsIgnoreCase(".pdf")) {

                        if (file.exists()) {
                            if (mReturnIntent) {
                                returnIntentResults(file);

                            } else {
                                /*Intent pdfIntent = new Intent();
                                pdfIntent.setAction(android.content.Intent.ACTION_VIEW);
                                pdfIntent.setDataAndType(Uri.fromFile(file),
                                        "application/pdf");*/

                                Intent pdfIntent = new Intent();
                                Uri pdfURI = FileProvider.getUriForFile(SmodeActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                                pdfIntent.putExtra(MediaStore.EXTRA_OUTPUT, pdfURI);
                                pdfIntent.setAction(android.content.Intent.ACTION_VIEW);
                                pdfIntent.setDataAndType(pdfURI, "application/pdf");
                                pdfIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);




                                try {
                                    startActivity(pdfIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(SmodeActivity.this, "Sorry, couldn't find a pdf viewer", LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    /*Android application file*/
                    else if (item_ext.equalsIgnoreCase(".apk")) {

                        if (file.exists()) {
                            if (mReturnIntent) {
                                returnIntentResults(file);

                            } else {
                                /*Intent apkIntent = new Intent();
                                apkIntent.setAction(android.content.Intent.ACTION_VIEW);
                                apkIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                startActivity(apkIntent);*/
                                Intent apkIntent = new Intent();
                                Uri apkURI = FileProvider.getUriForFile(SmodeActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                                apkIntent.putExtra(MediaStore.EXTRA_OUTPUT, apkURI);
                                apkIntent.setAction(android.content.Intent.ACTION_VIEW);
                                apkIntent.setDataAndType(apkURI, "application/pdf");
                                apkIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(apkIntent);
                            }
                        }
                    }

                    /* HTML file */
                    else if (item_ext.equalsIgnoreCase(".html")) {

                        if (file.exists()) {
                            if (mReturnIntent) {
                                returnIntentResults(file);

                            } else {
                               /* Intent htmlIntent = new Intent();
                                htmlIntent.setAction(android.content.Intent.ACTION_VIEW);
                                htmlIntent.setDataAndType(Uri.fromFile(file), "text/html");*/

                                Intent htmlIntent = new Intent();
                                Uri htmlURI = FileProvider.getUriForFile(SmodeActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                                htmlIntent.putExtra(MediaStore.EXTRA_OUTPUT, htmlURI);
                                htmlIntent.setAction(android.content.Intent.ACTION_VIEW);
                                htmlIntent.setDataAndType(htmlURI, "text/html");
                                htmlIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                try {
                                    startActivity(htmlIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(SmodeActivity.this, "Sorry, couldn't find a HTML viewer", LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    /* text file*/
                    else if (item_ext.equalsIgnoreCase(".txt")) {
                        if (file.exists()) {
                            if (mReturnIntent) {
                                returnIntentResults(file);

                            } else {
                                /*Intent txtIntent = new Intent();
                                txtIntent.setAction(android.content.Intent.ACTION_VIEW);
                                txtIntent.setDataAndType(Uri.fromFile(file), "text/plain");*/

                                Intent txtIntent = new Intent();
                                Uri txtURI = FileProvider.getUriForFile(SmodeActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                                txtIntent.putExtra(MediaStore.EXTRA_OUTPUT, txtURI);
                                txtIntent.setAction(android.content.Intent.ACTION_VIEW);
                                txtIntent.setDataAndType(txtURI, "text/plain");
                                txtIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(txtIntent);

                                try {
                                    startActivity(txtIntent);
                                } catch (ActivityNotFoundException e) {
                                    txtIntent.setType("text/*");
                                    startActivity(txtIntent);
                                }
                            }
                        }
                    }

                    /* generic intent */
                    else {
                        if (file.exists()) {
                            if (mReturnIntent) {
                                returnIntentResults(file);

                            } else {
                                Intent generic = new Intent();
                                generic.setAction(android.content.Intent.ACTION_VIEW);
                                generic.setDataAndType(Uri.fromFile(file), "text/plain");

                                try {
                                    startActivity(generic);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(SmodeActivity.this, "Sorry, couldn't find anything to open " + file.getName(), LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }


        });

        Dorca3_UI_Handler = new ActivityHandler();
        Dr3Functions = dorca3_function.getInstance();
        Dr3Functions.set_handler(Dorca3_UI_Handler);
        // Example of a call to a native method
        container = DrContainer.getInstance();


        //mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //CheckUSB_Device();
        //findViewById(R.id.button).setOnClickListener(
        //       new Button.OnClickListener() {
        //          public void onClick(View v) {
        //여기에 이벤트를 적어주세요

        if (container.mUsbManager == null) {
            Toast.makeText(getApplicationContext(), "container.mUsbManager is null", Toast.LENGTH_SHORT).show();
            // return;
        }

        if (container.mDevice == null) {
            Toast.makeText(getApplicationContext(), "container.mDevice is null", Toast.LENGTH_SHORT).show();
            // return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final int open_result;
                open_result = Dr3Functions.open(container.mUsbManager, container.mDevice);
                if (open_result < 0) {
                    runOnUiThread(
                            new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Dr3Functions.open error[" + open_result + "]", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    runOnUiThread(
                            new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Dr3Functions.open success", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                Dr3Functions.dorca_start();
                Dr3Functions.DorcaSetSpiClock(12000000);
                Dr3Functions.DorcaPowerReset();
                Dr3Functions.dorca_start_no_wakeup();
                Dr3Functions.DorcaSleepCheckLimit();
                /*
                byte[] input = new byte[32];
                byte[] key_local = new byte[32];

                byte[] buf_2_1FRM = {(byte)0xDC,(byte)0x95,(byte)0xC0,(byte)0x78,(byte)0xA2,(byte)0x40,(byte)0x89,(byte)0x89,(byte)0xAD,(byte)0x48,(byte)0xA2,(byte)0x14,(byte)0x92,(byte)0x84,(byte)0x20,(byte)0x87};
                int i = 0;
                for( i = 0; i < 32; i++) {
                    input[i] = 0;
                    key_local[i] = 0;
                    output[i] = 0;
                }
               // Dr3Functions.aes();
                //if(true)
                //return;
                Dr3Functions.AccessibleCheckAndBookingUSB();
                Dr3Functions.Dorca3CipherDecipher((byte)RG_ENC,(byte)1 AES,key_local,(byte)32,null,output,input,(byte)16,(byte)MODE_ECB,(byte)1);
                Dr3Functions.ReleaseBookingUSB();
                if(output[0] == buf_2_1FRM[0] && output[1] == buf_2_1FRM[1]) {
                    runOnUiThread(
                            new Runnable() {
                                public void run() { //

                                    Toast.makeText(getApplicationContext(), "USB_KEY TEST HW PASS", Toast.LENGTH_LONG).show();
                    Log.d("USB", "USB_KEY TEST HW PASS");
                                }
                            });

                }
                else {
                    runOnUiThread(
                            new Runnable() {
                                public void run() { //

                                    Toast.makeText(getApplicationContext(), "USB_KEY HW TEST FAIL", Toast.LENGTH_LONG).show();
                                    Log.d("USB", "USB_KEY TEST FAIL");
                                }
                            });
                }
                */

/*              if( Dr3Functions.SHA_1Frame_TEST() == 0)
                {
                    runOnUiThread(
                            new Runnable() {
                                public void run() { //
                                    Toast.makeText(getApplicationContext(), "smode activity SUCESS", Toast.LENGTH_SHORT).show();

                                }
                            });
                }
                else {
                    runOnUiThread(
                            new Runnable() {
                                public void run() { //
                                    Toast.makeText(getApplicationContext(), "smode activity FAIL", Toast.LENGTH_SHORT).show();

                                }
                            });
                }
*/
            }
        }).start();
        return;
    /*
        byte[] input = new byte[32];
        byte[] key_local = new byte[32];

        byte[] buf_2_1FRM = {(byte)0xDC,(byte)0x95,(byte)0xC0,(byte)0x78,(byte)0xA2,(byte)0x40,(byte)0x89,(byte)0x89,(byte)0xAD,(byte)0x48,(byte)0xA2,(byte)0x14,(byte)0x92,(byte)0x84,(byte)0x20,(byte)0x87};
        int i = 0;
        for( i = 0; i < 32; i++) {
            input[i] = 0;
            key_local[i] = 0;
            output[i] = 0;
        }
            JniBrige.AES_CIPHER(input, output, key_local);
        if(output[0] == buf_2_1FRM[0] && output[1] == buf_2_1FRM[1]) {
            Toast.makeText(getApplicationContext(), "USB_KEY TEST PASS", Toast.LENGTH_LONG).show();
            Log.d("USB", "USB_KEY TEST PASS");
        }
        else {
            Toast.makeText(getApplicationContext(), "USB_KEY TEST FAIL", Toast.LENGTH_LONG).show();
            Log.d("USB", "USB_KEY TEST FAIL");
        }
    */

    }
    //    }
    //);

    class ActivityHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case dorca3_activity_message_ids.MESSAGE_SEND_TEST:
                    String str = (String) msg.obj;
                    UpdateDebugLog(str, true);
                    break;
                case dorca3_activity_message_ids.MESSAGE_SEND_BYTE_DUMP:
                    byte[] data = (byte[]) msg.obj;
                    UpdateDebugLog(hexdump.dumpHexString(data) + "\n", false);
                    break;
                case dorca3_activity_message_ids.MESSAGE_SEND_SHOW_SPI_CLOCK:
                    String str_spi_clock = format("%d", msg.arg1);
                    UpdateDebugLog("Get Spi Clock [" + str_spi_clock + "]\n", true);

                    break;
                case dorca3_activity_message_ids.MESSAGE_SEND_TEST_MENU_RESULT:
                    String TestName = (String) msg.obj;
                    int Result = msg.arg1;
                    Toast.makeText(getApplicationContext(), TestName + " Result " + Result, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnTopNav2:
                e4net_util.intent(SmodeActivity.this, Main.class);
                break;

            case R.id.btnTopNav3:
                e4net_util.intent(SmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, mFileMag.getCurrentDir());
                break;

            case R.id.btnSideMenu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.btnHome:
                e4net_util.intent(SmodeActivity.this, Main.class);

            //   case R.id.btn_smode_bot_home:
            //        e4net_util.intent(SmodeActivity.this, Main.class);
            //        break;

      /*      case R.id.btn_smode_bot_local:
                intent = getIntent();
                intent.setAction(ACTION_WIDGET);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("folder", Environment.getExternalStorageDirectory().toString());
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;

            case R.id.btn_smode_bot_sort:
                mTable.notifyDataSetChanged();
                grid.setAdapter(mTable);
                Toast.makeText(getApplicationContext(), "Sorting ...", LENGTH_SHORT).show();
                break;
*/
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
        Log.d(e4net_util.e4_tag(), "POSITION : " + position);
        switch (position) {
            case 1:
                //e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory().toString());
                e4net_util.intent(SmodeActivity.this, Main.class);
                break;
            case 2:
                e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, Environment.getRootDirectory().toString());
                break;
            case 3:
                e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                break;
            case 4:
                e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory().toString());
                break;
            case 5:
                File ext_file = new File(System.getenv("SECONDARY_STORAGE"));
                String[] ext_file_list = ext_file.list();
                if ((ext_file_list != null) && (ext_file_list.length > 1)) {
                    e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, ext_file.toString());
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SmodeActivity.this);
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
                //e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
                e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, pic_dir);
                break;
            case 8:
                //e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString());
                e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, mov_dir);
                break;
            case 9:
                //e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString());
                e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, mus_dir);
                break;
            case 10:
                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
                boolean isPresent = true;
                if (!docsFolder.exists()) {
                    isPresent = docsFolder.mkdir();
                }
                e4net_util.intent(SmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory() + "/Documents");
                break;
        }
    }

    ////++++ Added by Mason, 20151008
    protected void createDorcaPath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File[] files = getExternalFilesDirs(null);
            if ((files.length > 1) && (files[1] != null)) {
                Log.i("MAIN", "Dorca path : " + files[1].getAbsolutePath());
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(SmodeActivity.this);
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("location", mFileMag.getCurrentDir());
    }

    /*(non Java-Doc)
     * Returns the file that was selected to the intent that
     * called this activity. usually from the caller is another application.
     */
    private void returnIntentResults(File data) {
        mReturnIntent = false;

        Intent ret = new Intent();
        ret.setData(Uri.fromFile(data));
        setResult(RESULT_OK, ret);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences.Editor editor = mSettings.edit();
        boolean check;
        boolean thumbnail;
        int color, sort, space;

        /* resultCode must equal RESULT_CANCELED because the only way
         * out of that activity is pressing the back button on the phone
         * this publishes a canceled result code not an ok result code
         */
        if (requestCode == SETTING_REQ && resultCode == RESULT_CANCELED) {
            //save the information we get from settings activity
            check = data.getBooleanExtra("HIDDEN", false);
            thumbnail = data.getBooleanExtra("THUMBNAIL", true);
            color = data.getIntExtra("COLOR", -1);
            sort = data.getIntExtra("SORT", 0);
            space = data.getIntExtra("SPACE", View.VISIBLE);

            editor.putBoolean(PREFS_HIDDEN, check);
            editor.putBoolean(PREFS_THUMBNAIL, thumbnail);
            editor.putInt(PREFS_COLOR, color);
            editor.putInt(PREFS_SORT, sort);
            editor.putInt(PREFS_STORAGE, space);
            editor.commit();

            mFileMag.setShowHiddenFiles(check);
            mFileMag.setSortType(sort);
            mHandler.setTextColor(color);
            mHandler.setShowThumbnails(thumbnail);
//            mStorageLabel.setVisibility(space);
            mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
        }
    }

    /* ================Menus, options menu and context menu start here=================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_PASTE, 0, "Paste");
        menu.add(0, MENU_QUIT, 0, "Quit");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PASTE:
                Log.d(e4net_util.e4_tag(), "Paste [" + mSelectedListItem + "]");
                if (mHoldingFile && mCopiedTarget.length() > 1) {
                    Log.d(e4net_util.e4_tag(), "oldLocation : " + mCopiedTarget);
                    Log.d(e4net_util.e4_tag(), "newLocation : " + mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                    mHandler.copyFile(mCopiedTarget, mFileMag.getCurrentDir() + "/" + mSelectedListItem);

                    mDetailLabel.setText("");
                }

                mSelectedListItem = "";

                mHoldingFile = false;
                return true;

            case MENU_QUIT:
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);

        boolean multi_data = mHandler.hasMultiSelectData();
        AdapterView.AdapterContextMenuInfo _info = (AdapterView.AdapterContextMenuInfo) info;
        mSelectedListItem = mHandler.getData(_info.position);

        /* is it a directory and is multi-select turned off */
        if (mFileMag.isDirectory(mSelectedListItem) && !mHandler.isMultiSelected()) {
            menu.setHeaderTitle("Folder operations");
            menu.add(0, D_MENU_DELETE, 0, "Delete Folder");
            menu.add(0, D_MENU_RENAME, 0, "Rename Folder");
            menu.add(0, D_MENU_COPY, 0, "Copy Folder");
            menu.add(0, D_MENU_MOVE, 0, "Move(Cut) Folder");
            menu.add(0, D_MENU_ZIP, 0, "Zip Folder");
            menu.add(0, D_MENU_PASTE, 0, "Paste into folder").setEnabled(mHoldingFile ||
                    multi_data);
            menu.add(0, D_MENU_UNZIP, 0, "Extract here").setEnabled(mHoldingZip);

            /* is it a file and is multi-select turned off */
        } else if (!mFileMag.isDirectory(mSelectedListItem) && !mHandler.isMultiSelected()) {
            menu.setHeaderTitle("File Operations");
            menu.add(0, F_MENU_DELETE, 0, "Delete File");
            menu.add(0, F_ENCRYPT_SWXOR, 0, "Encrypt");
            menu.add(0, F_DECRYPT_SWXOR, 0, "Decrypt");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case D_MENU_DELETE:
            case F_MENU_DELETE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Warning ");
                builder.setIcon(R.drawable.warning);
                builder.setMessage("Deleting " + mSelectedListItem +
                        " cannot be undone. Are you sure you want to delete?");
                builder.setCancelable(false);

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mHandler.deleteFile(mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                    }
                });
                AlertDialog alert_d = builder.create();
                alert_d.show();
                return true;

            case D_MENU_RENAME:
                showDialog(D_MENU_RENAME);
                return true;


            case F_ENCRYPT_SWAES:
                showDialog(F_ENCRYPT_SWAES);
                return true;

            case F_DECRYPT_SWAES:
                showDialog(F_DECRYPT_SWAES);
                return true;

            case F_ENCRYPT_FW:
                showDialog(F_ENCRYPT_FW);
                return true;

            case F_DECRYPT_FW:
                showDialog(F_DECRYPT_FW);
                return true;
            case F_ENCRYPT_SWXOR:

                showDialog(F_ENCRYPT_SWXOR);
                return true;

            case F_DECRYPT_SWXOR:
                showDialog(F_DECRYPT_SWXOR);
                return true;


            case D_MENU_MOVE:
            case D_MENU_COPY:
                if (item.getItemId() == D_MENU_MOVE)
                    mHandler.setDeleteAfterCopy(true);

                mHoldingFile = true;

                mCopiedTarget = mFileMag.getCurrentDir() + "/" + mSelectedListItem;
                mDetailLabel.setText("Holding " + mSelectedListItem);
                return true;


            case D_MENU_PASTE:
                boolean multi_select = mHandler.hasMultiSelectData();

                if (multi_select) {
                    mHandler.copyFileMultiSelect(mFileMag.getCurrentDir() + "/" + mSelectedListItem);

                } else if (mHoldingFile && mCopiedTarget.length() > 1) {

                    mHandler.copyFile(mCopiedTarget, mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                    mDetailLabel.setText("");
                }

                mHoldingFile = false;
                return true;

            case D_MENU_ZIP:
                String dir = mFileMag.getCurrentDir();

                mHandler.zipFile(dir + "/" + mSelectedListItem);
                return true;

            case D_MENU_UNZIP:
                if (mHoldingZip && mZippedTarget.length() > 1) {
                    String current_dir = mFileMag.getCurrentDir() + "/" + mSelectedListItem + "/";
                    String old_dir = mZippedTarget.substring(0, mZippedTarget.lastIndexOf("/"));
                    String name = mZippedTarget.substring(mZippedTarget.lastIndexOf("/") + 1, mZippedTarget.length());

                    if (new File(mZippedTarget).canRead() && new File(current_dir).canWrite()) {
                        mHandler.unZipFileToDir(name, current_dir, old_dir);
//                        mPathLabel.setText(current_dir);
                        e4net_util.create_path_view(current_dir, getBaseContext(), (LinearLayout) findViewById(R.id.smode_path_layout));
                    } else {
                        Toast.makeText(this, "You do not have permission to unzip " + name,
                                LENGTH_SHORT).show();
                    }
                }

                mHoldingZip = false;
                mDetailLabel.setText("");
                mZippedTarget = "";
                return true;


            ////++++ Added by Mason, 20150916
            case F_IO_TEST:
                showDialog(F_IO_TEST);
                break;
            ////++++.
        }
        return false;
    }

    /* ================Menus, options menu and context menu end here=================*/

    String GetOutFileENCName(String InputFileName) {
        int index = 0;
        int extention = 0;
        String Name = "";
        String OutputName = "";
        String FileName = InputFileName;
        Log.d(e4net_util.e4_tag(), "===================GetOutFileENCName====================");
        Log.d(e4net_util.e4_tag(), "FileName " + FileName);
        index = FileName.lastIndexOf('.');
        String Extension = FileName.substring(index + 1);
        Log.d(e4net_util.e4_tag(), "Extension " + Extension);
        if (index == -1) {
            Name = FileName;
            OutputName = Name + "_ENC";
            return OutputName;
        } else {
            Name = FileName.substring(0, index);

            Log.d(e4net_util.e4_tag(), "Name " + Name);
            OutputName = Name + "_ENC." + Extension;
            Log.d(e4net_util.e4_tag(), "OutputName " + OutputName);
            return OutputName;
        }
    }

    String GetOutFileDECName(String InputFileName) {
        int index = 0;
        int extention = 0;
        String OutputName = "";
        String Name = "";
        String FileName = InputFileName;
        Log.d(e4net_util.e4_tag(), "====================GetOutFileDECName===================");
        Log.d(e4net_util.e4_tag(), "FileName " + FileName);
        index = FileName.lastIndexOf('.');
        String Extension = FileName.substring(index + 1);
        if (Extension == ".enc") {
            Extension = "";
        }
        Log.d(e4net_util.e4_tag(), "Extension " + Extension);
        if (index == -1) {
            Name = FileName;
            OutputName = Name;//+ "_DEC";
            return OutputName;
        } else {
            Name = FileName.substring(0, index);
        }
        Log.d(e4net_util.e4_tag(), "Name " + Name);
        //OutputName = Name + "_DEC." + Extension;
        OutputName = Name.substring(0, index - 4) + "." + Extension;
        Log.d(e4net_util.e4_tag(), "GetOutFileDECName " + OutputName);
        return OutputName;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog = new Dialog(SmodeActivity.this);

        switch (id) {
//            case MENU_MKDIR:
//                dialog.setContentView(R.layout.input_layout);
//                dialog.setTitle("Create New Directory");
//                dialog.setCancelable(false);
//
//                ImageView icon = (ImageView) dialog.findViewById(R.id.input_icon);
//                icon.setImageResource(R.drawable.newfolder);
//
//                TextView label = (TextView) dialog.findViewById(R.id.input_label);
//                label.setText(mFileMag.getCurrentDir());
//                final EditText input = (EditText) dialog.findViewById(R.id.input_inputText);
//
//                Button cancel = (Button) dialog.findViewById(R.id.input_cancel_b);
//                Button create = (Button) dialog.findViewById(R.id.input_create_b);
//
//                create.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        if (input.getText().length() > 1) {
//                            if (mFileMag.createDir(mFileMag.getCurrentDir() + "/", input.getText().toString()) == 0)
//                                Toast.makeText(SmodeActivity.this,
//                                        "Folder " + input.getText().toString() + " created",
//                                        Toast.LENGTH_LONG).show();
//                            else
//                                Toast.makeText(SmodeActivity.this, "New folder was not created", LENGTH_SHORT).show();
//                        }
//
//                        dialog.dismiss();
//                        String temp = mFileMag.getCurrentDir();
//                        mHandler.updateDirectory(mFileMag.getNextDir(temp, true));
//                    }
//                });
//                cancel.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//                break;
            case D_MENU_RENAME:
                dialog.setContentView(R.layout.input_layout);
                dialog.setCancelable(false);

                final EditText rename_input = (EditText) dialog.findViewById(R.id.input_inputText);

                Button rename_cancel = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button rename_create = (Button) dialog.findViewById(R.id.input_create_b);
                rename_create.setText("Rename");

                rename_create.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (rename_input.getText().length() < 1)
                            dialog.dismiss();

                        if (mFileMag.renameTarget(mFileMag.getCurrentDir() + "/" + mSelectedListItem, rename_input.getText().toString()) == 0) {
                            Toast.makeText(SmodeActivity.this, mSelectedListItem + " was renamed to " + rename_input.getText().toString(),
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(SmodeActivity.this, mSelectedListItem + " was not renamed", Toast.LENGTH_LONG).show();

                        dialog.dismiss();
                        String temp = mFileMag.getCurrentDir();
                        mHandler.updateDirectory(mFileMag.getNextDir(temp, true));
                    }
                });
                rename_cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case F_ENCRYPT_SWAES:
                dialog.setContentView(R.layout.input_layout_hybird);
                dialog.setTitle("ENCRYPT " + mSelectedListItem);
                dialog.setCancelable(false);

                ImageView pw_icon = (ImageView) dialog.findViewById(R.id.input_icon);
                pw_icon.setImageResource(R.drawable.rename);

                TextView encrypt_label = (TextView) dialog.findViewById(R.id.input_label);
                encrypt_label.setText(mFileMag.getCurrentDir());

                Button encrypt_cancel = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button encrypt_create = (Button) dialog.findViewById(R.id.input_create_b);
                encrypt_create.setText("Encrypt");
                final EditText password_input = (EditText) dialog.findViewById(R.id.input_inputText);
                final EditText encrypt_hybirdstep_swaes = (EditText) dialog.findViewById(R.id.edit_hybirdstep);
                encrypt_create.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (password_input.getText().length() > 16)
                            dialog.dismiss();
                        Log.i(e4net_util.e4_tag(), mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                        //full to 16bytes key
                        String input_key = "";
                        if (password_input.getText().length() != 16) {
                            Log.i(e4net_util.e4_tag(), "key is NOT 16 bytes");
                            int len = password_input.getText().length();
                            input_key = password_input.getText().toString();
                            //full to 16bytes
                            for (int i = len; i < 16; i++)
                                input_key += "0";
                            Log.i(e4net_util.e4_tag(), "now input_key is: " + input_key);
                        } else {
                            Log.i(e4net_util.e4_tag(), "key is 16 bytes");
                            input_key = password_input.getText().toString();
                        }
                        /*
                        Dorca20EncryptfileSWAES(mFileMag.getCurrentDir()+"/"+mSelectedListItem, mFileMag.getCurrentDir()+"/"+mSelectedListItem+".ENC", input_key);
    					//update folder content
    					String temp = mFileMag.getCurrentDir();
    					mHandler.updateDirectory(mFileMag.getNextDir(temp, true));
    					*/
                        NeowineTask obj = null;

                        //obj = new NeowineTask(mFileMag.getCurrentDir() + "/" + mSelectedListItem, Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc" + "/" + GetOutFileENCName(mSelectedListItem), input_key, Dorcatype.EncSWAES, Integer.parseInt(encrypt_hybirdstep_swaes.getText().toString()));
                        obj = new NeowineTask(mFileMag.getCurrentDir() + "/" + mSelectedListItem, mFileMag.getCurrentDir() + "/" + GetOutFileENCName(mSelectedListItem), input_key, Dorcatype.EncSWAES, Integer.parseInt(encrypt_hybirdstep_swaes.getText().toString()));

                        obj.execute();
                        dialog.dismiss();
                    }
                });
                encrypt_cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case F_DECRYPT_SWAES:
                dialog.setContentView(R.layout.input_layout_hybird);
                dialog.setTitle("DECRYPT " + mSelectedListItem);
                dialog.setCancelable(false);

                ImageView pw_icon_de = (ImageView) dialog.findViewById(R.id.input_icon);
                pw_icon_de.setImageResource(R.drawable.rename);

                TextView decrypt_label = (TextView) dialog.findViewById(R.id.input_label);
                decrypt_label.setText(mFileMag.getCurrentDir());

                Button decrypt_cancel = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button decrypt_create = (Button) dialog.findViewById(R.id.input_create_b);
                decrypt_create.setText("Decrypt");
                final EditText password_input_de = (EditText) dialog.findViewById(R.id.input_inputText);
                final EditText decrypt_hybirdstep_swaes = (EditText) dialog.findViewById(R.id.edit_hybirdstep);
                decrypt_create.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (password_input_de.getText().length() > 16)
                            dialog.dismiss();
                        Log.i(e4net_util.e4_tag(), mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                        //check input file is *.ENC
                        String input_file_string = Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc" + mSelectedListItem;
                        if (input_file_string.contains(".ENC")) {
                            String[] output_file_string = input_file_string.split("\\.");
                            String outputfilestring = mFileMag.getCurrentDir() + "/";
                            Log.i(e4net_util.e4_tag(), "output_file_string.length: " + output_file_string.length);
                            for (String s : output_file_string) {
                                Log.i(e4net_util.e4_tag(), "output_file_string: " + s);
                            }
                            //full to 16bytes key
                            String input_key = "";
                            if (password_input_de.getText().length() != 16) {
                                Log.i(e4net_util.e4_tag(), "key is NOT 16 bytes");
                                int len = password_input_de.getText().length();
                                input_key = password_input_de.getText().toString();
                                //full to 16bytes
                                for (int i = len; i < 16; i++)
                                    input_key += "0";
                                Log.i(e4net_util.e4_tag(), "now input_key is: " + input_key);
                            } else {
                                Log.i(e4net_util.e4_tag(), "key is 16 bytes");
                                input_key = password_input_de.getText().toString();
                            }
                            //NeowineTask obj = new NeowineTask(input_file_string, outputfilestring + "_Dec." + output_file_string[1], input_key, Dorcatype.DecSWAES, Integer.parseInt(decrypt_hybirdstep_swaes.getText().toString()));
                            NeowineTask obj = new NeowineTask(input_file_string, output_file_string[0] + "_Dec." + output_file_string[1], input_key, Dorcatype.DecSWAES, Integer.parseInt(decrypt_hybirdstep_swaes.getText().toString()));

                            obj.execute();
                        }
                        dialog.dismiss();
                    }
                });
                decrypt_cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case F_ENCRYPT_SWXOR:
                dialog.setContentView(R.layout.input_layout_hybird);
                dialog.setTitle("ENCRYPT " + mSelectedListItem);
                dialog.setCancelable(false);

                ImageView pw_iconXOR = (ImageView) dialog.findViewById(R.id.input_icon);
                pw_iconXOR.setImageResource(R.drawable.rename);

                TextView encrypt_labelXOR = (TextView) dialog.findViewById(R.id.input_label);
                encrypt_labelXOR.setText(mFileMag.getCurrentDir());

                Button encrypt_cancelXOR = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button encrypt_createXOR = (Button) dialog.findViewById(R.id.input_create_b);
                encrypt_createXOR.setText("Encrypt");
                final EditText password_inputXOR = (EditText) dialog.findViewById(R.id.input_inputText);
                final EditText encrypt_hybirdstep = (EditText) dialog.findViewById(R.id.edit_hybirdstep);
                encrypt_createXOR.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (password_inputXOR.getText().length() > 16)
                            dialog.dismiss();
                        Log.i(e4net_util.e4_tag(), mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                        //full to 16bytes key
                        String input_key = "";
                        if (password_inputXOR.getText().length() != 16) {
                            Log.i(e4net_util.e4_tag(), "key is NOT 16 bytes");
                            int len = password_inputXOR.getText().length();
                            input_key = password_inputXOR.getText().toString();
                            //full to 16bytes
                            for (int i = len; i < 16; i++)
                                input_key += "0";
                            Log.i(e4net_util.e4_tag(), "now input_key is: " + input_key);
                        } else {
                            Log.i(e4net_util.e4_tag(), "key is 16 bytes");
                            input_key = password_inputXOR.getText().toString();
                        }

                        NeowineTask obj = null;

                        obj = new NeowineTask(mFileMag.getCurrentDir() + "/" + mSelectedListItem, mFileMag.getCurrentDir() + "/" + GetOutFileENCName(mSelectedListItem), input_key, Dorcatype.EncSWXOR, Integer.parseInt(encrypt_hybirdstep.getText().toString()));
                        //obj = new NeowineTask(mFileMag.getCurrentDir() + "/" + mSelectedListItem, Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc" + "/" + GetOutFileENCName(mSelectedListItem), input_key, Dorcatype.EncSWXOR, Integer.parseInt(encrypt_hybirdstep.getText().toString()));

                        obj.execute();
                        dialog.dismiss();
                    }
                });
                encrypt_cancelXOR.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case F_DECRYPT_SWXOR:
                dialog.setContentView(R.layout.input_layout_hybird);
                dialog.setTitle("DECRYPT " + mSelectedListItem);
                dialog.setCancelable(false);

                ImageView pw_icon_deXOR = (ImageView) dialog.findViewById(R.id.input_icon);
                pw_icon_deXOR.setImageResource(R.drawable.rename);

                TextView decrypt_labelXOR = (TextView) dialog.findViewById(R.id.input_label);
                decrypt_labelXOR.setText(mFileMag.getCurrentDir());

                Button decrypt_cancelXOR = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button decrypt_createXOR = (Button) dialog.findViewById(R.id.input_create_b);
                decrypt_createXOR.setText("Decrypt");
                final EditText password_input_deXOR = (EditText) dialog.findViewById(R.id.input_inputText);
                final EditText decrypt_hybirdstep = (EditText) dialog.findViewById(R.id.edit_hybirdstep);
                decrypt_createXOR.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (password_input_deXOR.getText().length() > 16)
                            dialog.dismiss();
                        Log.i(e4net_util.e4_tag(), mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                        //check input file is *.ENC
                        String input_file_string = Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc" + "/" + mSelectedListItem;
                        {
                            String[] output_file_string = input_file_string.split("\\.");
                            String outputfilestring = mFileMag.getCurrentDir() + "/";
                            Log.i(e4net_util.e4_tag(), "output_file_string.length: " + output_file_string.length);
                            for (String s : output_file_string) {
                                Log.i(e4net_util.e4_tag(), "output_file_string: " + s);
                            }
                            //full to 16bytes key
                            String input_key = "";
                            if (password_input_deXOR.getText().length() != 16) {
                                Log.i(e4net_util.e4_tag(), "key is NOT 16 bytes");
                                int len = password_input_deXOR.getText().length();
                                input_key = password_input_deXOR.getText().toString();
                                //full to 16bytes
                                for (int i = len; i < 16; i++)
                                    input_key += "0";
                                Log.i(e4net_util.e4_tag(), "now input_key is: " + input_key);
                            } else {
                                Log.i(e4net_util.e4_tag(), "key is 16 bytes");
                                input_key = password_input_deXOR.getText().toString();
                            }

                            NeowineTask obj = null;
                            obj = new NeowineTask(input_file_string, GetOutFileDECName(input_file_string), input_key, Dorcatype.DecSWXOR, Integer.parseInt(decrypt_hybirdstep.getText().toString()));
                            //obj = new NeowineTask(input_file_string, outputfilestring + "_Dec." + output_file_string[1], input_key, Dorcatype.DecSWXOR, Integer.parseInt(decrypt_hybirdstep.getText().toString()));

                            obj.execute();
                        }
                        dialog.dismiss();
                    }
                });
                decrypt_cancelXOR.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case F_ENCRYPT_FW:
                dialog.setContentView(R.layout.input_layout);
                dialog.setTitle("ENCRYPT " + mSelectedListItem);
                dialog.setCancelable(false);

                ImageView pw_iconFW = (ImageView) dialog.findViewById(R.id.input_icon);
                pw_iconFW.setImageResource(R.drawable.rename);

                TextView encrypt_labelFW = (TextView) dialog.findViewById(R.id.input_label);
                encrypt_labelFW.setText(mFileMag.getCurrentDir());

                Button encrypt_cancelFW = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button encrypt_createFW = (Button) dialog.findViewById(R.id.input_create_b);
                encrypt_createFW.setText("Encrypt");
                final EditText password_inputFW = (EditText) dialog.findViewById(R.id.input_inputText);
                encrypt_createFW.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (password_inputFW.getText().length() > 16)
                            dialog.dismiss();
                        Log.i(e4net_util.e4_tag(), mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                        //full to 16bytes key
                        String input_key = "";
                        if (password_inputFW.getText().length() != 16) {
                            Log.i(e4net_util.e4_tag(), "key is NOT 16 bytes");
                            int len = password_inputFW.getText().length();
                            input_key = password_inputFW.getText().toString();
                            //full to 16bytes
                            for (int i = len; i < 16; i++)
                                input_key += "0";
                            Log.i(e4net_util.e4_tag(), "now input_key is: " + input_key);
                        } else {
                            Log.i(e4net_util.e4_tag(), "key is 16 bytes");
                            input_key = password_inputFW.getText().toString();
                        }

                        //NeowineTask obj = new NeowineTask(mFileMag.getCurrentDir() + "/" + mSelectedListItem, Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc" + "/"  + mSelectedListItem + ".ENC", input_key, Dorcatype.EncFW, 0);
                        NeowineTask obj = new NeowineTask(mFileMag.getCurrentDir() + "/" + mSelectedListItem, mFileMag.getCurrentDir() + "/" + mSelectedListItem + ".ENC", input_key, Dorcatype.EncFW, 0);

                        obj.execute();
                        dialog.dismiss();
                    }
                });
                encrypt_cancelFW.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case F_DECRYPT_FW:
                dialog.setContentView(R.layout.input_layout);
                dialog.setTitle("DECRYPT " + mSelectedListItem);
                dialog.setCancelable(false);

                ImageView pw_icon_deFW = (ImageView) dialog.findViewById(R.id.input_icon);
                pw_icon_deFW.setImageResource(R.drawable.rename);

                TextView decrypt_labelFW = (TextView) dialog.findViewById(R.id.input_label);
                decrypt_labelFW.setText(mFileMag.getCurrentDir());

                Button decrypt_cancelFW = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button decrypt_createFW = (Button) dialog.findViewById(R.id.input_create_b);
                decrypt_createFW.setText("Decrypt");
                final EditText password_input_deFW = (EditText) dialog.findViewById(R.id.input_inputText);
                decrypt_createFW.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (password_input_deFW.getText().length() > 16)
                            dialog.dismiss();

                        Log.i(e4net_util.e4_tag(), mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                        //check input file is *.ENC
                        String input_file_string = Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc" + mSelectedListItem;
                        if (input_file_string.contains(".ENC")) {

                            //String output_file_string = input_file_string.replace(".ENC", ".DEC");
                            String output_file_string = mFileMag.getCurrentDir() + "/" + mSelectedListItem.replace(".ENC", ".DEC");
                            Log.i(e4net_util.e4_tag(), "output_file_string: " + output_file_string);
                            ////++++.
                            //full to 16bytes key
                            String input_key = "";
                            if (password_input_deFW.getText().length() != 16) {
                                Log.i(e4net_util.e4_tag(), "key is NOT 16 bytes");
                                int len = password_input_deFW.getText().length();
                                input_key = password_input_deFW.getText().toString();
                                //full to 16bytes
                                for (int i = len; i < 16; i++)
                                    input_key += "0";
                                Log.i(e4net_util.e4_tag(), "now input_key is: " + input_key);
                            } else {
                                Log.i(e4net_util.e4_tag(), "key is 16 bytes");
                                input_key = password_input_deFW.getText().toString();
                            }

                            //NeowineTask obj = new NeowineTask(input_file_string, output_file_string, input_key, Dorcatype.DecFW, 0);
                            NeowineTask obj = new NeowineTask(input_file_string, output_file_string, input_key, Dorcatype.DecFW, 0);

                            ////++++.
                            obj.execute();
                        }
                        dialog.dismiss();
                    }
                });
                decrypt_cancelFW.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
//            case SEARCH_B:
//            case MENU_SEARCH:
//                dialog.setContentView(R.layout.input_layout);
//                dialog.setTitle("Search");
//                dialog.setCancelable(false);
//
//                ImageView searchIcon = (ImageView) dialog.findViewById(R.id.input_icon);
//                searchIcon.setImageResource(R.drawable.search);
//
//                TextView search_label = (TextView) dialog.findViewById(R.id.input_label);
//                search_label.setText("Search for a file");
//                final EditText search_input = (EditText) dialog.findViewById(R.id.input_inputText);
//
//                Button search_button = (Button) dialog.findViewById(R.id.input_create_b);
//                Button cancel_button = (Button) dialog.findViewById(R.id.input_cancel_b);
//                search_button.setText("Search");
//
//                search_button.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        String temp = search_input.getText().toString();
//
//                        if (temp.length() > 0)
//                            mHandler.searchForFile(temp);
//                        dialog.dismiss();
//                    }
//                });
//
//                cancel_button.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//
//                break;

            case F_IO_TEST:
                dialog.setContentView(R.layout.input_layout);
                dialog.setTitle("I/O Test");
                dialog.setCancelable(false);

                ImageView icon_iotest = (ImageView) dialog.findViewById(R.id.input_icon);
                icon_iotest.setImageResource(R.drawable.rename);

                Button iotest_cancelFW = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button iotest_createFW = (Button) dialog.findViewById(R.id.input_create_b);
                iotest_createFW.setText("Go");
                final EditText editRepeat = (EditText) dialog.findViewById(R.id.input_inputText);
                iotest_createFW.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int repeat = Integer.parseInt(editRepeat.getText().toString());
                        Log.i(e4net_util.e4_tag(), "Repeat: " + repeat);

                        NeowineTask obj = new NeowineTask(mFileMag.getCurrentDir() + "/iotest.tmp", null, null, Dorcatype.IOTest, repeat);
                        obj.execute();

                        dialog.dismiss();
                    }
                });
                iotest_cancelFW.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {

        switch (id) {
            case D_MENU_RENAME: {
                dialog.setTitle("Rename " + mSelectedListItem);

                ImageView rename_icon = (ImageView) dialog.findViewById(R.id.input_icon);
                rename_icon.setImageResource(R.drawable.rename);

                TextView rename_label = (TextView) dialog.findViewById(R.id.input_label);
                rename_label.setText(mFileMag.getCurrentDir());

                TextView rename_extension = (TextView) dialog.findViewById(R.id.input_extension);
                rename_extension.setText(mSelectedListItem.substring(mSelectedListItem.lastIndexOf("."), mSelectedListItem.length()));

                EditText rename_input = (EditText) dialog.findViewById(R.id.input_inputText);
                rename_input.setText("");

                break;
            }
            default:
                break;
        }

        super.onPrepareDialog(id, dialog, args);
    }

    /*
     * (non-Javadoc)
     * This will check if the user is at root directory. If so, if they press back
     * again, it will close the application.
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        String current = mFileMag.getCurrentDir();

        if (keycode == KeyEvent.KEYCODE_SEARCH) {
//            showDialog(SEARCH_B);
            return true;

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && !current.equals("/")) {
            mHandler.updateDirectory(mFileMag.getPreviousDir());
            e4net_util.create_path_view(mFileMag.getCurrentDir(), getBaseContext(), (LinearLayout) findViewById(R.id.smode_path_layout));

            String[] tmp = mFileMag.getCurrentDir().split("/");
            if (tmp.length > 0)
                txTitle.setText(tmp[tmp.length - 1]);
            else
                txTitle.setText("/");
            return true;

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && current.equals("/")) {
            Toast.makeText(SmodeActivity.this, "Press back again to home.", LENGTH_SHORT).show();

            if (mHandler.isMultiSelected()) {
                mTable.killMultiSelect(true);
                Toast.makeText(SmodeActivity.this, "Multi-select is now off", LENGTH_SHORT).show();
            }

            mUseBackKey = false;
            e4net_util.create_path_view(mFileMag.getCurrentDir(), getBaseContext(), (LinearLayout) findViewById(R.id.smode_path_layout));

            String[] tmp = mFileMag.getCurrentDir().split("/");
            if (tmp.length > 0)
                txTitle.setText(tmp[tmp.length - 1]);

            return false;

        } else if (keycode == KeyEvent.KEYCODE_BACK && !mUseBackKey && current.equals("/")) {
            e4net_util.intent(SmodeActivity.this, Main.class);
            return false;
        }
        return false;
    }

    /*
     * getExternalFilesDir will create file in primary external storage
     * (usually is main storage's FAT partition)
     *
     * */
    @TargetApi(19)
    void createExternalStoragePrivateFile(Context context) {

        // Create a path where we will place our private file on external
        // storage.
        File[] sdcards = context.getExternalFilesDirs(null);
        Log.i(e4net_util.e4_tag(), "sdcards.length: " + sdcards.length);
        for (File sdcard : sdcards) {
            if (sdcard == null) {
                Log.i(e4net_util.e4_tag(), "sdcard is null");
                return;
            } else
                Log.i(e4net_util.e4_tag(), sdcard.getPath());
        }
        File file;
        if (sdcards.length < 2)//for Xiaomi phone
            return;
        else {
            file = new File(sdcards[1], "neowine.txt");//because i already know sd path is stored in array element 1.
            try {
                OutputStream os = new FileOutputStream(file);
                byte[] data = new byte[512];
                String welcome = "Welcome Neowine ";
                data = welcome.getBytes("UTF-8");
                os.write(data);//always write to stream position 0.
                os.close();
                Log.e(e4net_util.e4_tag(), "HELLO " + file);
            } catch (IOException e) {
                // Unable to create file, likely because external storage is
                // not currently mounted.
                Log.w("ExternalStorage", "Error writing " + file, e);
            }
        }
    }

    public boolean delDir(File dir) {
        if (dir == null || !dir.exists() || dir.isFile()) {
            return false;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                delDir(file);// recursive
            }
        }
        dir.delete();
        return true;
    }

    enum Dorcatype {EncSSL, DecSSL, EncSWAES, DecSWAES, EncFW, DecFW, EncSWXOR, DecSWXOR, IOTest}

    class NeowineTask extends AsyncTask<Void, Void, Void> {
        private String inputfilepath;
        private String outputfilepath;
        private String key;
        private Dorcatype todocase;
        private int hybirdstep;
        private String device = "/dev/spidev0.0";


        ProgressDialog pDialog;

        public NeowineTask(String input, String output, String Key, Dorcatype Todocase, int Hybirdstep) {
            this.inputfilepath = input;
            this.outputfilepath = output;
            this.key = Key;
            this.todocase = Todocase;
            this.hybirdstep = Hybirdstep;


        }

        protected void onPreExecute() {
            //create and display your alert here
            pDialog = ProgressDialog.show(SmodeActivity.this, "Please wait...", "Encrypt/Decrypt data ...", true);
        }

        protected Void doInBackground(Void... unused) {
            Log.d(LOG_TAG, "inputfilepath " + inputfilepath + " outputfilepath " + outputfilepath);

            //URL url = getClass().getResource(inputfilepath);

            byte[] input = new byte[32];


            byte[] buf_2_1FRM = {(byte) 0xDC, (byte) 0x95, (byte) 0xC0, (byte) 0x78, (byte) 0xA2, (byte) 0x40, (byte) 0x89, (byte) 0x89, (byte) 0xAD, (byte) 0x48, (byte) 0xA2, (byte) 0x14, (byte) 0x92, (byte) 0x84, (byte) 0x20, (byte) 0x87};
            int i = 0;

            File file = new File(inputfilepath);


            IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);


            switch (todocase) {

                case EncSSL:


                    final long actTimeEncSSL;
                    final long startEncSSL = System.currentTimeMillis();
                    final long actTimeFileEncSSL;
                    long TimeEncSSL;
                    long TotalEncSSL = 0;
                    Toast toast;
                    byte[] key_local = new byte[32];

                    for (i = 0; i < 32; i++) {
                        input[i] = 0;

                        output[i] = 0;
                    }

                    Dr3Functions = dorca3_function.getInstance();

                    if (Dr3Functions.AccessibleCheckAndBookingUSB() == false) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Dr3Functions.AccessibleCheckAndBookingUSB() == false", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //   return (null);

                    }

                    byte gAES_KEY_SEED[] = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f, (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f};
                    byte KEY[] = {(byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff};
                    byte testinput[] = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f, (byte) 0x10};
                    byte outSHA[] = new byte[32];
                    //byte outSHA[] = {(byte)0x00,(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x09,(byte)0x0a,(byte)0x0b,(byte)0x0c,(byte)0x0d,(byte)0x0e,(byte)0x0f,(byte)0x10,(byte)0x11,(byte)0x12,(byte)0x13,(byte)0x14,(byte)0x15,(byte)0x16,(byte)0x17,(byte)0x18,(byte)0x19,(byte)0x1a,(byte)0x1b,(byte)0x1c,(byte)0x1d,(byte)0x1e,(byte)0x1f};
                    //byte ENCoutput[] = {(byte)0x04,(byte)0xDF,(byte)0x55,(byte)0xDF,(byte)0xA7,(byte)0xF1,(byte)0xC4,(byte)0x6C,(byte)0x3F,(byte)0x7F,(byte)0xE0,(byte)0xD6,(byte)0x96,(byte)0xD5,(byte)0x2E,(byte)0xB2,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};

                    byte[] ENCoutput = new byte[32];


                    String strKey = new String();

                    Dr3Functions.Dorca3CipherDecipher((byte) RG_ENC, (byte) 1, gAES_KEY_SEED, (byte) 32, null, test_output, KEY, (byte) 16, (byte) MODE_ECB, (byte) 1);


                    Dr3Functions.send_string_to_main("\nDORCA_KEY_TEST: " + hexdump.toHexString(test_output) + "\n");

                    for (byte ii = 0; ii < 4; ii++) {
                        for (byte jj = 0; jj < 2; jj++) {
                            Dr3Functions.RSSHAReadIdx(ii, outSHA);
                            //Dr3Functions.send_string_to_main("Root Serial Test: " + hexdump.toHexString(outSHA) + "\n");
                        }
                    }


                    byte[] encKey = new byte[32];

                    String Keysha = testSHA256(file.getParent());


                    /*for (byte k = 0; k < 32; k++) {
                        encKey[k] = (byte)((int)Keysha.getBytes()[k] ^ (int)outSHA[k]);
                    }*/

                    for (byte k = 0; k < 32; k++) {
                        encKey[k] = outSHA[k];
                    }

                    Dr3Functions.send_string_to_main("encKey: " + hexdump.toHexString(encKey));

                    //Dr3Functions.Dorca3CipherDecipher((byte)RG_ENC,(byte)1,key_local,(byte)32,null,output,Keysha.getBytes(),(byte)16,(byte)MODE_ECB,(byte)1);
                    Dr3Functions.Dorca3CipherDecipher((byte) RG_ENC, (byte) 1, encKey, (byte) 32, null, ENCoutput, outSHA, (byte) 16, (byte) MODE_ECB, (byte) 1);
                    Dr3Functions.ReleaseBookingUSB();

                    Dr3Functions.send_string_to_main("output: " + hexdump.toHexString(ENCoutput) + "\n");
                    String EncKey = new String(ENCoutput);
                    for (i = 0; i < 16; i++) {
                        strKey += String.format("%02x", ENCoutput[i]);
                    }

                    for (i = 0; i < 32; i++) {
                        input[i] = 0;
                        output[i] = 0;
                        key_local[i] = 0;
                    }

                    JniBrige.AES_CIPHER(input, output, strKey.getBytes());

                    System.out.println("EncKey " + strKey);
                    System.out.println("outtest: " + hexdump.toHexString(output));

                    Dr3Functions.send_string_to_main("ENCKEY" + strKey);

                    if (file != null && file.isFile()) {
                        ActivityCompat.requestPermissions(SmodeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        actTimeEncSSL = JniBrige.OPENSSLENC(inputfilepath, outputfilepath, strKey, device);
                        Dr3Functions.send_string_to_main("ENCKEY" + strKey);

                        Log.e(LOG_TAG, "file: " + file);

                        Log.i(LOG_TAG, "ENCSSL");
                        final long endEncSSL = System.currentTimeMillis();
                        // ciphercounter ^
                        runOnUiThread(new Runnable() {
                            public void run() {
                                {
                                    long time = endEncSSL - startEncSSL;
                                    String temp = String.valueOf(time);
                                    String temp2 = String.valueOf(actTimeEncSSL);
                                    Context context = getApplicationContext();
                                    CharSequence text = "total: " + temp + "ms" + " : " + "OP:" + temp2 + "ms";
                                    Log.i(LOG_TAG, "DecSPI time " + text);
                                    int duration = Toast.LENGTH_LONG;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                            }
                        });
                        break;
                    } else {
                        Log.e(LOG_TAG, "directory info");

                        System.out.println("encrypting");

                        JniBrige.OPENSSLENC(inputfilepath, inputfilepath, strKey, device);
                        Dr3Functions.send_string_to_main("ENCKEY" + strKey);
                        File[] files = new File(inputfilepath).listFiles();

                        Log.e(LOG_TAG, "directory");
                        File out = new File(outputfilepath);

                        out.mkdirs();

                        for (File f : files) {

                            TimeEncSSL = OPENSSLENC(f.getAbsolutePath(), out.getAbsolutePath() + "/" + f.getName(), strKey, device);


                            Log.e(LOG_TAG, f.getAbsolutePath());
                            Log.e(LOG_TAG, out.getAbsolutePath() + "/" + f.getName());
                            TotalEncSSL += TimeEncSSL;

                        }
                        actTimeFileEncSSL = TotalEncSSL;


                        if (out.isDirectory()) {
                            String[] children = out.list();
                            for (int j = 0; j < children.length; j++) {
                                new File(out, children[j]).delete();
                            }
                            out.delete();
                        }
                        runOnUiThread(new Runnable() {
                            final long endEncSSL = System.currentTimeMillis();

                            public void run() {
                                {
                                    long time = endEncSSL - startEncSSL;
                                    String temp = String.valueOf(time);
                                    String temp2 = String.valueOf(actTimeFileEncSSL);
                                    Context context = getApplicationContext();
                                    CharSequence text = "total: " + temp + "ms" + " : " + "OP:" + temp2 + "ms";
                                    Log.i(LOG_TAG, "DecSPI time " + text);
                                    int duration = Toast.LENGTH_LONG;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                            }
                        });
                    }
                    break;


                case DecSSL:

                    final long actTimeDecSSL;
                    final long startDecSSL = System.currentTimeMillis();
                    final long actTimeFileDecSSL;
                    long TimeDecSSL;
                    long TotalDecSSL = 0;
                    byte[] DECoutput = new byte[32];


                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "before AccessibleCheckAndBookingUSB", Toast.LENGTH_LONG);
                        }
                    });
                    Dr3Functions = dorca3_function.getInstance();

                    if (Dr3Functions.AccessibleCheckAndBookingUSB() == false) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Dr3Functions.AccessibleCheckAndBookingUSB() == false", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //return (null);

                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "After Dorca3CipherDecipher", Toast.LENGTH_LONG).show();

//                            Toast.makeText(getApplicationContext(),
//                                    String.format("0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x",output[0],output[1],output[2],output[3],output[4],output[5],output[6],output[7],output[8],output[9],output[10],output[11],output[12],output[13],output[14],output[15]), Toast.LENGTH_LONG).show();
                        }
                    });
                    String strKeyDec = new String();

                    Log.e(LOG_TAG, "check");
                    String Keyshadec = testSHA256(file.getParent());
                    System.out.println("holla: " + file.getParent());

                    Log.d("SHA", file.getParent());

                    byte outSHAdec[] = new byte[32];

                    for (byte ii = 0; ii < 4; ii++) {
                        for (byte jj = 0; jj < 2; jj++) {
                            Dr3Functions.RSSHAReadIdx(ii, outSHAdec);
                             //Dr3Functions.send_string_to_main("Root Serial Test: " + hexdump.toHexString(outSHAdec) + "\n");
                        }
                    }

                    byte[] decKey = new byte[32];

                    /*for (byte k = 0; k < 32; k++) {
                        decKey[k] = (byte)((int)Keyshadec.getBytes()[k] ^ (int)outSHAdec[k]);
                    }*/

                    for (byte k = 0; k < 32; k++) {
                        decKey[k] = outSHAdec[k];
                    }


                    //Dr3Functions.Dorca3CipherDecipher((byte)RG_ENC,(byte)1,decKey,(byte)32,null,DECoutput,Keyshadec.getBytes(),(byte)16,(byte)MODE_ECB,(byte)1);
                    Dr3Functions.Dorca3CipherDecipher((byte) RG_ENC, (byte) 1, decKey, (byte) 32, null, DECoutput, outSHAdec, (byte) 16, (byte) MODE_ECB, (byte) 1);
                    //dorca3_fx.send_string_to_main("\nAES AES,AES AES");
                    Dr3Functions.ReleaseBookingUSB();


                    for (i = 0; i < 16; i++) {
                        strKeyDec += String.format("%02x", DECoutput[i]);
                    }
                    Dr3Functions.send_string_to_main("KEY" + strKeyDec);
                    System.out.println("DecKey: " + strKeyDec);

                    byte[] outdec = new byte[32];


                    JniBrige.AES_CIPHER(output, outdec, strKeyDec.getBytes());

                    System.out.println("deckey: " + strKeyDec);

                    System.out.println("outdec: " + hexdump.toHexString(outdec));

                    if (file != null && file.isFile()) {

                        ActivityCompat.requestPermissions(SmodeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        actTimeDecSSL = JniBrige.OPENSSLDEC(inputfilepath, outputfilepath, strKeyDec, device);
                        Dr3Functions.send_string_to_main("DecKEY" + strKeyDec);

                        final long endDecSSL = System.currentTimeMillis();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                {
                                    long time = endDecSSL - startDecSSL;
                                    String temp = String.valueOf(time);
                                    String temp2 = String.valueOf(actTimeDecSSL);
                                    Context context = getApplicationContext();
                                    CharSequence text = "total: " + temp + "ms" + " : " + "OP:" + temp2 + "ms";
                                    Log.i(LOG_TAG, "DecSPI time " + text);
                                    int duration = Toast.LENGTH_LONG;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                            }
                        });
                        break;
                    } else {
                        Log.e(LOG_TAG, "decrypting directory");
                        Log.e(LOG_TAG, "input file path: " + inputfilepath);


                        File out = new File(outputfilepath);
                        out.mkdirs();

                        File[] files = new File(inputfilepath).listFiles();
                        Log.e(LOG_TAG, inputfilepath);


                        Log.e(LOG_TAG, "input file path" + inputfilepath);
                        for (File f : files) {


                            TimeDecSSL = JniBrige.OPENSSLDEC(f.getAbsolutePath(), out.getAbsolutePath() + "/" + f.getName(), strKeyDec, device);
                            Dr3Functions.send_string_to_main("\nKEY" + strKeyDec);

                            Log.e(LOG_TAG, "decrypt inpath" + f.getAbsolutePath());
                            Log.e(LOG_TAG, "decrypt outpath" + out.getAbsolutePath() + "/" + f.getName());
                            TotalDecSSL += TimeDecSSL;
                        }
                        actTimeFileDecSSL = TotalDecSSL;

                        runOnUiThread(new Runnable() {
                            final long endDecSSL = System.currentTimeMillis();

                            public void run() {
                                {
                                    long time = endDecSSL - startDecSSL;
                                    String temp = String.valueOf(time);
                                    String temp2 = String.valueOf(actTimeFileDecSSL);
                                    Context context = getApplicationContext();
                                    CharSequence text = "total: " + temp + "ms" + " : " + "OP:" + temp2 + "ms";
                                    Log.i(LOG_TAG, "DecSPI time " + text);
                                    int duration = Toast.LENGTH_LONG;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                            }
                        });
                    }
                default:
                    break;
            }
            return (null);
        }


        protected void onPostExecute(Void unused) {
            //update folder content
            String temp = mFileMag.getCurrentDir();
            File file = new File(temp + "/" + m_CurrentFile);

            AlertDialog.Builder builder = new AlertDialog.Builder(SmodeActivity.this);
            builder.setTitle("Warning ");
            builder.setIcon(R.drawable.warning);
            builder.setMessage("Delete file" + m_CurrentFile + " ?");
            builder.setCancelable(false);

            builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //dialog.dismiss();
                    mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                }
            });
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {




                        //mHelper.close();
                            //mFileMag.deleteTarget(m_CurrentFile);

                        //mHandler.updateDirectory(mFileMag.getNextDir(temp, true));
                        //dismiss the alert here where the thread has finished his work



                    mFileMag.deleteTarget(m_CurrentFile);
                    Log.d("E4NET", "On Delete m_CurrentFile  :" + m_CurrentFile);
                    mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                }
            });


            SQLiteDatabase db;
            ContentValues row;
            db = mHelper.getWritableDatabase();

            Cursor cursor;

            cursor = db.rawQuery("SELECT pic_dir, mov_dir, mus_dir, doc_dir FROM dic", null);


            while (cursor.moveToNext()){
                pic_dir = cursor.getString(0);
                mov_dir = cursor.getString(1);
                mus_dir = cursor.getString(2);
                doc_dir = cursor.getString(3);
            }


            db.delete("dic", null, null);


            db = mHelper.getWritableDatabase();
            row = new ContentValues();



            final String ext = mFileMag.getCurrentDir() + "/" + m_CurrentFile;
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
            Log.d("E4NET", "sub_dir =  " + sub_ext);
            /* This series of else if statements will determine which
             * icon is displayed
             */


            if (sub_ext.equalsIgnoreCase("mp3") ||
                    sub_ext.equalsIgnoreCase("wma") ||
                    sub_ext.equalsIgnoreCase("m4a") ||
                    sub_ext.equalsIgnoreCase("m4p")) {


                mus_dir = mFileMag.getCurrentDir();

                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mFileMag.getCurrentDir());
                row.put("doc_dir", doc_dir);
            } else if (sub_ext.equalsIgnoreCase("png") ||
                    sub_ext.equalsIgnoreCase("jpg") ||
                    sub_ext.equalsIgnoreCase("jpeg") ||
                    sub_ext.equalsIgnoreCase("gif") ||
                    sub_ext.equalsIgnoreCase("tiff")) {
                pic_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", mFileMag.getCurrentDir());
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", doc_dir);


            } else if (sub_ext.equalsIgnoreCase("m4v") ||
                    sub_ext.equalsIgnoreCase("wmv") ||
                    sub_ext.equalsIgnoreCase("3gp") ||
                    sub_ext.equalsIgnoreCase("mp4")) {

                mov_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mFileMag.getCurrentDir());
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", doc_dir);
            } else if (sub_ext.equalsIgnoreCase("zip") ||
                    sub_ext.equalsIgnoreCase("gzip") ||
                    sub_ext.equalsIgnoreCase("gz")) {

                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());

            } else if (sub_ext.equalsIgnoreCase("doc") ||
                    sub_ext.equalsIgnoreCase("docx")) {

                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());

            } else if (sub_ext.equalsIgnoreCase("xls") ||
                    sub_ext.equalsIgnoreCase("xlsx")) {

                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());

            } else if (sub_ext.equalsIgnoreCase("ppt")) {

                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());

            } else if (sub_ext.equalsIgnoreCase("pptx")) {

                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());

            } else if (sub_ext.equalsIgnoreCase("html")) {
                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());

            } else if (sub_ext.equalsIgnoreCase("xml")) {
                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());

            } else if (sub_ext.equalsIgnoreCase("conf")) {
                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());

            } else if (sub_ext.equalsIgnoreCase("apk")) {
                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());

            } else if (sub_ext.equalsIgnoreCase("jar") || sub_ext.equalsIgnoreCase("pdf") || sub_ext.equalsIgnoreCase("txt")) {
                doc_dir = mFileMag.getCurrentDir();
                row.put("pic_dir", pic_dir);
                row.put("mov_dir", mov_dir);
                row.put("mus_dir", mus_dir);
                row.put("doc_dir", mFileMag.getCurrentDir());
            }

            db.insert("dic", null, row);

            AlertDialog alert_d = builder.create();
            alert_d.show();

            cursor.close();
            mHelper.close();
            pDialog.dismiss();
            m_myAdapter.notifyDataSetChanged();
            Log.d("sqasdf", "pho_dir =  " + pic_dir);
            Log.d("sqasdf", "mov_dir =  " + mov_dir);
            Log.d("sqasdf", "mus_dir =  " + mus_dir);
            Log.d("sqasdf", "doc_dir =  " + doc_dir);

        }
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
            }
        }

        BroadcastReceiver mUsbDetachAction = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    Toast.makeText(getApplicationContext(), "ACTION_USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();

                }
            }
        };

        BroadcastReceiver mUsbAttachReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    Toast.makeText(getApplicationContext(), "ACTION_USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();

                }
            }
        };
        BroadcastReceiver mUsbDetachReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Toast.makeText(getApplicationContext(), "action is" + action, Toast.LENGTH_SHORT);
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    Toast.makeText(getApplicationContext(), "ACTION_USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
                    Dr3Functions.close();
                }


                // if (CheckConnectedDevice() == false) {
                //     Toast.makeText(getApplicationContext(), "DORCA3 Device not connected", Toast.LENGTH_SHORT).show();
                //     finishAffinity();
                // }
            }
        };

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
                        "pic_dir TEXT, mus_dir TEXT, mov_dir TEXT, doc_dir TEXT);");
            }

            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS dic");
                onCreate(db);
            }

        }


    }









