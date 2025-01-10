package com.example.financer;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private String username;
    private String email;
    private int transactionCount;
    private HashMap<String,Integer> categories;
    public User(){}

    public User(String username, String email, int transactionCount, HashMap<String,Integer> categories){
        this.username = username;
        this.email = email;
        this.transactionCount = transactionCount;
        this.categories = categories;
    }

    public HashMap<String, Integer> getCategories(){
        return this.categories;
    }

    public void setCategories(HashMap<String,Integer> newCategories){
        this.categories = newCategories;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String new_email){
        this.email = new_email;
    }

    public int getTransactionCount() {
        return this.transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

}
