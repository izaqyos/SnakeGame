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

public class MyFBDB {
    private String TAG="Firebase";

    private  static FirebaseDatabase database = null;
    private static DatabaseReference myRef;
    private static ArrayList<User> usersArrayList;

    MyFBDB(){
        if(database==null){
            database =FirebaseDatabase.getInstance();
            myRef =database.getReference("Users");
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    long count = dataSnapshot.getChildrenCount();
                    Log.d(TAG, TAG + " | AllCheckIns | Count is: " + count);
                    usersArrayList = new ArrayList<User>();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        User user = dataSnapshot1.getValue(User.class);
                        usersArrayList.add(user);
                        Log.d(TAG, TAG + " | AllCheckIns | Value " + user.toString());
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
    }

    public void saveUser(User user){
        DatabaseReference newUserRef = myRef.push();
        user.key = newUserRef.getKey();
        newUserRef.setValue(user);
    }

    public boolean userExists(User u){
        for(int i=0; i<usersArrayList.size();i++){
            User usr = usersArrayList.get(i);
            if(usr.userName.equals(u.userName) ){
                return true;
            }
        }
        return false;
    }

    public boolean checkUser(String un, String pw){
        for(int i=0; i<usersArrayList.size();i++){
            User usr = usersArrayList.get(i);
            if(usr.userName.equals(un) && usr.password.equals(pw)){
                return true;
            }
        }
        return false;
    }




}
