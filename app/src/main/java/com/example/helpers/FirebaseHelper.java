package com.example.helpers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.managers.DiscussionsManager;
import com.example.model.Discussion;
import com.example.model.Friend;
import com.example.model.FriendRequest;
import com.example.model.LocationData;
import com.example.model.Message;
import com.example.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

    public void getDiscussions(DiscussionsCallback callback) {
        String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        List<Discussion> discussions = new ArrayList<>();
        usersRef.child(currentUser).child("discussions").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.d("getDiscussions", "getDiscussions: success");
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Discussion discussion = snapshot.getValue(Discussion.class);
                    discussions.add(discussion);
                }
                callback.onCallback(discussions);
            } else {
                Log.w("getDiscussions", "getDiscussions: failure", task.getException());
                callback.onCallback(null); // or an empty list
            }
        });
    }

    public void getConversationMessages(String discussionId, final MessagesCallback callback) {
        List<Message> messages = new ArrayList<>();
        discussionsRef.child(discussionId).child("messages").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.d("getMessages", "getMessages: success");
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                }
                callback.onCallback(messages);
            } else {
                Log.w("getMessages", "getMessages: failure", task.getException());
                callback.onCallback(null); // or an empty list
            }
        });
    }

    public interface MessagesCallback {
        void onCallback(List<Message> messages);
    }

    public void getConversationsMessages(final MessagesCallback callback) {

    }

    public void createDiscussion(String fromUserId, String toUserId, String toUserName, final AuthCallback callback) {
        String discussionId = usersRef.child(fromUserId).child("discussions").push().getKey();

        // get fromUserName
        usersRef.child(fromUserId).child("username").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String fromUserName = task.getResult().getValue(String.class);
                Log.d(TAG, "createDiscussion: " + fromUserId + " " + fromUserName + " " + toUserId + " " + toUserName + " " + discussionId);
                if (discussionId == null) {
                    callback.onFailure("Failed to create discussion");
                    return;
                }
                HashMap<String, Object> senderDiscussion = new HashMap<>();
                senderDiscussion.put("receiverId", toUserId);
                senderDiscussion.put("receiverUserName", toUserName);
                senderDiscussion.put("discussionId", discussionId);

                HashMap<String, Object> receiverDiscussion = new HashMap<>();
                receiverDiscussion.put("discussionId", discussionId);
                receiverDiscussion.put("receiverId", fromUserId);
                receiverDiscussion.put("receiverUserName", fromUserName);

                discussionsRef.child(discussionId).setValue(senderDiscussion)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirebaseHelper", "Discussion created successfully.");
                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirebaseHelper", "Failed to create discussion.", e);
                            callback.onFailure("Failed to create discussion: " + e.getMessage());
                        });

                usersRef.child(fromUserId).child("discussions").child(discussionId).setValue(senderDiscussion)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirebaseHelper", "Discussion created successfully.");
                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirebaseHelper", "Failed to create discussion.", e);
                            callback.onFailure("Failed to create discussion: " + e.getMessage());
                        });
                usersRef.child(toUserId).child("discussions").child(discussionId).setValue(receiverDiscussion)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirebaseHelper", "Discussion created successfully.");
                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirebaseHelper", "Failed to create discussion.", e);
                            callback.onFailure("Failed to create discussion: " + e.getMessage());
                        });
            } else {
                Log.w("getMessages", "getMessages: failure", task.getException());
                callback.onFailure("Failed to create discussion: " + task.getException().getMessage());
            }
        });
    }

    public void listenForNewDiscussions(DiscussionsManager manager, NewDiscussionsCallback callback) {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        Query newConversationQuery = usersRef.child(currentUserId).child("discussions");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Discussion discussion = dataSnapshot.getValue(Discussion.class);
                String discussionId = dataSnapshot.getKey();

                if (discussion != null) {
                    discussion.setDiscussionId(discussionId);
                    callback.onNewConversationAdded(discussion);
                    setDiscussionListener(discussionId, manager);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // Handle changes if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Handle removal if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // Handle moves if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onNewConversationsError(databaseError.toException());
            }
        };

        newConversationQuery.addChildEventListener(childEventListener);
    }

    public interface NewDiscussionsCallback {
        void onNewConversationAdded(Discussion newDiscussion);

        void onNewConversationsError(Exception exception);
    }

    public void listenForLastMessageUpdate(DiscussionsManager manager) {
        String currentUserId = mAuth.getCurrentUser().getUid();

        usersRef.child(currentUserId).child("discussions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot discussionSnapshot : dataSnapshot.getChildren()) {
                    String discussionId = discussionSnapshot.getKey();
                    setDiscussionListener(discussionId, manager);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void setDiscussionListener(String discussionId, DiscussionsManager manager) {
        discussionsRef.child(discussionId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Discussion discussion = dataSnapshot.getValue(Discussion.class);
                if (discussion != null) {
                    // Update your UI with the new discussion data, including the last message.
                    manager.updateDiscussion(discussion);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public interface LastMessageUpdateCallback {
        void onLastMessageUpdated(String discussionId, Message lastMessage);

        void onDiscussionError(Exception exception);
    }

    public void sendMessage(String discussionId, String receiverId, String content, final AuthCallback callback) {
        String messageId = discussionsRef.child(discussionId).child("messages").push().getKey();

        if (messageId == null) {
            callback.onFailure("Failed to send message");
            return;
        }
        String senderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("messageId", messageId);
        messageMap.put("senderId", senderId);
        messageMap.put("receiverId", receiverId);
        messageMap.put("content", content);
        messageMap.put("dateTime", String.valueOf(System.currentTimeMillis()));

        discussionsRef.child(discussionId).child("messages").child(messageId).setValue(messageMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Message sent successfully.");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Failed to send message.", e);
                    callback.onFailure("Failed to send message: " + e.getMessage());
                });

        discussionsRef.child(discussionId).child("lastMessage").setValue(messageMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Message sent successfully.");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Failed to send message.", e);
                    callback.onFailure("Failed to send message: " + e.getMessage());
                });
    }

    public void listenForMessagesInDiscussion(String discussionId, NewMessageCallback callback) {
        DatabaseReference messagesRef = discussionsRef.child(discussionId).child("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> newMessages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    String messageId = snapshot.getKey();

                    if (message != null) {
                        message.setMessageId(messageId);
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
        usersRef.child(currentUserId).child("discussions").child(discussionId).child("lastTimeOpened").setValue(String.valueOf(System.currentTimeMillis()));
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


}
