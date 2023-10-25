package com.example.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.model.Friend;
import com.example.zenly.R;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private List<Friend> friendlist;

    public FriendsAdapter(List<Friend> friendlist) {
        this.friendlist = friendlist;
    }

    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friendlist_item, parent, false);
        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FriendsAdapter.FriendViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Friend friend = friendlist.get(position);


        holder.userName.setText(friend.getUsername());


        holder.userAvatar.setText(friend.getUsername().substring(0, 1).toUpperCase());


    }


    @Override
    public int getItemCount() {
        return friendlist.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        public TextView userName, userAvatar;

        public FriendViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userAvatar = itemView.findViewById(R.id.user_avatar);
        }
    }

    public void setFriendList(List<Friend> friendlist) {
        this.friendlist = friendlist;
        notifyDataSetChanged();
    }
}
