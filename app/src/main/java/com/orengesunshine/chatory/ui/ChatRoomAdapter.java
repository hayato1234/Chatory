package com.orengesunshine.chatory.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.util.LoadTextFileActivity;
import com.orengesunshine.chatory.util.PrefUtil;


public class ChatRoomAdapter extends CursorAdapter {
    public ChatRoomAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        int type = getItemType(cursor);
        switch (type){
            case 0:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_chat_room_item_other,parent,false);
                break;
            case 1:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_chat_room_item_me,parent,false);
                break;
            case 2:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_chat_room_item_date,parent,false);
                break;
            case 3:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_chat_room_item_date,parent,false);
                break;
            default:
                view = null;
        }

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemType(cursor);
    }

    /**
     * @param cursor for the list item
     * @return item type as int, 0 for other users, 1 for app user message, 2 for invitation message, 3 for date
     */
    private int getItemType(Cursor cursor){

        int nameIndex = cursor.getColumnIndex(ChatContract.ChatEntry.NAME);
        String name = cursor.getString(nameIndex);

        if (name.equals(PrefUtil.getString(LoadTextFileActivity.APP_USER_NAME))){
            return 1;
        }else if (name.equals(LoadTextFileActivity.INVITATION)){
            return 2;
        }else if (name.equals(LoadTextFileActivity.DATE)){
            return 3;
        }else {
            return 0;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
