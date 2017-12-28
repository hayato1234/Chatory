package com.orengesunshine.chatory.util;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract.ChatRoomEntry;
import com.orengesunshine.chatory.data.ChatContract.ChatEntry;
import com.orengesunshine.chatory.model.Chat;
import com.orengesunshine.chatory.model.ChatRoom;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LoadTextFileActivity extends AppCompatActivity {

    private static final String TAG = "LoadTextFileActivity";
//    private static final String IS_USER_NAME_SET = "user_name_set";
//    public static final String APP_USER_NAME = "app_user_name";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1; // any #
//    private static final int NAME_START_POS = 19;
//    private static final int DATE_START_POS = 10;
//    public static final String INVITATION = "invitation";
//    public static final String DATE = "date";
    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_text_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        View view = getWindow().getDecorView().getRootView();
//        Snackbar.make(view,"got data ",Snackbar.LENGTH_SHORT).show();
        i =getIntent();

        if (ActivityCompat.checkSelfPermission(LoadTextFileActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //permission is not granted

            ActivityCompat.requestPermissions(LoadTextFileActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

//            if (ActivityCompat.shouldShowRequestPermissionRationale(LoadTextFileActivity.this,
//                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
//
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//
//            }
        } else {
            //permission is granted
            @SuppressWarnings("unchecked")
            ArrayList<Uri> uriList = (ArrayList<Uri>) i.getSerializableExtra(Intent.EXTRA_STREAM);
            if (uriList!=null){
                Intent intent = new Intent(this,LoadTextFileService.class);
                intent.setData(uriList.get(0));
                startService(intent);
                Toast.makeText(this,"saving the file",Toast.LENGTH_SHORT).show();
            }
            onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                @SuppressWarnings("unchecked")
                ArrayList<Uri> uriList = (ArrayList<Uri>) i.getSerializableExtra(Intent.EXTRA_STREAM);
                Intent intent = new Intent(this,LoadTextFileService.class);
                intent.setData(uriList.get(0));
                startService(intent);
                Toast.makeText(this,"saving the file",Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                //user denied permission
                Toast.makeText(this,"data can't be saved without your permission",Toast.LENGTH_SHORT).show();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
