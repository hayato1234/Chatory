package com.orengesunshine.chatory.service;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

// todo: auto link, why checkout master gets everything in branch?????
// ask user and merge

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
            @SuppressWarnings("unchecked")
            ArrayList<Uri> uriList = (ArrayList<Uri>) i.getSerializableExtra(Intent.EXTRA_STREAM);
            if (uriList!=null){
                boolean shouldAskUser = isRoomExist(uriList.get(0));
                if (shouldAskUser){
                    askUser(uriList.get(0));
                }else {
                    startLoadingService(uriList.get(0));
                }
            }
//            onBackPressed();
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
        Log.d(TAG, "askUser: ");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Same name found in database. Do you want to...")
                .setNegativeButton("Save as new", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startLoadingService(uri);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        onBackPressed();
                    }
                })
                .setPositiveButton("Combine", new DialogInterface.OnClickListener() {
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
                @SuppressWarnings("unchecked")
                ArrayList<Uri> uriList = (ArrayList<Uri>) i.getSerializableExtra(Intent.EXTRA_STREAM);
                startLoadingService(uriList.get(0));
            } else {
                //user denied permission
                Toast.makeText(this,"data can't be saved without your permission",Toast.LENGTH_SHORT).show();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
