package com.example.snake;

import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyFBDB {
    private String TAG="Firebase";

    // Use instance variables instead of static
    private FirebaseDatabase database = null;
    private DatabaseReference myRef;
    private List<User> usersArrayList = new CopyOnWriteArrayList<>(); // Initialize instance list

    // Listener interface for data load events
    public interface DataLoadListener {
        void onDataLoaded();
        void onDataLoadError(DatabaseError error);
    }
    // Listeners should probably be per-instance too, if multiple instances are expected
    private List<DataLoadListener> dataLoadListeners = new CopyOnWriteArrayList<>();

    // Method to add a listener (now operates on instance list)
    public void addDataLoadListener(DataLoadListener listener) {
        if (!dataLoadListeners.contains(listener)) {
            dataLoadListeners.add(listener);
        }
        if (!usersArrayList.isEmpty()) {
             listener.onDataLoaded();
        }
    }

    // Method to remove a listener (now operates on instance list)
    public void removeDataLoadListener(DataLoadListener listener) {
        dataLoadListeners.remove(listener);
    }

    MyFBDB(){
        Log.d(TAG, "MyFBDB constructor entry.");
        // Remove the static check - always initialize for this instance
        Log.d(TAG, "Initializing FirebaseDatabase instance for this MyFBDB object...");
        try {
            // Explicitly set the database URL
            String dbUrl = "https://snakegame-b3319-default-rtdb.europe-west1.firebasedatabase.app/";
            Log.d(TAG, "Trying to connect to Firebase URL: " + dbUrl);
            database = FirebaseDatabase.getInstance(dbUrl);
            
            if (database == null) {
                 Log.e(TAG, "FirebaseDatabase.getInstance() returned null!");
                 // Handle error appropriately - maybe throw exception?
                 return; 
            }
            Log.d(TAG, "FirebaseDatabase instance obtained.");
            myRef = database.getReference("Users");
            Log.d(TAG, "Got DatabaseReference for /Users. Attaching ValueEventListener...");

            // Test write to verify connection
            DatabaseReference testRef = database.getReference("connection_test");
            testRef.setValue("connected_at_" + System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Test write SUCCESS - connection confirmed"))
                .addOnFailureListener(e -> Log.e(TAG, "Test write FAILED - connection error: " + e.getMessage()));
                
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, TAG + " | onDataChange received for instance: " + MyFBDB.this.hashCode());
                    List<User> tempList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        User user = dataSnapshot1.getValue(User.class);
                        if (user != null) { 
                           tempList.add(user);
                           Log.d(TAG, TAG + " | Loaded User: " + user.toString());
                        } else {
                            Log.w(TAG, TAG + " | Found null user data in snapshot: " + dataSnapshot1.getKey());
                        }
                    }
                    // Update the instance list
                    usersArrayList.clear();
                    usersArrayList.addAll(tempList);
                    Log.d(TAG, TAG + " | Instance usersArrayList updated. Size: " + usersArrayList.size());

                    Log.d(TAG, "Notifying " + dataLoadListeners.size() + " instance listeners about data load.");
                    for (DataLoadListener listener : dataLoadListeners) {
                        listener.onDataLoaded();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value for instance: "+ MyFBDB.this.hashCode(), error.toException());
                    for (DataLoadListener listener : dataLoadListeners) {
                        listener.onDataLoadError(error);
                    }
                }
            });
            Log.d(TAG, "ValueEventListener attached for instance: " + this.hashCode());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
        }
        Log.d(TAG, "MyFBDB constructor exit.");
    }
    public List<User> getUsersArrayList() {
        if (this.usersArrayList != null) {
            return new ArrayList<>(this.usersArrayList); // Return a copy
        }
        return new ArrayList<>(); // Return an empty list if the source is null
    }
    public void saveUser(User user){
        // Ensure myRef is initialized before using
        if (myRef == null) {
             Log.e(TAG, "saveUser called before myRef is initialized!");
             return; // Or throw an exception
        }
        DatabaseReference newUserRef = myRef.push();
        user.key = newUserRef.getKey();
        newUserRef.setValue(user);
    }

    public boolean userExists(User u){
        if (usersArrayList == null) { // Should not happen with instance init
             Log.e(TAG, "userExists called when usersArrayList is null!");
             return false;
        }
        if (u == null || u.getUserName() == null) return false;

        for(User usr : usersArrayList){ 
            if(usr.userName != null && usr.userName.equals(u.userName)){
                return true;
            }
        }
        return false;
    }

    public boolean checkUser(String un, String pw){
        if (usersArrayList == null) { // Should not happen with instance init
             Log.e(TAG, "checkUser called when usersArrayList is null!");
             return false;
        }
        if (un == null || pw == null) return false;

        for(User usr : usersArrayList){ 
            if(usr.userName != null && usr.userName.equals(un) && usr.password != null && usr.password.equals(pw)){
                return true;
            }
        }
        return false;
    }

    // Method to ensure the test user exists in Firebase
    public void ensureTestUserExists() {
        Log.d(TAG, "ensureTestUserExists: Checking for testuser using addListenerForSingleValueEvent...");
        if (myRef == null) {
            Log.e(TAG, "ensureTestUserExists: myRef is null, cannot perform check.");
            return;
        }

        // Use a single-event listener for a more direct check
        myRef.orderByChild("userName").equalTo("testuser").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Test user does not exist, create it
                    Log.d(TAG, "ensureTestUserExists: Test user not found via single event check, creating...");
                    User testUser = new User("testuser", "abc123", 0, 0, 0);
                    saveUser(testUser);
                } else {
                    // Test user already exists
                    Log.d(TAG, "ensureTestUserExists: Test user found via single event check.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "ensureTestUserExists: Database error during single event check.", databaseError.toException());
            }
        });
    }

    // Async callback interface for username existence
    public interface UserExistsCallback {
        void onResult(boolean exists, Exception error);
    }

    // Async check for username existence in Firebase
    public void userExistsAsync(String username, UserExistsCallback callback) {
        Log.d(TAG, "userExistsAsync called for username: " + username);
        if (myRef == null) {
            Log.e(TAG, "Database reference not initialized");
            callback.onResult(false, new Exception("Database reference not initialized"));
            return;
        }
        
        try {
            Log.d(TAG, "Creating query: orderByChild(\"userName\").equalTo(\"" + username + "\")");
            myRef.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange for userExistsAsync, exists: " + dataSnapshot.exists() + ", count: " + dataSnapshot.getChildrenCount());
                    boolean exists = dataSnapshot.exists();
                    Log.d(TAG, "Calling callback.onResult with exists=" + exists);
                    callback.onResult(exists, null);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled for userExistsAsync: " + databaseError.getMessage() + ", code: " + databaseError.getCode());
                    callback.onResult(false, databaseError.toException());
                }
            });
            Log.d(TAG, "Added SingleValueEvent listener for username check");
        } catch (Exception e) {
            Log.e(TAG, "Exception in userExistsAsync: " + e.getMessage());
            callback.onResult(false, e);
        }
    }

    // Callback interface for login result
    public interface LoginCallback {
        void onResult(boolean isValid, Exception error);
    }

    // Async check for username and password against Firebase
    public void checkUserAsync(String username, String password, LoginCallback callback) {
        Log.d(TAG, "checkUserAsync called for username: " + username);
        if (myRef == null) {
            Log.e(TAG, "Database reference not initialized");
            callback.onResult(false, new Exception("Database reference not initialized"));
            return;
        }
        
        try {
            Log.d(TAG, "Creating query to check credentials");
            myRef.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange for checkUserAsync, results count: " + dataSnapshot.getChildrenCount());
                    boolean isValid = false;
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.userName != null && user.userName.equals(username) 
                            && user.password != null && user.password.equals(password)) {
                            isValid = true;
                            Log.d(TAG, "Found matching credentials for user: " + username);
                            break;
                        }
                    }
                    
                    Log.d(TAG, "Calling callback.onResult with isValid=" + isValid);
                    callback.onResult(isValid, null);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled for checkUserAsync: " + databaseError.getMessage() + ", code: " + databaseError.getCode());
                    callback.onResult(false, databaseError.toException());
                }
            });
            Log.d(TAG, "Added SingleValueEvent listener for credential check");
        } catch (Exception e) {
            Log.e(TAG, "Exception in checkUserAsync: " + e.getMessage());
            callback.onResult(false, e);
        }
    }

    // Callback interface for high score operations
    public interface ScoreCallback {
        void onResult(int score, Exception error);
    }

    // Get high score for a specific user
    public void getUserHighScore(String username, ScoreCallback callback) {
        Log.d(TAG, "getUserHighScore called for username: " + username);
        if (myRef == null) {
            callback.onResult(0, new Exception("Database reference not initialized"));
            return;
        }
        
        try {
            Log.d(TAG, "Creating query to check user's high score");
            myRef.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange for getUserHighScore, results count: " + dataSnapshot.getChildrenCount());
                    int highScore = 0;
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.userName != null && user.userName.equals(username)) {
                            highScore = user.score;
                            Log.d(TAG, "Found high score " + highScore + " for user: " + username);
                            break;
                        }
                    }
                    
                    Log.d(TAG, "Calling callback.onResult with highScore=" + highScore);
                    callback.onResult(highScore, null);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled for getUserHighScore: " + databaseError.getMessage());
                    callback.onResult(0, databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in getUserHighScore: " + e.getMessage());
            callback.onResult(0, e);
        }
    }

    // Get the global high score (highest score across all users)
    public void getGlobalHighScore(ScoreCallback callback) {
        Log.d(TAG, "getGlobalHighScore called");
        if (myRef == null) {
            callback.onResult(0, new Exception("Database reference not initialized"));
            return;
        }
        
        try {
            Log.d(TAG, "Creating query to get global high score");
            myRef.orderByChild("score").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange for getGlobalHighScore, results count: " + dataSnapshot.getChildrenCount());
                    int highScore = 0;
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            highScore = user.score;
                            Log.d(TAG, "Found global high score: " + highScore);
                            break;
                        }
                    }
                    
                    Log.d(TAG, "Calling callback.onResult with global highScore=" + highScore);
                    callback.onResult(highScore, null);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled for getGlobalHighScore: " + databaseError.getMessage());
                    callback.onResult(0, databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in getGlobalHighScore: " + e.getMessage());
            callback.onResult(0, e);
        }
    }

    // Update high score for a user
    public void updateUserHighScore(String username, int newScore, ScoreCallback callback) {
        Log.d(TAG, "updateUserHighScore called for username: " + username + " with score: " + newScore);
        if (myRef == null) {
            callback.onResult(0, new Exception("Database reference not initialized"));
            return;
        }
        
        try {
            Log.d(TAG, "Creating query to update user's high score");
            myRef.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange for updateUserHighScore, results count: " + dataSnapshot.getChildrenCount());
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.userName != null && user.userName.equals(username)) {
                            // Only update if new score is higher
                            if (newScore > user.score) {
                                user.score = newScore;
                                snapshot.getRef().child("score").setValue(newScore);
                                Log.d(TAG, "Updated high score to " + newScore + " for user: " + username);
                            } else {
                                Log.d(TAG, "New score " + newScore + " not higher than existing score " + user.score);
                            }
                            callback.onResult(user.score, null);
                            return;
                        }
                    }
                    
                    // User not found
                    Log.w(TAG, "User " + username + " not found for score update");
                    callback.onResult(0, new Exception("User not found"));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled for updateUserHighScore: " + databaseError.getMessage());
                    callback.onResult(0, databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in updateUserHighScore: " + e.getMessage());
            callback.onResult(0, e);
        }
    }
}
