package com.example.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpers.FirebaseHelper;
import com.example.model.Discussion;
import com.example.model.Friend;
import com.example.model.User;
import com.example.zenly.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DiscussionsAdapter extends RecyclerView.Adapter<DiscussionsAdapter.DiscussionViewHolder> {
    private List<Discussion> discussions;
    private String currentUserId;

    private FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();

    public interface OnDiscussionClickListener {
        void onDiscussionClick(Discussion discussion);
    }

    private OnDiscussionClickListener listener;

    public DiscussionsAdapter(String currentUserId, List<Discussion> discussions, OnDiscussionClickListener listener) {
        this.currentUserId = currentUserId;
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
    public void onBindViewHolder(DiscussionsAdapter.DiscussionViewHolder holder,
            @SuppressLint("RecyclerView") int position) {
        Discussion discussion = discussions.get(position);
        String receiverID = discussion.getOtherParticipants(currentUserId).get(0);
        firebaseHelper.usersRef.child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class); // Assuming the field is named "username"
                holder.userName.setText(username);
                holder.userAvatar.setText(username.substring(0, 1).toUpperCase());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Log.w("TAG", "loadUserName:onCancelled", databaseError.toException());
            }
        });



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

        public DiscussionViewHolder(View itemView, List<Discussion> discussions,
                final OnDiscussionClickListener listener) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            unreadBadge = itemView.findViewById(R.id.unread_badge);
            userAvatar = itemView.findViewById(R.id.user_avatar);

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
