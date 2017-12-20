package com.orengesunshine.chatory.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.util.DateTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChatListAdapter extends CursorAdapter {

    public ChatListAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_chat_list_item,viewGroup,false);
        Holder holder = new Holder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Holder holder = (Holder) view.getTag();
        if (holder!=null){
            int nameIndex = cursor.getColumnIndex(ChatContract.ChatRoomEntry.PARTICIPANTS_NAME);
            int timeIndex = cursor.getColumnIndex(ChatContract.ChatRoomEntry.LAST_CHAT_DATE);
            int messageIndex = cursor.getColumnIndex(ChatContract.ChatRoomEntry.LAST_CHAT_MESSAGE);
            String name = cursor.getString(nameIndex);
            //this is saved time, record last message time
            String time = cursor.getString(timeIndex);
            String onlyMMdd = time.substring(5,10);
            String message = cursor.getString(messageIndex);
            holder.name.setText(name);
            holder.time.setText(onlyMMdd);
            holder.message.setText(message);
        }
    }

    static class Holder{
        @BindView(R.id.chat_list_name)
        TextView name;
        @BindView(R.id.chat_list_time)
        TextView time;
        @BindView(R.id.chat_list_message)
        TextView message;

        Holder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
