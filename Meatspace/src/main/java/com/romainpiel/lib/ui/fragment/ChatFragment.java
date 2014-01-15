package com.romainpiel.lib.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.bus.ChatEvent;
import com.romainpiel.lib.bus.MuteEvent;
import com.romainpiel.lib.ui.adapter.ChatAdapter;
import com.romainpiel.lib.ui.listener.OnMenuClickListener;
import com.romainpiel.lib.utils.UIUtils;
import com.romainpiel.meatspace.R;
import com.romainpiel.model.Chat;
import com.romainpiel.model.ChatList;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 16:55
 */
public class ChatFragment extends Fragment {

    private static final String STATE_LISTVIEW = "state_listview";
    private static final String POSITION_LIST = "position_list";
    private static final String POSITION_ITEM = "position_item";

    @InjectView(R.id.fragment_chat_list) ListView listView;

    private ChatAdapter adapter;
    private Handler uiHandler;
    private Parcelable listViewState;
    private int listPosition, itemPosition;

    public ChatFragment() {
        this.uiHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            listViewState = savedInstanceState.getParcelable(STATE_LISTVIEW);
            listPosition = savedInstanceState.getInt(POSITION_LIST);
            itemPosition = savedInstanceState.getInt(POSITION_ITEM);
        }

        if (adapter == null) {
            adapter = new ChatAdapter(getActivity());
            adapter.setOnMuteClickListener(new OnMenuClickListener<Chat>() {
                @Override
                public void onMenuClick(Chat item) {
                    BusManager.get().getChatBus().post(new MuteEvent(true, item.getValue().getFingerprint()));
                }
            });
        }

        listView.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save list state
        Parcelable state = listView.onSaveInstanceState();
        outState.putParcelable(STATE_LISTVIEW, state);

        // Save position of first visible item
        listPosition = listView.getFirstVisiblePosition();
        outState.putInt(POSITION_LIST, listPosition);

        // Save scroll position of item
        View itemView = listView.getChildAt(0);
        itemPosition = itemView == null ? 0 : itemView.getTop();
        outState.putInt(POSITION_ITEM, itemPosition);
    }

    @Override
    public void onPause() {
        super.onPause();

        // unregister to bus
        BusManager.get().getChatBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // register to bus
        BusManager.get().getChatBus().register(this);
    }

    @Subscribe
    public void onMessage(ChatEvent event) {
        notifyDatasetChanged(event.getChatList(), event.isFromProducer());
    }

    public void notifyDatasetChanged(final ChatList chatList, final boolean forceScrollToBottom) {

        uiHandler.post(new Runnable() {
            @Override
            public void run() {

                adapter.setItems(chatList.get());
                adapter.notifyDataSetChanged();

                if (listViewState != null) {
                    listView.onRestoreInstanceState(listViewState);
                    listView.setSelectionFromTop(listPosition, itemPosition);
                    listViewState = null;
                } else if (forceScrollToBottom) {
                    UIUtils.scrollToBottom(listView, adapter);
                }
            }

        });
    }
}
