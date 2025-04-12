package com.example.budgetv3.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.budgetv3.R;
import com.example.budgetv3.api.CurrencyApi;
import com.example.budgetv3.databinding.DialogCurrencyBinding;
import java.util.List;

public class CurrencyDialog extends DialogFragment {
    private DialogCurrencyBinding binding;
    private CurrencySelectedListener listener;

    public interface CurrencySelectedListener {
        void onCurrencySelected(String currency);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogCurrencyBinding.inflate(LayoutInflater.from(getContext()));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Currency")
               .setView(binding.getRoot())
               .setNegativeButton("Cancel", (dialog, which) -> dismiss());

        // Load currencies
        CurrencyApi.getCurrencies(new CurrencyApi.CurrencyCallback() {
            @Override
            public void onSuccess(List<String> currencies) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    currencies
                );
                binding.currencyList.setAdapter(adapter);
                binding.currencyList.setOnItemClickListener((parent, view, position, id) -> {
                    String selected = currencies.get(position).split(" - ")[0]; // Get only the currency code
                    if (listener != null) {
                        listener.onCurrencySelected(selected);
                    }
                    dismiss();
                });
            }

            @Override
            public void onError(String error) {
                dismiss();
                // Show error toast or dialog
            }
        });

        return builder.create();
    }

    public void setListener(CurrencySelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
