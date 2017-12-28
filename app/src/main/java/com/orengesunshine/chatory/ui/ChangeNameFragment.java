package com.orengesunshine.chatory.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.util.LoadTextFileService;
import com.orengesunshine.chatory.util.PrefUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangeNameFragment extends Fragment {
//    private static final String ARG_CHANGE_WHAT = "arg_change_what";
    private static final String ARG_CURRENT = "arg_current";
    private int change_choice;
    private String currentName;

    public ChangeNameFragment(){

    }

    public static ChangeNameFragment newInstance(){
        ChangeNameFragment fragment = new ChangeNameFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt(ARG_CHANGE_WHAT,what);
//        bundle.putString(ARG_CURRENT,currentName);
//        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null){
//            change_choice = getArguments().getInt(ARG_CHANGE_WHAT);
//            currentName = getArguments().getString(ARG_CURRENT);
        }
        currentName = PrefUtil.getString(LoadTextFileService.APP_USER_NAME);
    }

    @BindView(R.id.change_name_current)
    TextView currentTextView;

    @BindView(R.id.change_name_input)
    EditText input;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_name,container,false);
        ButterKnife.bind(this,view);
        if (currentName!=null){
            currentTextView.setText(currentName);
        }else {
            currentTextView.setText(R.string.unknown);
        }
        return view;
    }

    @OnClick(R.id.change_name_button)
    public void onClick(View view){
        String newName = input.getText().toString();
        PrefUtil.saveString(LoadTextFileService.APP_USER_NAME,newName);
        Toast.makeText(getContext(),"name is changed to "+newName,Toast.LENGTH_SHORT).show();
    }
}
