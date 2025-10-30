package com.yakov.connecttogpt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Gpt4ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Gpt4ChatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public static final String GPT_AGENT_NAME = "gpt";
    public static final String NEW_CHAT_TITLE = "New Chat";
    Context context;
    ListView chatsListView;
    List<String> chatsTitles;
    ChatsAdapter chatsAdapter;
    List<Chat> chatsList;
    DatabaseManager db;
    FloatingActionButton newChatBtn;


    public Gpt4ChatFragment() {
        // Required empty public constructor
    }

    public static Gpt4ChatFragment newInstance(String param1, String param2) {
        Gpt4ChatFragment fragment = new Gpt4ChatFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        //chatsList = getChatsList();
        db = new DatabaseManager(context);
        chatsTitles = db.getChatTitles(GPT_AGENT_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gpt4_chat, container, false);
        chatsListView = rootView.findViewById(R.id.listOfChats);
        if (chatsTitles != null) {
            chatsAdapter = new ChatsAdapter(context, chatsTitles);
            chatsListView.setAdapter(chatsAdapter);
            chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String title = chatsTitles.get(i);
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("title", title);
                    startActivity(intent);
                }
            });
        } else {
            chatsListView.setVisibility(View.GONE);
        }
        newChatBtn = rootView.findViewById(R.id.newChatFloatingBtn);
        newChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newChat();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        chatsTitles = db.getChatTitles(GPT_AGENT_NAME);
        chatsAdapter.notifyDataSetChanged();
        Toast.makeText(context, "onResume", Toast.LENGTH_SHORT).show();
    }

    private List<Chat> getChatsList(){
        List<Chat> list = new ArrayList<>();
        JSONArray historyTestJson = new JSONArray();
        try {
            JSONObject tstSystemMsg = new JSONObject();
            JSONObject tstSystemMsg1 = new JSONObject();
            JSONObject tstSystemMsg2 = new JSONObject();
            tstSystemMsg.put("role", "system");
            tstSystemMsg.put("content", "you are a helpful assistant");
            historyTestJson.put(tstSystemMsg);
            tstSystemMsg1.put("role", "user");
            tstSystemMsg1.put("content", "hi there!");
            historyTestJson.put(tstSystemMsg1);
            tstSystemMsg2.put("role", "assistant");
            tstSystemMsg2.put("content", "hi! how can i help you?");
            historyTestJson.put(tstSystemMsg2);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String testHistory = "[ { \"sender\": \"me\", \"message\": \"Hello\" }, { \"sender\": \"other\", \"message\": \"Hi there\" }, { \"sender\": \"me\", \"message\": \"How are you?\" } ]";
        list.add(new Chat("test", historyTestJson.toString()));
        return list;
    }

    public void newChat() {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("title", NEW_CHAT_TITLE);
        startActivity(intent);
    }
}