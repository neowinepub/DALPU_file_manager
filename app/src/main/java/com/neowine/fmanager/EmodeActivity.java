package com.neowine.fmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import org.w3c.dom.Text;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by e4net on 16. 2. 4..
 */
public class EmodeActivity extends Activity implements View.OnClickListener {

    private static String ENC_DIR = Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc";
    private static String DEC_DIR = Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_dec";
    private boolean cyperEntered = false;
    private boolean workEntered = false;
    private String workFlag;
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
    private FileManager mFileMag;
    private EventHandler mHandler;
    private EventHandler.TableRow mTable;
    private SharedPreferences mSettings;
    private boolean mReturnIntent = false;
    private boolean mHoldingFile = false;
    private boolean mHoldingZip = false;
    private boolean mUseBackKey = true;
    private String mCopiedTarget;
    private String mZippedTarget;
    private String mSelectedListItem;                //item from context menu
    private TextView mPathLabel, mDetailLabel;
    private TextView workCopy, workMove, workDelete, workRename;
    private int workCopyX, workMoveX, workDeleteX, workRenameX, workEventX;
    private GridView grid;
    private ImageView enc_icon, enc_arrow, dec_icon, drag_drop_file;
    private Button btn_smode, btn_main, btn_side;
    private Button btn_home, btn_local, btn_sort;
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
    public Util e4net_util;
    final Handler cwjHandler = new Handler();
    ExecutorService eService = Executors.newSingleThreadExecutor();
    public ProgressDialog mypDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_frame);

        //Util Load
        e4net_util = Util.getInstance();

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
        sidemenu = mInflater.inflate(R.layout.emode_view, null);
        fm.addView(sidemenu);

        //Set Button Events
        btn_main = (Button) sidemenu.findViewById(R.id.btnTopNav2);
        btn_main.setOnClickListener(this);
        btn_smode = (Button) sidemenu.findViewById(R.id.btnTopNav1);
        btn_smode.setOnClickListener(this);
        btn_side = (Button) sidemenu.findViewById(R.id.btnSideMenu);
        btn_side.setOnClickListener(this);
        btn_home = (Button) sidemenu.findViewById(R.id.btn_emode_bot_home);
        btn_home.setOnClickListener(this);
        btn_local = (Button) sidemenu.findViewById(R.id.btn_emode_bot_local);
        btn_local.setOnClickListener(this);
        btn_sort = (Button) sidemenu.findViewById(R.id.btn_emode_bot_sort);
        btn_sort.setOnClickListener(this);
        enc_icon = (ImageView) sidemenu.findViewById(R.id.emodeEncIcon);
        enc_arrow = (ImageView) sidemenu.findViewById(R.id.emodeEncArrow);
        dec_icon = (ImageView) sidemenu.findViewById(R.id.emodeDecIcon);
        cypherZone = (LinearLayout) sidemenu.findViewById(R.id.emodeCypher);
        workZone = (LinearLayout) sidemenu.findViewById(R.id.emodeMiddleWorks);

        workCopy = (TextView) findViewById(R.id.emode_copy);
        workMove = (TextView) findViewById(R.id.emode_move);
        workDelete = (TextView) findViewById(R.id.emode_delete);
        workRename = (TextView) findViewById(R.id.emode_rename);

        drag_drop_file = (ImageView) sidemenu.findViewById(R.id.drag_drop_file);

        cypherZone.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                // 이벤트 시작
                //Log.d(e4net_util.e4_tag(), "cypherZone setOnDragListener");
                switch (event.getAction()) {

                    // 이미지를 드래그 시작될때
                    case DragEvent.ACTION_DRAG_STARTED:
                        cyperEntered = false;
                        Log.d(e4net_util.e4_tag(), "DragClickListener ACTION_DRAG_STARTED");
                        break;

                    // 드래그한 이미지를 옮길려는 지역으로 들어왔을때
                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d(e4net_util.e4_tag(), "DragClickListener ACTION_DRAG_ENTERED");
                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData cd = cm.getPrimaryClip();

                        if (cd != null) {
                            ClipData.Item item = cd.getItemAt(0);
                            Log.d(e4net_util.e4_tag(), "ClipData : " + item.getText().toString());

                            File f = new File(item.getText().toString());
                            String fileName = f.getName();

                            if (!f.isDirectory()) {
                                if (f.getParent().equals(ENC_DIR)) {
                                    dec_icon.setVisibility(View.VISIBLE);
                                    enc_arrow.setVisibility(View.VISIBLE);
                                } else {
                                    enc_icon.setVisibility(View.VISIBLE);
                                    enc_arrow.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        cyperEntered = true;
                        break;

                    // 드래그한 이미지가 영역을 빠져 나갈때
                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d("DragClickListener", "ACTION_DRAG_EXITED");
                        enc_arrow.setVisibility(View.INVISIBLE);
                        enc_icon.setVisibility(View.INVISIBLE);
                        dec_icon.setVisibility(View.INVISIBLE);
                        cyperEntered = false;
                        break;

                    // 이미지를 드래그해서 드랍시켰을때
                    case DragEvent.ACTION_DROP:
                        Log.d("DragClickListener", "ACTION_DROP");
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.d("DragClickListener", "ACTION_DRAG_ENDED");
                        if (cyperEntered) {
                            //파일 드랍시 암복호화 호출
                            ClipboardManager cm1 = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData cd1 = cm1.getPrimaryClip();

                            if (cd1 != null) {
                                ClipData.Item item = cd1.getItemAt(0);
                                Log.d(e4net_util.e4_tag(), "ClipData : " + item.getText().toString());

                                File f = new File(item.getText().toString());
                                String fileName = f.getName();

                                if (!f.isDirectory()) {
                                    try{
                                        fileName.substring(fileName.lastIndexOf("."), fileName.length());
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(),"Can't open File", LENGTH_SHORT).show();
                                        e.printStackTrace();
                                        enc_arrow.setVisibility(View.INVISIBLE);
                                        enc_icon.setVisibility(View.INVISIBLE);
                                        return false;
                                    }

                                    drag_drop_file.setImageResource(R.drawable.e_mode_bg2);
                                    if (f.getParent().equals(ENC_DIR)) {
                                        NeowineTask obj = new NeowineTask(item.getText().toString(), DEC_DIR + "/" + GetOutFileDECName(fileName), INPUT_KEY, Dorcatype.DecSWXOR, HYPERSTEP);
                                        obj.execute();
                                        Toast.makeText(EmodeActivity.this, "Decrypt Finished", LENGTH_SHORT).show();
                                    } else {
                                        NeowineTask obj = new NeowineTask(item.getText().toString(), ENC_DIR + "/" + GetOutFileENCName(fileName), INPUT_KEY, Dorcatype.EncSWXOR, HYPERSTEP);
                                        obj.execute();
                                        Toast.makeText(EmodeActivity.this, "Encrypt Finished", LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(EmodeActivity.this, "It's not File", LENGTH_SHORT).show();
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        workZone.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
//                Log.d(e4net_util.e4_tag(), "workZone setOnDragListener");
                // 이벤트 시작
                switch (event.getAction()) {

                    // 이미지를 드래그 시작될때
                    case DragEvent.ACTION_DRAG_STARTED:
                        workEntered = false;
                        Log.d(e4net_util.e4_tag(), "DragClickListener ACTION_DRAG_STARTED");
                        break;

                    // 드래그한 이미지를 옮길려는 지역으로 들어왔을때
                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d(e4net_util.e4_tag(), "DragClickListener ACTION_DRAG_ENTERED");
                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData cd = cm.getPrimaryClip();

                        if (cd != null) {
                            ClipData.Item item = cd.getItemAt(0);

                            File f = new File(item.getText().toString());
                            mSelectedListItem = f.getName();

                            workEventX = (int) event.getX();
                            workCopyX = (int) workCopy.getX();
                            workMoveX = (int) workMove.getX();
                            workDeleteX = (int) workDelete.getX();
                            workRenameX = (int) workRename.getX();
                            workFlag = "";
                            if (!f.isDirectory()) {
                                if (workEventX < workDeleteX) {
                                    if(workEventX >= workMoveX) {
                                        Log.d(e4net_util.e4_tag(), "Move [" + mSelectedListItem + "]");
                                        workFlag = "MOVE";
                                    } else {
                                        Log.d(e4net_util.e4_tag(), "Copy ["+ mSelectedListItem +"]");
                                        workFlag = "COPY";
                                    }
                                } else if(workEventX < workRenameX) {
                                    Log.d(e4net_util.e4_tag(), "Delete ["+ mSelectedListItem +"]");
                                    workFlag = "DELETE";
                                } else if(workEventX >= workRenameX) {
                                    Log.d(e4net_util.e4_tag(), "Rename ["+ mSelectedListItem +"]");
                                    workFlag = "RENAME";
                                }
                            }
                        }
                        workEntered = true;
                        break;

                    // 드래그한 이미지가 영역을 빠져 나갈때
                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d("DragClickListener", "ACTION_DRAG_EXITED");
                        workEntered = false;
                        break;

                    // 이미지를 드래그해서 드랍시켰을때
                    case DragEvent.ACTION_DROP:
                        Log.d("DragClickListener", "ACTION_DROP");
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.d(e4net_util.e4_tag(), "DragClickListener ACTION_DRAG_ENDED : " + workEntered);
                        if (workEntered) {
                            ClipboardManager cm1 = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData cd1 = cm1.getPrimaryClip();
                            if (cd1 != null) {
                                if(workFlag.equals("MOVE") || workFlag.equals("COPY")) {
                                    mHoldingFile = true;
                                    mCopiedTarget = mFileMag.getCurrentDir() + "/" + mSelectedListItem;

                                    if(workFlag.equals("MOVE")) {
                                        mHandler.setDeleteAfterCopy(true);
                                        Log.d(e4net_util.e4_tag(), "Move [" + mSelectedListItem + "]");
                                        Toast.makeText(EmodeActivity.this, "Move : " + mSelectedListItem + " was selected",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Log.d(e4net_util.e4_tag(), "Copy ["+ mSelectedListItem +"]");
                                        Toast.makeText(EmodeActivity.this, "Copy : " + mSelectedListItem + " was selected",
                                                Toast.LENGTH_LONG).show();
                                    }
                                } else if(workFlag.equals("DELETE")) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EmodeActivity.this);
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
                                } else if(workFlag.equals("RENAME")) {
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

        grid = (GridView) findViewById(R.id.grid);
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
            mHandler = new EventHandler(EmodeActivity.this, mFileMag, savedInstanceState.getString("location"));
        else
            mHandler = new EventHandler(EmodeActivity.this, mFileMag);
        mHandler.setTextColor(Color.BLACK);
        mHandler.setShowThumbnails(thumb);
        mTable = mHandler.new TableRow();

        /*sets the ListAdapter for our ListActivity and
         *gives our EventHandler class the same adapter
         */
        mHandler.setListAdapter(mTable);
        grid.setAdapter(mTable);
        mDetailLabel = (TextView) findViewById(R.id.emode_detail_label1);
        mPathLabel = (TextView) findViewById(R.id.emode_path_label1);
        mPathLabel.setText("path: /sdcard");
        mHandler.setUpdateLabels(mPathLabel, mDetailLabel);
        txTitle = (TextView) findViewById(R.id.emodeTopText);
        txTitle.setText("sdcard");
        e4net_util.create_path_view("/storage/emulated/0", getBaseContext(), (LinearLayout) findViewById(R.id.emode_path_layout));
        Intent intent = getIntent();
        if (intent.getAction() == null) {
            Log.d(e4net_util.e4_tag(), "Main Activity");
        } else if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
            mReturnIntent = true;
        } else if (intent.getAction().equals(ACTION_WIDGET)) {
            Log.e(e4net_util.e4_tag(), "Widget action, string = " + intent.getExtras().getString("folder"));
            e4net_util.create_path_view(intent.getExtras().getString("folder"), getBaseContext(), (LinearLayout) findViewById(R.id.emode_path_layout));
            String[] tmp = intent.getExtras().getString("folder").split("/");
            txTitle.setText(tmp[tmp.length - 1]);
            mHandler.updateDirectory(mFileMag.getNextDir(intent.getExtras().getString("folder"), true));
        }
        //Dorca init
        createDorcaPath();
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                workZone.setVisibility(View.VISIBLE);

                TextView filepath = (TextView) view.findViewById(R.id.filePath);
                Log.d(e4net_util.e4_tag(), "File Path : " + filepath.getText());
                ClipboardManager clipmgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData.Item item = new ClipData.Item(filepath.getText());
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData data = new ClipData(filepath.getText(), mimeTypes, item);
                clipmgr.setPrimaryClip(data);
                view.setBackgroundColor(Color.parseColor("#f24c4c"));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, // data to be dragged
                        shadowBuilder, // drag shadow
                        view, // 드래그 드랍할  Vew
                        0 // 필요없는 플래그
                );
                view.setBackgroundColor(Color.parseColor("#EBEAEB"));
                return true;
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String item = mHandler.getData(position);
                boolean multiSelect = mHandler.isMultiSelected();
                File file = new File(mFileMag.getCurrentDir() + "/" + item);
                String item_ext = null;

                if (file.isDirectory())
                    txTitle.setText(item);

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
                            e4net_util.create_path_view(mFileMag.getCurrentDir(), getBaseContext(), (LinearLayout) findViewById(R.id.emode_path_layout));

		    		/*set back button switch to true
                     * (this will be better implemented later)
		    		 */
                            if (!mUseBackKey)
                                mUseBackKey = true;

                        } else {
                            Toast.makeText(EmodeActivity.this, "Can't read folder due to permissions", LENGTH_SHORT).show();
                        }
                    }

	    	/*music file selected--add more audio formats*/
                    else if (item_ext.equalsIgnoreCase(".mp3") ||
                            item_ext.equalsIgnoreCase(".m4a") ||
                            item_ext.equalsIgnoreCase(".mp4")) {

                        if (mReturnIntent) {
                            returnIntentResults(file);
                        } else {
                            Intent i = new Intent();
                            i.setAction(android.content.Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(file), "audio/*");
                            startActivity(i);
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
                                picIntent.setAction(android.content.Intent.ACTION_VIEW);
                                picIntent.setDataAndType(Uri.fromFile(file), "image/*");
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
                                Intent movieIntent = new Intent();
                                movieIntent.setAction(android.content.Intent.ACTION_VIEW);
                                movieIntent.setDataAndType(Uri.fromFile(file), "video/*");
                                startActivity(movieIntent);
                            }
                        }
                    }

	    	/*zip file */
                    else if (item_ext.equalsIgnoreCase(".zip")) {

                        if (mReturnIntent) {
                            returnIntentResults(file);

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(EmodeActivity.this);
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
                                            mDetailLabel.setText("Holding " + item +
                                                    " to extract");
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
                                Intent pdfIntent = new Intent();
                                pdfIntent.setAction(android.content.Intent.ACTION_VIEW);
                                pdfIntent.setDataAndType(Uri.fromFile(file),
                                        "application/pdf");

                                try {
                                    startActivity(pdfIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(EmodeActivity.this, "Sorry, couldn't find a pdf viewer", LENGTH_SHORT).show();
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
                                Intent apkIntent = new Intent();
                                apkIntent.setAction(android.content.Intent.ACTION_VIEW);
                                apkIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
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
                                Intent htmlIntent = new Intent();
                                htmlIntent.setAction(android.content.Intent.ACTION_VIEW);
                                htmlIntent.setDataAndType(Uri.fromFile(file), "text/html");

                                try {
                                    startActivity(htmlIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(EmodeActivity.this, "Sorry, couldn't find a HTML viewer", LENGTH_SHORT).show();
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
                                Intent txtIntent = new Intent();
                                txtIntent.setAction(android.content.Intent.ACTION_VIEW);
                                txtIntent.setDataAndType(Uri.fromFile(file), "text/plain");

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
                                    Toast.makeText(EmodeActivity.this, "Sorry, couldn't find anything to open " + file.getName(), LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnTopNav1:
                e4net_util.intent(EmodeActivity.this, SmodeActivity.class, ACTION_WIDGET, mFileMag.getCurrentDir());
                break;

            case R.id.btnTopNav3:
                e4net_util.intent(EmodeActivity.this, EmodeActivity.class);
                break;

            case R.id.btnTopNav2:
                e4net_util.intent(EmodeActivity.this, Main.class);
                break;

            case R.id.btnSideMenu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.btn_emode_bot_home:
                e4net_util.intent(EmodeActivity.this, Main.class);
                break;

            case R.id.btn_emode_bot_local:
                intent = getIntent();
                intent.setAction(ACTION_WIDGET);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("folder", Environment.getExternalStorageDirectory().toString());
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;

            case R.id.btn_emode_bot_sort:
                mTable.notifyDataSetChanged();
                grid.setAdapter(mTable);
                Toast.makeText(getApplicationContext(), "Sorting ...", LENGTH_SHORT).show();
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
                //e4net_util.intent(EmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory().toString());
                e4net_util.intent(EmodeActivity.this, Main.class);
                break;
            case 2:
                e4net_util.intent(EmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, Environment.getRootDirectory().toString());
                break;
            case 3:
                e4net_util.intent(EmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                break;
            case 4:
                e4net_util.intent(EmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory().toString());
                break;
            case 5:
                File ext_file = new File(System.getenv("SECONDARY_STORAGE"));
                String[] ext_file_list = ext_file.list();
                if ((ext_file_list != null) && (ext_file_list.length > 1)) {
                    e4net_util.intent(EmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, ext_file.toString());
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(EmodeActivity.this);
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
                e4net_util.intent(EmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
                break;
            case 8:
                e4net_util.intent(EmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString());
                break;
            case 9:
                e4net_util.intent(EmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString());
                break;
            case 10:
                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
                boolean isPresent = true;
                if (!docsFolder.exists()) {
                    isPresent = docsFolder.mkdir();
                }
                e4net_util.intent(EmodeActivity.this, EmodeActivity.class, ACTION_WIDGET, Environment.getExternalStorageDirectory() + "/Documents");
                break;
        }
    }

    ////++++ Added by Mason, 20151008
    protected void createDorcaPath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File[] files = getExternalFilesDirs(null);
            if ((files.length > 1) && (files[1] != null)) {
                Log.i(e4net_util.e4_tag(), "Dorca path : " + files[1].getAbsolutePath());
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(EmodeActivity.this);
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);

        boolean multi_data = mHandler.hasMultiSelectData();
        AdapterContextMenuInfo _info = (AdapterContextMenuInfo) info;
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
                        e4net_util.create_path_view(current_dir, getBaseContext(), (LinearLayout) findViewById(R.id.emode_path_layout));

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
        String FileName = InputFileName;
        index = FileName.lastIndexOf('.');
        String Extension = FileName.substring(index + 1);
        String Name = FileName.substring(0, index);
        String OutputName = Name + "_ENC." + Extension;
        Log.d(e4net_util.e4_tag(), "FileName " + FileName);
        Log.d(e4net_util.e4_tag(), "Extension " + Extension);
        Log.d(e4net_util.e4_tag(), "Name " + Name);
        Log.d(e4net_util.e4_tag(), "OutputName " + OutputName);
        return OutputName;
    }

    String GetOutFileDECName(String InputFileName) {
        int index = 0;
        int extention = 0;
        String FileName = InputFileName;
        index = FileName.lastIndexOf('.');
        String Extension = FileName.substring(index + 1);
        String Name = FileName.substring(0, index);
        String OutputName = Name + "_DEC." + Extension;
        Log.d(e4net_util.e4_tag(), "FileName " + FileName);
        Log.d(e4net_util.e4_tag(), "Extension " + Extension);
        Log.d(e4net_util.e4_tag(), "Name " + Name);
        Log.d(e4net_util.e4_tag(), "OutputName " + OutputName);
        return OutputName;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog = new Dialog(EmodeActivity.this);

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
//                create.setOnClickListener(new OnClickListener() {
//                    public void onClick(View v) {
//                        if (input.getText().length() > 1) {
//                            if (mFileMag.createDir(mFileMag.getCurrentDir() + "/", input.getText().toString()) == 0)
//                                Toast.makeText(EmodeActivity.this,
//                                        "Folder " + input.getText().toString() + " created",
//                                        Toast.LENGTH_LONG).show();
//                            else
//                                Toast.makeText(EmodeActivity.this, "New folder was not created", LENGTH_SHORT).show();
//                        }
//
//                        dialog.dismiss();
//                        String temp = mFileMag.getCurrentDir();
//                        mHandler.updateDirectory(mFileMag.getNextDir(temp, true));
//                    }
//                });
//                cancel.setOnClickListener(new OnClickListener() {
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

                rename_create.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (rename_input.getText().length() < 1)
                            dialog.dismiss();

                        if (mFileMag.renameTarget(mFileMag.getCurrentDir() + "/" + mSelectedListItem, rename_input.getText().toString()) == 0) {
                            Toast.makeText(EmodeActivity.this, mSelectedListItem + " was renamed to " + rename_input.getText().toString(),
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(EmodeActivity.this, mSelectedListItem + " was not renamed", Toast.LENGTH_LONG).show();

                        dialog.dismiss();
                        String temp = mFileMag.getCurrentDir();
                        mHandler.updateDirectory(mFileMag.getNextDir(temp, true));
                    }
                });
                rename_cancel.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
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
                encrypt_create.setOnClickListener(new OnClickListener() {
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

                        NeowineTask obj = new NeowineTask(mFileMag.getCurrentDir() + "/" + mSelectedListItem, mFileMag.getCurrentDir() + "/" + GetOutFileENCName(mSelectedListItem), input_key, Dorcatype.EncSWAES, Integer.parseInt(encrypt_hybirdstep_swaes.getText().toString()));
                        obj.execute();
                        dialog.dismiss();
                    }
                });
                encrypt_cancel.setOnClickListener(new OnClickListener() {
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
                decrypt_create.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (password_input_de.getText().length() > 16)
                            dialog.dismiss();
                        Log.i(e4net_util.e4_tag(), mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                        //check input file is *.ENC
                        String input_file_string = mFileMag.getCurrentDir() + "/" + mSelectedListItem;
                        if (input_file_string.contains(".ENC")) {
                            String[] output_file_string = input_file_string.split("\\.");
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

                            NeowineTask obj = new NeowineTask(input_file_string, output_file_string[0] + "_Dec." + output_file_string[1], input_key, Dorcatype.DecSWAES, Integer.parseInt(decrypt_hybirdstep_swaes.getText().toString()));
                            obj.execute();
                        }
                        dialog.dismiss();
                    }
                });
                decrypt_cancel.setOnClickListener(new OnClickListener() {
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
                encrypt_createXOR.setOnClickListener(new OnClickListener() {
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

                        NeowineTask obj = new NeowineTask(mFileMag.getCurrentDir() + "/" + mSelectedListItem, mFileMag.getCurrentDir() + "/" + GetOutFileENCName(mSelectedListItem), input_key, Dorcatype.EncSWXOR, Integer.parseInt(encrypt_hybirdstep.getText().toString()));
                        obj.execute();
                        dialog.dismiss();
                    }
                });
                encrypt_cancelXOR.setOnClickListener(new OnClickListener() {
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
                decrypt_createXOR.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (password_input_deXOR.getText().length() > 16)
                            dialog.dismiss();
                        Log.i(e4net_util.e4_tag(), mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                        //check input file is *.ENC
                        String input_file_string = mFileMag.getCurrentDir() + "/" + mSelectedListItem;
                        {
                            String[] output_file_string = input_file_string.split("\\.");
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

                            NeowineTask obj = new NeowineTask(input_file_string, GetOutFileDECName(input_file_string), input_key, Dorcatype.DecSWXOR, Integer.parseInt(decrypt_hybirdstep.getText().toString()));
                            obj.execute();
                        }
                        dialog.dismiss();
                    }
                });
                decrypt_cancelXOR.setOnClickListener(new OnClickListener() {
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
                encrypt_createFW.setOnClickListener(new OnClickListener() {
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

                        NeowineTask obj = new NeowineTask(mFileMag.getCurrentDir() + "/" + mSelectedListItem, mFileMag.getCurrentDir() + "/" + mSelectedListItem + ".ENC", input_key, Dorcatype.EncFW, 0);
                        obj.execute();
                        dialog.dismiss();
                    }
                });
                encrypt_cancelFW.setOnClickListener(new OnClickListener() {
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
                decrypt_createFW.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (password_input_deFW.getText().length() > 16)
                            dialog.dismiss();

                        Log.i(e4net_util.e4_tag(), mFileMag.getCurrentDir() + "/" + mSelectedListItem);
                        //check input file is *.ENC
                        String input_file_string = mFileMag.getCurrentDir() + "/" + mSelectedListItem;
                        if (input_file_string.contains(".ENC")) {

                            String output_file_string = input_file_string.replace(".ENC", ".DEC");
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

                            NeowineTask obj = new NeowineTask(input_file_string, output_file_string, input_key, Dorcatype.DecFW, 0);
                            ////++++.
                            obj.execute();
                        }
                        dialog.dismiss();
                    }
                });
                decrypt_cancelFW.setOnClickListener(new OnClickListener() {
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
//                search_button.setOnClickListener(new OnClickListener() {
//                    public void onClick(View v) {
//                        String temp = search_input.getText().toString();
//
//                        if (temp.length() > 0)
//                            mHandler.searchForFile(temp);
//                        dialog.dismiss();
//                    }
//                });
//
//                cancel_button.setOnClickListener(new OnClickListener() {
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
                iotest_createFW.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        int repeat = Integer.parseInt(editRepeat.getText().toString());
                        Log.i(e4net_util.e4_tag(), "Repeat: " + repeat);

                        NeowineTask obj = new NeowineTask(mFileMag.getCurrentDir() + "/iotest.tmp", null, null, Dorcatype.IOTest, repeat);
                        obj.execute();

                        dialog.dismiss();
                    }
                });
                iotest_cancelFW.setOnClickListener(new OnClickListener() {
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
            case D_MENU_RENAME:
            {
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
            e4net_util.create_path_view(mFileMag.getCurrentDir(), getBaseContext(), (LinearLayout) findViewById(R.id.emode_path_layout));

            String[] tmp = mFileMag.getCurrentDir().split("/");
            if (tmp.length > 0)
                txTitle.setText(tmp[tmp.length - 1]);
            else
                txTitle.setText("/");
            return true;

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && current.equals("/")) {
            Toast.makeText(EmodeActivity.this, "Press back again to home.", LENGTH_SHORT).show();

            if (mHandler.isMultiSelected()) {
                mTable.killMultiSelect(true);
                Toast.makeText(EmodeActivity.this, "Multi-select is now off", LENGTH_SHORT).show();
            }

            mUseBackKey = false;
            e4net_util.create_path_view(mFileMag.getCurrentDir(), getBaseContext(), (LinearLayout) findViewById(R.id.emode_path_layout));

            String[] tmp = mFileMag.getCurrentDir().split("/");
            if (tmp.length > 0)
                txTitle.setText(tmp[tmp.length - 1]);

            return false;

        } else if (keycode == KeyEvent.KEYCODE_BACK && !mUseBackKey && current.equals("/")) {
            e4net_util.intent(EmodeActivity.this, Main.class);
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
                Log.e("Neowine", "HELLO " + file);
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

    enum Dorcatype {EncSWAES, DecSWAES, EncFW, DecFW, EncSWXOR, DecSWXOR, IOTest}

    class NeowineTask extends AsyncTask<Void, Void, Void> {
        private String inputfilepath;
        private String outputfilepath;
        private String key;
        private Dorcatype todocase;
        private int hybirdstep;
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
            pDialog = ProgressDialog.show(EmodeActivity.this, "Please wait...", "Encrypt/Decrypt data ...", true);
        }

        protected Void doInBackground(Void... unused) {
            switch (todocase) {
                case EncSWAES:
                    e4net_util.Dorca20EncryptfileSWAES(inputfilepath, outputfilepath, key, hybirdstep);
                    break;
                case DecSWAES:
                    e4net_util.Dorca20DecryptfileSWAES(inputfilepath, outputfilepath, key, hybirdstep);
                    break;
                case EncFW:
                    e4net_util.Dorca20EncryptfileFW(inputfilepath, outputfilepath, key);
                    break;
                case DecFW:
                    e4net_util.Dorca20DecryptfileFW(inputfilepath, outputfilepath, key);
                    break;
                case EncSWXOR:
                    e4net_util.Dorca20EncryptfileSWXOR(inputfilepath, outputfilepath, key, hybirdstep);
                    //Dorca20EncryptfileFast(inputfilepath, outputfilepath, key);
                    break;
                case DecSWXOR:
                    e4net_util.Dorca20DecryptfileSWXOR(inputfilepath, outputfilepath, key, hybirdstep);
                    //Dorca20DecryptfileFast(inputfilepath, outputfilepath, key);
                    break;
                case IOTest:
                    //jIOTest(inputfilepath, hybirdstep);
                    File[] temp = getExternalFilesDirs(null);
                    Log.i(e4net_util.e4_tag(), "length = " + temp.length + ", temp[1] = " + temp[1].getAbsolutePath());
                    File testFile = new File(temp[1].getAbsolutePath(), "IOTest.txt");

                    for (int i = 0; i < hybirdstep; i++) {
                        try {
                            OutputStream os = new FileOutputStream(testFile);
                            Log.i(e4net_util.e4_tag(), "SUCCESS : " + testFile);
                            os.close();
                        } catch (IOException e) {
                            Log.w(e4net_util.e4_tag(), "ERROR : " + testFile, e);
                        }
                    }
                    break;

                default:
                    break;
            }
            return (null);
        }

        protected void onPostExecute(Void unused) {
            //update folder content
            String temp = mFileMag.getCurrentDir();
            mHandler.updateDirectory(mFileMag.getNextDir(temp, true));
            //dismiss the alert here where the thread has finished his work
            enc_arrow.setVisibility(View.INVISIBLE);
            enc_icon.setVisibility(View.INVISIBLE);
            dec_icon.setVisibility(View.INVISIBLE);
            drag_drop_file.setImageResource(R.drawable.e_mode_bg);
            pDialog.dismiss();
        }
    }
}
