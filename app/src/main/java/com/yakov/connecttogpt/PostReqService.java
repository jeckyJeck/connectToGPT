package com.yakov.connecttogpt;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostReqService {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void sendConversationToServer(JSONArray conversationHistory, ServerResponseCallback callback, String url) {
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(JSON, conversationHistory.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    final String responseText = response.body().string();
                    // You might need to use a Handler or runOnUiThread to update the UI from the background thread
                    callback.onResponse(responseText);
                } else {
                    callback.onResponse("Error: " + response.code());
                }
            } catch (IOException e) {
                e.printStackTrace();
                callback.onResponse("Error: " + e.getMessage());
            }
        });
    }

    public interface ServerResponseCallback {
        void onResponse(String response);
    }
}