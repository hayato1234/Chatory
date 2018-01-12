package com.orengesunshine.chatory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orengesunshine.chatory.data.ChatContract.ChatEntry;
import com.orengesunshine.chatory.data.ChatContract.ChatRoomEntry;
import com.orengesunshine.chatory.model.Chat;
import com.orengesunshine.chatory.model.ChatRoom;


public class ChatProvider extends ContentProvider {

    public static final String TAG = "ChatProvider";

    public static final int CHAT_TABLE = 100;
    public static final int CHAT_TABLE_ID = 101;
    public static final int CHAT_ROOM_TABLE = 200;
    public static final int CHAT_ROOM_TABLE_ID = 201;

    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ChatContract.AUTHORITY,ChatContract.PATH_CHAT_TABLE,CHAT_TABLE);
        sUriMatcher.addURI(ChatContract.AUTHORITY,ChatContract.PATH_CHAT_TABLE+"/#",CHAT_TABLE_ID);
        sUriMatcher.addURI(ChatContract.AUTHORITY,ChatContract.PATH_CHAT_ROOM_TABLE,CHAT_ROOM_TABLE);
        sUriMatcher.addURI(ChatContract.AUTHORITY,ChatContract.PATH_CHAT_ROOM_TABLE+"/#",CHAT_ROOM_TABLE_ID);
    }

    ChatDbHelper mChatDbHelper;

    @Override
    public boolean onCreate() {
        mChatDbHelper = new ChatDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String order) {
        SQLiteDatabase db = mChatDbHelper.getReadableDatabase();

        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match){
            case CHAT_TABLE:
                cursor = db.query(
                        ChatEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        order
                );
                break;
            case CHAT_TABLE_ID:
                selection = ChatEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(
                        ChatEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,null,
                        order
                );
                break;
            case CHAT_ROOM_TABLE:
                cursor = db.query(
                        ChatRoomEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,null,
                        order
                );
                break;
            case CHAT_ROOM_TABLE_ID:
                selection = ChatRoomEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ChatRoomEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,null,
                        order
                        );
                break;
            default:
                throw new IllegalArgumentException(TAG+": Uri doesn't match");
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
//        cursor.close();
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case CHAT_ROOM_TABLE:
                return insertChatRoom(uri,contentValues);
            case CHAT_TABLE:
                return insertChat(uri,contentValues);
            default:
                throw new IllegalArgumentException(TAG+": insert uri doesn't match");
        }
    }

    private Uri insertChatRoom(Uri uri, ContentValues contentValues) {

        if (contentValues.size()==0){throw new IllegalArgumentException(TAG+": contentValues cannot be empty");}
        String names = contentValues.getAsString(ChatRoomEntry.PARTICIPANTS_NAME);
        if (names==null){ throw new IllegalArgumentException(TAG+": participant names cannot be null");}

        SQLiteDatabase db = mChatDbHelper.getWritableDatabase();
        long insertedId = db.insert(ChatRoomEntry.TABLE_NAME,null,contentValues);
        if(insertedId==-1){return null;}
        if (getContext()!=null){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return ContentUris.withAppendedId(uri,insertedId);
    }

    private Uri insertChat(Uri uri, ContentValues contentValues) {
        if (contentValues.size()==0){throw new IllegalArgumentException(TAG+": contentValues cannot be empty");}

        String name = contentValues.getAsString(ChatEntry.NAME);
        if (name==null){ throw new IllegalArgumentException(TAG+": name cannot be null");}

        Integer chatRoomId = contentValues.getAsInteger(ChatEntry.CHAT_ROOM_ID);
        if (chatRoomId==null){throw new IllegalArgumentException(TAG+": chat room id must be set");}

        SQLiteDatabase db = mChatDbHelper.getWritableDatabase();
        long insertedId = db.insert(ChatEntry.TABLE_NAME,null,contentValues);
        if (insertedId==-1){return null;}
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri,insertedId);
    }

    //todo: complete delete
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case CHAT_TABLE:

            case CHAT_TABLE_ID:
                selection = ChatEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri,null);
                return mChatDbHelper.getWritableDatabase().delete(ChatEntry.TABLE_NAME,selection,selectionArgs);
            case CHAT_ROOM_TABLE:

//                return mChatDbHelper.getWritableDatabase().delete(ChatRoomEntry.TABLE_NAME,s,strings);
            case CHAT_ROOM_TABLE_ID:
                selection = ChatRoomEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri,null);
                return mChatDbHelper.getWritableDatabase().delete(ChatRoomEntry.TABLE_NAME,selection,selectionArgs);
            default:
                throw new IllegalArgumentException(TAG+": Uri doesn't match");
        }

    }

    //todo: complete update
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case CHAT_TABLE:
                return  updateChatEntry(uri,contentValues,selection,selectionArgs);
            case CHAT_TABLE_ID:

            case CHAT_ROOM_TABLE:
                return updateChatRoom(uri,contentValues,selection,selectionArgs);
            case CHAT_ROOM_TABLE_ID:
                selection = ChatRoomEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateChatRoom(uri,contentValues,selection,selectionArgs);
            default:
                throw new IllegalArgumentException(TAG+": Uri doesn't match");
        }
    }

    public int updateChatRoom(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings){

        if (contentValues.size()==0){return 0;}

        SQLiteDatabase db = mChatDbHelper.getWritableDatabase();
        int rowsAffected = db.update(ChatRoomEntry.TABLE_NAME,contentValues,s,strings);
        if (rowsAffected!=0&&(getContext()!=null)){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsAffected;
    }
    
    public int updateChatEntry(Uri uri,ContentValues contentValues,String selection,String[] args){
        if (contentValues.size()==0){
            Log.d(TAG, "updateChatEntry: empty content");
            return 0;
        }
        SQLiteDatabase db = mChatDbHelper.getWritableDatabase();
        int rowsAffected = db.update(ChatEntry.TABLE_NAME,contentValues,selection,args);
        if (rowsAffected!=0&&(getContext()!=null)){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsAffected;
    }
}
