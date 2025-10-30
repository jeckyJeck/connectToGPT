package com.yakov.connecttogpt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatsAdapter extends BaseAdapter {
    Context context;
    List<String> chatsList;

    public ChatsAdapter(Context context, List<String> chatsList){
        this.context = context;
        this.chatsList = chatsList;
    }

    @Override
    public int getCount() {
        return chatsList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.chats__list_item, viewGroup, false);
        }
        TextView primaryTv = view.findViewById(R.id.primaryItemTv);
        primaryTv.setText(chatsList.get(i));


        return view;
    }
}
