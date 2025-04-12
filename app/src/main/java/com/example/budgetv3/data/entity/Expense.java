package com.example.budgetv3.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "expenses",
        foreignKeys = @ForeignKey(
            entity = Budget.class,
            parentColumns = "id",
            childColumns = "budgetId",
            onDelete = ForeignKey.CASCADE
        ))
public class Expense {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int budgetId;
    private String name;
    private double amount;
    private String originalCurrency;
    private double convertedAmount;
    private Date date;

    public Expense(int budgetId, String name, double amount, String originalCurrency, double convertedAmount, Date date) {
        this.budgetId = budgetId;
        this.name = name;
        this.amount = amount;
        this.originalCurrency = originalCurrency;
        this.convertedAmount = convertedAmount;
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
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

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public void setOriginalCurrency(String originalCurrency) {
        this.originalCurrency = originalCurrency;
    }

    public double getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(double convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
