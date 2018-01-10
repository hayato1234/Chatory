package com.orengesunshine.chatory.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.util.LoadTextFileActivity;
import com.orengesunshine.chatory.util.LoadTextFileService;
import com.orengesunshine.chatory.util.PrefUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangeNameActivity extends AppCompatActivity {

    private static final String TAG = ChangeNameActivity.class.getSimpleName();
    public static final String FRIEND_NAME = "friend_name";
    private String currentName;
    private boolean isFriend;

    @BindView(R.id.change_name_current)
    TextView currentTextView;
    @BindView(R.id.change_name_input)
    EditText input;
    @BindView(R.id.change_name_warning)
    TextView warning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);
        ButterKnife.bind(this);

        if (getIntent().getExtras()==null){ //user
            isFriend = false;
            currentName = PrefUtil.getString(LoadTextFileService.APP_USER_NAME);
        }else {//friend
            currentName = getIntent().getExtras().getString(FRIEND_NAME);
            isFriend = true;
            warning.setVisibility(View.GONE);
        }

        currentTextView.setText(currentName);
    }

    @OnClick(R.id.change_name_button)
    public void onClick(View view){
        if (this.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        String newName = input.getText().toString();
        currentTextView.setText(newName);
        if (isFriend){
            updateDateBase(currentName,newName);
            updateIconName(currentName,newName);
        }else {
            PrefUtil.saveString(LoadTextFileService.APP_USER_NAME,newName);
        }
        currentName = newName;
//        Toast.makeText(this,"name is changed to "+currentName,Toast.LENGTH_SHORT).show();
    }

    private void updateIconName(String oldName, String newName){
        String iconUri = PrefUtil.getIconUri(oldName);
        PrefUtil.saveIconUri(newName, Uri.parse(iconUri));
        PrefUtil.deleteString(oldName);
    }

    private void updateDateBase(String oldName, String newName){
        ContentResolver cv = getContentResolver();
        Uri uri = ChatContract.ChatEntry.CONTENT_URI;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatContract.ChatEntry.NAME,newName);
        String where = ChatContract.ChatEntry.NAME +"=?";
        String[] args = new String[]{oldName};
        cv.update(uri,contentValues,where,args);
    }
}
