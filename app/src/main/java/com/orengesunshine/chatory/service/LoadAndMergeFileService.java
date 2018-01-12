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
import java.util.ArrayList;
import java.util.List;

import static com.orengesunshine.chatory.service.LoadTextFileService.DATE;
import static com.orengesunshine.chatory.service.LoadTextFileService.INVITATION;
import static com.orengesunshine.chatory.service.LoadTextFileService.NOTIFICATION_CHANNEL_ID_SAVING;


public class LoadAndMergeFileService extends IntentService {
    private static final String TAG = LoadAndMergeFileService.class.getSimpleName();

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    public static final String SAVING_IN_PROGRESS = "Saving in progress";
    public static final String LAST_CHAT_ID = "last_chat_id";
    public static final String LAST_CHAT_DATE = "last_chat_date";
    public static final String LAST_CHAT_MESSAGE = "last_chat_message";
    private static final int NAME_START_POS = 19;
    private static final int DATE_START_POS = 10;
    private int mLoadingId = 1;
    private int mTotalLineCount;
    private String onlyNames;
    private int roomId;
    private String lastDate;
    private String lastMessage;

    public LoadAndMergeFileService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
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
            if (intent.getExtras()!=null){
                roomId = intent.getExtras().getInt(LAST_CHAT_ID);
                lastDate = intent.getExtras().getString(LAST_CHAT_DATE);
                lastMessage = intent.getExtras().getString(LAST_CHAT_MESSAGE);
            }

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
        BufferedReader readerForCount;
        try {

            FileInputStream fisCount = new FileInputStream(path);
            readerForCount = new BufferedReader(new InputStreamReader(fisCount));
            mTotalLineCount = (countTotalLines(readerForCount)-2)*2;

            FileInputStream fis = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            combineData(reader);
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
    private void combineData(BufferedReader reader) {

        mBuilder.setProgress(mTotalLineCount,0,false);

        ChatRoom chatRoom = new ChatRoom();
        ArrayList<Chat> chats;

        try {
            //get all user(s) in the chat
            String chatWithNames = reader.readLine();
//            Log.d(TAG, "makeChatRoom: "+chatWithNames);
            onlyNames = chatWithNames.substring(NAME_START_POS);
            mBuilder.setContentTitle(getString(R.string.saving_chat_history_with)+" "+onlyNames);
            mNotificationManager.notify(mLoadingId,mBuilder.build());
            String[] names = onlyNames.split(",");
            chatRoom.setNames(names);

            //get date
            String savedDate = reader.readLine().substring(DATE_START_POS);
            chatRoom.setSavedOn(savedDate);
            updateDBDate(savedDate);

            chats = createChats(reader);



//            if (uri!=null){
//                long roomId = ContentUris.parseId(uri);
//                chats = createChats(reader,roomId);
//
//                chatRoom.setChats(chats);
//            }else {
//                Log.d(TAG, "makeChatRoom: uri is null");
//            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDBDate(String savedDate){
        ContentValues values = new ContentValues();
        values.put(ChatContract.ChatRoomEntry.UPDATED_AT,savedDate);
        String where = ChatContract.ChatRoomEntry._ID+"=?";
        String[] args = new String[]{String.valueOf(roomId)};
        getContentResolver().update(ChatContract.ChatRoomEntry.CONTENT_URI,values,where,args);
    }

    private ArrayList<Chat> createChats(BufferedReader reader) throws IOException{
        ArrayList<Chat> chats = new ArrayList<>();

        //get all chats
        Chat chat = null;
        StringBuilder sb = null;
        String line;
        String chatDate = "";
        boolean skipFirstLine = false;
        int currentCount=0;

        boolean skipToNextDate = false;

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
                if (DateTimeUtils.isAOlderDateThanB(line,lastDate)){
                    skipToNextDate =true;
                    continue;
                }else {
                    skipToNextDate =false;
                }
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
                if (skipToNextDate) continue;

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

    private int countTotalLines(BufferedReader reader) throws IOException {
        int count = 0;
        while (reader.readLine()!=null){
            count++;
        }
        reader.close();
        return count;
    }
}
