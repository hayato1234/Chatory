package com.orengesunshine.chatory.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.model.Chat;
import com.orengesunshine.chatory.model.ChatRoom;
import com.orengesunshine.chatory.ui.MainActivity;
import com.orengesunshine.chatory.util.DateTimeUtils;
import com.orengesunshine.chatory.util.PrefUtil;

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


public class LoadTextFileService extends IntentService {

    public static final String SAVING_CHAT_HISTORY = "Saving chat history with "; //todo to string file
    public static final String SAVING_IN_PROGRESS = "Saving in progress";
    private static final String TAG = LoadTextFileService.class.getSimpleName();
    private static final String IS_USER_NAME_SET = "user_name_set";
    public static final String APP_USER_NAME = "app_user_name";
    private static final int DATE_START_POS = 10;
    private static final int NAME_START_POS = 19;
    public static final String INVITATION = "invitation";
    public static final String DATE = "date";
    private int mTotalLineCount;
    private int mLoadingId = 0;
    public static final String NOTIFICATION_CHANNEL_ID_SAVING = "notification_channel_saving";
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private String onlyNames;

    public LoadTextFileService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

//        Log.d(TAG, "onStartCommand: "+startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG, "onHandleIntent: called");

        if (mNotificationManager==null){
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }

        mBuilder =  new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID_SAVING);
        registerLocationNotifChnnl();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentTitle(getString(R.string.saving_chat_history_with))
                .setSmallIcon(R.drawable.ic_notification_saving)
                .setContentText(SAVING_IN_PROGRESS)
                .setContentIntent(contentIntent);
        mNotificationManager.notify(mLoadingId,mBuilder.build());

        if (intent != null) {
            if (intent.getData()!=null){

                String path = intent.getData().getPath();
                Log.d(TAG, "onHandleIntent: "+path);
                loadTextFile(path);
                Log.d(TAG, "onHandleIntent: saving done");
                mNotificationManager.cancel(mLoadingId);
                mBuilder.setProgress(0,0,false)
                        .setContentTitle(getString(R.string.finish_saving)+" "+onlyNames)
                        .setContentText(getString(R.string.finished));
                mNotificationManager.notify(mLoadingId,mBuilder.build());

                mLoadingId++;
            }else Log.d(TAG, "onHandleIntent: uri is null");
        }else Log.d(TAG, "onHandleIntent: intent is null");
    }

    private void loadTextFile(String path) {
        BufferedReader readerForCount = null;
        try {

            FileInputStream fisCount = new FileInputStream(path);
            readerForCount = new BufferedReader(new InputStreamReader(fisCount));
            mTotalLineCount = (countTotalLines(readerForCount)-2)*2;

            FileInputStream fis = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            makeChatRoom(reader);
        } catch (FileNotFoundException e) {
            mNotificationManager.cancel(mLoadingId);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * makes chatRoom object and save it to database
     * @param reader is .txt file from line app in BufferedReader
     */
    private void makeChatRoom(BufferedReader reader) {

        mBuilder.setProgress(mTotalLineCount,0,false);

//        ChatRoom chatRoom = new ChatRoom();
        ArrayList<Chat> chats;

        try {
            //get all user(s) in the chat
            String chatWithNames = reader.readLine();
            Log.d(TAG, "makeChatRoom: "+chatWithNames);
            onlyNames = chatWithNames.substring(NAME_START_POS);
            mBuilder.setContentTitle(getString(R.string.saving_chat_history_with)+" "+onlyNames);
            mNotificationManager.notify(mLoadingId,mBuilder.build());
            String[] names = onlyNames.split(",");
//            chatRoom.setNames(names);

            //get date
            String savedDate = reader.readLine().substring(DATE_START_POS);
//            chatRoom.setSavedOn(savedDate);

            ContentValues values = new ContentValues();
            values.put(ChatContract.ChatRoomEntry.PARTICIPANTS_NAME,onlyNames);
            values.put(ChatContract.ChatRoomEntry.CREATED_AT,savedDate);
            values.put(ChatContract.ChatRoomEntry.UPDATED_AT,savedDate);
            Uri uri = getContentResolver().insert(ChatContract.ChatRoomEntry.CONTENT_URI,values);
            if (uri!=null){
                long roomId = ContentUris.parseId(uri);
                chats = createChats(reader,roomId);

                if (!PrefUtil.getBoolean(IS_USER_NAME_SET)){
                    saveAppUserName(names,chats);
                }
//                chatRoom.setChats(chats);
            }else {
                Log.d(TAG, "makeChatRoom: uri is null");
            }

            reader.close();
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
        Log.d(TAG, "saveAppUserName: chat with "+ Arrays.toString(names));

        for (Chat chat:chats){
            nameListFromChats.add(chat.getName());
        }
        Log.d(TAG, "saveAppUserName: "+nameListFromChats);

        nameListFromChats.removeAll(Arrays.asList(names));
        nameListFromChats.remove(INVITATION);
        nameListFromChats.remove(DATE);

        if (nameListFromChats.size()==1){
            appUserName = nameListFromChats.iterator().next();
            PrefUtil.saveString(APP_USER_NAME,appUserName);
            PrefUtil.saveBoolean(IS_USER_NAME_SET,true);
        }

        Log.d(TAG, "saveAppUserName: "+PrefUtil.getBoolean(IS_USER_NAME_SET)+" , "+PrefUtil.getString(APP_USER_NAME));
    }

//    private boolean isRoomExist(String participants){
//        String projection[] = new String[]{ChatContract.ChatRoomEntry._ID};
//        String selection = ChatContract.ChatRoomEntry.PARTICIPANTS_NAME+"=?";
//        String[] selectionArg = new String[]{participants};
//        Cursor cursor = getContentResolver().query(
//                ChatContract.ChatRoomEntry.CONTENT_URI,
//                projection,
//                selection,
//                selectionArg,
//                null
//        );
//
//        // count is 0 if there is no room with same names
//        if (cursor!=null){
//            return cursor.getCount()!=0;
//        }
//        return false;
//    }

    private ArrayList<Chat> createChats(BufferedReader reader,long roomId) throws IOException{
        ArrayList<Chat> chats = new ArrayList<>();

        //get all chats
        Chat chat = null;
        StringBuilder sb = null;
        String line;
        String chatDate = "";
        boolean skipFirstLine = false;
        int currentCount=0;


        while ((line = reader.readLine()) != null) {
            currentCount++;
            //skip a empty line after date
            if (!skipFirstLine){
                skipFirstLine = true;
                continue;
            }else if (line.length()==0){
                continue; //skip empty line between lines
                //this could be either btwn chats or user input or at the end of file

            }

            if (DateTimeUtils.isDate(line)){ //meaning new date
                chatDate = line; //save date for same day chat
                if(chat!=null){ //chat is null only the first date line
                    chat.setText(sb.toString());
                    chats.add(chat);
                }
                chat = new Chat();
                sb = new StringBuilder();
                chat.setName(DATE);
                chat.setDate(chatDate);
            }else {
                String[] chatLine = line.split("\t");

                if (DateTimeUtils.isTime(chatLine[0])&&chatLine.length>1){
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
            mBuilder.setProgress(mTotalLineCount,currentCount,false);
            mNotificationManager.notify(mLoadingId,mBuilder.build());
        }
//        Log.d(TAG, "createChats: estimate: "+mTotalLineCount+" actual: "+currentCount);

        //save last chat which is not saved in the while loop
        if (chat!=null){
            chat.setText(sb.toString());
            chats.add(chat);
        }

        mTotalLineCount = currentCount+chats.size();
        boolean isSaved = saveChats(chats,roomId,currentCount);

        Chat lastChat = chats.get(chats.size()-1);
        ContentValues values = new ContentValues();
        values.put(ChatContract.ChatRoomEntry.LAST_CHAT_DATE,lastChat.getDate());
        values.put(ChatContract.ChatRoomEntry.LAST_CHAT_MESSAGE,lastChat.getText());
        Uri uri = ContentUris.withAppendedId(ChatContract.ChatRoomEntry.CONTENT_URI,roomId);
        int rowsUpdated = getContentResolver().update(uri,values,null,null);
//        Log.d(TAG, "createChats: update "+rowsUpdated);

        return chats;
    }

    private boolean saveChats(List<Chat> chats, long roomId, int currentCount){
        boolean saveSuccess = true;
        Log.d(TAG, "saveChats: estimate: "+mTotalLineCount+" now at: "+currentCount);
        for (Chat c: chats){
            ContentValues values = new ContentValues();
            values.put(ChatContract.ChatEntry.CHAT_ROOM_ID,roomId);
            values.put(ChatContract.ChatEntry.NAME,c.getName());
            values.put(ChatContract.ChatEntry.MESSAGE,c.getText());
            values.put(ChatContract.ChatEntry.CREATED_AT_DATE,c.getDate());
            values.put(ChatContract.ChatEntry.CREATED_AT_TIME,c.getTime());
            Uri uri = getContentResolver().insert(ChatContract.ChatEntry.CONTENT_URI,values);
            if (uri==null){
                saveSuccess = false;
                Log.d(TAG, "createChats: inserting failed for "+c.getTime() );
            }
            currentCount++;
            mBuilder.setProgress(mTotalLineCount,currentCount,false);
            mNotificationManager.notify(mLoadingId,mBuilder.build());
        }

        Log.d(TAG, "saveChats: estimate: "+mTotalLineCount+" end at: "+currentCount);

        return saveSuccess;
    }

    private int countTotalLines(BufferedReader reader) throws IOException {
        int count = 0;
        while (reader.readLine()!=null){
            count++;
        }
        reader.close();
        return count;
    }

//    private boolean isDate(String line){
//        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd(EEE)",new Locale("en","US"));
//        format.setLenient(false); //has to match day of week, go wrong with time zone if not set?
//        try {
//            format.parse(line);
//            return true;
//        } catch (ParseException e) {
//            //means it's not a date so return false
//
//            return false;
//        }
//    }

//    private boolean isTime(String line){
//        SimpleDateFormat format = new SimpleDateFormat("h:mm",new Locale("en","US"));
//        try {
//            format.parse(line);
//            return true;
//        } catch (ParseException e) {
//            //means it's not time so return false
//
//            return false;
//        }
//    }

    public void registerLocationNotifChnnl() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_SAVING) != null) {
                return;
            }
            //
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID_SAVING,
                    "notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("make notification");
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null,null);

            channel.setVibrationPattern(new long[]{0L});

            mNotificationManager.createNotificationChannel(channel);
        }
    }

            @Override
    public void onDestroy() {

        super.onDestroy();
    }
}
