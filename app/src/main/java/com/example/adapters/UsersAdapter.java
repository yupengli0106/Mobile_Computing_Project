package com.example.adapters;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.helpers.FirebaseHelper;
import com.example.model.User;
import com.example.zenly.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> userList;


    public UsersAdapter(List<User> userList) {
        this.userList = userList;
    }


    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.userlist_item, parent, false);
        return new UserViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User currentUser = userList.get(position);
        holder.userName.setText(currentUser.username);
        holder.userAvatar.setText(currentUser.username.substring(0, 1).toUpperCase());

        holder.addFriendButton.setOnClickListener(v -> {
            String message = "Are you sure you want to add " + currentUser.username + " as a friend?";
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Confirm")
                    .setMessage(message)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();

                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            firebaseHelper.getUserProfile(currentUserId, new FirebaseHelper.UserProfileCallback() {
                                @Override
                                public void onProfileReceived(User user) {
                                    firebaseHelper.sendFriendRequest(user.username, currentUserId, currentUser.getUserId(), new FirebaseHelper.AuthCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(v.getContext(), "Friend request sent!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            Toast.makeText(v.getContext(), "Failed to send friend request: " + errorMessage, Toast.LENGTH_SHORT).show();
                                        }

                                    });
                                }

                                @Override
                                public void onFailed(Exception e) {

                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView userName, userAvatar;
        public FloatingActionButton addFriendButton;

        public UserViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userAvatar = itemView.findViewById(R.id.user_avatar);
            addFriendButton = itemView.findViewById(R.id.add_friend_button);
        }
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }
}
