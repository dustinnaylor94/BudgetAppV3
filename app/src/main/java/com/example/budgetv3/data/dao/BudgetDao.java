package com.example.budgetv3.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.budgetv3.data.entity.Budget;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets ORDER BY name ASC")
    LiveData<List<Budget>> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    LiveData<Budget> getBudgetById(int budgetId);

    @Query("UPDATE budgets SET spent = spent + :amount WHERE id = :budgetId")
    void updateSpentAmount(int budgetId, double amount);
}
