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
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;
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
import java.io.InputStream;
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
 * Created by e4net on 16. 2. 3..
 */
public class IntroView extends Activity {

    static{
        System.loadLibrary("NeowineNative");
    }

    private Handler mHandler;
    private Runnable mRunnable;
    public Util e4_util;
    String LOG_TAG = "E4NET";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_view);

        TextView tv = (TextView) findViewById(R.id.textView3);

       // tv.setText(stringFromJNI());
        Intent calIntent = new Intent(this, CustomDialog.class);
        startActivityForResult(calIntent,3000);
        Log.d("E4NET", "startActivityForResult(calIntent,3000);;");
        //Log.d("E4NET",System.getenv("SECONDARY_STORAGE"));
//        String SEC_STOR = System.getenv("SECONDARY_STORAGE");
//        if(SEC_STOR != null)
//            Log.d("E4NET","SEC IS" + System.getenv("SECONDARY_STORAGE"));
//        else
//            Log.d("E4NET","SEC IS NULL");
//        {
//            mRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    Intent intent = new Intent(getApplicationContext(), Main.class);
//                    startActivity(intent);
//                    finish();
//                }
//            };
//
//            mHandler = new Handler();
//            mHandler.postDelayed(mRunnable, 10);
//            Log.d("E4NET", "mHandler.postDelayed(mRunnable, 2000);");
//        }

        //while(dlg.isFinished() != "TRUE");
        //if(dlg.getResult() == "TRUE") {

      //  }
//        else {
//            Toast.makeText(getApplicationContext(), "wrong passwd", Toast.LENGTH_LONG).show();
//            finish();
//        }

    }

    @Override
    protected void onDestroy() {
        Log.i(Util.getInstance().e4_tag(), "IntroView onDstory");
        mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("E4NET", "onActivityResult");
        {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), Main.class);
                    startActivity(intent);
                    finish();
                }
            };

            mHandler = new Handler();
            mHandler.postDelayed(mRunnable, 2000);
            Log.d("E4NET", "mHandler.postDelayed(mRunnable, 2000);");
            return;
        }
//        if(resultCode == RESULT_OK){
//            switch (requestCode){
//// MainActivity 에서 요청할 때 보낸 요청 코드 (3000)
//                case 3000:
// //                   String result = data.getStringExtra("result");
////                    if( result.equals("TRUE") ) {
//                    Log.d("E4NET", "onActivityResult");
//                    {
//                        mRunnable = new Runnable() {
//                            @Override
//                            public void run() {
//                                Intent intent = new Intent(getApplicationContext(), Main.class);
//                                startActivity(intent);
//                                finish();
//                            }
//                        };
//
//                        mHandler = new Handler();
//                        mHandler.postDelayed(mRunnable, 2000);
//                        Log.d("E4NET", "mHandler.postDelayed(mRunnable, 2000);");
//                    }
//
//
//                    break;
//            }
//        }
    }
}
