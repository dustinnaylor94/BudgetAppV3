package com.example.budgetv3.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.budgetv3.data.BudgetDatabase;
import com.example.budgetv3.data.dao.BudgetDao;
import com.example.budgetv3.data.dao.ExpenseDao;
import com.example.budgetv3.data.entity.Budget;
import com.example.budgetv3.data.entity.Expense;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetRepository {
    private final BudgetDao budgetDao;
    private final ExpenseDao expenseDao;
    private final ExecutorService executorService;

    public BudgetRepository(Application application) {
        BudgetDatabase database = BudgetDatabase.getInstance(application);
        budgetDao = database.budgetDao();
        expenseDao = database.expenseDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    // Budget operations
    public void insertBudget(Budget budget) {
        executorService.execute(() -> budgetDao.insert(budget));
    }

    public void updateBudget(Budget budget) {
        executorService.execute(() -> budgetDao.update(budget));
    }

    public void deleteBudget(Budget budget) {
        executorService.execute(() -> budgetDao.delete(budget));
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return budgetDao.getAllBudgets();
    }

    public LiveData<Budget> getBudgetById(int budgetId) {
        return budgetDao.getBudgetById(budgetId);
    }

    // Expense operations
    public void insertExpense(Expense expense) {
        executorService.execute(() -> {
            expenseDao.insert(expense);
            budgetDao.updateSpentAmount(expense.getBudgetId(), expense.getConvertedAmount());
        });
    }

    public void updateExpense(Expense oldExpense, Expense newExpense) {
        executorService.execute(() -> {
            // Update the spent amount in the budget
            double amountDifference = newExpense.getConvertedAmount() - oldExpense.getConvertedAmount();
            budgetDao.updateSpentAmount(newExpense.getBudgetId(), amountDifference);
            expenseDao.update(newExpense);
        });
    }

    public void deleteExpense(Expense expense) {
        executorService.execute(() -> {
            expenseDao.delete(expense);
            // Subtract the expense amount from the budget's spent amount
            budgetDao.updateSpentAmount(expense.getBudgetId(), -expense.getConvertedAmount());
        });
    }

    public LiveData<List<Expense>> getExpensesForBudget(int budgetId) {
        return expenseDao.getExpensesForBudget(budgetId);
    }

    public LiveData<Expense> getExpenseById(int expenseId) {
        return expenseDao.getExpenseById(expenseId);
    }
}
