package com.example.financer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class Manual_entry_activity extends AppCompatActivity {
    Button date_button, submit_button;
    EditText descriptionEditText, amountEditText;
    Spinner categorySpinner;
    ArrayAdapter<String> adapter;
    DatePickerDialog datePickerDialog;
    DatabaseReference firebase;
    FirebaseAuth mAuth;
    String userId;
    int transaction_amount = 0;
    User user;

    DatabaseReference userAccount_reference;
    HashMap<String, Integer> categories;
    ArrayList<String> categoryList = new ArrayList<>();
    String[] months = {"JAN", "FEB","MAR","APR", "MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        //grab ui elements
        descriptionEditText = findViewById(R.id.description_EditText);
        amountEditText = findViewById(R.id.amount_EditText);
        date_button = findViewById(R.id.date_Button);
        submit_button = findViewById(R.id.submit_button);
        categorySpinner = findViewById(R.id.category_spinner);

        //get reference to firebase
        firebase = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = mAuth.getCurrentUser();
        if(fbUser != null){
            userId = mAuth.getCurrentUser().getUid();
        }



        //grab the amount of transactions a user has, to number each new transaction
        userAccount_reference = firebase.child(userId).child("profile");


        userAccount_reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(Manual_entry_activity.this, "failed to get user", Toast.LENGTH_SHORT).show();
                    return;
                }
                user = task.getResult().getValue(User.class);
                transaction_amount = user.getTransactionCount();
                //grab users hashmap that keeps track of every category count
                categories = user.getCategories();
                if(user != null){
                    for(String category: categories.keySet()){
                        categoryList.add(category);
                        Log.w("user", category);
                    }
                    Log.w("user", "THESE ARE THE CATEGORIES " + categoryList.toString());

                    //THIS HAS TO BE DONE HERE BECAUSE RETRIEVING REFERENCE IN FIREBASE IS DONE BY A DIFFERENT THREAD.
                    //SO INITIALIZING SPINNER OUTSIDE OF THIS FUNCTION CAN LEAD TO POPULATING THE SPINNER WITH AN EMPTY ARRAYLIST IF THIS FUNCTION HASN'T COMPLETED YET
                    //initialize spinner
                    adapter = new ArrayAdapter<>(Manual_entry_activity.this, android.R.layout.simple_spinner_item, categoryList);
                    adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
                    categorySpinner.setAdapter(adapter);

                }
            }
        });


        //Initiate Date picker
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        //By default, set date picker to Today's date
        String date_string = createDateString(year,month,day);
        date_button.setText(date_string);
        initiateDatePicker();

        //store purchase entry
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //wait for firebase thread to finish retrieving data
                if(categoryList.isEmpty()){
                    Toast.makeText(Manual_entry_activity.this, "One moment, retrieving data.....", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                //grab input
                String description = descriptionEditText.getText().toString().trim();
                String amount =  amountEditText.getText().toString().trim();
                //Date will always default to today's date, simply parse
                String date = date_button.getText().toString();
                String category = categorySpinner.getSelectedItem().toString();
                //error check
                if(description.isEmpty()){
                    Toast.makeText(Manual_entry_activity.this, "Description is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(amount.isEmpty()){
                    Toast.makeText(Manual_entry_activity.this, "Amount is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(category.isEmpty()){
                    Toast.makeText(Manual_entry_activity.this, "Pick a category", Toast.LENGTH_SHORT).show();
                    return;
                }
                //convert cost to Double
                Double amountValue = Double.parseDouble(amount);

                //CREATE TRANSACTION AND UPDATE THE USER'S OBJECT MEMBERS
                //increment transaction count
                transaction_amount = transaction_amount + 1;
                user.setTransactionCount(transaction_amount);
                //add transaction to firebase
                Transaction currentEntry = new Transaction(description,date, category, amountValue);
                DatabaseReference userTransactions = firebase.child(userId).child("transactions");
                //a user's transactions node will have a child for each transaction
                userTransactions.child("Transaction " + transaction_amount).setValue(currentEntry);

                //update user's members
                int categoryCount = categories.get(category);
                categoryCount = categoryCount + 1;

                categories.replace(category, categoryCount);
                //update the user object in firebase, with new transaction count and category hashmap
                userAccount_reference.setValue(user);

                //reset edit texts to indicate ready for new entry
                descriptionEditText.getText().clear();
                amountEditText.getText().clear();
            }
        });


    }

    public String createDateString(int year, int month, int day){
        return months[month] + " " + day + " " + year;
    }


    public void initiateDatePicker(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = createDateString(year, month, dayOfMonth);
                date_button.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int style = AlertDialog.THEME_HOLO_DARK;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }


    public void openDatePicker(View view){
        datePickerDialog.show();
    }

}