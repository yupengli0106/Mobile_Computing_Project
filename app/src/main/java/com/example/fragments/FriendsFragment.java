package com.example.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.FriendRequestsAdapter;
import com.example.adapters.FriendsAdapter;
import com.example.adapters.UsersAdapter;
import com.example.helpers.FirebaseHelper;
import com.example.managers.FriendManager;
import com.example.managers.FriendRequestManager;
import com.example.model.User;
import com.example.zenly.R;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class FriendsFragment extends Fragment {
    private UsersAdapter usersAdapter;

    private FriendRequestsAdapter friendRequestsAdapter;

    private FriendsAdapter friendsAdapter;

    private FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();

    private BadgeDrawable badgeDrawable;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        friendRequestsAdapter = new FriendRequestsAdapter(new ArrayList<>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button addFriendButton = view.findViewById(R.id.button_add_friend);
        addFriendButton.setOnClickListener(v -> showAddFriendDialog());

        ImageButton notificationButton = view.findViewById(R.id.button_notifications);
        FrameLayout frameLayoutParent = (FrameLayout) notificationButton.getParent();
        badgeDrawable = BadgeDrawable.create(requireContext());
        BadgeUtils.attachBadgeDrawable(badgeDrawable, notificationButton, frameLayoutParent);
        badgeDrawable.setVerticalOffset(60);
        badgeDrawable.setHorizontalOffset(-90);


        notificationButton.setOnClickListener(v -> showFriendRequestsDialog());

        SearchBar searchBar = view.findViewById(R.id.search_bar);
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setupWithSearchBar(searchBar);

        RecyclerView friendListView = view.findViewById(R.id.friends_list);
        friendListView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsAdapter = new FriendsAdapter(new ArrayList<>());
        friendListView.setAdapter(friendsAdapter);
        FriendManager.getInstance().getFriendsList().observe(getViewLifecycleOwner(), friendList -> {
            friendsAdapter.setFriendList(friendList);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        FriendRequestManager.getInstance().getFriendRequests().observe(getViewLifecycleOwner(), friendRequests -> {
            friendRequestsAdapter.setFriendRequests(friendRequests);
            if (badgeDrawable != null) {
                if (friendRequests.size() == 0) {
                    badgeDrawable.setVisible(false);
                } else {
//                    badgeDrawable.setNumber(friendRequests.size());
                    badgeDrawable.setVisible(true);
                }
            }
        });

    }


    private void showAddFriendDialog() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_friend, null);


        TextInputEditText searchField = dialogView.findViewById(R.id.search_field);
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                String searchText = searchField.getText().toString();
                performUserSearch(searchText);

                handled = true;
            }
            return handled;
        });

        RecyclerView usersList = dialogView.findViewById(R.id.users_list);
        usersAdapter = new UsersAdapter(new ArrayList<>());
        usersList.setLayoutManager(new LinearLayoutManager(getContext()));
        usersList.setAdapter(usersAdapter);

        builder.setView(dialogView)
                .setTitle("Add friend")
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());


        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void performUserSearch(String searchText) {

        firebaseHelper.searchUsers(searchText, new FirebaseHelper.UserSearchCallback() {
            @Override
            public void onUserFound(List<User> users) {
                usersAdapter.setUsers(users);
            }

            @Override
            public void onUserNotFound() {
                Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFriendRequestsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_friend_requests, null);

        RecyclerView friendRequestsView = dialogView.findViewById(R.id.friend_requests);
        friendRequestsView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendRequestsView.setAdapter(friendRequestsAdapter);

        builder.setView(dialogView)
                .setTitle("Friend Apply")
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
        AlertDialog dialog = builder.create();

        dialog.show();

    }
}
