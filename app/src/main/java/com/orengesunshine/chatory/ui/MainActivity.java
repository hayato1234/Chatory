package com.orengesunshine.chatory.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.util.LoadTextFileActivity;
import com.orengesunshine.chatory.util.PrefUtil;

import butterknife.BindView;
import butterknife.ButterKnife;




public class MainActivity extends AppCompatActivity implements ChatListFragment.OnListFragmentInteractionListener,ChatRoomFragment.OnFragmentInteractionListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: "+ PrefUtil.getString(LoadTextFileActivity.APP_USER_NAME));
        Log.d(TAG, "onCreate: "+ PrefUtil.getBoolean("user_name_set"));

        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState==null){
            ChatListFragment chatListFragment = ChatListFragment.newInstance(0);
            mFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container,chatListFragment)
                    .commit();
        }
    }

    @Override
    public void onListFragmentInteraction(long id) {
        ChatRoomFragment chatRoomFragment =ChatRoomFragment.newInstance(id);
        mFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container,chatRoomFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
