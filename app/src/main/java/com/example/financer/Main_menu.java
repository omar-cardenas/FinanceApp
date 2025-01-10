package com.example.financer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Main_menu extends AppCompatActivity {

    TextView username_TextView, entry_textView, analytics_textView, importTransactions_textView, scanReceipt_TextView;
    Button sign_out_button;

    DatabaseReference firebase;
    DatabaseReference userReference;
    String username = "";
    String userId;

    FirebaseUser authenticated_user = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //grab ui elements
        username_TextView = findViewById(R.id.username_TextView);
        entry_textView = findViewById(R.id.entry_TextView);
        sign_out_button = findViewById(R.id.sign_out_Button);
        analytics_textView = findViewById(R.id.analytics_TextView);
        importTransactions_textView = findViewById(R.id.import_transactions_TextView);
        scanReceipt_TextView = findViewById(R.id.scanReceipt_TextView);

        //grab user to display username
        authenticated_user = FirebaseAuth.getInstance().getCurrentUser();

        firebase = FirebaseDatabase.getInstance().getReference();

        if(authenticated_user != null) {
            username = authenticated_user.getDisplayName();
            String displayUser = "Welcome back\n" + username;
            username_TextView.setText(displayUser);
            userId = authenticated_user.getUid();
        }

        Log.w("user's data" , authenticated_user.getDisplayName() + " , " + authenticated_user.getEmail() + " , " + authenticated_user.getUid());

        //get reference to user's node
        userReference = firebase.child(userId).child("profile");


        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent log_off_intent = new Intent(getBaseContext(), Sign_in.class);
                startActivity(log_off_intent);
                finish();

            }
        });

        //manual entry for purchases
        entry_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent entry_intent = new Intent(getBaseContext(), Manual_entry_activity.class);
                startActivity(entry_intent);
            }
        });

        //show analytics for the user's purchases
        analytics_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent analytics_intent = new Intent(getBaseContext(), Analytics.class);
                startActivity(analytics_intent);

            }
        });


        //upload csv file
        importTransactions_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent importIntent = new Intent(getBaseContext(), UploadTransactions.class);
                startActivity(importIntent);
            }
        });

        //allows user to scan receipts
        scanReceipt_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Main_menu.this, CaptureImage.class);
                startActivity(intent);
                
            }
        });


    }
}