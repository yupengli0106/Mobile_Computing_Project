package com.example.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.adapters.FriendRequestsAdapter;
import com.example.adapters.FriendsAdapter;
import com.example.adapters.TopListAdapter;
import com.example.adapters.UsersAdapter;
import com.example.helpers.FirebaseHelper;
import com.example.managers.FriendManager;
import com.example.managers.FriendRequestManager;
import com.example.model.Friend;
import com.example.model.User;
import com.example.zenly.R;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TopListFragment extends Fragment {
    private TopListAdapter topListAdapter;
    private List<User> list = new ArrayList<>();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
    private SwipeRefreshLayout refreshLayout;

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_top_list, container, false);
        RecyclerView recyclerView = inflate.findViewById(R.id.rv_list);
        refreshLayout = inflate.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                reload();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        topListAdapter = new TopListAdapter(getContext(), list);
        recyclerView.setAdapter(topListAdapter);
        return inflate;
    }

    private void reload() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                refreshLayout.setRefreshing(false);
                list.clear();
                if (user != null) {
                    Map<String, String> friends = user.getFriends();
                    if (friends != null) {
                        for (String s : friends.keySet()) {
                            firebaseHelper.getUserProfile(friends.get(s), new FirebaseHelper.UserProfileCallback() {
                                @Override
                                public void onProfileReceived(User user) {
                                    list.add(user);
                                }

                                @Override
                                public void onFailed(Exception e) {

                                }
                            });
                        }
                    }
                }
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       // Iterate over the list of users
                       for (int i = 0; i < list.size() - 1; i++) {
                           // Perform a bubble sort pass on the list to order the users by steps in descending order
                           for (int j = 0; j < list.size() - i - 1; j++) {
                               // Compare adjacent users
                               if (list.get(j).getStep() < list.get(j + 1).getStep()) {
                                   // If the current user has fewer steps than the next user, swap them
                                   User temp = list.get(j);
                                   list.set(j, list.get(j + 1));
                                   list.set(j + 1, temp);
                               }
                           }
                       }


                       topListAdapter.notifyDataSetChanged();
                   }
               },2000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", "Get username failed: " + databaseError.getMessage());
            }
        });
    }
}
