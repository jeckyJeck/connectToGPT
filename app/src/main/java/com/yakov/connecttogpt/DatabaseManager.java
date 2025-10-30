package com.yakov.connecttogpt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "chat_history.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_CHAT_MESSAGES = "chat_messages";
    private static final String COLUMN_ID = "message_id";
    private static final String COLUMN_AGENT_NAME = "agent_name";
    private static final String COLUMN_CHAT_TITLE = "chat_title";
    private static final String COLUMN_CHAT_HISTORY = "chat_history";
    public static final String DEFAULT_SYSTEM_MESSAGE = "you are a helpful assistant";
    private static final String DEFAULT_ASSISTANT_MESSAGE = "Hi! How can I help you?";



    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_CHAT_MESSAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_AGENT_NAME + " TEXT NOT NULL, " +
                COLUMN_CHAT_TITLE + " TEXT NOT NULL, " +
                COLUMN_CHAT_HISTORY + " TEXT NOT NULL, " +
                "last_modified INTEGER NOT NULL DEFAULT (strftime('%s','now')), " +
                "UNIQUE (" + COLUMN_AGENT_NAME + ", " + COLUMN_CHAT_TITLE + ") ON CONFLICT REPLACE)";
        db.execSQL(createTableQuery);

        String createIndexQuery = "CREATE INDEX idx_agent_chat ON " + TABLE_CHAT_MESSAGES +
                " (" + COLUMN_AGENT_NAME + ", " + COLUMN_CHAT_TITLE + ")";
        db.execSQL(createIndexQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGES);
        onCreate(db);
    }

    public void storeChat(String agentName, String chatTitle, JSONArray chatHistory) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AGENT_NAME, agentName);
        values.put(COLUMN_CHAT_TITLE, chatTitle);
        values.put(COLUMN_CHAT_HISTORY, chatHistory.toString());
        db.insert(TABLE_CHAT_MESSAGES, null, values);
        db.close();
    }

    public JSONArray getChatHistory(String agentName, String chatTitle) {
        //if this is a new chat
        if (chatTitle.equals(Gpt4ChatFragment.NEW_CHAT_TITLE)){
            JSONArray newChatHistory = new JSONArray();
            try {
                JSONObject defaultSystemMessage = new JSONObject();
                defaultSystemMessage.put("role", "system");
                defaultSystemMessage.put("content", DEFAULT_SYSTEM_MESSAGE);
                JSONObject defaultAssistantMessage = new JSONObject();
                defaultAssistantMessage.put("role", "assistant");
                defaultAssistantMessage.put("content", DEFAULT_ASSISTANT_MESSAGE);
                newChatHistory.put(defaultSystemMessage);
                newChatHistory.put(defaultAssistantMessage);
            } catch (JSONException e){
                throw new RuntimeException(e);
            }
            return newChatHistory;
        }
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {COLUMN_CHAT_HISTORY};
        String selection = COLUMN_AGENT_NAME + " = ? AND " + COLUMN_CHAT_TITLE + " = ?";
        String[] selectionArgs = {agentName, chatTitle};
        Cursor cursor = db.query(
                TABLE_CHAT_MESSAGES,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );
        JSONArray chatHistory = null;
        if (cursor.moveToFirst()) {
            String jsonString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHAT_HISTORY));
            try {
                chatHistory = new JSONArray(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        db.close();
        return chatHistory;
    }

    public List<String> getChatTitles(String agentName) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {COLUMN_CHAT_TITLE};
        String selection = COLUMN_AGENT_NAME + " = ?";
        String[] selectionArgs = {agentName};
        String orderBy = "last_modified DESC";
        Cursor cursor = db.query(
                TABLE_CHAT_MESSAGES,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                orderBy,
                null
        );
        List<String> chatTitles = new ArrayList<>();
        while (cursor.moveToNext()) {
            String chatTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHAT_TITLE));
            chatTitles.add(chatTitle);
        }
        cursor.close();
        db.close();
        return chatTitles;
    }

    public void deleteChat(String agentName, String chatTitle) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = COLUMN_AGENT_NAME + " = ? AND " + COLUMN_CHAT_TITLE + " = ?";
        String[] selectionArgs = {agentName, chatTitle};
        db.delete(TABLE_CHAT_MESSAGES, selection, selectionArgs);
        db.close();
    }

    public void updateChatTitle(String agentName, String oldChatTitle, String newChatTitle) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_TITLE, newChatTitle);
        String selection = COLUMN_AGENT_NAME + " = ? AND " + COLUMN_CHAT_TITLE + " = ?";
        String[] selectionArgs = {agentName, oldChatTitle};
        db.update(TABLE_CHAT_MESSAGES, values, selection, selectionArgs);
        db.close();
    }
}
