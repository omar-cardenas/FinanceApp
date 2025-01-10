package com.example.financer;

public class Transaction {
    String description;
    String date;
    Double amount;
    String category;

    public Transaction(){

    }

    public Transaction(String description, String date, String category, Double amount){
        this.description = description;
        this.date  = date;
        this.category = category;
        this.amount = amount;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }




}
