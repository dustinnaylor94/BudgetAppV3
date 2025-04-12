package com.example.budgetv3.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.budgetv3.data.entity.Expense;
import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    long insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses WHERE budgetId = :budgetId ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesForBudget(int budgetId);

    @Query("SELECT * FROM expenses WHERE budgetId = :budgetId ORDER BY date DESC")
    List<Expense> getExpensesForBudgetSync(int budgetId);

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    LiveData<Expense> getExpenseById(int expenseId);

    @Query("DELETE FROM expenses WHERE budgetId = :budgetId")
    void deleteExpensesForBudget(int budgetId);
}
