package com.example.adapters;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.helpers.FirebaseHelper;
import com.example.model.Friend;
import com.example.zenly.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class NewDiscussionAdapter extends RecyclerView.Adapter<NewDiscussionAdapter.NewDiscussionViewHolder> {

    private List<Friend> friends;
    private final String TAG = "NewDiscussionAdapter";

    public NewDiscussionAdapter(List<Friend> friends) {
        this.friends = friends;
    }

    public NewDiscussionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_discussion_item, parent, false);
        return new NewDiscussionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NewDiscussionAdapter.NewDiscussionViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Friend friend = friends.get(position);
        holder.userName.setText(friend.getUsername());
        holder.userAvatar.setText(friend.getUsername().substring(0, 1).toUpperCase());

        holder.newDiscussionButton.setOnClickListener(
                v -> {
                    FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    firebaseHelper.createDiscussion(friend.getUserId(), new FirebaseHelper.CreateDiscussionCallback() {
                                @Override
                                public void onDiscussionCreated(String discussionId) {
                                    Log.d(TAG, "onDiscussionCreated");
                                }

                                @Override
                                public void onFailed(Exception e) {
                                    Log.d(TAG, "onFailed");
                                }
                            }
                    );
                }
        );
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class NewDiscussionViewHolder extends RecyclerView.ViewHolder {
        public TextView userName, userAvatar;
        public FloatingActionButton newDiscussionButton;

        public NewDiscussionViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userAvatar = itemView.findViewById(R.id.user_avatar);
            newDiscussionButton = itemView.findViewById(R.id.new_discussion_button);
        }
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }
}
