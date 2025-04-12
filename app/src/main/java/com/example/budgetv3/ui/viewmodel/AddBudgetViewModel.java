package com.example.budgetv3.ui.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.example.budgetv3.data.AppDatabase;
import com.example.budgetv3.data.dao.BudgetDao;
import com.example.budgetv3.data.entity.Budget;

public class AddBudgetViewModel extends AndroidViewModel {
    private final BudgetDao budgetDao;

    public AddBudgetViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        budgetDao = db.budgetDao();
    }

    public void saveBudget(String name, double amount) {
        // Using USD as default currency for now
        Budget budget = new Budget(name, amount, "USD");
        
        new Thread(() -> budgetDao.insert(budget)).start();
    }
}
