package com.example.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.UsersAdapter;
import com.example.helpers.FirebaseHelper;
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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        usersList.setLayoutManager(new LinearLayoutManager(getContext())); // 设置布局管理器
        usersList.setAdapter(usersAdapter); // 设置适配器

        builder.setView(dialogView)
                .setTitle("Add friend")
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());


        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void performUserSearch(String searchText) {
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
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

}