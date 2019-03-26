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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

/**
 * This class sits between the Main activity and the FileManager class.
 * To keep the FileManager class modular, this class exists to handle
 * UI events and communicate that information to the FileManger class
 * <p/>
 * This class is responsible for the buttons onClick method. If one needs
 * to change the functionality of the buttons found from the Main activity
 * or add button logic, this is the class that will need to be edited.
 * <p/>
 * This class is responsible for handling the information that is displayed
 * from the list view (the files and folder) with a a nested class TableRow.
 * The TableRow class is responsible for displaying which icon is shown for each
 * entry. For example a folder will display the folder icon, a Word doc will
 * display a word icon and so on. If more icons are to be added, the TableRow
 * class must be updated to display those changes.
 *
 * @author Joe Berria
 */
public class EventHandler implements OnClickListener {
    /*
     * Unique types to control which file operation gets
     * performed in the background
     */
    private static final int SEARCH_TYPE = 0x00;
    private static final int COPY_TYPE = 0x01;
    private static final int UNZIP_TYPE = 0x02;
    private static final int UNZIPTO_TYPE = 0x03;
    private static final int ZIP_TYPE = 0x04;
    private static final int DELETE_TYPE = 0x05;
    private static final int MANAGE_DIALOG = 0x06;

    private final Context mContext;
    private final FileManager mFileMang;
    private TableRow mDelegate;

    private boolean multi_select_flag = false;
    private boolean delete_after_copy = false;
    private boolean thumbnail_flag = true;
    private int mColor = Color.WHITE;

    //the list used to feed info into the array adapter and when multi-select is on
    private ArrayList<String> mDataSource, mMultiSelectData;
    private TextView mPathLabel;
    private TextView mInfoLabel;
    public Util e4_util = Util.getInstance();
    MyData mEncRGN;
    ArrayList<String> mCipherList;
    MyAdapter mEncAdapter;
    /**
     * Creates an EventHandler object. This object is used to communicate
     * most work from the Main activity to the FileManager class.
     *
     * @param context The context of the main activity e.g  Main
     * @param manager The FileManager object that was instantiated from Main
     */
    public EventHandler(Context context, final FileManager manager) {
        mContext = context;
        mFileMang = manager;

        mDataSource = new ArrayList<String>(mFileMang.setHomeDir(Environment.getExternalStorageDirectory().getPath()));
    }
	public void SetEncAdapters(MyData encRGN,MyAdapter EncAdapter, ArrayList<String> cipherList)
		{

				mEncRGN = encRGN;
				mCipherList = cipherList;
				mEncAdapter = EncAdapter;
		}

    /**
     * This constructor is called if the user has changed the screen orientation
     * and does not want the directory to be reset to home.
     *
     * @param context  The context of the main activity e.g  Main
     * @param manager  The FileManager object that was instantiated from Main
     * @param location The first directory to display to the user
     */
    public EventHandler(Context context, final FileManager manager, String location) {
        mContext = context;
        mFileMang = manager;

        mDataSource = new ArrayList<String>(mFileMang.getNextDir(location, true));
    }

    /**
     * This method is called from the Main activity and this has the same
     * reference to the same object so when changes are made here or there
     * they will display in the same way.
     *
     * @param adapter The TableRow object
     */
    public void setListAdapter(TableRow adapter) {
        mDelegate = adapter;
    }

    /**
     * This method is called from the Main activity and is passed
     * the TextView that should be updated as the directory changes
     * so the user knows which folder they are in.
     *
     * @param path  The label to update as the directory changes
     * @param label the label to update information
     */
    public void setUpdateLabels(TextView path, TextView label) {
        mPathLabel = path;
        mInfoLabel = label;
    }

    /**
     * @param color
     */
    public void setTextColor(int color) {
        mColor = color;
    }

    /**
     * Set this true and thumbnails will be used as the icon for image files. False will
     * show a default image.
     *
     * @param show
     */
    public void setShowThumbnails(boolean show) {
        thumbnail_flag = show;
    }

    /**
     * If you want to move a file (cut/paste) and not just copy/paste use this method to
     * tell the file manager to delete the old reference of the file.
     *
     * @param delete true if you want to move a file, false to copy the file
     */
    public void setDeleteAfterCopy(boolean delete) {
        delete_after_copy = delete;
    }

    /**
     * Indicates whether the user wants to select
     * multiple files or folders at a time.
     * <br><br>
     * false by default
     *
     * @return true if the user has turned on multi selection
     */
    public boolean isMultiSelected() {
        return multi_select_flag;
    }

    /**
     * Use this method to determine if the user has selected multiple files/folders
     *
     * @return returns true if the user is holding multiple objects (multi-select)
     */
    public boolean hasMultiSelectData() {
        return (mMultiSelectData != null && mMultiSelectData.size() > 0);
    }

    /**
     * Will search for a file then display all files with the
     * search parameter in its name
     *
     * @param name the name to search for
     */
    public void searchForFile(String name) {
        new BackgroundWork(SEARCH_TYPE).execute(name);
    }

    /**
     * Will delete the file name that is passed on a background
     * thread.
     *
     * @param name
     */
    public void deleteFile(String name) {
        new BackgroundWork(DELETE_TYPE).execute(name);
    }

    /**
     * Will copy a file or folder to another location.
     *
     * @param oldLocation from location
     * @param newLocation to location
     */
    public void copyFile(String oldLocation, String newLocation) {
        String[] data = {oldLocation, newLocation};

        new BackgroundWork(COPY_TYPE).execute(data);
    }

    /**
     * @param newLocation
     */
    public void copyFileMultiSelect(String newLocation) {
        String[] data;
        int index = 1;

        if (mMultiSelectData.size() > 0) {
            data = new String[mMultiSelectData.size() + 1];
            data[0] = newLocation;

            for (String s : mMultiSelectData)
                data[index++] = s;

            new BackgroundWork(COPY_TYPE).execute(data);
        }
    }

    /**
     * This will extract a zip file to the same directory.
     *
     * @param file the zip file name
     * @param path the path were the zip file will be extracted (the current directory)
     */
    public void unZipFile(String file, String path) {
        new BackgroundWork(UNZIP_TYPE).execute(file, path);
    }

    /**
     * This method will take a zip file and extract it to another
     * location
     *
     * @param name   the name of the of the new file (the dir name is used)
     * @param newDir the dir where to extract to
     * @param oldDir the dir where the zip file is
     */
    public void unZipFileToDir(String name, String newDir, String oldDir) {
        new BackgroundWork(UNZIPTO_TYPE).execute(name, newDir, oldDir);
    }

    /**
     * Creates a zip file
     *
     * @param zipPath the path to the directory you want to zip
     */
    public void zipFile(String zipPath) {
        new BackgroundWork(ZIP_TYPE).execute(zipPath);
    }

    /**
     * This method, handles the button presses of the top buttons found
     * in the Main activity.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.back_button:
                if (mFileMang.getCurrentDir() != "/") {
                    if (multi_select_flag) {
                        mDelegate.killMultiSelect(true);
                        Toast.makeText(mContext, "Multi-select is now off",
                                Toast.LENGTH_SHORT).show();
                    }

                    updateDirectory(mFileMang.getPreviousDir());
                    if (mPathLabel != null)
                        mPathLabel.setText(mFileMang.getCurrentDir());
                }
                break;
        }
    }

    /**
     * will return the data in the ArrayList that holds the dir contents.
     *
     * @param position the indext of the arraylist holding the dir content
     * @return the data in the arraylist at position (position)
     */
    public String getData(int position) {

        if (position > mDataSource.size() - 1 || position < 0)
            return null;

        return mDataSource.get(position);
    }

    /**
     * called to update the file contents as the user navigates there
     * phones file system.
     *
     * @param content an ArrayList of the file/folders in the current directory.
     */
    public void updateDirectory(ArrayList<String> content) {
        if (!mDataSource.isEmpty()) {
            mDataSource.clear();
            mEncRGN.strs.clear();
            mEncRGN.imgs.clear();
            mCipherList.clear();
            Log.d("E4NET", "clear array lists");
        }

        for (String data : content) {

            int index = 0;
            String EncType =new String();
            index = data.lastIndexOf('.');
            if( index >= 3)
                EncType = data.substring(index - 3, index) ;
           // else
             //   continue;

            Log.d("E4NET", "EncType EncType EncType =  "+ EncType);
            String temp = mFileMang.getCurrentDir();
            mDataSource.add(data);
            if(EncType.equals("ENC") ) {
                mEncRGN.strs.add(temp + "/" + data);
                mCipherList.add(temp + "/" + data);

                File file = new File(temp + "/" + data);
                Log.d("E4NET", "CURRENT FILE PATH =  " + temp + "/" + data);
                if (file != null && file.isFile()) {
                    final String ext = file.toString();
                    String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
                    /* This series of else if statements will determine which
                     * icon is displayed
                     */
                    if (sub_ext.equalsIgnoreCase("pdf")) {
                        mEncRGN.imgs.add(R.drawable.ico_pdf_256);

                    } else if (sub_ext.equalsIgnoreCase("mp3") ||
                            sub_ext.equalsIgnoreCase("wma") ||
                            sub_ext.equalsIgnoreCase("m4a") ||
                            sub_ext.equalsIgnoreCase("m4p")) {

                        mEncRGN.imgs.add(R.drawable.music);

                    } else if (sub_ext.equalsIgnoreCase("png") ||
                            sub_ext.equalsIgnoreCase("jpg") ||
                            sub_ext.equalsIgnoreCase("jpeg") ||
                            sub_ext.equalsIgnoreCase("gif") ||
                            sub_ext.equalsIgnoreCase("tiff")) {
                        mEncRGN.imgs.add(R.drawable.ico_menu_photo);

                    } else if (sub_ext.equalsIgnoreCase("zip") ||
                            sub_ext.equalsIgnoreCase("gzip") ||
                            sub_ext.equalsIgnoreCase("gz")) {

                        mEncRGN.imgs.add(R.drawable.ico_zip_256);

                    } else if (sub_ext.equalsIgnoreCase("m4v") ||
                            sub_ext.equalsIgnoreCase("wmv") ||
                            sub_ext.equalsIgnoreCase("3gp") ||
                            sub_ext.equalsIgnoreCase("mp4")) {

                        mEncRGN.imgs.add(R.drawable.movies);

                    } else if (sub_ext.equalsIgnoreCase("doc") ||
                            sub_ext.equalsIgnoreCase("docx")) {

                        mEncRGN.imgs.add(R.drawable.ico_doc_256);

                    } else if (sub_ext.equalsIgnoreCase("xls") ||
                            sub_ext.equalsIgnoreCase("xlsx")) {

                        mEncRGN.imgs.add(R.drawable.ico_xls_256);

                    } else if (sub_ext.equalsIgnoreCase("ppt")) {

                        mEncRGN.imgs.add(R.drawable.ico_ppt_256);

                    } else if (sub_ext.equalsIgnoreCase("pptx")) {

                        mEncRGN.imgs.add(R.drawable.ico_pptx_256);

                    } else if (sub_ext.equalsIgnoreCase("html")) {
                        mEncRGN.imgs.add(R.drawable.ico_html_256);

                    } else if (sub_ext.equalsIgnoreCase("xml")) {
                        mEncRGN.imgs.add(R.drawable.xml32);

                    } else if (sub_ext.equalsIgnoreCase("conf")) {
                        mEncRGN.imgs.add(R.drawable.config32);

                    } else if (sub_ext.equalsIgnoreCase("apk")) {
                        mEncRGN.imgs.add(R.drawable.appicon);

                    } else if (sub_ext.equalsIgnoreCase("jar")) {
                        mEncRGN.imgs.add(R.drawable.jar32);

                    } else if (ext.contains("_ENC")){
                        mEncRGN.imgs.add(R.drawable.ico_enc_256);
                        Log.e("yea", "yeaaa");

                    } else {
                        mEncRGN.imgs.add(R.drawable.ico_txt_256);
                    }

                } else if (file != null && file.isDirectory()) {
                    mEncRGN.imgs.add(R.drawable.folder_256);
                }
            }

        }
        mEncAdapter.notifyDataSetChanged();
        mDelegate.notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView name;
        TextView filePath;
        ImageView icon;
        ImageView enc_icon;
    }


    /**
     * A nested class to handle displaying a custom view in the ListView that
     * is used in the Main activity. If any icons are to be added, they must
     * be implemented in the getView method. This class is instantiated once in Main
     * and has no reason to be instantiated again.
     *
     * @author Joe Berria
     */
    public class TableRow extends ArrayAdapter<String> {
        private final int KB = 1024;
        private final int MG = KB * KB;
        private final int GB = MG * KB;
        private String display_size;
        private ArrayList<Integer> positions;
        private LinearLayout hidden_layout;

        public TableRow() {
            super(mContext, R.layout.tablerow, mDataSource);
        }

        public void addMultiPosition(int index, String path) {
            /*
            if (positions == null)
                positions = new ArrayList<Integer>();

            if (mMultiSelectData == null) {
                positions.add(index);
                add_multiSelect_file(path);

            } else if (mMultiSelectData.contains(path)) {
                if (positions.contains(index))
                    positions.remove(new Integer(index));

                mMultiSelectData.remove(path);

            } else {
                positions.add(index);
                add_multiSelect_file(path);
            }
            */
            notifyDataSetChanged();
        }

        /**
         * This will turn off multi-select and hide the multi-select buttons at the
         * bottom of the view.
         *
         * @param clearData if this is true any files/folders the user selected for multi-select
         *                  will be cleared. If false, the data will be kept for later use. Note:
         *                  multi-select copy and move will usually be the only one to pass false,
         *                  so we can later paste it to another folder.
         */
        public void killMultiSelect(boolean clearData) {
            multi_select_flag = false;

            if (positions != null && !positions.isEmpty())
                positions.clear();

            if (clearData)
                if (mMultiSelectData != null && !mMultiSelectData.isEmpty())
                    mMultiSelectData.clear();

            notifyDataSetChanged();
        }

        public String getFilePermissions(File file) {
            String per = "-";

            if (file.isDirectory())
                per += "d";
            if (file.canRead())
                per += "r";
            if (file.canWrite())
                per += "w";

            return per;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder mViewHolder;
            int num_items = 0;
            String temp = mFileMang.getCurrentDir();
            File file = new File(temp + "/" + mDataSource.get(position));
            String[] list = file.list();

            if (list != null)
                num_items = list.length;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.tablerow, parent, false);

                mViewHolder = new ViewHolder();
                mViewHolder.name = (TextView) convertView.findViewById(R.id.name);
                mViewHolder.filePath = (TextView) convertView.findViewById(R.id.filePath);
                mViewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
                mViewHolder.enc_icon = (ImageView) convertView.findViewById(R.id.enc_dec_icon);
                convertView.setTag(mViewHolder);

            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            mViewHolder.name.setTextColor(mColor);
            mViewHolder.enc_icon.setVisibility(View.INVISIBLE);

            if (file != null && file.isFile()) {
                final String ext = file.toString();
                String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
                String enc = ext.substring(ext.lastIndexOf(".") - 4);
                /* This series of else if statements will determine which
                 * icon is displayed
    			 */

                if (ext.contains("_ENC")){
                    mViewHolder.icon.setImageResource(R.drawable.ico_enc_situation);

                }

                else if (sub_ext.equalsIgnoreCase("pdf")) {
                    mViewHolder.icon.setImageResource(R.drawable.ico_pdf_256);

                } else if (sub_ext.equalsIgnoreCase("mp3") ||
                        sub_ext.equalsIgnoreCase("wma") ||
                        sub_ext.equalsIgnoreCase("m4a") ||
                        sub_ext.equalsIgnoreCase("m4p")) {

                    mViewHolder.icon.setImageResource(R.drawable.music);

                } else if (sub_ext.equalsIgnoreCase("png") ||
                        sub_ext.equalsIgnoreCase("jpg") ||
                        sub_ext.equalsIgnoreCase("jpeg") ||
                        sub_ext.equalsIgnoreCase("gif") ||
                        sub_ext.equalsIgnoreCase("tiff")) {
                    if (file.getParent().equals(Environment.getExternalStorageDirectory() + "/Neowine/SPhoto_enc")) {
                        try {
                            if (!e4_util.enc_dec_file(file)) {
                                mViewHolder.enc_icon.setVisibility(View.VISIBLE);
                                //mViewHolder.icon.setImageResource(R.drawable.ico_enc_situation);
                            } else {
                                mViewHolder.enc_icon.setVisibility(View.INVISIBLE);
                                //mViewHolder.icon.setImageResource(R.drawable.ico_enc_situation);
                                //mViewHolder.icon.setImageResource(R.drawable.ico_enc_situation);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Glide.with(mViewHolder.icon.getContext()).load(file).thumbnail(0.1f).into(mViewHolder.icon);

                } else if (sub_ext.equalsIgnoreCase("zip") ||
                        sub_ext.equalsIgnoreCase("gzip") ||
                        sub_ext.equalsIgnoreCase("gz")) {

                    mViewHolder.icon.setImageResource(R.drawable.ico_zip_256);

                } else if (sub_ext.equalsIgnoreCase("m4v") ||
                        sub_ext.equalsIgnoreCase("wmv") ||
                        sub_ext.equalsIgnoreCase("3gp") ||
                        sub_ext.equalsIgnoreCase("mp4")) {

                    mViewHolder.icon.setImageResource(R.drawable.movies);

                } else if (sub_ext.equalsIgnoreCase("doc") ||
                        sub_ext.equalsIgnoreCase("docx")) {

                    mViewHolder.icon.setImageResource(R.drawable.ico_doc_256);

                } else if (sub_ext.equalsIgnoreCase("xls") ||
                        sub_ext.equalsIgnoreCase("xlsx")) {

                    mViewHolder.icon.setImageResource(R.drawable.ico_xls_256);

                } else if (sub_ext.equalsIgnoreCase("ppt")) {

                    mViewHolder.icon.setImageResource(R.drawable.ico_ppt_256);

                } else if (sub_ext.equalsIgnoreCase("pptx")) {

                    mViewHolder.icon.setImageResource(R.drawable.ico_pptx_256);

                } else if (sub_ext.equalsIgnoreCase("html")) {
                    mViewHolder.icon.setImageResource(R.drawable.ico_html_256);

                } else if (sub_ext.equalsIgnoreCase("xml")) {
                    mViewHolder.icon.setImageResource(R.drawable.xml32);

                } else if (sub_ext.equalsIgnoreCase("conf")) {
                    mViewHolder.icon.setImageResource(R.drawable.config32);

                } else if (sub_ext.equalsIgnoreCase("apk")) {
                    mViewHolder.icon.setImageResource(R.drawable.appicon);

                } else if (sub_ext.equalsIgnoreCase("jar")) {
                    mViewHolder.icon.setImageResource(R.drawable.jar32);

                } else {
                    mViewHolder.icon.setImageResource(R.drawable.ico_txt_256);
                }

            } else if (file != null && file.isDirectory()) {
                mViewHolder.icon.setImageResource(R.drawable.folder_256);
            }

            String permission = getFilePermissions(file);


            if (file.isFile()) {
                double size = file.length();
                if (size > GB)
                    display_size = String.format("%.2f Gb ", (double) size / GB);
                else if (size < GB && size > MG)
                    display_size = String.format("%.2f Mb ", (double) size / MG);
                else if (size < MG && size > KB)
                    display_size = String.format("%.2f Kb ", (double) size / KB);
                else
                    display_size = String.format("%.2f bytes ", (double) size);
            }

            mViewHolder.name.setText(file.getName());
            mViewHolder.filePath.setText(file.getPath());
            return convertView;
        }

        private void add_multiSelect_file(String src) {
            if (mMultiSelectData == null)
                mMultiSelectData = new ArrayList<String>();

            //mMultiSelectData.add(src);
        }
    }

    /**
     * A private inner class of EventHandler used to perform time extensive
     * operations. So the user does not think the the application has hung,
     * operations such as copy/past, search, unzip and zip will all be performed
     * in the background. This class extends AsyncTask in order to give the user
     * a progress dialog to show that the app is working properly.
     * <p/>
     * (note): this class will eventually be changed from using AsyncTask to using
     * Handlers and messages to perform background operations.
     *
     * @author Joe Berria
     */
    private class BackgroundWork extends AsyncTask<String, Void, ArrayList<String>> {
        private String file_name;
        private ProgressDialog pr_dialog;
        private int type;
        private int copy_rtn;

        private BackgroundWork(int type) {
            this.type = type;
        }

        /**
         * This is done on the EDT thread. this is called before
         * doInBackground is called
         */
        @Override
        protected void onPreExecute() {

            switch (type) {
                case SEARCH_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Searching",
                            "Searching current file system...",
                            true, true);
                    break;

                case COPY_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Copying",
                            "Copying file...",
                            true, false);
                    break;

                case UNZIP_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Unzipping",
                            "Unpacking zip file please wait...",
                            true, false);
                    break;

                case UNZIPTO_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Unzipping",
                            "Unpacking zip file please wait...",
                            true, false);
                    break;

                case ZIP_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Zipping",
                            "Zipping folder...",
                            true, false);
                    break;

                case DELETE_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Deleting",
                            "Deleting files...",
                            true, false);
                    break;
            }
        }

        /**
         * background thread here
         */
        @Override
        protected ArrayList<String> doInBackground(String... params) {

            switch (type) {
                case SEARCH_TYPE:
                    file_name = params[0];
                    ArrayList<String> found = mFileMang.searchInDirectory(mFileMang.getCurrentDir(),
                            file_name);
                    return found;

                case COPY_TYPE:
                    int len = params.length;

                    if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
                        for (int i = 1; i < len; i++) {
                            copy_rtn = mFileMang.copyToDirectory(params[i], params[0]);

                            if (delete_after_copy)
                                mFileMang.deleteTarget(params[i]);
                        }
                    } else {
//						copy_rtn = mFileMang.copyToDirectory(params[0], params[1]);
//
//						if(delete_after_copy)
//							mFileMang.deleteTarget(params[0]);
                        copy_rtn = mFileMang.copyToDirectory(params[0], params[1].substring(0, params[1].lastIndexOf("/")));

                        if (delete_after_copy)
                            mFileMang.deleteTarget(params[0]);
                    }

                    delete_after_copy = false;
                    return null;

                case UNZIP_TYPE:
                    mFileMang.extractZipFiles(params[0], params[1]);
                    return null;

                case UNZIPTO_TYPE:
                    mFileMang.extractZipFilesFromDir(params[0], params[1], params[2]);
                    return null;

                case ZIP_TYPE:
                    mFileMang.createZipFile(params[0]);
                    return null;

                case DELETE_TYPE:
                    int size = params.length;

                    for (int i = 0; i < size; i++)
                        mFileMang.deleteTarget(params[i]);

                    return null;
            }
            return null;
        }

        /**
         * This is called when the background thread is finished. Like onPreExecute, anything
         * here will be done on the EDT thread.
         */
        @Override
        protected void onPostExecute(final ArrayList<String> file) {
            final CharSequence[] names;
            int len = file != null ? file.size() : 0;

            switch (type) {
                case SEARCH_TYPE:
                    if (len == 0) {
                        Toast.makeText(mContext, "Couldn't find " + file_name,
                                Toast.LENGTH_SHORT).show();

                    } else {
                        names = new CharSequence[len];

                        for (int i = 0; i < len; i++) {
                            String entry = file.get(i);
                            names[i] = entry.substring(entry.lastIndexOf("/") + 1, entry.length());
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Found " + len + " file(s)");
                        builder.setItems(names, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int position) {
                                String path = file.get(position);
                                updateDirectory(mFileMang.getNextDir(path.
                                        substring(0, path.lastIndexOf("/")), true));
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    pr_dialog.dismiss();
                    break;

                case COPY_TYPE:
//					if(mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
//						multi_select_flag = false;
//						mMultiSelectData.clear();
//					}
                    Log.d(e4_util.e4_tag(), "copy_rtn : " + copy_rtn);
                    if (copy_rtn == 0) {
                        Toast.makeText(mContext, "File successfully copied and pasted",
                                Toast.LENGTH_SHORT).show();
                        updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    } else
                        Toast.makeText(mContext, "Copy pasted failed", Toast.LENGTH_SHORT).show();

                    pr_dialog.dismiss();
                    mInfoLabel.setText("");
                    break;

                case UNZIP_TYPE:
                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;

                case UNZIPTO_TYPE:
                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;

                case ZIP_TYPE:
                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;

                case DELETE_TYPE:
                    if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
                        mMultiSelectData.clear();
                        multi_select_flag = false;
                    }

                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    mInfoLabel.setText("");
                    break;
            }
        }
    }

    /*
     * getExternalFilesDir will create file in primary external storage
	 * (usually is main storage's FAT partition)
	 * 
	 * */
    @TargetApi(19)
    String createExternalSDStorage(Context context) {

        // Create a path where we will place our private file on external
        // storage.
        File[] sdcards = context.getExternalFilesDirs(null);

        ////++++ Modified by Mason, 20150809, Fix a bug
        if ((sdcards.length > 1) && (sdcards[1] != null)) return sdcards[1].getPath();

        return null;
    }

}
