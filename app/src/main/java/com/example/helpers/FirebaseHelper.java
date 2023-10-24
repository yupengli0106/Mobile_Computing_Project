package com.example.helpers;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.model.FriendRequest;
import com.example.model.LocationData;
import com.example.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * FirebaseHelper is a singleton class that provides helper methods for Firebase Realtime Database.
 * It is used for CRUD operations on the database.
 */
public class FirebaseHelper implements Serializable {
    public interface AuthCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public interface UserSearchCallback {
        void onUserFound(List<User> users);

        void onUserNotFound();

        void onError(Exception e);
    }

    public interface FriendRequestCallback {
        void onFriendRequestReceived(List<FriendRequest> friendRequests);

        void onFriendRequestError(Exception e);
    }

    public interface UserProfileCallback {
        void onProfileReceived(User user);

        void onFailed(Exception e);
    }

    public interface FriendshipResponseCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    // Singleton pattern
    private static volatile FirebaseHelper instance;
    // Firebase Authentication instance
    private final FirebaseAuth mAuth;
    // Firebase Realtime Database reference
    private final DatabaseReference myDatabase;
    // Firebase Realtime Database reference to the users node
    private final DatabaseReference usersRef;
    // Firebase Realtime Database reference to the locations node
    private final DatabaseReference locationsRef;
    // Firebase Realtime Database URL
    private final DatabaseReference friendRequestsRef;
    private static final String URL = "https://mobile-computing-ef31f-default-rtdb.asia-southeast1.firebasedatabase.app/";


    private FirebaseHelper() {
        if (instance != null) {
            throw new RuntimeException("Please Use getInstance() method to get the single instance of this class.");
        }
        // Firebase Realtime Database initialization
        myDatabase = FirebaseDatabase.getInstance(URL).getReference();
        // Firebase Authentication initialization
        mAuth = FirebaseAuth.getInstance();
        // Get a reference to the root node of the database (users)
        usersRef = myDatabase.child("users");
        locationsRef = myDatabase.child("locations");
        friendRequestsRef = myDatabase.child("friendRequests");
    }

    /**
     * Get the singleton instance of this class
     *
     * @return the singleton instance of this class
     */
    public static FirebaseHelper getInstance() {
        // Double check locking pattern
        if (instance == null) {
            synchronized (FirebaseHelper.class) {
                if (instance == null) {
                    instance = new FirebaseHelper();
                }
            }
        }
        return instance;
    }

    /**
     * This method is called immediately after an object of this class is deserialized.
     *
     * @return the singleton instance of this class
     */
    protected Object readResolve() {
        return getInstance();
    }

    /**
     * use firebase authentication to register a user
     *
     * @param email    the email of the user
     * @param password the password of the user
     * @param user     the user object
     * @param callback the callback function
     */
    public void registerUser(String email, String password, final User user, final AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        usersRef.child(userId).setValue(user);
                        callback.onSuccess();
                    } else {
                        callback.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    /**
     * use firebase authentication to login a user
     *
     * @param email    the email of the user
     * @param password the password of the user
     * @param callback the callback function
     */
    public void loginUser(String email, String password, final AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    /**
     * upload user location data to firebase realtime database
     *
     * @param locationData the location data to be uploaded (Object of LocationData class)
     */
    public void uploadLocation(LocationData locationData) {
        // get current user's uid from firebase authentication
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        // uid is the key of the location data in the database (locations node)
        locationsRef.child(uid).setValue(locationData)
                .addOnSuccessListener(unused -> Log.d("uploadLocation", "uploadLocation: success"))
                .addOnFailureListener(e -> Log.w("uploadLocation", "uploadLocation: failure", e));
    }

    public void searchUsers(String keyword, final UserSearchCallback callback) {
        Query searchQuery = usersRef.orderByChild("username").startAt(keyword).endAt(keyword + "\uf8ff");

        // Executing the query
        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> userList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        String userId = userSnapshot.getKey();
                        if (user != null) {
                            user.setUserId(userId);
                            userList.add(user);
                        }
                    }
                    callback.onUserFound(userList);
                } else {
                    callback.onUserNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handling errors
                callback.onError(databaseError.toException());
            }
        });
    }

    public void sendFriendRequest(String requester, String fromUserId, String toUserId, final AuthCallback callback) {
        HashMap<String, String> friendRequest = new HashMap<>();
        friendRequest.put("requester", requester);
        friendRequest.put("fromUserId", fromUserId);
        friendRequest.put("toUserId", toUserId);
        friendRequest.put("status", "pending");

        String requestId = friendRequestsRef.push().getKey();

        if (requestId == null) {
            callback.onFailure("Failed to send friend request");
            return;
        }

        friendRequestsRef.child(requestId).setValue(friendRequest)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Friend request sent successfully.");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Failed to send friend request.", e);
                    callback.onFailure("Failed to send friend request: " + e.getMessage());
                });
    }

    public void listenForFriendRequests(FriendRequestCallback callback) {

        Query friendRequestQuery = friendRequestsRef.orderByChild("toUserId").equalTo(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        friendRequestQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FriendRequest> friendRequests = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FriendRequest friendRequest = snapshot.getValue(FriendRequest.class);
                    String requestId = snapshot.getKey();

                    if (friendRequest != null) {
                        friendRequest.setRequestId(requestId);
                        if ("pending".equals(friendRequest.getStatus())) {
                            friendRequests.add(friendRequest);
                        }
                    }
                }
                callback.onFriendRequestReceived(friendRequests);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFriendRequestError(databaseError.toException());
            }
        });
    }


    public void getUserProfile(String userId, final UserProfileCallback callback) {
        DatabaseReference userReference = usersRef.child(userId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    user.setUserId(dataSnapshot.getKey());
                    callback.onProfileReceived(user);
                } else {
                    callback.onFailed(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error callback
                callback.onFailed(databaseError.toException());
            }
        });
    }

    public void respondToFriendRequest(String requestId, String fromUserId, String toUserId, boolean accepted, final FriendshipResponseCallback callback) {
        DatabaseReference requestRef = friendRequestsRef.child(requestId);

        requestRef.child("status").setValue(accepted ? "accepted" : "rejected")
                .addOnSuccessListener(aVoid -> {
                    if (accepted) {
                        DatabaseReference user1FriendsRef = usersRef.child(fromUserId).child("friends");
                        DatabaseReference user2FriendsRef = usersRef.child(toUserId).child("friends");

                        // Check if "friends" nodes exist for both users, and then proceed
                        user1FriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // Proceed only if the "friends" node exists or it's okay to create it
                                proceedWithFriendship(fromUserId, toUserId, user1FriendsRef, user2FriendsRef, callback);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.w("FirebaseHelper", "Failed to check if 'friends' node exists for user1", error.toException());
                                callback.onFailure("Failed to check friends data for users.");
                            }
                        });
                    } else {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("FirebaseHelper", "Error updating friend request status", e);
                    callback.onFailure(e.getMessage());
                });
    }

    private void proceedWithFriendship(String fromUserId, String toUserId, DatabaseReference user1FriendsRef, DatabaseReference user2FriendsRef, final FriendshipResponseCallback callback) {
        String friendshipKey1 = user1FriendsRef.push().getKey();
        String friendshipKey2 = user2FriendsRef.push().getKey();

        if (friendshipKey1 != null && friendshipKey2 != null) {
            // Prepare to update both users' friends lists
            user1FriendsRef.child(friendshipKey1).setValue(toUserId)
                    .addOnSuccessListener(aVoid ->
                            // Once user1 is updated, proceed to update user2
                            user2FriendsRef.child(friendshipKey2).setValue(fromUserId)
                                    .addOnSuccessListener(aVoidInner -> callback.onSuccess())
                                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()))
                    )
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            Log.w("FirebaseHelper", "Error obtaining unique key for friendship");
            callback.onFailure("Could not generate unique key for friendship.");
        }
    }


}
