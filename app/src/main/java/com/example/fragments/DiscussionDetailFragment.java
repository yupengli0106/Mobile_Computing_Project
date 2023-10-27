package com.example.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.adapters.MessagesAdapter;
import com.example.helpers.FirebaseHelper;
import com.example.managers.MessagesManager;
import com.example.model.Discussion;
import com.example.model.Message;
import com.example.zenly.R;

import java.util.ArrayList;
import java.util.List;

public class DiscussionDetailFragment extends Fragment {

    private Discussion discussion;

    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();

    public DiscussionDetailFragment() {
        // Required empty public constructor
    }

    public static DiscussionDetailFragment newInstance(Discussion discussion) {
        DiscussionDetailFragment fragment = new DiscussionDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("selected_discussion", discussion);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            discussion = (Discussion) getArguments().getSerializable("selected_discussion");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button sendButton = view.findViewById(R.id.send_button);
        Button backButton = view.findViewById(R.id.back_button);
        TextView receiverIdTextView = view.findViewById(R.id.receiver_text_view);
        receiverIdTextView.setText(discussion.getReceiverUserName());
        EditText messageInput = view.findViewById(R.id.message_input);

        RecyclerView messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MessagesAdapter messagesAdapter = new MessagesAdapter(new ArrayList<>());
        messagesRecyclerView.setAdapter(messagesAdapter);
        MessagesManager.getInstance().getMessages().observe(getViewLifecycleOwner(), messagesAdapter::setMessages);

        backButton.setOnClickListener(v -> {
            DiscussionsFragment discussionsFragmentFragment = DiscussionsFragment.newInstance();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, discussionsFragmentFragment)
                    .addToBackStack(null)
                    .commit();
        });

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty() && discussion != null) {
                MessagesManager.getInstance().addMessage(discussion, messageText);
                messageInput.setText("");
            }
        });


        firebaseHelper.listenForMessagesInDiscussion(discussion.getDiscussionId(), new FirebaseHelper.NewMessageCallback() {
            @Override
            public void onNewMessagesReceived(List<Message> messages) {
                MessagesManager.getInstance().setMessages(messages);
                if (messagesRecyclerView.getAdapter() != null && messagesRecyclerView.getAdapter().getItemCount() > 0)  {
                    int lastPosition = messagesRecyclerView.getAdapter().getItemCount() - 1;
                    messagesRecyclerView.smoothScrollToPosition(lastPosition);
                }
            }

            @Override
            public void onMessageError(Exception e) {
            }
        });
    }
}