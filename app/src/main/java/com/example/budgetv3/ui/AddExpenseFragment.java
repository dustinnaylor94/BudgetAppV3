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
import com.example.budgetv3.ui.viewmodel.AddExpenseViewModel;
import java.util.List;

public class AddExpenseFragment extends Fragment {
    private FragmentAddExpenseBinding binding;
    private AddExpenseViewModel viewModel;
    private List<Budget> budgets;

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
            
            viewModel.saveExpense(name, amount, selectedBudget.getId());
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
