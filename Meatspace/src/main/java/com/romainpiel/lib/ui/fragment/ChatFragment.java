package com.romainpiel.lib.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.romainpiel.lib.api.ApiManager;
import com.romainpiel.lib.ui.adapter.BaseFragment;
import com.romainpiel.lib.ui.adapter.ChatAdapter;
import com.romainpiel.lib.utils.BackgroundExecutor;
import com.romainpiel.meatspace.R;
import com.romainpiel.model.ChatList;

import butterknife.InjectView;
import butterknife.Views;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 16:55
 */
public class ChatFragment extends BaseFragment {

    private static final String API_GET_CHAT_REQ_ID = "ChatFragment.GET_CHAT";

    @InjectView(R.id.fragment_chat_list) ListView listView;

    private ChatAdapter adapter;
    private boolean initialized;

    public ChatFragment() {
        this.initialized = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);
        Views.inject(this, view);
        return view;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (adapter == null) {
            adapter = new ChatAdapter(getActivity());
        }

        if (!initialized) {
            fetchChat();
        }

        listView.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        BackgroundExecutor.cancelAll(API_GET_CHAT_REQ_ID, true);
    }

    public void fetchChat() {
        BackgroundExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        final ChatList chatList = ApiManager.get().meatspace().getChats();
                        if (chatList != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.setItems(chatList.get());
                                    adapter.notifyDataSetChanged();
                                    initialized = true;
                                }
                            });
                        }
                    }
                },
                API_GET_CHAT_REQ_ID,
                null
        );
    }

}
