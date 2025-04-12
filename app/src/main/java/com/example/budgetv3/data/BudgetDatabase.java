package com.example.budgetv3.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.budgetv3.data.dao.BudgetDao;
import com.example.budgetv3.data.dao.ExpenseDao;
import com.example.budgetv3.data.entity.Budget;
import com.example.budgetv3.data.entity.Expense;
import com.example.budgetv3.util.DateConverter;

@Database(entities = {Budget.class, Expense.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class BudgetDatabase extends RoomDatabase {
    private static BudgetDatabase instance;

    public abstract BudgetDao budgetDao();
    public abstract ExpenseDao expenseDao();

    public static synchronized BudgetDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    BudgetDatabase.class,
                    "budget_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
