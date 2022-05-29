package com.thenav.thenav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RoutesTaken extends AppCompatActivity {

    //calling the Route taken Adapter class
    private RouteTakenAdapter RouteTakenAdapter;
    //getting a refrence to the table users activity in firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refToUsersRoutesTakenTableInFireBase = database.getReference("UsersRoutesTaken");
    //used to get the current user email
    private FirebaseAuth mAuth;
    //Strings to hold user email
    String userEmail;
    //List that will be sent to the RecyclerView
    ArrayList<String> mylist = new ArrayList<String>();
    //RecyclerView that will display the routes
    RecyclerView listRoutes;
    private RecyclerView.LayoutManager layoutManager;
    //Declaring the float button to go back
    private FloatingActionButton back;
    //Bundle to check if the user came from the settings page
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_taken);

        //Get the current user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser userE = mAuth.getCurrentUser();

        //Setting the layout component
        listRoutes = findViewById(R.id.rclv_ListRoutes);
        userEmail = userE.getEmail();
        back = findViewById(R.id.fab_back);

        //gets the bundle information
        extras = getIntent().getExtras();

        //button to go bac to the map screen
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoutesTaken.this, Settings.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //Method that will get all the user previous routes taken that will be passed into a recycler view to display to the user
    //---------------------------------------------------------------------------------------------------------------------------------
    private void getRoutesTaken(){
        refToUsersRoutesTakenTableInFireBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                    UserRoutesTakenUpload UserContainerInFirebase = userInfo.getValue(UserRoutesTakenUpload.class);
                    if (UserContainerInFirebase.getUpEmail().equals(userEmail)) {
                        if (Map.userMetImp.equals("metric")){
                            mylist.add("Route" + "\n\n" + UserContainerInFirebase.getUpDestination() + "\n\n" +
                                    "Distance Traveled : " + UserContainerInFirebase.getUpDistanceKM() + "\n\n" +
                                    "Transport : " + UserContainerInFirebase.getUpTransport() + "\n\n" +
                                    "Duration : " + UserContainerInFirebase.getUpDuration());
                        }
                        else{
                            mylist.add("Route" + "\n\n" + UserContainerInFirebase.getUpDestination() + "\n\n" +
                                    "Distance Traveled : " + UserContainerInFirebase.getUpDistanceMI() + "\n\n" +
                                    "Transport : " + UserContainerInFirebase.getUpTransport() + "\n\n" +
                                    "Duration : " + UserContainerInFirebase.getUpDuration());
                        }
                    }
                }

                layoutManager = new LinearLayoutManager(RoutesTaken.this);
                listRoutes.setLayoutManager(layoutManager);

                RouteTakenAdapter = new RouteTakenAdapter(RoutesTaken.this,mylist);
                listRoutes.setAdapter(RouteTakenAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    //---------------------------------------------------------------------------------------------------------------------------------

    //Method that will get information needed when the activity starts
    //---------------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        getRoutesTaken();
    }
    //---------------------------------------------------------------------------------
}
