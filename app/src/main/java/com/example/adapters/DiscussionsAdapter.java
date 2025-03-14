package com.example.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpers.ImageHelper;
import com.example.model.Discussion;
import com.example.zenly.R;

import java.util.List;

public class DiscussionsAdapter extends RecyclerView.Adapter<DiscussionsAdapter.DiscussionViewHolder> {
    private List<Discussion> discussions;
    private final String currentUserId;
    private final ImageHelper imageHelper = new ImageHelper();


    public interface OnDiscussionClickListener {
        void onDiscussionClick(Discussion discussion);
    }

    private final OnDiscussionClickListener listener;

    public DiscussionsAdapter(String currentUserId, List<Discussion> discussions, OnDiscussionClickListener listener) {
        this.currentUserId = currentUserId;
        this.discussions = discussions;
        this.listener = listener;
    }

    @NonNull
    public DiscussionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discussion_list, parent, false);
        return new DiscussionViewHolder(itemView, discussions, listener);
    }

    @Override
    public void onBindViewHolder(DiscussionsAdapter.DiscussionViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Discussion discussion = discussions.get(position);
        String receiverID = discussion.getOtherParticipantIDs(currentUserId).get(0);
        String receiverUsername = discussion.getOtherParticipantDetails(currentUserId).get(receiverID).get("username").toString();
        holder.userName.setText(receiverUsername);
        imageHelper.fetchAndSetUserProfileImage(receiverID, holder.userPic, holder.userAvatar);

        if (discussion.getLastMessage() == null) {
            holder.lastMessage.setText("");
            return;
        }
        holder.lastMessage.setText(discussion.getLastMessage().getContent());
        if (discussion.isUnread()) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return discussions.size();
    }

    public static class DiscussionViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public TextView lastMessage;
        public TextView unreadBadge;
        public TextView userAvatar;

        public ImageView userPic;

        public DiscussionViewHolder(View itemView, List<Discussion> discussions, final OnDiscussionClickListener listener) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            unreadBadge = itemView.findViewById(R.id.unread_badge);
            userAvatar = itemView.findViewById(R.id.user_avatar);
            userPic = itemView.findViewById(R.id.profileImageView);

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
