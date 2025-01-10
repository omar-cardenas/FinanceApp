package com.example.financer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UploadTransactions extends AppCompatActivity {

    TextView displayData_textView;
    Button chooseFile_button;
    
    private final int csv_from_device = 1001;
    FirebaseAuth myAuth;
    DatabaseReference firebase;
    DatabaseReference userProfileReference;

    String userId;
    String username;
    String email;
    User user;
    int transaction_count;

    //THE DATES BEING PARSED FROM URI WON'T START WITH 0. LIKE THEY DO WITH THE CALENDAR library
    String[] months = {"SKIP 0", "JAN", "FEB","MAR","APR", "MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
    ArrayList<String> categoryList;

    public void checkCategories(String category){
        //check if category contains a '/', since hashmap keys CANT contain this
        if(category.contains("/")){
            //just use the first word: Gas/Automotive -> Gas
            category =  category.split("/")[0];

        }


        HashMap<String,Integer> usersCategories = user.getCategories();
        if(usersCategories.containsKey(category)){
            int currentCount = (int)usersCategories.get(category);
            currentCount = currentCount + 1;
            usersCategories.replace(category, currentCount);
            user.setCategories(usersCategories);
            return;

        }
        //add to their list of categories (for analytics)
        usersCategories.put(category, 1);
        user.setCategories(usersCategories);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_transactions);
        displayData_textView = findViewById(R.id.file_data_TextView);
        chooseFile_button = findViewById(R.id.file_button);
        categoryList = new ArrayList<>();

        myAuth = FirebaseAuth.getInstance();

        if (myAuth.getCurrentUser() != null) {
            username = myAuth.getCurrentUser().getDisplayName();
            userId = myAuth.getCurrentUser().getUid();
            Log.w("user", username);
            Log.w("user", userId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }


        firebase = FirebaseDatabase.getInstance().getReference();
        userProfileReference = firebase.child(userId).child("profile");


        userProfileReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(UploadTransactions.this, "failed to get user", Toast.LENGTH_SHORT).show();
                    return;
                }
                user = task.getResult().getValue(User.class);
                if(user!= null){
                    transaction_count = user.getTransactionCount();
                    email = user.getEmail();
                    Log.w("user", "the user's email is " + email);
                }
            }
        });

        chooseFile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openFileSelectionInDevice();

            }
        });



    }

    public void openFileSelectionInDevice(){
        Intent filePickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        filePickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerIntent.setType("*/*"); //any

        startActivityForResult(filePickerIntent, csv_from_device);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode,resultCode,resultData);

        if (requestCode == csv_from_device
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                readFileFromUri(uri);

            }
        }

    }

    public void addToFirebase(Transaction t){

        //update user's transaction count
        transaction_count = transaction_count + 1;

        //get reference to user's transactions node
        DatabaseReference transactionsReference = firebase.child(userId).child("transactions");
        transactionsReference.child("Transaction " + transaction_count).setValue(t);


        //userProfileReference.child("transactionCount").setValue(transaction_count);

    }

    private String changeDateFormat(String date){
        String[] tokens = date.split("-");
        String year = tokens[0];
        String month = tokens[1];
        String day = tokens[2];


        month = months[Integer.parseInt(month)];

        return month + "  " + day + " " + year;
    }

    private void readFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            //skip first line, it contains column names
            //0-transaction date, 1-posted date, 2-card number, 3-description, 4-category, 5-debit, 6-credit
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
                //parse line
                String[] tokens = line.split(",");
                if(!(tokens.length >= 6)){
                    continue;
                }
                String date = changeDateFormat(tokens[0]);
                String description = tokens[3];
                Double amount;
                //this could be empty if the transaction is not a purchase
                if(!tokens[5].isEmpty()) {
                    amount = Double.parseDouble(tokens[5]);
                }else{
                    continue;
                }

                String category = tokens[4];
                //check if category is new to user, if so add it to their categories hashmap and update its counts
                checkCategories(category);

                //create transaction and add to firebase
                Transaction t = new Transaction(description,date,category, amount);
                addToFirebase(t);
                //update count
                int count = user.getTransactionCount() + 1;
                user.setTransactionCount(count);

            }

            //update user object in Firebase, to reflect new category counts
            userProfileReference.setValue(user);

            reader.close();
            inputStream.close();

            displayData_textView.setText(stringBuilder.toString());
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
            Log.d("FileContent", stringBuilder.toString());

        } catch (Exception e) {
            Log.d("IO error", e.toString());
            Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
        }
    }

}