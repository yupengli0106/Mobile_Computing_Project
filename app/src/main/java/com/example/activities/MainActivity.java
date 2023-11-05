package com.example.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fragments.ChatFragment;
import com.example.fragments.FriendsFragment;
import com.example.fragments.MapFragment;
import com.example.fragments.DiscussionsFragment;
import com.example.fragments.ProfileFragment;
import com.example.helpers.FirebaseHelper;
import com.example.managers.DiscussionsManager;
import com.example.managers.FriendManager;
import com.example.managers.FriendRequestManager;
import com.example.model.Discussion;
import com.example.model.Friend;
import com.example.model.FriendRequest;
import com.example.zenly.R;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    // fragment manager and fragments
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    // reuse mapFragment
    private final MapFragment mapFragment = new MapFragment();
    private final DiscussionsFragment discussionsFragment = new DiscussionsFragment();
    private final ChatFragment chatFragment = new ChatFragment();
    private final FriendsFragment friendsFragment = new FriendsFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();

    private BadgeDrawable friendRequestsBadge;

    private BadgeDrawable discussionUpdatesBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // set badge for friends_nav(request)
        friendRequestsBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_friends);

        discussionUpdatesBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_chat);

        // set map fragment as default fragment
        fragmentManager.beginTransaction().replace(R.id.fragment_container, mapFragment).commit();

        bottomNavigationView.setSelectedItemId(R.id.nav_map);

        // listen the friendrequest
        firebaseHelper.listenForFriendRequests(new FirebaseHelper.FriendRequestCallback() {
            @Override
            public void onFriendRequestReceived(List<FriendRequest> friendRequests) {
                FriendRequestManager.getInstance().setFriendRequests(friendRequests);
                // update the badge
                if (friendRequestsBadge != null) {
                    if (friendRequests.size() == 0) {
                        friendRequestsBadge.setVisible(false);
                    } else {
                        friendRequestsBadge.setNumber(friendRequests.size());
                        friendRequestsBadge.setVisible(true);
                    }

                }
            }

            @Override
            public void onFriendRequestError(Exception e) {
                Toast.makeText(MainActivity.this, "can not get the friend requests: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // listen for friend list
        firebaseHelper.listenForFriendsList(new FirebaseHelper.FriendsListCallback() {
            @Override
            public void onFriendsListReceived(List<Friend> friendsList) {
                FriendManager.getInstance().setFriendsList(friendsList);
                Log.d("FriendsListUpdate", "Updated friends list:");
                for (Friend friend : friendsList) {
                    // 假设 Friend 类有一个名为 getName 的方法来获取朋友的名字
                    Log.d("FriendsListUpdate", "Friend: " + friend.toString());
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MainActivity.this, "can not get the friend list: " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });

        firebaseHelper.fetchUserDiscussions(new FirebaseHelper.DiscussionListCallback() {
            @Override
            public void onDiscussionListReceived(List<Discussion> discussionList) {
                DiscussionsManager.getInstance().setDiscussions(discussionList);
                int unreadCount = 0;
                for (Discussion discussion : discussionList) {
                    if (discussion.isUnread()) {
                        unreadCount++;
                    }
                }
                if (discussionUpdatesBadge != null) {
                    if (unreadCount == 0) {
                        discussionUpdatesBadge.setVisible(false);
                    } else {
                        discussionUpdatesBadge.setNumber(unreadCount);
                        discussionUpdatesBadge.setVisible(true);
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MainActivity.this, "can not get the discussion list: " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });

        firebaseHelper.listenForDiscussionUpdates(new FirebaseHelper.DiscussionUpdateCallback() {
            @Override
            public void onDiscussionUpdateReceived(List<Discussion> updatedDiscussionList) {
                DiscussionsManager.getInstance().setDiscussions(updatedDiscussionList);
                int unreadCount = 0;
                for (Discussion discussion : updatedDiscussionList) {
                    if (discussion.isUnread()) {
                        unreadCount++;
                    }
                }
                if (discussionUpdatesBadge != null) {
                    if (unreadCount == 0) {
                        discussionUpdatesBadge.setVisible(false);
                    } else {
                        discussionUpdatesBadge.setNumber(unreadCount);
                        discussionUpdatesBadge.setVisible(true);
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MainActivity.this, "can not get the discussion list: " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });


        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_map) {
                selectedFragment = new MapFragment();
            } else if (itemId == R.id.nav_chat) {
                selectedFragment = discussionsFragment;
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = profileFragment;
            } else if (itemId == R.id.nav_friends) {
                selectedFragment = friendsFragment;
            }

            if (selectedFragment != null) {
                fragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                Log.d("MainActivityLog", "onCreate:  selectedFragment set as default fragment !");
                return true;
            } else {
                // Handle null case
                Log.w("MainActivityLog", "No fragment selected");
                Toast.makeText(this, "No fragment selected", Toast.LENGTH_SHORT).show();
                return false;
            }

        });
    }
}
