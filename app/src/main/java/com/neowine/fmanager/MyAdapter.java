package com.neowine.fmanager;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

class MyAdapter extends BaseAdapter {
    Context context;
    int layout;
    MyData myData;
    LayoutInflater inf;

    public MyAdapter(Context context, int layout, MyData myData) {
        this.context = context;
        this.layout = layout;
        this.myData = myData;
        inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return myData.imgs.size();
    }

    @Override
    public Object getItem(int position) {
        return myData.imgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView = inf.inflate(layout, null);
        ImageView iv = (ImageView)convertView.findViewById(R.id.imageView1);
        TextView tv = (TextView)convertView.findViewById(R.id.txt_filename);
        iv.setImageResource(myData.imgs.get(position));
        String temp = myData.strs.get(position);
        String temp_sub = temp.substring(temp.lastIndexOf("/") + 1);
        Log.d("View.OnDragListener","String : " + temp_sub);
        tv.setText(temp_sub);
        File file = new File(myData.strs.get(position) );
        if (file != null && file.isFile()) {
            final String ext = file.toString();
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
            /* This series of else if statements will determine which
             * icon is displayed
             */


            if (sub_ext.equalsIgnoreCase("pdf")) {
                iv.setImageResource(R.drawable.ico_pdf_256);

            } else if (sub_ext.equalsIgnoreCase("mp3") ||
                    sub_ext.equalsIgnoreCase("wma") ||
                    sub_ext.equalsIgnoreCase("m4a") ||
                    sub_ext.equalsIgnoreCase("m4p")) {

                iv.setImageResource(R.drawable.music);

            } else if (sub_ext.equalsIgnoreCase("png") ||
                    sub_ext.equalsIgnoreCase("jpg") ||
                    sub_ext.equalsIgnoreCase("jpeg") ||
                    sub_ext.equalsIgnoreCase("gif") ||
                    sub_ext.equalsIgnoreCase("tiff")) {
                //iv.setImageResource(R.drawable.ico_menu_photo);
                iv.setImageResource(R.drawable.ico_enc_situation);

            } else if (sub_ext.equalsIgnoreCase("zip") ||
                    sub_ext.equalsIgnoreCase("gzip") ||
                    sub_ext.equalsIgnoreCase("gz")) {

                iv.setImageResource(R.drawable.ico_zip_256);

            } else if (sub_ext.equalsIgnoreCase("m4v") ||
                    sub_ext.equalsIgnoreCase("wmv") ||
                    sub_ext.equalsIgnoreCase("3gp") ||
                    sub_ext.equalsIgnoreCase("mp4")) {

                iv.setImageResource(R.drawable.movies);

            } else if (sub_ext.equalsIgnoreCase("doc") ||
                    sub_ext.equalsIgnoreCase("docx")) {

                iv.setImageResource(R.drawable.ico_doc_256);

            } else if (sub_ext.equalsIgnoreCase("xls") ||
                    sub_ext.equalsIgnoreCase("xlsx")) {

                iv.setImageResource(R.drawable.ico_xls_256);

            } else if (sub_ext.equalsIgnoreCase("ppt")) {

                iv.setImageResource(R.drawable.ico_ppt_256);

            } else if (sub_ext.equalsIgnoreCase("pptx")) {

                iv.setImageResource(R.drawable.ico_pptx_256);

            } else if (sub_ext.equalsIgnoreCase("html")) {
                iv.setImageResource(R.drawable.ico_html_256);

            } else if (sub_ext.equalsIgnoreCase("xml")) {
                iv.setImageResource(R.drawable.xml32);

            } else if (sub_ext.equalsIgnoreCase("conf")) {
                iv.setImageResource(R.drawable.config32);

            } else if (sub_ext.equalsIgnoreCase("apk")) {
                iv.setImageResource(R.drawable.appicon);

            } else if (sub_ext.equalsIgnoreCase("jar")) {
                iv.setImageResource(R.drawable.jar32);

            } else if (ext.contains("_ENC")){
                iv.setImageResource(R.drawable.ico_enc_256);
                Log.e("yea", "yeaaa");
            }

            else {
                iv.setImageResource(R.drawable.ico_txt_256);
            }

        } else if (file != null && file.isDirectory()) {
            iv.setImageResource(R.drawable.folder_256);
        }

        return convertView;
    }
}
