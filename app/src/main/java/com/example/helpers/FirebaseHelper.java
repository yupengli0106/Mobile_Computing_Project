package com.example.helpers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.model.Discussion;
import com.example.model.Friend;
import com.example.model.FriendRequest;
import com.example.model.LocationData;
import com.example.model.Message;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FirebaseHelper is a singleton class that provides helper methods for Firebase
 * Realtime Database.
 * It is used for CRUD operations on the database.
 */
public class FirebaseHelper implements Serializable {

    private static final String TAG = "FirebaseHelper";

    public interface AuthCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public interface DiscussionsCallback {
        void onCallback(List<Discussion> discussions);
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

    public interface FriendsListCallback {
        void onFriendsListReceived(List<Friend> friendsList);

        void onFailed(Exception e);
    }

    public interface DeletionCallback {
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
    public final DatabaseReference usersRef;
    // Firebase Realtime Database reference to the locations node
    private final DatabaseReference locationsRef;
    // Firebase Realtime Database URL
    private final DatabaseReference friendRequestsRef;

    private final DatabaseReference discussionsRef;

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
        discussionsRef = myDatabase.child("discussions");
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
     * Get the current user's uid
     *
     * @return the current user's uid
     */
    @Nullable
    public String getCurrentUserId() {
        return mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
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
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
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
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
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
        locationsRef.child(uid).setValue(locationData).addOnSuccessListener(unused -> Log.d("uploadLocation", "uploadLocation: success")).addOnFailureListener(e -> Log.w("uploadLocation", "uploadLocation: failure", e));
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

        friendRequestsRef.child(requestId).setValue(friendRequest).addOnSuccessListener(aVoid -> {
            Log.d("FirebaseHelper", "Friend request sent successfully.");
            callback.onSuccess();
        }).addOnFailureListener(e -> {
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

        requestRef.child("status").setValue(accepted ? "accepted" : "rejected").addOnSuccessListener(aVoid -> {
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
        }).addOnFailureListener(e -> {
            Log.w("FirebaseHelper", "Error updating friend request status", e);
            callback.onFailure(e.getMessage());
        });
    }

    private void proceedWithFriendship(String fromUserId, String toUserId, DatabaseReference user1FriendsRef, DatabaseReference user2FriendsRef, final FriendshipResponseCallback callback) {
        String friendshipKey1 = user1FriendsRef.push().getKey();
        String friendshipKey2 = user2FriendsRef.push().getKey();

        if (friendshipKey1 != null && friendshipKey2 != null) {
            // Prepare to update both users' friends lists
            user1FriendsRef.child(friendshipKey1).setValue(toUserId).addOnSuccessListener(aVoid ->
                    // Once user1 is updated, proceed to update user2
                    user2FriendsRef.child(friendshipKey2).setValue(fromUserId).addOnSuccessListener(aVoidInner -> callback.onSuccess()).addOnFailureListener(e -> callback.onFailure(e.getMessage()))).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            Log.w("FirebaseHelper", "Error obtaining unique key for friendship");
            callback.onFailure("Could not generate unique key for friendship.");
        }
    }

    public void listenForFriendsList(final FriendsListCallback callback) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DatabaseReference friendsRef = usersRef.child(uid).child("friends");

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> friendIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String friendId = snapshot.getValue(String.class);
                    if (friendId != null) {
                        friendIds.add(friendId);
                    }
                }

                fetchFriendDetails(friendIds, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailed(databaseError.toException());
            }
        });
    }

    private void fetchFriendDetails(List<String> friendIds, final FriendsListCallback callback) {
        if (friendIds.isEmpty()) {
            callback.onFriendsListReceived(new ArrayList<>());
            return;
        }

        List<Friend> friendsList = new ArrayList<>();
        AtomicInteger completionCount = new AtomicInteger(0);

        for (String friendId : friendIds) {
            usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    String userId = dataSnapshot.getKey();
                    if (friend != null) {
                        friend.setUserId(userId);
                        friendsList.add(friend);
                    }

                    if (completionCount.incrementAndGet() == friendIds.size()) {
                        callback.onFriendsListReceived(friendsList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onFailed(databaseError.toException());
                }
            });
        }
    }

    public void deleteFriend(String friendId, final DeletionCallback callback) {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        DatabaseReference currentUserFriendsRef = usersRef.child(currentUserId).child("friends");
        currentUserFriendsRef.orderByValue().equalTo(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                    DatabaseReference friendRef = usersRef.child(currentUserId).child("friends").child(childSnapshot.getKey());
                    friendRef.removeValue()
                            .addOnSuccessListener(aVoid ->
                                    usersRef.child(friendId).child("friends").orderByValue().equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshotFriend) {
                                            for (DataSnapshot childSnapshotFriend : dataSnapshotFriend.getChildren()) {
                                                usersRef.child(friendId).child("friends").child(childSnapshotFriend.getKey()).removeValue()
                                                        .addOnSuccessListener(aVoidInner -> callback.onSuccess())
                                                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            callback.onFailure(databaseError.getMessage());
                                        }
                                    })
                            )
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }


    /**
     * reset the password of the user with the given email by Firebase Authentication
     *
     * @param email    the email of the user
     * @param callback the callback function
     */
    public void resetPassword(String email, final AuthCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    public void fetchUserDiscussions(final DiscussionListCallback callback) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Query userDiscussionsQuery = discussionsRef.orderByChild("participants/" + uid + "/status").equalTo(true);

        userDiscussionsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Discussion> discussions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Discussion discussion = snapshot.getValue(Discussion.class);
                    if (discussion != null) {
//                        discussion.setDiscussionId(dataSnapshot.getKey());
                        Log.d(TAG, "onDataChange: " + dataSnapshot.getKey());
                        discussions.add(discussion);
                    }
                }
                sortDiscussions(discussions);
                callback.onDiscussionListReceived(discussions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailed(databaseError.toException());
            }
        });
    }


    public void listenForDiscussionUpdates(final DiscussionUpdateCallback callback) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Query userDiscussionsQuery = discussionsRef.orderByChild("participants/" + uid + "/status").equalTo(true);

        userDiscussionsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Discussion> updatedDiscussions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Discussion discussion = snapshot.getValue(Discussion.class);
                    if (discussion != null) {
//                        discussion.setDiscussionId(dataSnapshot.getKey());
                        updatedDiscussions.add(discussion);
                    }
                }
                sortDiscussions(updatedDiscussions);
                callback.onDiscussionUpdateReceived(updatedDiscussions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailed(databaseError.toException());
            }
        });
    }

    public void createDiscussion(String otherParticipantID, final CreateDiscussionCallback callback) {
        DatabaseReference newDiscussionRef = discussionsRef.push();
        String discussionId = newDiscussionRef.getKey();
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Discussion newDiscussion = new Discussion();
        Map<String, Map<String, Object>> participants = new HashMap<>();
        Map<String, Object> currentParticipantDetails = new HashMap<>();
        Map<String, Object> otherParticipantDetails = new HashMap<>();

        AtomicInteger counter = new AtomicInteger(0);  // Counter to track fetched usernames

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey().equals(currentUserId)) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    currentParticipantDetails.put("username", username);
                } else {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    otherParticipantDetails.put("username", username);
                }

                if (counter.incrementAndGet() == 2) { // Both usernames have been fetched
                    currentParticipantDetails.put("status", true);
                    otherParticipantDetails.put("status", true);

                    participants.put(currentUserId, currentParticipantDetails);
                    participants.put(otherParticipantID, otherParticipantDetails);

                    newDiscussion.setParticipants(participants);
                    newDiscussion.setDiscussionId(discussionId);

                    // Push the new discussion to Firebase
                    newDiscussionRef.setValue(newDiscussion).addOnSuccessListener(aVoid -> callback.onDiscussionCreated(newDiscussionRef.getKey())) // Return the ID of the newly created discussion
                            .addOnFailureListener(callback::onFailed);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Log.w("TAG", "loadUserName:onCancelled", databaseError.toException());
            }
        };

        usersRef.child(currentUserId).addListenerForSingleValueEvent(listener);
        usersRef.child(otherParticipantID).addListenerForSingleValueEvent(listener);
    }

    public void sendMessage(String discussionId, String content, final AuthCallback callback) {
        String messageId = discussionsRef.child(discussionId).child("messages").push().getKey();
        Log.d(TAG, "sendMessage: " + discussionId + " " + content + " " + messageId);

        if (messageId == null) {
            callback.onFailure("Failed to send message");
            return;
        }
        String senderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", senderId);
        messageMap.put("content", content);
        messageMap.put("dateTime", String.valueOf(System.currentTimeMillis()));

        discussionsRef.child(discussionId).child("messages").child(messageId).setValue(messageMap).addOnSuccessListener(aVoid -> {
            Log.d("FirebaseHelper", "Message sent successfully.");
            callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseHelper", "Failed to send message.", e);
            callback.onFailure("Failed to send message: " + e.getMessage());
        });

        discussionsRef.child(discussionId).child("lastMessage").setValue(messageMap).addOnSuccessListener(aVoid -> {
            Log.d("FirebaseHelper", "Message sent successfully.");
            callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseHelper", "Failed to send message.", e);
            callback.onFailure("Failed to send message: " + e.getMessage());
        });
    }

    public void listenForMessagesInDiscussion(String discussionId, NewMessageCallback callback) {
        Log.d(TAG, "listenForMessagesInDiscussion: " + discussionId);
        DatabaseReference messagesRef = discussionsRef.child(discussionId).child("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> newMessages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    String messageId = snapshot.getKey();

                    if (message != null) {
                        newMessages.add(message);
                    }
                }
                callback.onNewMessagesReceived(newMessages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onMessageError(databaseError.toException());
            }
        });
    }


    public void updateConversationLastTimeOpened(String discussionId) {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        discussionsRef.child(discussionId).child("participants").child(currentUserId).child("lastTimeOpened").setValue(String.valueOf(System.currentTimeMillis()));
    }

    public interface NewMessageCallback {

        /**
         * Callback triggered when new messages are received for a discussion.
         *
         * @param messages The list of new messages received.
         */
        void onNewMessagesReceived(List<Message> messages);

        /**
         * Callback triggered in case of any error while fetching new messages.
         *
         * @param exception The exception representing the error.
         */
        void onMessageError(Exception exception);
    }

    public interface CreateDiscussionCallback {
        void onDiscussionCreated(String discussionId);

        void onFailed(Exception e);
    }


    public interface DiscussionListCallback {
        void onDiscussionListReceived(List<Discussion> discussionList);

        void onFailed(Exception e);
    }

    public interface DiscussionUpdateCallback {
        void onDiscussionUpdateReceived(List<Discussion> updatedDiscussionList);

        void onFailed(Exception e);
    }

    public void sortDiscussions(List<Discussion> discussions) {
        Collections.sort(discussions, (d1, d2) -> {
            Message message1 = d1.getLastMessage();
            Message message2 = d2.getLastMessage();

            // Check if discussions have a last message with a valid DateTime
            boolean hasLastMessage1 = (message1 != null && message1.getDateTime() != null);
            boolean hasLastMessage2 = (message2 != null && message2.getDateTime() != null);

            // If neither discussion has a last message or valid DateTime
            if (!hasLastMessage1 && !hasLastMessage2) {
                return 0; // Consider other criteria for sorting if needed
            }

            // If only the first discussion doesn't have a last message, it should appear below
            if (!hasLastMessage1) {
                return 1;
            }

            // If only the second discussion doesn't have a last message, it should appear above
            if (!hasLastMessage2) {
                return -1;
            }

            // If both discussions have a last message, sort by the DateTime of the last message
            Long time1 = Long.valueOf(message1.getDateTime());
            Long time2 = Long.valueOf(message2.getDateTime());

            return time2.compareTo(time1); // Descending order
        });
    }

}
