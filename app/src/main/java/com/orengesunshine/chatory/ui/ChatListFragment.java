package com.orengesunshine.chatory.ui;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.orengesunshine.chatory.R;
import com.orengesunshine.chatory.data.ChatContract;
import com.orengesunshine.chatory.data.ChatDbHelper;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChatListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final int CHAT_ROOM_LOADER = 0;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private static final String TAG = ChatListFragment.class.getSimpleName();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ChatListFragment newInstance(int columnCount) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    private Cursor cursor;
    private ChatListAdapter adapter;
    @BindView(R.id.chat_list_view)
    ListViewCompat listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        ButterKnife.bind(this,view);
        getLoaderManager().initLoader(CHAT_ROOM_LOADER,null,this);

        adapter = new ChatListAdapter(getContext(),null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                mListener.onListFragmentInteraction(id);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, final long l) {
                new AlertDialog.Builder(getContext())
                        .setTitle("delete?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Uri uri = ContentUris.withAppendedId(ChatContract.ChatRoomEntry.CONTENT_URI,l);
                                getContext().getContentResolver().delete(uri,null,null);
                            }
                        }).show();
                return true;
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id==CHAT_ROOM_LOADER){
            String [] projection = {ChatContract.ChatRoomEntry._ID,
                    ChatContract.ChatRoomEntry.PARTICIPANTS_NAME,
                    ChatContract.ChatRoomEntry.LAST_CHAT_DATE,
                    ChatContract.ChatRoomEntry.LAST_CHAT_MESSAGE
            };
            String sortBy = ChatContract.ChatRoomEntry.LAST_CHAT_DATE+" DESC";
//            String sortBy = ChatContract.ChatRoomEntry.LAST_CHAT_DATE+" ASC";
            return new android.support.v4.content.CursorLoader(getActivity(),
                    ChatContract.ChatRoomEntry.CONTENT_URI,
                    projection,null,null,sortBy
                    );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor = data;
        adapter.swapCursor(data);
//        Log.d(TAG, "onLoadFinished: "+data.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(long id);
    }
}
