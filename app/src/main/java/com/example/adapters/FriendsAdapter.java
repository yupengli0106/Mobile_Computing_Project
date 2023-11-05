package com.example.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.helpers.FirebaseHelper;
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

        holder.itemView.setOnLongClickListener(v -> {
            holder.showPopupMenu(v, position);
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return friendlist.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {
        public TextView userName, userAvatar;

        public FriendViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userAvatar = itemView.findViewById(R.id.user_avatar);
        }

        private void showPopupMenu(View view, int position) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.friend_popup_menu, popup.getMenu());

            FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    firebaseHelper.deleteFriend(friendlist.get(position).getUserId(), new FirebaseHelper.DeletionCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(view.getContext(), "Friend deleted successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(view.getContext(), "Error deleting friend: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                } else {
                    return false;
                }
            });

            popup.show();
        }
    }

    public void setFriendList(List<Friend> friendlist) {
        this.friendlist = friendlist;
        notifyDataSetChanged();
    }

}
