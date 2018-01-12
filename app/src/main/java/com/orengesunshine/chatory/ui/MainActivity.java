package com.orengesunshine.chatory.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.orengesunshine.chatory.R;


public class MainActivity extends AppCompatActivity implements ChatListFragment.OnListFragmentInteractionListener,
        ChatRoomFragment.OnFragmentInteractionListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState==null){
            ChatListFragment chatListFragment = ChatListFragment.newInstance(0);
            mFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container,chatListFragment)
                    .commit();
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
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_change_name:
                startActivity(new Intent(this,ChangeNameActivity.class));
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
}
