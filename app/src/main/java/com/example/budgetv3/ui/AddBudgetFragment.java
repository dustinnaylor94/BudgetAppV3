package com.example.budgetv3.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.budgetv3.databinding.FragmentAddBudgetBinding;
import com.example.budgetv3.ui.viewmodel.AddBudgetViewModel;

public class AddBudgetFragment extends Fragment {
    private FragmentAddBudgetBinding binding;
    private AddBudgetViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(AddBudgetViewModel.class);
        setupButtons();
    }

    private void setupButtons() {
        binding.saveButton.setOnClickListener(v -> saveBudget());
        binding.cancelButton.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());
    }

    private void saveBudget() {
        String name = binding.budgetNameInput.getText().toString().trim();
        String amountStr = binding.budgetAmountInput.getText().toString().trim();

        if (name.isEmpty()) {
            binding.budgetNameLayout.setError("Please enter a budget name");
            return;
        }

        if (amountStr.isEmpty()) {
            binding.budgetAmountLayout.setError("Please enter an amount");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            viewModel.saveBudget(name, amount);
            Toast.makeText(requireContext(), "Budget saved", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
        } catch (NumberFormatException e) {
            binding.budgetAmountLayout.setError("Please enter a valid amount");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
