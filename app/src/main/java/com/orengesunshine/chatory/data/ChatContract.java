package com.orengesunshine.chatory.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ChatContract {

    public static final String AUTHORITY = "com.orengesunshine.chatory";
    public static final Uri BASE_CONTENT_URI =
            Uri.parse("content://"+AUTHORITY);
    public static final String PATH_CHAT_TABLE = "chats";
    public static final String PATH_CHAT_ROOM_TABLE = "chat_room";

    public ChatContract() {}

    public static final class ChatRoomEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.
                withAppendedPath(BASE_CONTENT_URI,PATH_CHAT_ROOM_TABLE);
        public static final String TABLE_NAME = "chat_room";
        public static final String _ID = BaseColumns._ID;
        public static final String PARTICIPANTS_NAME = "participants_name";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String LAST_CHAT_DATE = "last_chat_date";
        public static final String LAST_CHAT_MESSAGE = "last_chat_massage";
//        public static final String Creator_NAME = "creator_name";
    }

    public static final class ChatEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.
                withAppendedPath(BASE_CONTENT_URI,PATH_CHAT_TABLE);
        public static final String TABLE_NAME = "chats";
        public static final String _ID = BaseColumns._ID;
        public static final String CHAT_ROOM_ID = "chat_room_id";
        public static final String NAME = "name";
        public static final String CREATED_AT_DATE = "created_at_date";
        public static final String CREATED_AT_TIME = "created_at_time";
        public static final String MESSAGE = "message";
    }

}
