package com.orengesunshine.chatory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.orengesunshine.chatory.data.ChatContract.ChatEntry;
import com.orengesunshine.chatory.data.ChatContract.ChatRoomEntry;
import com.orengesunshine.chatory.model.ChatRoom;


public class ChatDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "chat.db";
    public static final int version = 1;


    public ChatDbHelper(Context context) {
        super(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_CHAT_ROOM_TABLE = "CREATE TABLE "+ChatRoomEntry.TABLE_NAME+" ("
                +ChatRoomEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +ChatRoomEntry.PARTICIPANTS_NAME+" TEXT NOT NULL, "
                +ChatRoomEntry.CREATED_AT+" TEXT, "
                +ChatRoomEntry.UPDATED_AT+" TEXT, "
                +ChatRoomEntry.LAST_CHAT_DATE+" TEXT, "
                +ChatRoomEntry.LAST_CHAT_MESSAGE+" TEXT);";
        db.execSQL(SQL_CREATE_CHAT_ROOM_TABLE);

        String SQL_CREATE_CHAT_TABLE = "CREATE TABLE "+ ChatEntry.TABLE_NAME+" ("
                +ChatEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +ChatEntry.CHAT_ROOM_ID+" INTEGER NOT NULL, "
                +ChatEntry.NAME+" TEXT NOT NULL, "
                +ChatEntry.MESSAGE+" TEXT, "
                +ChatEntry.CREATED_AT_DATE+" TEXT, "
                +ChatEntry.CREATED_AT_TIME+" TEXT);";
        db.execSQL(SQL_CREATE_CHAT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
