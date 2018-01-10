package com.orengesunshine.chatory.ui;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.orengesunshine.chatory.R;


public class UserDialog extends DialogFragment {
    private static final String TAG = UserDialog.class.getSimpleName();
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.user_action_list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: "+i);
            }
        });
        return builder.create();
    }
}
