package com.thenav.thenav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SignUp extends AppCompatActivity {
    //edit text for email an password
    EditText currentUserEmail, currentUserPassword,userFirstName, userLastName;

    //Declaring the sign up and update button
    Button signUp;

    //declaring strings that will be used to hold information
    String currentUserEmailToString, currentUserPasswordToString, currentUserFirstNameToString, currentUserLastNameToString,
            currentUserChoiceMetOrImp, currentUserChoiceOfTransport, userContainerKey, editprofile, userEmail;

    //used to save the user to firebase
    private FirebaseAuth mAuth;

    //declaring a progress bar
    ProgressBar signUpProgress;

    //getting a refrence to the table users activity in firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refToUsersTableInFireBase = database.getReference("UsersInfo");

    //text input layout to float lables and check for errors
    TextInputLayout floatingEmailSignUpLabel, floatingPasswordSignUpLabel, floatingFirstNameLabel, floatingLastNameLabel;

    //switch to allow a user to choose the unit measurements display
    Switch userChoiceOfMeasurements;

    //Declaring the dropdown
    Spinner transport;

    //Declaring the sign up toolbar
    Toolbar signUpToolBar;

    //Declaring the float button to go back
    private FloatingActionButton back;

    TextView info;

    //Bundle to check if the user came from the settings page
    Bundle extras;

    //Boolean values to see if any errors were made by a user
    //----------------------------------
    Boolean emailError = true;
    Boolean passwordError = true;
    Boolean firstNameError = true;
    Boolean lastNameError = true;
    //----------------------------------

    //On create method to set buttons an text edit views to be used in this class  and checks if the one of the buttons is click
    //and takes the user to the appropriate activity also saves the user to firebase with dummy data
    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);
        signUpToolBar = findViewById(R.id.tbSignUp);
        currentUserEmail = findViewById(R.id.txbEmail);
        currentUserPassword = findViewById(R.id.txbPassword);
        userFirstName = findViewById(R.id.txbFirstName);
        userLastName = findViewById(R.id.txbLastName);
        transport = findViewById(R.id.cmbTransportOptions);
        signUp = findViewById(R.id.btnSignUp);
        signUpProgress = findViewById(R.id.pgbSignUpProgress);
        back = findViewById(R.id.fab_back);
        info = findViewById(R.id.txvinfo);
        signUpProgress.setVisibility(View.INVISIBLE);
        userChoiceOfMeasurements = findViewById(R.id.swhMetricOrImperial);

        LoadComboBoxes();

        setupFloatingLabelError();

        extras = getIntent().getExtras();

        if (extras.getString("Profile").equals("Edit Profile")){
            editprofile = extras.getString("Profile");
            signUpToolBar.setPadding(305,0,0,0);
            info.setText("Edit Information");
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser userE = mAuth.getCurrentUser();
            userEmail = userE.getEmail();
            LoadUserInfo();
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (extras.getString("Profile").equals("Edit Profile")){
                    startActivity(new Intent(SignUp.this,Settings.class));
                    finish();
                }
                else{
                    startActivity(new Intent(SignUp.this,LogIn.class));
                    finish();
                }
            }
        });

        userChoiceOfMeasurements.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                CheckUserMeasurementsChoice();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignUpUser();
            }
        });
    }
    //-------------------------------------------------------------------------------------------------------------------------

    //Method load the different choices of transportation
    //----------------------------------------------------------------------------------------------------
    private void LoadComboBoxes() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("--- Select Default Transport ---");
        arrayList.add("Car");
        arrayList.add("Walking");
        arrayList.add("Bicycle");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        transport.setAdapter(arrayAdapter);
    }
    //----------------------------------------------------------------------------------------------------

    //Method to change the user choice of system
    //----------------------------------------------------------------------------------------------------
    private void CheckUserMeasurementsChoice(){
        if (userChoiceOfMeasurements.isChecked()){
            userChoiceOfMeasurements.setText("Imperial System (Miles - Mi)");
            currentUserChoiceMetOrImp = userChoiceOfMeasurements.getText().toString();
        } else{
            userChoiceOfMeasurements.setText("Metric System (Kilometers - Km)");
            currentUserChoiceMetOrImp = userChoiceOfMeasurements.getText().toString();
        }
    }
    //----------------------------------------------------------------------------------------------------

    //Method to enable labels on text views to float above the user text and have error checking
    //the error checking calls strings to be displayed if there is an error, I will make the error checking
    // a little bit more strict in the final POE of this app
    //----------------------------------------------------------------------------------------------------
    private void setupFloatingLabelError() {
        floatingEmailSignUpLabel = (TextInputLayout) findViewById(R.id.text_email_signup_input_layout);
        floatingEmailSignUpLabel.getEditText().addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                if (!text.toString().contains("@")) {
                    floatingEmailSignUpLabel.setError(getString(R.string.signup_email_required));
                    floatingEmailSignUpLabel.setErrorEnabled(true);
                    emailError = true;
                } else {
                    floatingEmailSignUpLabel.setErrorEnabled(false);
                    emailError = false;
                }

                if (text.length() == 0) {
                    floatingEmailSignUpLabel.setErrorEnabled(false);
                    emailError = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        floatingPasswordSignUpLabel = (TextInputLayout) findViewById(R.id.text_password_signup_input_layout);
        floatingPasswordSignUpLabel.getEditText().addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                if (text.length() > 0 && text.length() <= 7) {
                    floatingPasswordSignUpLabel.setError(getString(R.string.signup_password_required));
                    floatingPasswordSignUpLabel.setErrorEnabled(true);
                    passwordError = true;
                } else {
                    floatingPasswordSignUpLabel.setErrorEnabled(false);
                    passwordError = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        floatingFirstNameLabel = (TextInputLayout) findViewById(R.id.text_firstname_input_layout);
        floatingFirstNameLabel.getEditText().addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                if (text.toString().trim().equals("")) {
                    floatingFirstNameLabel.setError(getString(R.string.firstname_required));
                    floatingFirstNameLabel.setErrorEnabled(true);
                    firstNameError = true;
                } else {
                    floatingFirstNameLabel.setErrorEnabled(false);
                    firstNameError = false;
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        floatingLastNameLabel = (TextInputLayout) findViewById(R.id.text_lastname_input_layout);
        floatingLastNameLabel.getEditText().addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                if (text.toString().trim().equals("")) {
                    floatingLastNameLabel.setError(getString(R.string.lastname_required));
                    floatingLastNameLabel.setErrorEnabled(true);
                    lastNameError = true;
                } else {
                    floatingLastNameLabel.setErrorEnabled(false);
                    lastNameError = false;
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    //----------------------------------------------------------------------------------------------------


    //Method to save the user Information to firebase
    //----------------------------------------------------------------------------------------------------
    private void SignUpUser(){
        signUpProgress.setVisibility(View.VISIBLE);
        mAuth = FirebaseAuth.getInstance();
        currentUserEmailToString = currentUserEmail.getText().toString();
        currentUserPasswordToString = currentUserPassword.getText().toString();
        currentUserFirstNameToString = userFirstName.getText().toString();
        currentUserLastNameToString = userLastName.getText().toString();
        currentUserChoiceOfTransport = transport.getSelectedItem().toString();

        if (emailError == false && passwordError == false && firstNameError == false && lastNameError == false) {
            if (editprofile == null){
                mAuth.createUserWithEmailAndPassword(currentUserEmailToString, currentUserPasswordToString).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            userContainerKey = refToUsersTableInFireBase.child("UsersInfo").push().getKey();
                            UserInfoUpload userInfoUpload = new UserInfoUpload(currentUserEmailToString, userContainerKey, currentUserFirstNameToString, currentUserLastNameToString, currentUserChoiceMetOrImp, currentUserChoiceOfTransport);
                            refToUsersTableInFireBase.child(userContainerKey).setValue(userInfoUpload);
                            Intent intent = new Intent(SignUp.this, Map.class);
                            startActivity(intent);
                            SignUp.this.finish();
                        }
                        else {
                            Toast.makeText(SignUp.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                            signUpProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            } else {
                UserInfoUpload userInfoUpload = new UserInfoUpload(currentUserEmailToString, userContainerKey, currentUserFirstNameToString, currentUserLastNameToString, currentUserChoiceMetOrImp, currentUserChoiceOfTransport);
                refToUsersTableInFireBase.child(userContainerKey).setValue(userInfoUpload);
                Intent intent = new Intent(SignUp.this, Map.class);
                startActivity(intent);
                SignUp.this.finish();
            }
        }
        else {
            Toast.makeText(SignUp.this, "There are still errors on the page", Toast.LENGTH_LONG).show();
            signUpProgress.setVisibility(View.INVISIBLE);
        }
    }
    //----------------------------------------------------------------------------------------------------

    //Method to load the user Information if they came from the setting page to edit their profile
    //----------------------------------------------------------------------------------------------------
    private void LoadUserInfo(){
        signUpToolBar.setTitle("Edit Profile");
        signUp.setText("Update");

        refToUsersTableInFireBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userInfo : dataSnapshot.getChildren()) {

                    UserInfoUpload UserContainerInFirebase = userInfo.getValue(UserInfoUpload.class);

                    if (UserContainerInFirebase.getUpEmail().equals(userEmail)) {
                        userContainerKey = UserContainerInFirebase.getUpContainer();
                        currentUserEmail.setText(UserContainerInFirebase.getUpEmail());
                        currentUserPassword.setText("****************");
                        userFirstName.setText(UserContainerInFirebase.getUpFirstname());
                        userLastName.setText(UserContainerInFirebase.getUpLastname());
                        currentUserEmail.setEnabled(false);
                        currentUserPassword.setEnabled(false);

                        currentUserChoiceMetOrImp = UserContainerInFirebase.getUpMetOrImp();

                        if (currentUserChoiceMetOrImp.equals("Metric System (Kilometers - Km)")){
                            userChoiceOfMeasurements.setChecked(false);
                        }
                        else {
                            userChoiceOfMeasurements.setChecked(true);
                        }

                        if(UserContainerInFirebase.getUpTransport().equals("Car")){
                            transport.setSelection(1);
                        } else if(UserContainerInFirebase.getUpTransport().equals("Walking")){
                            transport.setSelection(2);
                        } else if(UserContainerInFirebase.getUpTransport().equals("Bicycle")){
                            transport.setSelection(3);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    //----------------------------------------------------------------------------------------------------
}


