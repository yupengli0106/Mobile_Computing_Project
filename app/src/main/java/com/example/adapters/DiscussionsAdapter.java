package com.example.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.model.Discussion;
import com.example.model.Friend;
import com.example.model.User;
import com.example.zenly.R;

import java.util.List;

public class DiscussionsAdapter extends RecyclerView.Adapter<DiscussionsAdapter.DiscussionViewHolder> {
    private List<Discussion> discussions;

    public interface OnDiscussionClickListener {
        void onDiscussionClick(Discussion discussion);
    }

    private OnDiscussionClickListener listener;

    public DiscussionsAdapter(List<Discussion> discussions, OnDiscussionClickListener listener) {
        this.discussions = discussions;
        this.listener = listener;
    }

    @NonNull
    public DiscussionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discussion_list, parent, false);
        return new DiscussionViewHolder(itemView, discussions, listener);
    }

    @Override
    public void onBindViewHolder(DiscussionsAdapter.DiscussionViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Discussion discussion = discussions.get(position);
        holder.userName.setText(discussion.getReceiverUserName());
        if (discussion.getLastMessage() == null) {
            holder.lastMessage.setText("");
            return;
        }
        holder.lastMessage.setText(discussion.getLastMessage().getContent());
    }


    @Override
    public int getItemCount() {
        return discussions.size();
    }

    public static class DiscussionViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public TextView lastMessage;
        public TextView unreadBadge;

        public DiscussionViewHolder(View itemView, List<Discussion> discussions, final OnDiscussionClickListener listener) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            unreadBadge = itemView.findViewById(R.id.unread_badge);

            // for each conversation, if it is unread, set the badge as visible
            for (Discussion discussion : discussions) {
                if (discussion.isUnread()) {
                    unreadBadge.setVisibility(View.VISIBLE);
                } else {
                    unreadBadge.setVisibility(View.GONE);
                }
            }

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDiscussionClick(discussions.get(position));
                }
            });
        }
    }

    public void setDiscussionList(List<Discussion> discussions) {
        this.discussions = discussions;
        notifyDataSetChanged();
    }
}

