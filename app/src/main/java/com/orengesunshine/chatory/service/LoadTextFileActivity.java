package com.orengesunshine.chatory.service;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.data.ChatDbHelper;
import com.orengesunshine.chatory.ui.MainActivity;
import com.orengesunshine.chatory.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;


public class LoadTextFileActivity extends AppCompatActivity {

    private static final String TAG = "LoadTextFileActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1; // any #
    private static final int NAME_START_POS = 19;
    private Intent i;
    private Cursor cursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_text_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        i =getIntent();

        if (ActivityCompat.checkSelfPermission(LoadTextFileActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //permission is not granted

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(LoadTextFileActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }

        } else {
            //permission is granted
            handleIntent(i);
        }
    }

    private void handleIntent(Intent i){
        String mimeType = i.getType();
        if (mimeType==null) {
            Log.d(TAG, "onCreate: mimeType is null "+i.getData());
            //import from option
            if (i.getData()!=null){
                //mimeType = null and data means it's from menu option import
                importDatabase(i.getData());
            }
            return;
        }
        // shared text file
        if (mimeType.equals("text/plain")){
            Uri uri = null;
            if (Intent.ACTION_SEND.equals(i.getAction())){
                String s = (String) i.getSerializableExtra(Intent.EXTRA_TEXT);
                uri = Uri.parse(FileUtils.writeToFile(this,s));
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(i.getAction())){
                @SuppressWarnings("unchecked")
                ArrayList<Uri> uriList = (ArrayList<Uri>) i.getSerializableExtra(Intent.EXTRA_STREAM);
                uri = uriList.get(0);
//                if (uriList!=null){
//                    boolean shouldAskUser = isRoomExist(uriList.get(0));
//                    if (shouldAskUser){
//                        askUser(uriList.get(0));
//                    }else {
//                        startLoadingService(uriList.get(0));
//                    }
//                }
            }

            if (uri!=null){
                boolean shouldAskUser = isRoomExist(uri);
                if (shouldAskUser){
                    askUser(uri);
                }else {
                    startLoadingService(uri);
                }
            }

        }else if (mimeType.equals("application/x-sqlite3")){ //shared database
            Uri uri = i.getParcelableExtra(Intent.EXTRA_STREAM);
            importDatabase(uri);

        } else {
            Toast.makeText(this, R.string.file_fail,Toast.LENGTH_LONG).show();
            onBackPressed();
        }
    }

    private void importDatabase(Uri uri) {
        try {
            FileInputStream is = (FileInputStream) getContentResolver().openInputStream(uri);
            File data  = Environment.getDataDirectory();
            String  currentDBPath= "//data//" + "com.orengesunshine.chatory"
                    + "//databases//" + ChatDbHelper.DB_NAME;
            File  backupDB= new File(data, currentDBPath);
            FileUtils.copyDb(is,backupDB);
//            Log.d(TAG, "onCreate: start activity");
            startActivity(new Intent(this, MainActivity.class));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassCastException cce){
            Log.d(TAG, "onCreate cast error: "+cce.toString());
        }
    }

    private void startLoadingService(Uri uri){
        Intent intent = new Intent(this,LoadTextFileService.class);
        intent.setData(uri);
        startService(intent);
        Toast.makeText(this,"saving the file",Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    /**
     * check if there is a room with same name
     */
    private boolean isRoomExist(Uri uri){
        String chatWithNames = null;

        try {
            FileInputStream fis = new FileInputStream(uri.getPath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            chatWithNames = reader.readLine();
            String onlyNames = chatWithNames.substring(NAME_START_POS);
            String projection[] = new String[]{ChatContract.ChatRoomEntry._ID, ChatContract.ChatRoomEntry.LAST_CHAT_MESSAGE,ChatContract.ChatRoomEntry.LAST_CHAT_DATE};
            String selection = ChatContract.ChatRoomEntry.PARTICIPANTS_NAME+"=?";
            String[] selectionArg = new String[]{onlyNames};
            cursor = getContentResolver().query(
                    ChatContract.ChatRoomEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArg,
                    null
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // count is 0 if there is no room with same names
        if (cursor!=null){
            return cursor.getCount()!=0;
        }
        return false;
    }

    private void askUser(final Uri uri){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.ask_user_for_combine_action)
                .setNegativeButton(getString(R.string.save_as_new), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startLoadingService(uri);
                    }
                })
                .setNeutralButton(getString(R.string.canel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        onBackPressed();
                    }
                })
                .setPositiveButton(getString(R.string.combine), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startCombineService(uri);
                    }
                });
        builder.create().show();
    }

    private void startCombineService(Uri uri){
        int idIndex = cursor.getColumnIndex(ChatContract.ChatRoomEntry._ID);
        int messageIndex = cursor.getColumnIndex(ChatContract.ChatRoomEntry.LAST_CHAT_MESSAGE);
        int dateIndex = cursor.getColumnIndex(ChatContract.ChatRoomEntry.LAST_CHAT_DATE);

//        Log.d(TAG, "startCombineService: "+cursor.getString(0));
        cursor.moveToFirst();
        int id = cursor.getInt(idIndex);
        String message = cursor.getString(messageIndex);
        String date = cursor.getString(dateIndex);
        cursor.close();
        Intent intent = new Intent(this,LoadAndMergeFileService.class);
        intent.setData(uri);
        intent.putExtra(LoadAndMergeFileService.LAST_CHAT_ID,id);
        intent.putExtra(LoadAndMergeFileService.LAST_CHAT_DATE,date);
        intent.putExtra(LoadAndMergeFileService.LAST_CHAT_MESSAGE,message);
        startService(intent);
        onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
//                @SuppressWarnings("unchecked")
//                ArrayList<Uri> uriList = (ArrayList<Uri>) i.getSerializableExtra(Intent.EXTRA_STREAM);
//                startLoadingService(uriList.get(0));
                handleIntent(i);
            } else {
                //user denied permission
                Toast.makeText(this, R.string.warning_require_permission,Toast.LENGTH_SHORT).show();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
