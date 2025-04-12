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
import java.util.Date;

public class AddExpenseViewModel extends AndroidViewModel {
    private final ExpenseDao expenseDao;
    private final BudgetDao budgetDao;
    private final LiveData<List<Budget>> budgets;

    public AddExpenseViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        expenseDao = db.expenseDao();
        budgetDao = db.budgetDao();
        budgets = budgetDao.getAllBudgets();
    }

    public LiveData<List<Budget>> getBudgets() {
        return budgets;
    }

    public void saveExpense(String name, double amount, int budgetId) {
        // For now, we'll use USD and set converted amount same as original
        Expense expense = new Expense(budgetId, name, amount, "USD", amount, new Date());
        
        new Thread(() -> {
            // Save the expense
            expenseDao.insert(expense);
            
            // Update the budget's spent amount
            budgetDao.updateSpentAmount(budgetId, amount);
        }).start();
    }
}
