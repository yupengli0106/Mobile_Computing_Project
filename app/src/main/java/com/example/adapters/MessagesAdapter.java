package com.example.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.helpers.FirebaseHelper;
import com.example.model.Message;
import com.example.zenly.R;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private static final String TAG = "MessagesAdapter";
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
    private String currentUserId = firebaseHelper.getCurrentUserId(); // Replace this with the ID of the logged-in user.
    private List<Message> messages;

    public MessagesAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == VIEW_TYPE_SENT) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_message_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_message_item, parent, false);
        }
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageTextView.setText(message.getContent());
        holder.dateTextView.setText(message.getFormattedDateTime());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView dateTextView;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_content);
            dateTextView = itemView.findViewById(R.id.date_text_view);
        }
    }

    public void setMessages(List<Message> messages) {
        Log.d(TAG, "setMessages: messages set");
        this.messages = messages;
        notifyDataSetChanged();
    }

}