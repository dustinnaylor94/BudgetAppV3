package com.example.budgetv3;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;
import com.example.budgetv3.R;
import com.example.budgetv3.api.CurrencyApi;
import com.example.budgetv3.data.AppDatabase;
import com.example.budgetv3.data.entity.Budget;
import com.example.budgetv3.data.entity.Expense;
import com.example.budgetv3.databinding.ActivityMainBinding;
import com.example.budgetv3.ui.dialog.CurrencyDialog;
import com.example.budgetv3.ui.viewmodel.MainViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private String currentCurrency = "USD"; // Default currency
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        menu.add(Menu.NONE, R.id.action_clear_data, Menu.NONE, "Clear All Data");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (item.getItemId() == android.R.id.home) {
            return navController.navigateUp() || super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == R.id.action_set_currency) {
            showCurrencyDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_clear_data) {
            clearAllData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private NavController navController;
    private BottomNavigationView navigationView;
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    private void clearAllData() {
        new AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("Are you sure you want to delete all budgets and expenses? This cannot be undone.")
            .setPositiveButton("Clear", (dialog, which) -> {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    db.clearAllTables();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();
                        currentCurrency = "USD"; // Reset to default currency
                    });
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showCurrencyDialog() {
        CurrencyDialog dialog = new CurrencyDialog();
        dialog.setListener(selectedCurrency -> {
            try {
                // Extract just the currency code (e.g., "USD" from "USD - United States Dollar")
                String toCurrency = selectedCurrency.split(" - ")[0].trim();
                Log.d("MainActivity", "Converting from " + currentCurrency + " to " + toCurrency);
                convertAllAmounts(currentCurrency, toCurrency);
            } catch (Exception e) {
                Log.e("MainActivity", "Error processing currency selection: " + e.getMessage());
                Toast.makeText(this, "Error processing currency selection", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "currency_dialog");
    }

    private void convertAllAmounts(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            Toast.makeText(this, "Already using " + toCurrency, Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Converting currencies...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get budgets directly from database
            List<Budget> budgets = AppDatabase.getDatabase(getApplicationContext())
                    .budgetDao()
                    .getAllBudgetsSync();

            Log.d("MainActivity", "Converting " + budgets.size() + " budgets from " + fromCurrency + " to " + toCurrency);
            for (Budget budget : budgets) {
                Log.d("MainActivity", "Budget before conversion: " + budget.getAmount() + " " + budget.getCurrency());
            }

            AtomicInteger pendingConversions = new AtomicInteger(budgets.size());
            if (budgets.isEmpty()) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    currentCurrency = toCurrency;
                    Toast.makeText(this, "Currency updated to " + toCurrency, Toast.LENGTH_SHORT).show();
                });
                return;
            }

            for (Budget budget : budgets) {
                double amount = budget.getAmount();
                CurrencyApi.convertCurrency(fromCurrency, toCurrency, amount,
                    new CurrencyApi.ConversionCallback() {
                        @Override
                        public void onSuccess(double convertedAmount) {
                            Log.d("MainActivity", "Converting budget: " + budget.getAmount() + " " + budget.getCurrency() + " to " + convertedAmount + " " + toCurrency);
                            
                            // Create final copies for the lambda
                            final double finalConvertedAmount = convertedAmount;
                            final Budget finalBudget = budget;
                            
                            // Convert spent amount
                            CurrencyApi.convertCurrency(budget.getSpentCurrency(), toCurrency, budget.getSpent(),
                                new CurrencyApi.ConversionCallback() {
                                    @Override
                                    public void onSuccess(double convertedSpentAmount) {
                                        AppDatabase.databaseWriteExecutor.execute(() -> {
                                            finalBudget.setAmount(finalConvertedAmount);
                                            finalBudget.setCurrency(toCurrency);
                                            finalBudget.setSpent(convertedSpentAmount);
                                            finalBudget.setSpentCurrency(toCurrency);
                                            AppDatabase.getDatabase(getApplicationContext())
                                                .budgetDao()
                                                .update(finalBudget);
                                            Log.d("MainActivity", "Updated budget in database: " + 
                                                finalBudget.getAmount() + " " + finalBudget.getCurrency() + 
                                                " (spent: " + finalBudget.getSpent() + " " + finalBudget.getSpentCurrency() + ")");
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Log.e("MainActivity", "Error converting spent amount: " + errorMessage);
                                    }
                                });

                            // Convert expenses for this budget
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                List<Expense> expenses = AppDatabase.getDatabase(getApplicationContext())
                                    .expenseDao()
                                    .getExpensesForBudgetSync(budget.getId());
                                
                                Log.d("MainActivity", "Converting " + expenses.size() + " expenses for budget " + budget.getId());
                                for (Expense expense : expenses) {
                                    Log.d("MainActivity", "Expense before conversion: " + expense.getAmount() + " " + expense.getOriginalCurrency());
                                    double expenseAmount = expense.getAmount();
                                    CurrencyApi.convertCurrency(fromCurrency, toCurrency, expenseAmount,
                                        new CurrencyApi.ConversionCallback() {
                                            @Override
                                            public void onSuccess(double convertedExpenseAmount) {
                                                // Create final copies for the lambda
                                                final double finalExpenseAmount = convertedExpenseAmount;
                                                final Expense finalExpense = expense;
                                                
                                                AppDatabase.databaseWriteExecutor.execute(() -> {
                                                    finalExpense.setAmount(finalExpenseAmount);
                                                    finalExpense.setOriginalCurrency(toCurrency);
                                                    AppDatabase.getDatabase(getApplicationContext())
                                                        .expenseDao()
                                                        .update(finalExpense);
                                                    Log.d("MainActivity", "Updated expense in database: " + finalExpense.getAmount() + " " + finalExpense.getOriginalCurrency());
                                                });
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.e("MainActivity", "Error converting expense: " + error);
                                                Toast.makeText(MainActivity.this, "Error converting expense: " + error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                }
                            });

                            if (pendingConversions.decrementAndGet() == 0) {
                                progressDialog.dismiss();
                                currentCurrency = toCurrency;
                                Toast.makeText(MainActivity.this, "Currency conversion complete", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("MainActivity", "Error converting budget: " + error);
                            Toast.makeText(MainActivity.this, "Error converting budget: " + error, Toast.LENGTH_SHORT).show();
                            if (pendingConversions.decrementAndGet() == 0) {
                                progressDialog.dismiss();
                                // Keep the old currency since conversion failed
                                currentCurrency = fromCurrency;
                            }
                        }
                    });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setContentView(binding.getRoot());

        MaterialToolbar toolbar = binding.toolbar.getRoot();
        setSupportActionBar(toolbar);

        // Set up Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            navigationView = findViewById(R.id.nav_view);

            // Set up top-level destinations (no back button)
            appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home)
                    .build();

            NavigationUI.setupWithNavController(navigationView, navController);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }


}