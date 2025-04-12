package com.example.budgetv3.ui.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.budgetv3.data.AppDatabase;
import com.example.budgetv3.data.dao.BudgetDao;
import com.example.budgetv3.data.entity.Budget;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final BudgetDao budgetDao;
    private final LiveData<List<Budget>> budgets;

    public HomeViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        budgetDao = db.budgetDao();
        budgets = budgetDao.getAllBudgets();
    }

    public LiveData<List<Budget>> getBudgets() {
        return budgets;
    }

    public void deleteBudget(Budget budget) {
        new Thread(() -> budgetDao.delete(budget)).start();
    }
}
