package com.example.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.helpers.FirebaseHelper;
import com.example.model.FriendRequest;
import com.example.zenly.R;

import java.util.List;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.FriendRequestViewHolder> {
    private List<FriendRequest> friendRequests;

    public FriendRequestsAdapter(List<FriendRequest> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public FriendRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_requests_item, parent, false);
        return new FriendRequestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FriendRequestsAdapter.FriendRequestViewHolder holder, @SuppressLint("RecyclerView") int position) {

        FriendRequest friendRequest = friendRequests.get(position);


        holder.userName.setText(friendRequest.getRequester());


        holder.userAvatar.setText(friendRequest.getRequester().substring(0, 1).toUpperCase());

        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseHelper.respondToFriendRequest(friendRequest.getRequestId(), friendRequest.getFromUserId(), friendRequest.getToUserId(), true, new FirebaseHelper.FriendshipResponseCallback() {
                    @Override
                    public void onSuccess() {

                        notifyItemRemoved(position);
                        Toast.makeText(holder.itemView.getContext(), "Successfully add friend.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(holder.itemView.getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        holder.refuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseHelper.respondToFriendRequest(friendRequest.getRequestId(), friendRequest.getFromUserId(), friendRequest.getToUserId(), false, new FirebaseHelper.FriendshipResponseCallback() {
                    @Override
                    public void onSuccess() {
                        notifyItemRemoved(position);
                        Toast.makeText(holder.itemView.getContext(), "Successfully denny friend.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(holder.itemView.getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    public static class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        public TextView userName, userAvatar;
        public Button acceptButton, refuseButton;

        public FriendRequestViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userAvatar = itemView.findViewById(R.id.user_avatar);
            acceptButton = itemView.findViewById(R.id.accept_button);
            refuseButton = itemView.findViewById(R.id.refuse_button);
        }
    }

    public void setFriendRequests(List<FriendRequest> friendRequests) {
        this.friendRequests = friendRequests;
        notifyDataSetChanged();
    }
}
