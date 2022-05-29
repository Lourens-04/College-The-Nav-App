package com.thenav.thenav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn extends AppCompatActivity {
    //used to check if the user exist
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    //edit text for user email and password
    EditText email, password;
    //button for signed up and log in
    Button logIn;
    //strings for current user email and current user password
    String emailE, passwordE;
    //declaring a progress bar
    ProgressBar logInProgress;
    //text input layout to float lables and check for errors
    TextInputLayout floatingEmailLabel, floatingPasswordLabel;
    TextView signUp;

    //On create method to set buttons an text edit views to be used in this class  and checks if the one of the buttons is click
    //and takes the user to the appropriate activity also check if the user signing in exist in firebase
    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser != null){
            Toast.makeText(LogIn.this, "Log in successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LogIn.this, Map.class);
            startActivity(intent);
            finish();
        }

        email = findViewById(R.id.txbEmail);
        password = findViewById(R.id.txbPassword);
        signUp = findViewById(R.id.txvSignUp);
        logIn = findViewById(R.id.btnLogIn);
        logInProgress = findViewById(R.id.prgbLogInProgress);
        logInProgress.setVisibility(View.INVISIBLE);

        setupFloatingLabelError();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                intent.putExtra("Profile", "CreateProfile");
                startActivity(intent);
                finish();
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                logInProgress.setVisibility(View.VISIBLE);

                emailE = email.getText().toString();
                passwordE = password.getText().toString();

                if (!emailE.equals("") && !passwordE.equals("")){
                    mAuth.signInWithEmailAndPassword(emailE, passwordE ).addOnCompleteListener(LogIn.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(LogIn.this, "Log in successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LogIn.this, Map.class);
                                logInProgress.setVisibility(View.INVISIBLE);
                                startActivity(intent);
                                LogIn.this.finish();
                            }
                            else
                            {
                                Toast.makeText(LogIn.this, "Failed to log in", Toast.LENGTH_LONG).show();
                                logInProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
                else{
                    logInProgress.setVisibility(View.INVISIBLE);
                    Toast.makeText(LogIn.this, "There are fields that are left empty", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    //-------------------------------------------------------------------------------------------------------------------------

    //Method to enable labels on text views to float above the user text and have error checking
    //the error checking calls strings to be displayed if there is an error, I will make the error checking
    // a little bit more strict in the final POE of this app
    //----------------------------------------------------------------------------------------------------
    private void setupFloatingLabelError() {

        floatingEmailLabel = (TextInputLayout) findViewById(R.id.text_email_signin_input_layout);
        floatingEmailLabel.getEditText().addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                if (!text.toString().contains("@")) {
                    floatingEmailLabel.setError(getString(R.string.login_email_required));
                    floatingEmailLabel.setErrorEnabled(true);
                } else {
                    floatingEmailLabel.setErrorEnabled(false);
                }
                if (text.length() == 0){
                    floatingEmailLabel.setErrorEnabled(false);
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

        floatingPasswordLabel = (TextInputLayout) findViewById(R.id.text_password_signin_input_layout);
        floatingPasswordLabel.getEditText().addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                if (text.length() > 0 && text.length() <= 7) {
                    floatingPasswordLabel.setError(getString(R.string.login_password_required));
                    floatingPasswordLabel.setErrorEnabled(true);
                } else {
                    floatingPasswordLabel.setErrorEnabled(false);
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
}
