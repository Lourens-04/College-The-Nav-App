package com.thenav.thenav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity {

    //butons to take the user back to the log on page or to the edit profile
    Button signOut, editProfile, viewRoutesCompleted;
    //getting the firebase database instance
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    //getting a refrence to the table users in firebase
    DatabaseReference getRefrenceToAllUsersInFirebase = database.getReference("UsersInfo");
    //used to get the current user email
    private FirebaseAuth mAuth;
    //Text view to display the user
    TextView username;
    //Strings to hold information
    String usernameInFirebase, userEmail;
    //float button to go back to the previous screen
    private FloatingActionButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Get the current user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser userE = mAuth.getCurrentUser();
        userEmail = userE.getEmail();

        //Setting the layout component
        back = findViewById(R.id.fab_back);
        signOut  = findViewById(R.id.btnSignOut);
        editProfile = findViewById(R.id.btnEditProfile);
        viewRoutesCompleted = findViewById(R.id.btnViewRoutesCompleted);
        username = findViewById(R.id.txvUsername);

        //gets the user first name and last name
        getRefrenceToAllUsersInFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                    UserInfoUpload UserContainerInFirebase = userInfo.getValue(UserInfoUpload.class);

                    if (UserContainerInFirebase.getUpEmail().equals(userEmail)) {
                        usernameInFirebase = "Account Owner \n" + UserContainerInFirebase.getUpFirstname().charAt(0) + ". " + UserContainerInFirebase.getUpLastname();
                        username.setText(usernameInFirebase);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //button to go to routes taken screen
        viewRoutesCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, RoutesTaken.class);
                startActivity(intent);
                finish();
            }
        });

        //button to sign the user out of the app
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Settings.this, LogIn.class);
                startActivity(intent);
                finish();
            }
        });

        //button to take the user to the edit profile page
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, SignUp.class);
                intent.putExtra("Profile", "Edit Profile");
                startActivity(intent);
                finish();
            }
        });

        //button to go bac to the map screen
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, Map.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
