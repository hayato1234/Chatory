package com.orengesunshine.chatory.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.util.LoadTextFileActivity;
import com.orengesunshine.chatory.util.PrefUtil;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChatRoomAdapter extends CursorAdapter {
    private static final String TAG = ChatRoomAdapter.class.getSimpleName();
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
                ViewHolderOther vhother = new ViewHolderOther(view);
                view.setTag(vhother);
                break;
            case 1:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_chat_room_item_me,parent,false);
                ViewHolderMe vhme = new ViewHolderMe(view);
                view.setTag(vhme);
                break;
            case 2:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_chat_room_item_date_invite,parent,false);
                ViewHolderDateOrInvite vhdi = new ViewHolderDateOrInvite(view);
                view.setTag(vhdi);
                break;
            default:
                view = null;
        }
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int type = getItemType(cursor);
        switch (type){
            case 0:
                setUpOther(view,cursor);
                break;
            case 1:
                setUpMe(view,cursor);
                break;
            case 2:
                setUpTimeOrInvite(view,cursor);
                break;
            default:
                break;
        }
    }

    private void setUpOther(View view, Cursor cursor){
        ViewHolderOther holderOther = (ViewHolderOther) view.getTag();
        int nameIndex = cursor.getColumnIndex(ChatContract.ChatEntry.NAME);
        int messageIndex = cursor.getColumnIndex(ChatContract.ChatEntry.MESSAGE);
        int timeIndex = cursor.getColumnIndex(ChatContract.ChatEntry.CREATED_AT_TIME);

        String name = cursor.getString(nameIndex);
        String message = cursor.getString(messageIndex);
        String time = cursor.getString(timeIndex);

        holderOther.name.setText(name);
        holderOther.message.setText(message);
        holderOther.message_time.setText(time);
    }

    private void setUpMe(View view, Cursor cursor){
        ViewHolderMe holderMe = (ViewHolderMe) view.getTag();
        int messageIndex = cursor.getColumnIndex(ChatContract.ChatEntry.MESSAGE);
        int timeIndex = cursor.getColumnIndex(ChatContract.ChatEntry.CREATED_AT_TIME);

        String message = cursor.getString(messageIndex);
        String time = cursor.getString(timeIndex);

        holderMe.message.setText(message);
        holderMe.message_time.setText(time);
    }

    private void setUpTimeOrInvite(View view, Cursor cursor){
        ViewHolderDateOrInvite holderDateOrInvite = (ViewHolderDateOrInvite)view.getTag();
        int messageIndex = cursor.getColumnIndex(ChatContract.ChatEntry.MESSAGE);
        String message = cursor.getString(messageIndex);
        if (message==null||message.length()==0){
            // this is date only entry
            int timeIndex = cursor.getColumnIndex(ChatContract.ChatEntry.CREATED_AT_DATE);
            String time = cursor.getString(timeIndex);
            Log.d(TAG, "setUpTimeOrInvite: "+time);
            holderDateOrInvite.message.setText(time);
        }else {
            // this is invite only entry
            holderDateOrInvite.message.setText(message);
        }
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
            return 2;
        }else {
            return 0;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    static class ViewHolderMe{
        @BindView(R.id.room_list_item_time)
        TextView message_time;
        @BindView(R.id.room_list_item_message)
        TextView message;

        public ViewHolderMe(View view) {
            ButterKnife.bind(this,view);
        }
    }
    static class ViewHolderOther{
        @BindView(R.id.room_list_item_time)
        TextView message_time;
        @BindView(R.id.room_list_item_message)
        TextView message;
        @BindView(R.id.room_list_item_name)
        TextView name;

        public ViewHolderOther(View view) {
            ButterKnife.bind(this,view);
        }
    }

    static class ViewHolderDateOrInvite{

        @BindView(R.id.room_list_item_message)
        TextView message;

        public ViewHolderDateOrInvite(View view) {
            ButterKnife.bind(this,view);
        }
    }
}
