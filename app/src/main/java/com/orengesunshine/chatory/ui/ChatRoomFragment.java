package com.orengesunshine.chatory.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.util.PrefUtil;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatRoomFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatRoomFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ChatRoomAdapter.OnRoomItemClickListener {
    private static final String TAG = ChatRoomFragment.class.getSimpleName();
    private static final String ARG_ROOM_ID = "arg_room_id";
    private static final String ARG_ROOM_NAME = "arg_room_name";
    private static final String CLICKED_USER = "clicked_user";
    private static final int ROOM_LOADER = 1;
    private static final int INTENT_PICKER = 1000;

    private Context context;
    private long roomId;
    private String roomTitle;
    private String mClickedUserName;

    private OnFragmentInteractionListener mListener;

    public ChatRoomFragment() {
        // Required empty public constructor
    }

   /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param id is room id
     * @return A new instance of fragment ChatRoomFragment.
     */
    public static ChatRoomFragment newInstance(long id,String roomTitle) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ROOM_ID, id);
        args.putString(ARG_ROOM_NAME,roomTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            roomId = getArguments().getLong(ARG_ROOM_ID);
            roomTitle = getArguments().getString(ARG_ROOM_NAME);
        }
        if (savedInstanceState!=null){
            mClickedUserName = savedInstanceState.getString(CLICKED_USER);
        }
    }

    @BindView(R.id.room_list_view)
    ListView mListView;

    private ChatRoomAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);
        ButterKnife.bind(this,view);
        if (roomTitle!=null){
            getActivity().setTitle(roomTitle);
        }

        mAdapter = new ChatRoomAdapter(getContext(),null);
        mAdapter.setOnRoomItemClickListener(this);
        //todo: set an empty view for the list view
        mListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(ROOM_LOADER,null,this);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id==ROOM_LOADER){
            String[] projection = {ChatContract.ChatEntry._ID,
                    ChatContract.ChatEntry.NAME,
                    ChatContract.ChatEntry.CREATED_AT_DATE,
                    ChatContract.ChatEntry.CREATED_AT_TIME,
                    ChatContract.ChatEntry.MESSAGE
            };
            String selection = ChatContract.ChatEntry.CHAT_ROOM_ID+"=?";
            String[] selectionArg = {String.valueOf(roomId)};
            return new CursorLoader(getContext(),
                    ChatContract.ChatEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArg,
                    null
                    );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onRoomItemClicked(final String userName) {
        mClickedUserName = userName;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.user_action_list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        changeIcon();
                        break;
                    case 1:
                        changeName();
                        break;
                        default: break;
                }
            }
        });
        builder.create().show();

//
    }

    private void changeName() {
        Intent intent = new Intent(getContext(),ChangeNameActivity.class);
        intent.putExtra(ChangeNameActivity.FRIEND_NAME,mClickedUserName);
        startActivity(intent);
    }

    private void changeIcon() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent,"Crop an icon"),INTENT_PICKER);
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
                .start(getContext(),this);
    }

    private Uri mImageUri;
    private Uri mCropImageUri;

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }else if (requestCode == INTENT_PICKER&&resultCode ==RESULT_OK){
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mCropImageUri = result.getUri();
                File cacheFile = new File(mCropImageUri.getPath());
                String internalFilePath = "user_icon_"+mClickedUserName+".jpg";
                File internalFile = new File(context.getFilesDir(),internalFilePath);
                try {
                    copy(cacheFile,internalFile);
                } catch (IOException e) {
                    Log.d(TAG, "onActivityResult: error, "+e.toString());
                }
                //todo: save actual value
                Uri innerUri = Uri.parse("file://"+internalFile.getAbsolutePath());
                PrefUtil.saveIconUri(mClickedUserName, innerUri);
                mAdapter.notifyDataSetChanged();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "onActivityResult: crop "+error);
            }
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mImageUri);
            } else {
                Toast.makeText(getContext(), "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * copy cache file from crop activity and save to internal file
     * @param src cached file
     * @param dst file with new file name
     * @throws IOException for streams
     */
    private void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mClickedUserName!=null){
            outState.putString(CLICKED_USER,mClickedUserName);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(R.string.menu_option_change_room_name).setIcon(R.drawable.ic_edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CharSequence id = item.getTitle();
        if (id.equals(getResources().getString(R.string.menu_option_change_room_name))){
            Intent intent = new Intent(context,ChangeNameActivity.class);
            intent.putExtra(ChangeNameActivity.ROOM_NAME,roomTitle);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
