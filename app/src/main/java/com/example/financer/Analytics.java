package com.example.financer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analytics extends AppCompatActivity {
    TextView totalTextView, topCategoriesTextView, viewTransactionsTextView;
    FirebaseAuth myAuth;
    DatabaseReference firebaseReference;
    User user;
    String userId;
    String username;

    PieChart pieChart;

    int totalTransactions;
    ArrayList<Transaction> transactions = new ArrayList<>();


    public void create_pieChart(){

        HashMap<String, Integer> usedCategories = new HashMap<>();

        totalTransactions = user.getTransactionCount();
        Log.w("user", Integer.toString(totalTransactions));
        if(totalTransactions == 0){
            Toast.makeText(this, "No transactions in Firebase", Toast.LENGTH_SHORT).show();
            return;
        }

        //only want to show the categories with counts > 0
        HashMap<String, Integer> categories = user.getCategories();
        for(String key: categories.keySet()){
            int categoryCount = categories.get(key);
            if(categoryCount > 0){
                usedCategories.put(key,categoryCount );
            }
        }


        // Initialize the PieChart
        pieChart = findViewById(R.id.pie_chart);
        // Sample data for the PieChart
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        for(String key: usedCategories.keySet()){
            int categoryCount = usedCategories.get(key);
            float pieValue = ((float)categoryCount/totalTransactions) * 1000;

            Log.w("user", key + " count = " + Integer.toString(categoryCount));
            Log.w("user", key + ": " + Float.toString(pieValue));

            pieEntries.add(new PieEntry(pieValue,key));
        }


        // Create the PieDataSet
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Categories");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Use predefined colors

        // Create PieData
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(12f); // Set text size

        // Set data to the PieChart
        pieChart.setData(pieData);
        pieChart.setCenterText("Category Percentages (1000%)"); // Set center text
        pieChart.setHoleRadius(40f); // Adjust hole size
        pieChart.animateY(1000); // Add animation
        pieChart.invalidate(); // Refresh the chart


    }
    //format floats to only show 2 digits after decimal
    public static String roundToTwoDigit(double money){
        return String.format("%.2f", money);
    }

    public void updateTotal(){
        Double total = 0d;
        for(Transaction t: transactions){
            total += t.getAmount();
        }

        totalTextView.setText("Total spending: $" + roundToTwoDigit(total));
    }

    public void updateDataView(){

        String displayString = "Top Categories:\n";

        // Sort entries by value in descending order
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(user.getCategories().entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Get the top 3 entries, if possible
        for (int i = 0; i < Math.min(3, sortedEntries.size()); i++) {
            Map.Entry<String, Integer> entry = sortedEntries.get(i);
            int count = entry.getValue();
            if(count > 0) {
                if(count == 1) {
                    displayString += entry.getKey() + ": " + entry.getValue() + " match\n";
                }else{
                    displayString += entry.getKey() + ": " + entry.getValue() + " matches\n";
                }
            }
        }

        topCategoriesTextView.setText(displayString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        totalTextView = findViewById(R.id.total_textView);
        topCategoriesTextView = findViewById(R.id.topCategories_textView);
        viewTransactionsTextView = findViewById(R.id.viewTransactions_TextView);

        myAuth = FirebaseAuth.getInstance();
        username = myAuth.getCurrentUser().getDisplayName();
        userId = myAuth.getCurrentUser().getUid();

        firebaseReference = FirebaseDatabase.getInstance().getReference();

        DatabaseReference transactionReference = firebaseReference.child(userId).child("transactions");
        transactionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data: snapshot.getChildren()){
                    Transaction t = data.getValue(Transaction.class);
                    transactions.add(t);
                }

                updateTotal();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference userAccount_reference = firebaseReference.child(userId).child("profile");

        userAccount_reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    user = task.getResult().getValue(User.class);
                    //Toast.makeText(Analytics.this, "success", Toast.LENGTH_SHORT).show();
                    //done here to avoid race conditions
                    create_pieChart();
                    updateDataView();
                }else{
                    Toast.makeText(Analytics.this, "There was an error retrieving User object", Toast.LENGTH_SHORT).show();
                }
            }
        });


        viewTransactionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent transactionsIntent = new Intent(Analytics.this, ViewTransactions.class);
                startActivity(transactionsIntent);
            }
        });




    }
}