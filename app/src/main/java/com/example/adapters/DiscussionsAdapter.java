package com.example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.model.User;
import com.example.zenly.R;

import java.util.List;

public class DiscussionsAdapter extends RecyclerView.Adapter<DiscussionsAdapter.UserViewHolder> {

    private final List<User> users;

    public DiscussionsAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        ItemContainerChatUserBinding itemContainerChatUserBinding = ItemContainerChatUserBinding.inflate(
//                LayoutInflater.from(parent.getContext()),
//                parent,
//                false
//        );
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_container_chat_user,
                parent,
                false
        );
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public UserViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.textUsername);
        }

        void setUserData(User user) {
            userName.setText(user.getUsername());
//            binding.lastMessage.setText(user.getLastMessage());
//            binding.imageProfile.setImageBitmap(getUserImage(user.getImage()));
        }
    }
//    private Bitmap getUserImage(String encodedImage){
//        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//    }
}
