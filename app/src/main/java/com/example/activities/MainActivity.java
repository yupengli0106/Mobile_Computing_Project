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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // set badge for friends_nav(request)
        friendRequestsBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_friends);

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

        firebaseHelper.getDiscussions(new FirebaseHelper.DiscussionsCallback() {
            @Override
            public void onCallback(List<Discussion> discussions) {
                DiscussionsManager.getInstance().setDiscussions(discussions);
            }
        });


        firebaseHelper.listenForNewDiscussions(new FirebaseHelper.NewDiscussionsCallback() {
            @Override
            public void onNewConversationAdded(Discussion newDiscussion) {
                DiscussionsManager.getInstance().addNewDiscussion(newDiscussion);
                Log.d("Discussions Updated", "Updated discussions:");
            }

            @Override
            public void onNewConversationsError(Exception e) {
                Toast.makeText(MainActivity.this, "can not get the conversation: " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });

//        firebaseHelper.listenForNewMessagesInDiscussions(new FirebaseHelper.NewMessageCallback() {
//            @Override
//            public void onNewMessagesReceived(List<Message> messages) {
//                MessagesManager.getInstance().setMessages(messages);
//                Log.d("Messages Updated", "Updated messages:");
//                for (Message message : messages) {
//                    Log.d("Messages Updated", "Message: " + message.toString());
//                }
//            }
//
//            @Override
//            public void onMessageError(Exception e) {
//                Toast.makeText(MainActivity.this, "cannot load conversation: " + e.getMessage(), Toast.LENGTH_LONG)
//                        .show();
//            }
//        });

        // setOnNavigationItemSelectedListener
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
