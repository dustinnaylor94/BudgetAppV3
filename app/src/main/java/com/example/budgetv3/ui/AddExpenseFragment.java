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

        if (name.isEmpty()) {
            binding.expenseNameLayout.setError("Please enter an expense name");
            return;
        }

        if (amountStr.isEmpty()) {
            binding.expenseAmountLayout.setError("Please enter an amount");
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
            
            viewModel.saveExpense(name, amount, selectedBudget.getId(), selectedDate);
            Toast.makeText(requireContext(), "Expense saved", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
        } catch (NumberFormatException e) {
            binding.expenseAmountLayout.setError("Please enter a valid amount");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
