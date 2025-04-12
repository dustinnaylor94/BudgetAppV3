package com.example.budgetv3.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.budgetv3.data.entity.Budget;
import com.example.budgetv3.databinding.FragmentHomeBinding;
import com.example.budgetv3.ui.adapter.BudgetAdapter;
import com.example.budgetv3.ui.viewmodel.HomeViewModel;

public class HomeFragment extends Fragment implements BudgetAdapter.OnBudgetClickListener {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private BudgetAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewModel();
        setupRecyclerView();
        observeBudgets();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new BudgetAdapter(this);
        binding.budgetsRecyclerView.setAdapter(adapter);
    }

    private void observeBudgets() {
        viewModel.getBudgets().observe(getViewLifecycleOwner(), budgets -> {
            adapter.submitList(budgets);
            updateEmptyState(budgets == null || budgets.isEmpty());
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.budgetsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onBudgetClick(Budget budget) {
        // TODO: Navigate to budget detail screen
        // Bundle args = new Bundle();
        // args.putInt("budgetId", budget.getId());
        // Navigation.findNavController(requireView())
        //     .navigate(R.id.action_navigation_home_to_budgetDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
