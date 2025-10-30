package com.yakov.connecttogpt;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WordsListsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordsListsFragment extends Fragment {

    RecyclerView mRecyclerView;
    FloatingActionButton fab;
    List<String[]> mList;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    boolean mIsDataBaseEmpty;
    Context mContext;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public WordsListsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WordListsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WordsListsFragment newInstance(String param1, String param2) {
        WordsListsFragment fragment = new WordsListsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mContext = getContext();
        databaseHelper = new DatabaseHelper(getContext());
        db = databaseHelper.getReadableDatabase();
        mIsDataBaseEmpty = isDataBaseEmpty();
        if(!mIsDataBaseEmpty) {

        } else {

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_word_lists, container, false);
        if(mIsDataBaseEmpty){
            //TODO animation
            //Toast.makeText(mContext, "data base empty", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "filling list", Toast.LENGTH_SHORT).show();
            fillList();
            mRecyclerView = rootView.findViewById(R.id.recyclerview);
            mRecyclerView.setAdapter(new WordslistsAdapter(getContext(),mList));
            Toast.makeText(mContext, mList.get(0)[0], Toast.LENGTH_SHORT).show();
        }


        fab = rootView.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WLNewListActivity.class);
                startActivity(intent);

                // Perform an action when the floating button is clicked
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(!mIsDataBaseEmpty) {
//            fillList();
//        }
        //mRecyclerView.setAdapter(new WordslistsAdapter(getContext(),mList));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public Boolean isDataBaseEmpty(){
        String query = "SELECT COUNT(*) FROM word_packets"; //+ DatabaseHelper.DATABASE_NAME.split("/.")[0];
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();


        if (count > 0) {
            // Database has data
            return false;
        } else {
            // Database is empty
            return true;
        }
    }

    public void fillList(){
        String[] projection = {"creation_date", "metadata"};
        Cursor cursor = db.query("word_packets", projection, null, null, null, null, null);

        mList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String creationDate = cursor.getString(cursor.getColumnIndexOrThrow("creation_date"));
                String metadata = cursor.getString(cursor.getColumnIndexOrThrow("metadata"));

                String[] data = new String[2];
                data[0] = creationDate;
                data[1] = metadata;
                mList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //db.close();
    }
}