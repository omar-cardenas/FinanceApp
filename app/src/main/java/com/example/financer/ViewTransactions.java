package com.example.financer;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewTransactions extends AppCompatActivity implements RecyclerViewInterface {
    ProgressBar progressBar;
    RecyclerView recyclerView;
    DatabaseReference firebase;
    FirebaseAuth fbAuth;
    ArrayList<Transaction> transactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transactions);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        //grab user info to access their data in firebase
        fbAuth = FirebaseAuth.getInstance();
        firebase = FirebaseDatabase.getInstance().getReference();
        String userId = fbAuth.getCurrentUser().getUid();

        DatabaseReference usersTransactions = firebase.child(userId).child("transactions");

        usersTransactions.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data: snapshot.getChildren()){
                    Transaction t = data.getValue(Transaction.class);
                    transactions.add(t);
                }
                MyRecyclerViewAdapter recyclerViewAdapter = new MyRecyclerViewAdapter(ViewTransactions.this, transactions,ViewTransactions.this );
                recyclerView.setAdapter(recyclerViewAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(ViewTransactions.this));

                progressBar.setVisibility(View.INVISIBLE);
                if(transactions.isEmpty()){
                    Toast.makeText(ViewTransactions.this, "No transactions found", Toast.LENGTH_SHORT).show();
                }
            }
            
            

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onLongItemClick(int position) {

    }
}