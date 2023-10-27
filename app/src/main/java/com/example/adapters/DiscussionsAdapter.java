package com.example.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    }


    @Override
    public int getItemCount() {
        return discussions.size();
    }

    public static class DiscussionViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;

        public DiscussionViewHolder(View itemView, List<Discussion> discussions, final OnDiscussionClickListener listener) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
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
