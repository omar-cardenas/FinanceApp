package com.example.financer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public  class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.myViewHolder> {
    Context context;
    ArrayList<Transaction> list;
    private final RecyclerViewInterface recyclerViewInterface;
    public MyRecyclerViewAdapter(Context context, ArrayList<Transaction> list, RecyclerViewInterface recyclerViewInterface){
        this.context = context;
        this.list = list;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public MyRecyclerViewAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //this method inflates the layout and gives each row a look
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.transaction_cardview,parent, false);
        return new MyRecyclerViewAdapter.myViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewAdapter.myViewHolder holder, int position) {
        //assigns values to each view (row), based on position of recycler view
        String name =  list.get(position).getDescription();
        holder.descriptionTextView.setText(name);
        String price = "$" + Double.toString(list.get(position).getAmount());
        String date = list.get(position).getDate();
        holder.priceTextView.setText(price);
        holder.dateTextView.setText(date);
        String category = list.get(position).getCategory();
        holder.categoryTextView.setText(category);



    }

    @Override
    public int getItemCount() {
        //the number of items to display
        return list.size();
    }
    //this class works like a onCreate() method
    public static class myViewHolder extends RecyclerView.ViewHolder{
        //grabs the views of equipment_view layout file
        TextView descriptionTextView, priceTextView, dateTextView, categoryTextView;


        public myViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(position);
                        }
                    }


                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(recyclerViewInterface != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onLongItemClick(position);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }
}