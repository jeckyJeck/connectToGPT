package com.yakov.connecttogpt;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String GPT_URL = "https://on-request-example-v7gbt7cgma-uc.a.run.app";

    Button titleButton;
    private ListView chatListView;
    private EditText messageEditText;
    private ChatAdapter chatAdapter;
    private List<JSONObject> chatMessages;
    private JSONArray conversationHistory;
    private String title;
    String systemMessage;
    DatabaseManager db;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = getApplicationContext();
        db = new DatabaseManager(context);

        titleButton = findViewById(R.id.chat_title_button);
        chatListView = findViewById(R.id.chat_list_view);
        messageEditText = findViewById(R.id.message_edit_text);


        title = getIntent().getStringExtra("title");
        titleButton.setText(title);
        titleButton.setOnClickListener(this::showTitleMenu);

        conversationHistory = db.getChatHistory(Gpt4ChatFragment.GPT_AGENT_NAME, title);
        chatMessages = new ArrayList<>();

        // Load the chat history from JSONArray conversationHistory to chatMessage
        loadChatHistory();

        chatAdapter = new ChatAdapter(this, chatMessages);
        chatListView.setAdapter(chatAdapter);

        //get system message from first JSONObject in conversationHistory
        try {
            systemMessage = conversationHistory.getJSONObject(0).getString("content");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveChatHistory();
        Toast.makeText(context, "onStop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveChatHistory();
        Toast.makeText(context, "onPause", Toast.LENGTH_SHORT).show();
    }

    private void loadChatHistory() {
        chatMessages.clear();
        for (int i = 1; i < conversationHistory.length(); i++) {//first message is system message
            try {
                JSONObject jsonObject = conversationHistory.getJSONObject(i);
                chatMessages.add(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void sendMessage(View view) {
        String messageText = messageEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            try {
                JSONObject sentMessage = new JSONObject();
                sentMessage.put("role", "user");
                sentMessage.put("content", messageText);
                chatMessages.add(sentMessage);
                conversationHistory.put(sentMessage);
                chatAdapter.notifyDataSetChanged();
                messageEditText.setText("");

                PostReqService.sendConversationToServer(conversationHistory, new PostReqService.ServerResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject receivedMessage = new JSONObject();
                            receivedMessage.put("role", "assistant");
                            receivedMessage.put("content", response);
                            chatMessages.add(receivedMessage);
                            conversationHistory.put(receivedMessage);
                            runOnUiThread(() -> chatAdapter.notifyDataSetChanged());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, GPT_URL);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void saveChatHistory(){
        db.storeChat(Gpt4ChatFragment.GPT_AGENT_NAME, title ,conversationHistory);
    }

    private void editSystemMessage(String systemMessage){
        this.systemMessage = systemMessage;
        JSONObject newSystemMessage = new JSONObject();
        try {
            newSystemMessage.put("role", "system");
            newSystemMessage.put("content", systemMessage);
            conversationHistory.put(0, newSystemMessage);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private void showTitleMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.title_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_rename:
                    showRenameDialog();
                    return true;
                case R.id.menu_edit_system_message:
                    showEditSystemMessageDialog();
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    private void showRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Chat Title");

        final EditText input = new EditText(this);
        input.setText(title);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newChatTitle = input.getText().toString().trim();
            String oldChatTitle = title;
            if (!newChatTitle.isEmpty()) {
                title = newChatTitle;
                titleButton.setText(title);
                db.updateChatTitle(Gpt4ChatFragment.GPT_AGENT_NAME, oldChatTitle, newChatTitle);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void showEditSystemMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(DatabaseManager.DEFAULT_SYSTEM_MESSAGE);

        final EditText input = new EditText(this);
        input.setText(systemMessage);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newSystemMessage = input.getText().toString().trim();
            if (!newSystemMessage.isEmpty()) {
                editSystemMessage(newSystemMessage);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private class ChatAdapter extends ArrayAdapter<JSONObject> {
        private static final int VIEW_TYPE_MESSAGE_SENT = 0;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 1;

        ChatAdapter(Context context, List<JSONObject> chatMessages) {
            super(context, 0, chatMessages);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            JSONObject message = getItem(position);
            try {
                String role = message.getString("role");
                return role.equals("user") ? VIEW_TYPE_MESSAGE_SENT : VIEW_TYPE_MESSAGE_RECEIVED;
            } catch (JSONException e) {
                e.printStackTrace();
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int viewType = getItemViewType(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                int layoutResId = viewType == VIEW_TYPE_MESSAGE_SENT
                        ? R.layout.item_message_sent
                        : R.layout.item_message_received;

                convertView = LayoutInflater.from(getContext()).inflate(layoutResId, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.messageText = convertView.findViewById(R.id.message_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            JSONObject message = getItem(position);
            try {
                viewHolder.messageText.setText(message.getString("content"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }

        private class ViewHolder {
            TextView messageText;
        }
    }
}