package com.neowine.fmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


/**
 * Created by e4net on 16. 2. 4..
 */
public class Util {
    private static Util st = null;

   protected Util() {}

    //Singleton
    public static Util getInstance() {
        if (st == null)
            st = new Util();
        return st;
    }

    //Log Tag
    public static String e4_tag() {
        String e4_tag = "E4NET";
        return e4_tag;
    }

    //Custom Intent(src view, dst view)
    public void intent(Activity srcContext, Class dstContext) {
        Intent intent = new Intent(srcContext, dstContext);
        srcContext.startActivity(intent);
        srcContext.overridePendingTransition(0, 0);
        srcContext.finish();
    }

    //Custom Intent(src view, dst view, actionType, dir path)
    public void intent(Activity srcContext, Class dstContext, String actionType, String path) {
        Intent intent = new Intent(srcContext, dstContext);
        intent.setAction(actionType);
        intent.putExtra("folder", path);
        srcContext.startActivity(intent);
        srcContext.overridePendingTransition(0, 0);
        srcContext.finish();
    }

    //Path View
    public void create_path_view(String path, Context context, LinearLayout layout) {
        // layout clear
        layout.removeAllViews();

        String[] pathArry = path.split("/");

        if (pathArry.length < 1) {
            pathArry = new String[1];
        }
        pathArry[0] = "  /";

        ImageView ImageView[] = new ImageView[pathArry.length];
        TextView TextView[] = new TextView[pathArry.length];

        for(int i = 0; i < pathArry.length; i++){
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            DisplayMetrics dm = layout.getResources().getDisplayMetrics();

            TextView[i] = new TextView(context);
            TextView[i].setLayoutParams(lp);
            TextView[i].setText(pathArry[i]);
            TextView[i].setTextColor(Color.rgb(22,22,22));
            TextView[i].setTextSize(12);
            layout.addView(TextView[i]);

            ImageView[i] = new ImageView(context);
            ImageView[i].setLayoutParams(lp);
            ImageView[i].setImageResource(R.drawable.ico_section);
            layout.addView(ImageView[i]);
        }
    }

    //Devine Image and Other Data
    public String devine_img_dec(String srcPath) throws IOException{
        File srcFile = new File(srcPath);
        int imgSize = 0;
        int nameLength = 0;
        int exif = 0;
        String fileName = "";

        int srcSize = (int)srcFile.length();
        try {
            byte[] srcByte = getBytesFromFile(srcFile);
            String imgEnd = "ffd9";
            byte[] comp = hexStringToByteArray(imgEnd);
            String exif_flag = "ffd8ffe1";
            byte[] exif_comp = hexStringToByteArray(exif_flag);

            if(srcByte[2] == exif_comp[2] && srcByte[3] == exif_comp[3])
            {
                for (int i = 0; i < srcSize; i++) {
                    if (srcByte[i] == comp[0] && srcByte[i + 1] == comp[1]) {
                        exif++;
                        if(exif == 2) {
                            imgSize = i + 2;
                            break;
                        }
                    }
                }
            }
            else
            {
                for (int i = 0; i < srcSize; i++) {
                    if (srcByte[i] == comp[0] && srcByte[i + 1] == comp[1]) {
                        imgSize = i + 2;
                        break;
                    }
                }
            }

            byte[] imgData = new byte[imgSize];
            for(int i = 0; i < imgSize; i++)
            {
                imgData[i] = srcByte[i];
            }

            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_dec/" + srcFile.getName());
            fos.write(imgData);
            fos.close();

            nameLength = srcByte[imgSize];
            if(nameLength < 0){
                throw new IOException();
            }
            char[] fileNameArray = new char[nameLength];
            for(int i = 0; i < nameLength; i ++)
            {
                fileNameArray[i] = (char) srcByte[imgSize + 1 + i];
            }

            fileName = String.valueOf(fileNameArray);
            int output_length = srcSize - (imgSize + 1 + nameLength) + 1;
            byte[] output = new byte[output_length + 1];
            for(int i = 0; i < output_length - 2; i++){
                output[i] = srcByte[(imgSize + nameLength + 1) + i];
            }

            FileOutputStream fos2 = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_dec/" + fileName);
            fos2.write(output);
            fos2.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    //Combine Image and Other Data
    public Boolean combine_img_enc(String imgNamepath, String encNamepath, String outputName) {

        Boolean result = false;
        File file1 = new File(imgNamepath);
        File file2 = new File(encNamepath);

        int size = file2.getName().length();
//        char[] fileName = new char[size];
//        fileName = file2.getName().toCharArray();
        try {
            this.Dorca20MergeFile(imgNamepath, encNamepath, outputName, size);
            result = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result = false;
        }
        return result;
//        try {
//            byte[] tmp1 = getBytesFromFile(file1);
//            byte[] tmp2 = getBytesFromFile(file2);
//
//            byte[] combined = new byte[tmp1.length + tmp2.length + 1 + file2.getName().length()];
//            for (int i = 0; i < combined.length; ++i) {
//                if( i < tmp1.length)
//                {
//                    combined[i] = tmp1[i];
//                }
//
//                if( i == tmp1.length)
//                {
//                    combined[i] = (byte)file2.getName().length();
//                }
//
//                if( i > tmp1.length)
//                {
//                    if(i - tmp1.length - 1 < file2.getName().length())
//                    {
//                        combined[i] = (byte) fileName[i - tmp1.length - 1];
//                    }
//                    else
//                    {
//                        combined[i] = tmp2[i - (tmp1.length + file2.getName().length() + 1)];
//                    }
//                }
//            }
//
//            FileOutputStream fos = new FileOutputStream(outputName);
//            fos.write(combined);
//            fos.close();
//            result = true;
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            System.out.println("combine image and data fail throw execption");
//            result = false;
//        }
//        return result;
    }

    //Check Encrypt ro Decrypt File
    public Boolean enc_dec_file(File file) throws IOException {
        Boolean result = false;
        byte[] byteArray = getBytesFromFile(file);
        byte[] row = new byte[2];
        String imgEnd = "ffd9";
        byte[] comp = hexStringToByteArray(imgEnd);

        int start_loc = byteArray.length - 2;
        System.out.println("start_loc : " + start_loc);
        int end_loc = byteArray.length - 1;
        System.out.println("end_loc : " + end_loc);

        row[0] = byteArray[start_loc];
        row[1] = byteArray[end_loc];

        if (Arrays.equals(row, comp)) {
            result = true;
        } else {
            result = false;
        }

        return result;
    }

    //String to Hex
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    //File to Byte
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            Log.d(Util.e4_tag(), "File is too large");
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    ////++++ Added by Mason, 20150916,
    public native void jIOTest(String filename, int repeat);
    ////++++.

    ////++++ Added by Mason, 20150809,
    public native String jGetSecondaryStorageDirectory();

    public native void jInitialize(String strDorcaPath);

    ////++++.
    public native void Dorca20EncryptfileSWAES(String src_file, String dest_file, String key, int hybirdstep);

    public native void Dorca20DecryptfileSWAES(String src_file, String dest_file, String key, int hybirdstep);

    public native void Dorca20EncryptfileFW(String src_file, String dest_file, String key);

    public native void Dorca20DecryptfileFW(String src_file, String dest_file, String key);

    public native void Dorca20EncryptfileSWXOR(String src_file, String dest_file, String key, int hybirdstep);

    public native void Dorca20DecryptfileSWXOR(String src_file, String dest_file, String key, int hybirdstep);

    public native void Dorca20EncryptfileFast(String src_file, String dest_file, String key);

    public native void Dorca20DecryptfileFast(String src_file, String dest_file, String key);

    public native void Dorca20MergeFile(String file1, String file2, String Outfile, int size);

    static {
        System.loadLibrary("NeowineNative");
    }
}
