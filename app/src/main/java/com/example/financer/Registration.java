package com.example.financer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class Registration extends AppCompatActivity {

    TextView toLoginView;
    EditText emailEditText,passwordEditText,usernameEditText;
    Button registerButton;
    private FirebaseAuth mAuth;

    DatabaseReference firebase;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent mainIntent = new Intent(this, Sign_in.class);
            startActivity(mainIntent);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //initialize ui objects
        emailEditText = findViewById(R.id.emailRegEditText);
        passwordEditText = findViewById(R.id.passwordRegEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        registerButton = findViewById(R.id.registerButton);
        toLoginView = findViewById(R.id.loginClickView);

        firebase = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Once user registers they can go back to login screen
        toLoginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getBaseContext(), Sign_in.class);
                startActivity(loginIntent);
            }
        });



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email,password, user_name;
                email = emailEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();
                user_name = usernameEditText.getText().toString().trim();

                //error checking
                if(email.isEmpty()){
                    Toast.makeText(Registration.this, "Enter an email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.isEmpty()){
                    Toast.makeText(Registration.this, "Enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(user_name.isEmpty()){
                    Toast.makeText(Registration.this, "Enter a username", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    // Get the FirebaseUser object
                                    FirebaseUser user = task.getResult().getUser();
                                    // Retrieve the unique UID
                                    String userId = user.getUid();
                                    //add the user to the database
                                    DatabaseReference users_reference = FirebaseDatabase.getInstance().getReference().child(userId);

                                    //hashmap will keep track of the user's categories and their counts
                                    //will have more categories if user uploads transactions file
                                    HashMap<String, Integer> categories = new HashMap<>();
                                    categories.put("Food", 0);
                                    categories.put("Transportation", 0);
                                    categories.put("Subscriptions", 0);
                                    categories.put("Shopping", 0);
                                    //create user data object
                                    User new_user = new User(user_name,email,0,categories);
                                    users_reference.child("profile").setValue(new_user);

                                    //save user's username with Firebase authentication
                                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(user_name).build();

                                    user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(Registration.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                                //send new user to login, done in here to ensure username is saved before proceeding
                                                Intent intent = new Intent(getApplicationContext(), Sign_in.class);
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                Toast.makeText(Registration.this, "Profile update failed", Toast.LENGTH_SHORT).show();
                                            }
                                            
                                        }
                                    });


                                } else {

                                    Toast.makeText(Registration.this, "Registration failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

    }


}