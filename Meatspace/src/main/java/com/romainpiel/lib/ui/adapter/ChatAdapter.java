package com.romainpiel.lib.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.romainpiel.lib.ui.view.ChatItemView;
import com.romainpiel.model.Chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 18:11
 */
public class ChatAdapter extends BaseAdapter {

    private Context context;
    private List<Chat> items;

    public ChatAdapter(Context context) {
        this.context = context;
        items = new ArrayList<Chat>();
    }

    public void setItems(List<Chat> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Chat getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatItemView itemView;
        if (convertView == null) {
            itemView = ChatItemView.build(context);
        } else {
            itemView = (ChatItemView) convertView;
        }

        itemView.bind(getItem(position));
        return itemView;
    }
}
