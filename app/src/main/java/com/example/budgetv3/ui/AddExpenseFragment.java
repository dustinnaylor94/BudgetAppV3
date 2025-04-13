package com.example.budgetv3.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.budgetv3.data.entity.Budget;
import com.example.budgetv3.databinding.FragmentAddExpenseBinding;
import com.example.budgetv3.ui.dialog.CurrencyDialog;
import com.example.budgetv3.ui.viewmodel.AddExpenseViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AddExpenseFragment extends Fragment {
    private FragmentAddExpenseBinding binding;
    private AddExpenseViewModel viewModel;
    private List<Budget> budgets;
    private Date selectedDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(AddExpenseViewModel.class);
        setupButtons();
        observeBudgets();
    }

    private void setupButtons() {
        binding.saveButton.setOnClickListener(v -> saveExpense());
        binding.cancelButton.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());
        
        binding.dateInput.setOnClickListener(v -> showDatePicker());
        binding.currencySpinner.setOnClickListener(v -> showCurrencyDialog());
        binding.datePickerButton.setOnClickListener(v -> showDatePicker());
        
        // Set default date to today
        selectedDate = new Date();
        updateDateDisplay();
    }

    private void observeBudgets() {
        viewModel.getBudgets().observe(getViewLifecycleOwner(), budgetList -> {
            this.budgets = budgetList;
            setupBudgetSpinner(budgetList);
        });
    }

    private void setupBudgetSpinner(List<Budget> budgets) {
        ArrayAdapter<Budget> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, budgets);
        binding.budgetSpinner.setAdapter(adapter);
    }

    private void showCurrencyDialog() {
        CurrencyDialog dialog = new CurrencyDialog();
        dialog.setListener(currency -> {
            binding.currencySpinner.setText(currency);
        });
        dialog.show(getParentFragmentManager(), "currency_dialog");
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(selectedDate.getTime())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert UTC to local date
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            selectedDate = calendar.getTime();
            updateDateDisplay();
        });

        datePicker.show(getParentFragmentManager(), "date_picker");
    }

    private void updateDateDisplay() {
        binding.dateInput.setText(dateFormat.format(selectedDate));
    }

    private void saveExpense() {
        String name = binding.expenseNameInput.getText().toString().trim();
        String amountStr = binding.expenseAmountInput.getText().toString().trim();
        String expenseCurrency = binding.currencySpinner.getText().toString().trim();

        if (name.isEmpty()) {
            binding.expenseNameLayout.setError("Please enter an expense name");
            return;
        }

        if (amountStr.isEmpty()) {
            binding.expenseAmountLayout.setError("Please enter an amount");
            return;
        }

        if (expenseCurrency.isEmpty()) {
            binding.currencySpinnerLayout.setError("Please select a currency");
            return;
        }

        String selectedBudgetText = binding.budgetSpinner.getText().toString();
        if (selectedBudgetText.isEmpty()) {
            binding.budgetSpinnerLayout.setError("Please select a budget");
            return;
        }

        // Find the selected budget by matching the toString() representation
        Budget selectedBudget = null;
        for (Budget budget : budgets) {
            if (budget.toString().equals(selectedBudgetText)) {
                selectedBudget = budget;
                break;
            }
        }

        if (selectedBudget == null) {
            binding.budgetSpinnerLayout.setError("Invalid budget selection");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String budgetCurrency = selectedBudget.getCurrency();

            // Create final copies for lambda
            final String finalName = name;
            final Budget finalBudget = selectedBudget;
            final String finalExpenseCurrency = expenseCurrency;

            // If currencies are different, convert the amount
            if (!expenseCurrency.equals(budgetCurrency)) {
                // Show loading state
                binding.saveButton.setEnabled(false);
                binding.progressBar.setVisibility(View.VISIBLE);

                // Convert amount to budget's currency
                com.example.budgetv3.api.CurrencyApi.convertCurrency(
                    expenseCurrency,
                    budgetCurrency,
                    amount,
                    new com.example.budgetv3.api.CurrencyApi.ConversionCallback() {
                        @Override
                        public void onSuccess(double convertedAmount) {
                            // Run on UI thread since we're updating UI
                            requireActivity().runOnUiThread(() -> {
                                saveExpenseToDatabase(finalName, convertedAmount, finalBudget, finalExpenseCurrency);
                                binding.saveButton.setEnabled(true);
                                binding.progressBar.setVisibility(View.GONE);
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), 
                                    "Error converting currency: " + errorMessage, 
                                    Toast.LENGTH_LONG).show();
                                binding.saveButton.setEnabled(true);
                                binding.progressBar.setVisibility(View.GONE);
                            });
                        }
                    });
            } else {
                // Same currency, no conversion needed
                saveExpenseToDatabase(name, amount, selectedBudget, expenseCurrency);
            }
        } catch (NumberFormatException e) {
            binding.expenseAmountLayout.setError("Please enter a valid amount");
        }
    }

    private void saveExpenseToDatabase(String name, double amount, Budget budget, String originalCurrency) {
        viewModel.saveExpense(name, amount, budget.getId(), selectedDate, originalCurrency);
        Toast.makeText(requireContext(), 
            String.format("Expense saved (%.2f %s)", amount, budget.getCurrency()), 
            Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
