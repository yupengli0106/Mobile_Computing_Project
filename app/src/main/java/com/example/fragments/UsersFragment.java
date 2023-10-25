package com.example.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.adapters.UserAdapter;
import com.example.helpers.FirebaseHelper;
import com.example.model.User;
import com.example.zenly.R;
import com.example.zenly.databinding.FragmentUsersBinding;

import java.util.List;

public class UsersFragment extends Fragment {

    private final String TAG = "UsersFragment";
    private FragmentUsersBinding binding;

    public UsersFragment() {
        // Required empty public constructor
    }

    public static UsersFragment newInstance(String param1, String param2) {
        UsersFragment fragment = new UsersFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getUsers(view);
    }

    public interface UsersCallback {
        void onCallback(List<User> users);
    }

    private void getUsers(@NonNull View view) {
        FirebaseHelper db = FirebaseHelper.getInstance();
//        List< User > users = db.getUsers();
//        Log.d(TAG, "getUsers: users: " + users.size() + " users: " + users.toString());
//        RecyclerView userRecyclerView = view.findViewById(R.id.usersRecyclerView);
//        if (users.size() > 0) {
//            Log.d(TAG, "getUsers: going in");
//            userRecyclerView.setAdapter(new UserAdapter(users));
//            userRecyclerView.setVisibility(View.VISIBLE);
//        } else {
//            Log.d(TAG, "getUsers: lol not going");
//            userRecyclerView.setVisibility(View.GONE);
//        }
        db.getUsers(new FirebaseHelper.UsersCallback() {
            @Override
            public void onCallback(List<User> users) {
                Log.d(TAG, "getUsers: users: " + (users != null ? users.size() : 0));
                RecyclerView userRecyclerView = view.findViewById(R.id.usersRecyclerView);
                if (users != null && !users.isEmpty()) {
                    Log.d(TAG, "getUsers: going in");
                    userRecyclerView.setAdapter(new UserAdapter(users));
                    userRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "getUsers: lol not going");
                    userRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }

//    private void isLoading(Boolean isLoading) {
//        if (isLoading) {
//            binding.progressBar.setVisibility(View.VISIBLE);
//        } else {
//            binding.progressBar.setVisibility(View.GONE);
//        }
//    }
}