package com.example.budgetv3.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.budgetv3.R;
import com.example.budgetv3.data.entity.Budget;
import com.example.budgetv3.databinding.ItemBudgetCardBinding;
import java.text.NumberFormat;
import java.util.Locale;

public class BudgetAdapter extends ListAdapter<Budget, BudgetAdapter.BudgetViewHolder> {
    private final OnBudgetClickListener listener;

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }

    public BudgetAdapter(OnBudgetClickListener listener) {
        super(new BudgetDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBudgetCardBinding binding = ItemBudgetCardBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new BudgetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = getItem(position);
        holder.bind(budget);
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private final ItemBudgetCardBinding binding;
        private final NumberFormat currencyFormatter;

        BudgetViewHolder(ItemBudgetCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBudgetClick(getItem(position));
                }
            });
        }

        void bind(Budget budget) {
            binding.budgetNameText.setText(budget.getName());
            
            String currency = budget.getCurrency();
            binding.budgetAmount.setText(String.format("%.2f %s", budget.getAmount(), currency));
            double spent = budget.getSpent();
            double remaining = budget.getAmount() - spent;

            binding.totalText.setText(itemView.getContext().getString(R.string.budget_total, 
                String.format("%.2f %s", budget.getAmount(), currency)));
            binding.spentText.setText(itemView.getContext().getString(R.string.budget_spent, 
                String.format("%.2f %s", spent, currency)));
            binding.remainingText.setText(itemView.getContext().getString(R.string.budget_remaining, 
                String.format("%.2f %s", remaining, currency)));

            // Calculate progress percentage (0-100)
            int progress = (int) ((budget.getSpent() / budget.getAmount()) * 100);
            binding.budgetProgress.setProgress(Math.min(progress, 100));

            // Set progress color based on spending
            int colorRes;
            if (progress >= 100) {
                colorRes = R.color.progress_error;
            } else if (progress >= 80) {
                colorRes = R.color.progress_warning;
            } else {
                colorRes = R.color.progress_normal;
            }
            binding.budgetProgress.setIndicatorColor(itemView.getContext().getColor(colorRes));
        }
    }

    private static class BudgetDiffCallback extends DiffUtil.ItemCallback<Budget> {
        @Override
        public boolean areItemsTheSame(@NonNull Budget oldItem, @NonNull Budget newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Budget oldItem, @NonNull Budget newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getAmount() == newItem.getAmount() &&
                   oldItem.getSpent() == newItem.getSpent() &&
                   oldItem.getCurrency().equals(newItem.getCurrency());
        }
    }
}
