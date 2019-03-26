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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
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
import android.support.v4.content.ContextCompat;

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
import android.app.Activity;

import org.w3c.dom.Text;
import android.database.Cursor;
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
import static java.lang.String.format;

public class CustomDialog extends Activity implements View.OnClickListener{
    private static final int LAYOUT = R.layout.custom_dlg;

    private Context context;
    private String strTestResult = "FALSE";
    private Button bt_confirm;
    private TextView cancelTv;
    private TextView searchTv;
    private EditText et_passwd;
    private String name;
    private String finish;
    private TextView tv_photo_cnt, tv_movie_cnt, tv_music_cnt, tv_doc_cnt;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
        bt_confirm = findViewById(R.id.button_ok);
        bt_confirm.setOnClickListener(this);
        et_passwd =  findViewById(R.id.txtPassword);
        finish = "FALSE";
        tv_photo_cnt = (TextView) findViewById(R.id.tv_photo_cnt_dlg);
        tv_movie_cnt = (TextView) findViewById(R.id.tv_movie_cnt_dlg);
        tv_music_cnt = (TextView) findViewById(R.id.tv_music_cnt_dlg);
        tv_doc_cnt = (TextView)   findViewById(R.id.tv_doc_cnt_dlg);
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
    }
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != getPackageManager().PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d("E4NET", "if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {");
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
    @Override
    public void onClick(View v) {

        Log.d("E4NET", "input message"+et_passwd.getText().toString());
        if(v.getId() == R.id.button_ok) {
            if ((et_passwd.getText().toString()).equals("1234") ) {
                strTestResult = "TRUE";
                finish = "TRUE";
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result","TRUE");
                setResult(RESULT_OK,resultIntent);
                Log.d("E4NET", "setResult(RESULT_OK,resultIntent);");
                finish();
            }
            else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result","FALSE");
                setResult(RESULT_CANCELED,resultIntent);
                Log.d("E4NET", "setResult(RESULT_CANCELED,resultIntent);");
                finish();
            }
        }
    }
    public String getResult()
    {
        return strTestResult;
    }
    public String isFinished()
    {
        return finish;
    }
}


