package com.example.budgetv3.ui.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.budgetv3.data.AppDatabase;
import com.example.budgetv3.data.dao.BudgetDao;
import com.example.budgetv3.data.dao.ExpenseDao;
import com.example.budgetv3.data.entity.Budget;
import com.example.budgetv3.data.entity.Expense;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final BudgetDao budgetDao;
    private final ExpenseDao expenseDao;
    private final LiveData<List<Budget>> allBudgets;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        budgetDao = db.budgetDao();
        expenseDao = db.expenseDao();
        allBudgets = budgetDao.getAllBudgets();
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    public LiveData<List<Expense>> getExpensesForBudget(int budgetId) {
        return expenseDao.getExpensesForBudget(budgetId);
    }

    public void updateBudget(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.update(budget);
        });
    }

    public void updateExpense(Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.update(expense);
        });
    }
}
