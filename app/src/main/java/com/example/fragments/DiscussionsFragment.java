package com.example.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.adapters.DiscussionsAdapter;
import com.example.adapters.NewDiscussionAdapter;
import com.example.helpers.FirebaseHelper;
import com.example.managers.DiscussionsManager;
import com.example.managers.FriendManager;
import com.example.model.Discussion;
import com.example.zenly.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class DiscussionsFragment extends Fragment implements DiscussionsAdapter.OnDiscussionClickListener {

    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
    private final String currentUserId = firebaseHelper.getCurrentUserId();

    private NewDiscussionAdapter newDiscussionAdapter;

    public DiscussionsFragment() {
        // Required empty public constructor
    }

    public static DiscussionsFragment newInstance() {
        DiscussionsFragment fragment = new DiscussionsFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button newDiscussionButton = view.findViewById(R.id.button_add_friend);
        newDiscussionButton.setOnClickListener(v -> showNewDiscussionDialog());

        RecyclerView discussionRecyclerView = view.findViewById(R.id.discussions_list);
        discussionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DiscussionsAdapter discussionsAdapter = new DiscussionsAdapter(currentUserId, new ArrayList<>(), this);
        discussionRecyclerView.setAdapter(discussionsAdapter);
        DiscussionsManager.getInstance().getDiscussions().observe(getViewLifecycleOwner(),
                discussionsAdapter::setDiscussionList);
    }

    private void showNewDiscussionDialog() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_discussion, null);

        RecyclerView friendListView = dialogView.findViewById(R.id.friend_list);
        friendListView.setLayoutManager(new LinearLayoutManager(getContext()));
        newDiscussionAdapter = new NewDiscussionAdapter(new ArrayList<>());
        friendListView.setAdapter(newDiscussionAdapter);
        FriendManager.getInstance().getFriendsList().observe(getViewLifecycleOwner(), friends -> {
            newDiscussionAdapter.setFriends(friends);
        });

        builder.setView(dialogView)
                .setTitle("Start Discussion")
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Override
    public void onDiscussionClick(Discussion discussion) {
        String TAG = "DiscussionsFragment";
        Log.d(TAG, "onDiscussionClick: " + discussion.getDiscussionId());
        DiscussionDetailFragment detailFragment = DiscussionDetailFragment.newInstance(discussion);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
        firebaseHelper.updateConversationLastTimeOpened(discussion.getDiscussionId());
    }
}