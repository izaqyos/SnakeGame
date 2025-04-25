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
        database = FirebaseDatabase.getInstance();
        if (database == null) {
             Log.e(TAG, "FirebaseDatabase.getInstance() returned null!");
             // Handle error appropriately - maybe throw exception?
             return; 
        }
        Log.d(TAG, "FirebaseDatabase instance obtained.");
        myRef = database.getReference("Users");
        Log.d(TAG, "Got DatabaseReference for /Users. Attaching ValueEventListener...");
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
        Log.d(TAG, "MyFBDB constructor exit.");
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
}
