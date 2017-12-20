package com.orengesunshine.chatory.util;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
    private static final String IS_USER_NAME_SET = "user_name_set";
    public static final String APP_USER_NAME = "app_user_name";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1; // any #
    private static final int NAME_START_POS = 19;
    private static final int DATE_START_POS = 10;
    public static final String INVITATION = "invitation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_text_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View view = getWindow().getDecorView().getRootView();
        Snackbar.make(view,"got data ",Snackbar.LENGTH_SHORT).show();
        Intent i =getIntent();

        if (ActivityCompat.checkSelfPermission(LoadTextFileActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoadTextFileActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                ActivityCompat.requestPermissions(LoadTextFileActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            //permission is granted
            @SuppressWarnings("unchecked")
            ArrayList<Uri> uriList = (ArrayList<Uri>) i.getSerializableExtra(Intent.EXTRA_STREAM);
            if (uriList!=null){
                loadTextFile(uriList);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void loadTextFile(ArrayList<Uri> uriList){
        String path = uriList.get(0).getPath();
        try {
            FileInputStream fis = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            makeChatRoom(reader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * makes chatRoom object and save it to database
     * @param reader is .txt file from line app in BufferedReader
     */
    private void makeChatRoom(BufferedReader reader) {

        ChatRoom chatRoom = new ChatRoom();
        ArrayList<Chat> chats;

        try {
            //get all user(s) in the chat
            String chatWithNames = reader.readLine();
            Log.d(TAG, "makeChatRoom: "+chatWithNames);
            String onlyNames = chatWithNames.substring(NAME_START_POS);
            String[] names = onlyNames.split(",");
            chatRoom.setNames(names);

            //get date
            String savedDate = reader.readLine().substring(DATE_START_POS);
            chatRoom.setSavedOn(savedDate);

            if (isRoomExist(onlyNames)){
                Log.d(TAG, "makeChatRoom: room already exists, not saving");
            }else {
                ContentValues values = new ContentValues();
                values.put(ChatRoomEntry.PARTICIPANTS_NAME,onlyNames);
                values.put(ChatRoomEntry.CREATED_AT,savedDate);
                values.put(ChatRoomEntry.UPDATED_AT,savedDate);
                Uri uri = getContentResolver().insert(ChatRoomEntry.CONTENT_URI,values);
                if (uri!=null){
                    long roomId = ContentUris.parseId(uri);
                    chats = createChats(reader,roomId);

                    boolean firstTimeOrWithoutName = true;
                    if (!PrefUtil.getBoolean(IS_USER_NAME_SET)){
                        saveAppUserName(names,chats);
                    }
                    chatRoom.setChats(chats);
                }else {
                    Log.d(TAG, "makeChatRoom: uri is null");
                }
            }
            reader.close();
            onBackPressed();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * try to save app user name to preference.
     * can be successful when app user sent at least one message
     * AND it is a group message without anyone leaving the group
     *
     * @param names names from "chat history with XX,XX...."
     * @param chats all the chats to get names
     */
    private void saveAppUserName(String[] names, ArrayList<Chat> chats) {

        String appUserName = null;
        Set<String> nameListFromChats = new HashSet<>();
        Log.d(TAG, "saveAppUserName: chat with "+Arrays.toString(names));

        for (Chat chat:chats){
            nameListFromChats.add(chat.getName());
        }
        Log.d(TAG, "saveAppUserName: "+nameListFromChats);

        nameListFromChats.removeAll(Arrays.asList(names));
        nameListFromChats.remove(INVITATION);

        if (nameListFromChats.size()==1){
            appUserName = nameListFromChats.iterator().next();
            PrefUtil.saveString(APP_USER_NAME,appUserName);
            PrefUtil.saveBoolean(IS_USER_NAME_SET,true);
        }

        Log.d(TAG, "saveAppUserName: "+PrefUtil.getBoolean(IS_USER_NAME_SET)+" , "+PrefUtil.getString(APP_USER_NAME));


    }

    private boolean isRoomExist(String participants){
        String projection[] = new String[]{ChatRoomEntry._ID};
        String selection = ChatRoomEntry.PARTICIPANTS_NAME+"=?";
        String[] selectionArg = new String[]{participants};
        Cursor cursor = getContentResolver().query(
                ChatRoomEntry.CONTENT_URI,
                projection,
                selection,
                selectionArg,
                null
        );

        // count is 0 if there is no room with same names
        if (cursor!=null){
            return cursor.getCount()!=0;
        }
        return false;
    }

    private ArrayList<Chat> createChats(BufferedReader reader,long roomId) throws IOException{
        ArrayList<Chat> chats = new ArrayList<>();

        //get all chats
        Chat chat = null;
        StringBuilder sb = null;
        String line;
        String chatDate = "";
        boolean skipFirstLine = false;

        //todo: split data for larger file
        while ((line = reader.readLine()) != null) {
            //skip a empty line after date
            if (!skipFirstLine){
                skipFirstLine = true;
                continue;
            }else if (line.length()==0){
                continue; //skip empty line between lines
                //this could be either btwn chats or user input
            }

            if (isDate(line)){ //meaning new date
                chatDate = line; //save date for same day chat
                if(chat!=null){ //chat is null only on chat line
                    chat.setText(sb.toString());
                }
            }else {
                String[] chatLine = line.split("\t");

                if (isTime(chatLine[0])&&chatLine.length>1){
                    // text with time, which means new chat and structure is "date name text"
                    if (chat!=null){
                        chat.setText(sb.toString());
                        chats.add(chat); // if chat exist, save it, and make new chat
                    }
                    chat = new Chat();
                    sb = new StringBuilder();
                    chat.setDate(chatDate);
                    chat.setTime(chatLine[0]);

                    if (chatLine.length<3){ // means this is invitation message
                        chat.setName(INVITATION);
                        sb.append(chatLine[1]); // 2nd contains "XX invited YYY,ZZZ to the group" etc
                    }else { //normal chat contains 3 parts (time, name, content)
                        chat.setName(chatLine[1]);
                        sb.append(chatLine[2]);
                    }
                } else {
                    // only text, meaning continuous from previous line

                    if (sb!=null){
                        sb.append(chatLine[0]).append("\n");
                    }
                }
            }
        }

        boolean isSaved = saveChats(chats,roomId);
        if (isSaved){
            makeToast("successfully saved line history");
        }else {
            makeToast("failed to save line history");
        }

        Chat lastChat = chats.get(chats.size()-1);
        ContentValues values = new ContentValues();
        values.put(ChatRoomEntry.LAST_CHAT_DATE,lastChat.getDate());
        values.put(ChatRoomEntry.LAST_CHAT_MESSAGE,lastChat.getText());
        Uri uri = ContentUris.withAppendedId(ChatRoomEntry.CONTENT_URI,roomId);
        int rowsUpdated = getContentResolver().update(uri,values,null,null);
//        Log.d(TAG, "createChats: update "+rowsUpdated);
        return chats;
    }

    private boolean saveChats(List<Chat> chats, long roomId){
        boolean saveSuccess = true;
        for (Chat c: chats){
            ContentValues values = new ContentValues();
            values.put(ChatEntry.CHAT_ROOM_ID,roomId);
            values.put(ChatEntry.NAME,c.getName());
            values.put(ChatEntry.MESSAGE,c.getText());
            values.put(ChatEntry.CREATED_AT_DATE,c.getDate());
            values.put(ChatEntry.CREATED_AT_TIME,c.getTime());
            Uri uri = getContentResolver().insert(ChatEntry.CONTENT_URI,values);
            if (uri==null){
                saveSuccess = false;
                Log.d(TAG, "createChats: inserting failed for "+c.getTime() );
            }
        }

        return saveSuccess;
    }

    private boolean isDate(String line){
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd(EEE)",new Locale("en","US"));
        format.setLenient(false); //has to match day of week, go wrong with time zone if not set?
        try {
            format.parse(line);
            return true;
        } catch (ParseException e) {
            //means it's not a date so return false

            return false;
        }
    }

    private boolean isTime(String line){
        SimpleDateFormat format = new SimpleDateFormat("h:mm",new Locale("en","US"));
        try {
            format.parse(line);
            return true;
        } catch (ParseException e) {
            //means it's not time so return false

            return false;
        }
    }

    private void makeToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
