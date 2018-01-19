package com.orengesunshine.chatory.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.data.ChatDbHelper;
import com.orengesunshine.chatory.service.LoadTextFileActivity;

import java.io.File;


public class MainActivity extends AppCompatActivity implements ChatListFragment.OnListFragmentInteractionListener,
        ChatRoomFragment.OnFragmentInteractionListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TYPE_SQLITE = "application/x-sqlite3";
    private static final int PICK_DB_REQUEST = 600;
    FragmentManager mFragmentManager;

    private ChatListFragment chatListFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState==null){
//            Log.d(TAG, "onCreate: null savedInstance");
            chatListFragment = ChatListFragment.newInstance(0);
            mFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container,chatListFragment)
                    .commit();
        }else {
//            Log.d(TAG, "onCreate: savedInstance");
        }
    }

    @Override
    public void onListFragmentInteraction(long id,String roomName) {
        ChatRoomFragment chatRoomFragment =ChatRoomFragment.newInstance(id,roomName);
        mFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container,chatRoomFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.add(R.string.delete_all_chats).setIcon(R.drawable.ic_delete);
        menu.add(R.string.import_data).setIcon(R.drawable.ic_action_add);
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = item.getTitle().toString();
        if (title.equals(getResources().getString(R.string.delete_all_chats))){
            boolean deleted = this.deleteDatabase(ChatDbHelper.DB_NAME);
            if (deleted){
                chatListFragment.getLoaderManager().destroyLoader(ChatListFragment.CHAT_ROOM_LOADER);
            }
            return true;
        }else if (title.equals(getResources().getString(R.string.import_data))){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(TYPE_SQLITE);
            startActivityForResult(Intent.createChooser(intent,"choose .db file"),PICK_DB_REQUEST);
            return true;
        }
        int id = item.getItemId();
        switch (id){
            case R.id.menu_change_name:
                startActivity(new Intent(this,ChangeNameActivity.class));
                return true;
            case R.id.menu_option_export_db:
                exportDataBase();
                return true;
            case R.id.test:
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d(TAG, "onOptionsItemSelected: oreo!");
                    NotificationChannel channel = new NotificationChannel("channel_id","setting",NotificationManager.IMPORTANCE_HIGH);

                    manager.createNotificationChannel(channel);
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"channel_id")
                        .setSmallIcon(R.drawable.ic_notification_saving)
                        .setContentTitle("hello")
                        .setContentText("notification");
                manager.notify(3938752,builder.build());
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(TAG, "onActivityResult: "+requestCode);
        if (resultCode==RESULT_OK){
            if (requestCode==PICK_DB_REQUEST){
                Intent intent = new Intent(this, LoadTextFileActivity.class);
                intent.setType(TYPE_SQLITE);
                intent.setData(data.getData());
                intent.setAction(Intent.ACTION_SEND);
                Log.d(TAG, "onActivityResult: "+intent.getData());
                startActivity(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void exportDataBase() {
        File dbFile = getDatabasePath(ChatDbHelper.DB_NAME);
        Uri uri = FileProvider.getUriForFile(this,"com.orengesunshine.fileprovider",dbFile);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(TYPE_SQLITE);
//        intent.setType("application/octet-stream");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        startActivity(intent);
    }
}
