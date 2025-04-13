package com.example.budgetv3.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private double amount;
    private String currency;
    private double spent;
    private String spentCurrency; // Currency of the spent amount

    public Budget(String name, double amount, String currency) {
        this.name = name;
        this.amount = amount;
        this.currency = currency;
        this.spent = 0.0;
        this.spentCurrency = currency;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public String getSpentCurrency() {
        return spentCurrency;
    }

    public void setSpentCurrency(String spentCurrency) {
        this.spentCurrency = spentCurrency;
    }

    @Override
    public String toString() {
        return name + " ($" + amount + " USD)";
    }
}
