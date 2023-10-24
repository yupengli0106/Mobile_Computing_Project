package com.example.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.FriendRequestsAdapter;
import com.example.adapters.UsersAdapter;
import com.example.helpers.FirebaseHelper;
import com.example.model.FriendRequest;
import com.example.model.User;
import com.example.zenly.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;


public class FriendsFragment extends Fragment {
    private UsersAdapter usersAdapter;

    private FriendRequestsAdapter friendRequestsAdapter;

    private FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFriendRequestsDialog();
            }
        });

        SearchBar searchBar = view.findViewById(R.id.search_bar);
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setupWithSearchBar(searchBar);
    }

    private void showAddFriendDialog() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_friend, null);


        TextInputEditText searchField = dialogView.findViewById(R.id.search_field);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    String searchText = searchField.getText().toString();
                    performUserSearch(searchText);

                    handled = true;
                }
                return handled;
            }
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
        friendRequestsAdapter = new FriendRequestsAdapter(new ArrayList<>());
        friendRequestsView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendRequestsView.setAdapter(friendRequestsAdapter);
        firebaseHelper.listenForFriendRequests(new FirebaseHelper.FriendRequestCallback() {
            @Override
            public void onFriendRequestReceived(List<FriendRequest> friendRequests) {
                friendRequestsAdapter.setFriendRequests(friendRequests);
            }

            @Override
            public void onFriendRequestError(Exception e) {
                Log.e("FriendRequestDialog", "Error getting friend requests", e);
                Toast.makeText(getContext(), "Error while loading friend requests.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(dialogView)
                .setTitle("Friend Apply")
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
        AlertDialog dialog = builder.create();

        dialog.show();

    }


}