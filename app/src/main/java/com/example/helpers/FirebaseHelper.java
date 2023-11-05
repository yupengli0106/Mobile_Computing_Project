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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

//    public void fetchAndListenToDiscussions(DiscussionsManager manager) {
//        String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
//        usersRef.child(currentUser).child("discussions").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
//                    String discussionID = childSnapshot.getKey();
//
//                    // Fetch the discussion object (if needed)
//                    discussionsRef.child(discussionID).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot discussionSnapshot) {
//                            Discussion discussion = discussionSnapshot.getValue(Discussion.class);
//                            // Add discussion to your manager
//                            manager.addNewDiscussion(discussion);
//                            // Set listener on the last message for this discussion
//                            attachLastMessageListener(discussionID, manager);
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError error) {
//                            // Handle error here
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Handle error here
//            }
//        });
//    }
//
//    public void attachLastMessageListener(String discussionID, DiscussionsManager manager) {
//        discussionsRef.child(discussionID).child("lastMessage").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Message lastMessage = dataSnapshot.getValue(Message.class);
//                manager.updateDiscussion(discussionID, lastMessage);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Handle error here
//            }
//        });
//    }
//
//    public void listenForNewDiscussions(DiscussionsManager manager) {
//        String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
//        usersRef.child(currentUser).child("discussions").addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//                String newDiscussionID = dataSnapshot.getKey();
//
//                // Fetch the new discussion object (if needed)
//                discussionsRef.child(newDiscussionID).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot discussionSnapshot) {
//                        Discussion newDiscussion = discussionSnapshot.getValue(Discussion.class);
//                        // Add new discussion to your manager
//                        manager.addNewDiscussion(newDiscussion);
//                        // Attach a listener on the last message for this new discussion
//                        attachLastMessageListener(newDiscussionID, manager);
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//                        // Handle error here
//                    }
//                });
//            }
//
//            // Handle other ChildEventListener methods if needed...
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {}
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
//            @Override
//            public void onCancelled(DatabaseError databaseError) {}
//        });
//    }
//
//
//    public void fetchDiscussions(DiscussionsManager manager) {
//        String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
//        DatabaseReference userDiscussionsRef = usersRef.child(currentUser).child("discussions");
//        userDiscussionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot discussionSnapshot : dataSnapshot.getChildren()) {
//                    String discussionID = discussionSnapshot.getKey();
//                    fetchDiscussionDetails(manager, discussionID);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "Failed to read discussions.", databaseError.toException());
//            }
//        });
//    }
//
//    private void fetchDiscussionDetails(DiscussionsManager manager, String discussionID) {
//        DatabaseReference discussionRef = FirebaseDatabase.getInstance().getReference("discussions/" + discussionID);
//
//        discussionRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Discussion discussion = dataSnapshot.getValue(Discussion.class);
//                if (discussion != null) {
//                    discussion.setDiscussionId(dataSnapshot.getKey()); // This sets the discussionID in your Java object
//                }
//                // Update your UI with this discussion
//                // e.g., add this discussion to an adapter for a RecyclerView
//                //attachDiscussionMessageListener(discussionID);
//                manager.addNewDiscussion(discussion);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "Failed to read discussion details.", databaseError.toException());
//            }
//        });
//    }

//    private void attachDiscussionMessageListener(DiscussionsManager manager, String discussionID) {
//        DatabaseReference discussionMessagesRef = FirebaseDatabase.getInstance().getReference("discussions/" + discussionID + "/messages");
//
//        ChildEventListener messageListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//                Message newMessage = dataSnapshot.getValue(Message.class);
//                // Update your UI with this new message
//                // For instance, if you're using a RecyclerView to display messages,
//                // you'd notify the adapter of a new item here.
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
//                Message updatedMessage = dataSnapshot.getValue(Message.class);
//                // Handle changes to an existing message, if needed
//            }
//
//            // Implement other ChildEventListener methods if needed
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                // Handle a message being removed, if that's a feature in your app
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
//                // Handle a message being moved, though this is less common
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "Listener was cancelled for discussion " + discussionID, databaseError.toException());
//            }
//        };
//
//        // Attach the listener
//        discussionMessagesRef.addChildEventListener(messageListener);
//
//        // Optional: Keep a reference to the listener so you can remove it later if needed
//        // For instance, you could store it in a Map<String, ChildEventListener> with the discussionID as the key
//    }

//    private void attachNewDiscussionListener(DiscussionsManager manager) {
//        String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
//        DatabaseReference currentUserDiscussionRef = usersRef.child(currentUser).child("discussions");
//
//        ChildEventListener newDiscussionListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//                String discussionId = dataSnapshot.getKey();
//                fetchDiscussionDetails(manager, discussionId);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
//                Message updatedMessage = dataSnapshot.getValue(Message.class);
//                // Handle changes to an existing message, if needed
//            }
//
//            // Implement other ChildEventListener methods if needed
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                // Handle a message being removed, if that's a feature in your app
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
//                // Handle a message being moved, though this is less common
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        };
//
//        // Attach the listener
//        currentUserDiscussionRef.addChildEventListener(newDiscussionListener);
//
//        // Optional: Keep a reference to the listener so you can remove it later if needed
//        // For instance, you could store it in a Map<String, ChildEventListener> with the discussionID as the key
//    }
//
//    private void attachLastMessageListener(DiscussionsManager manager, String discussionID) {
//        DatabaseReference discussionLastMessageRef = FirebaseDatabase.getInstance().getReference("discussions/" + discussionID + "/lastMessage");
//
//        ChildEventListener lastMessageListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//                Message newMessage = dataSnapshot.getValue(Message.class);
//                fetchDiscussionDetails(manager, discussionID);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
//                Message updatedMessage = dataSnapshot.getValue(Message.class);
//                fetchDiscussionDetails(manager, discussionID);
//            }
//
//            // Implement other ChildEventListener methods if needed
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                // Handle a message being removed, if that's a feature in your app
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
//                // Handle a message being moved, though this is less common
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "Listener was cancelled for discussion " + discussionID, databaseError.toException());
//            }
//        };
//
//        // Attach the listener
//        discussionLastMessageRef.addChildEventListener(lastMessageListener);
//
//        // Optional: Keep a reference to the listener so you can remove it later if needed
//        // For instance, you could store it in a Map<String, ChildEventListener> with the discussionID as the key
//    }


//    public void getDiscussions(DiscussionsCallback callback) {
//        String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
//        List<Discussion> discussions = new ArrayList<>();
//        usersRef.child(currentUser).child("discussions").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                Log.d("getDiscussions", "getDiscussions: success");
//                for (DataSnapshot snapshot : task.getResult().getChildren()) {
//                    Discussion discussion = snapshot.getValue(Discussion.class);
//                    discussions.add(discussion);
//                }
//                callback.onCallback(discussions);
//            } else {
//                Log.w("getDiscussions", "getDiscussions: failure", task.getException());
//                callback.onCallback(null); // or an empty list
//            }
//        });
//    }

    //    public void getConversationMessages(String discussionId, final MessagesCallback callback) {
//        List<Message> messages = new ArrayList<>();
//        discussionsRef.child(discussionId).child("messages").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                Log.d("getMessages", "getMessages: success");
//                for (DataSnapshot snapshot : task.getResult().getChildren()) {
//                    Message message = snapshot.getValue(Message.class);
//                    messages.add(message);
//                }
//                callback.onCallback(messages);
//            } else {
//                Log.w("getMessages", "getMessages: failure", task.getException());
//                callback.onCallback(null); // or an empty list
//            }
//        });
//    }
//
//    public interface MessagesCallback {
//        void onCallback(List<Message> messages);
//    }
//
//    public void createDiscussion(String fromUserId, String toUserId, String toUserName, final AuthCallback callback) {
//        String discussionId = usersRef.child(fromUserId).child("discussions").push().getKey();
//
//        // get fromUserName
//        usersRef.child(fromUserId).child("username").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                String fromUserName = task.getResult().getValue(String.class);
//                Log.d(TAG, "createDiscussion: " + fromUserId + " " + fromUserName + " " + toUserId + " " + toUserName + " " + discussionId);
//                if (discussionId == null) {
//                    callback.onFailure("Failed to create discussion");
//                    return;
//                }
//                HashMap<String, Object> senderDiscussion = new HashMap<>();
//                senderDiscussion.put("receiverId", toUserId);
//                senderDiscussion.put("receiverUserName", toUserName);
//                senderDiscussion.put("discussionId", discussionId);
//
//                HashMap<String, Object> receiverDiscussion = new HashMap<>();
//                receiverDiscussion.put("discussionId", discussionId);
//                receiverDiscussion.put("receiverId", fromUserId);
//                receiverDiscussion.put("receiverUserName", fromUserName);
//
//                discussionsRef.child(discussionId).setValue(senderDiscussion)
//                        .addOnSuccessListener(aVoid -> {
//                            Log.d("FirebaseHelper", "Discussion created successfully.");
//                            callback.onSuccess();
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.e("FirebaseHelper", "Failed to create discussion.", e);
//                            callback.onFailure("Failed to create discussion: " + e.getMessage());
//                        });
//
//                usersRef.child(fromUserId).child("discussions").child(discussionId).setValue(senderDiscussion)
//                        .addOnSuccessListener(aVoid -> {
//                            Log.d("FirebaseHelper", "Discussion created successfully.");
//                            callback.onSuccess();
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.e("FirebaseHelper", "Failed to create discussion.", e);
//                            callback.onFailure("Failed to create discussion: " + e.getMessage());
//                        });
//                usersRef.child(toUserId).child("discussions").child(discussionId).setValue(receiverDiscussion)
//                        .addOnSuccessListener(aVoid -> {
//                            Log.d("FirebaseHelper", "Discussion created successfully.");
//                            callback.onSuccess();
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.e("FirebaseHelper", "Failed to create discussion.", e);
//                            callback.onFailure("Failed to create discussion: " + e.getMessage());
//                        });
//            } else {
//                Log.w("getMessages", "getMessages: failure", task.getException());
//                callback.onFailure("Failed to create discussion: " + task.getException().getMessage());
//            }
//        });
//    }
//
//    public void listenForNewDiscussions(DiscussionsManager manager, NewDiscussionsCallback callback) {
//        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
//        Query newConversationQuery = usersRef.child(currentUserId).child("discussions");
//        ChildEventListener childEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
//                Discussion discussion = dataSnapshot.getValue(Discussion.class);
//                String discussionId = dataSnapshot.getKey();
//
//                if (discussion != null) {
//                    discussion.setDiscussionId(discussionId);
//                    callback.onNewConversationAdded(discussion);
//                    setDiscussionListener(discussionId, manager);
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
//                // Handle changes if needed
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                // Handle removal if needed
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
//                // Handle moves if needed
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                callback.onNewConversationsError(databaseError.toException());
//            }
//        };
//
//        newConversationQuery.addChildEventListener(childEventListener);
//    }
//
//    public interface NewDiscussionsCallback {
//        void onNewConversationAdded(Discussion newDiscussion);
//
//        void onNewConversationsError(Exception exception);
//    }
//
//    public void listenForLastMessageUpdate(DiscussionsManager manager) {
//        String currentUserId = mAuth.getCurrentUser().getUid();
//
//        usersRef.child(currentUserId).child("discussions").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot discussionSnapshot : dataSnapshot.getChildren()) {
//                    String discussionId = discussionSnapshot.getKey();
//                    setDiscussionListener(discussionId, manager);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Handle error
//            }
//        });
//    }
//
//    private void setDiscussionListener(String discussionId, DiscussionsManager manager) {
//        discussionsRef.child(discussionId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Discussion discussion = dataSnapshot.getValue(Discussion.class);
//                if (discussion != null) {
//                    // Update your UI with the new discussion data, including the last message.
//                    manager.updateDiscussion(discussion);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Handle error
//            }
//        });
//    }
//
//    public interface LastMessageUpdateCallback {
//        void onLastMessageUpdated(String discussionId, Message lastMessage);
//
//        void onDiscussionError(Exception exception);
//    }
//
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
                        //message.setMessageId(messageId);
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
        Map<String, Object> participantDetails = new HashMap<>();

        participantDetails.put("status", true);

        participants.put(currentUserId, participantDetails);
        participants.put(otherParticipantID, participantDetails);

        newDiscussion.setParticipants(participants);
        newDiscussion.setDiscussionId(discussionId);

        // Add other properties to the discussion as needed, e.g., timestamps

        // Push the new discussion to Firebase
        newDiscussionRef.setValue(newDiscussion)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onDiscussionCreated(newDiscussionRef.getKey()); // Return the ID of the newly created discussion
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailed(e);
                    }
                });

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

    public interface DiscussionDetailsCallback {
        void onDiscussionDetailsReceived(List<Discussion> discussionList);

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
