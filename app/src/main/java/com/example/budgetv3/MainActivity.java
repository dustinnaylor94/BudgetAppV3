package com.example.budgetv3;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;
import com.example.budgetv3.R;
import com.example.budgetv3.databinding.ActivityMainBinding;
import com.example.budgetv3.ui.dialog.CurrencyDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return navController.navigateUp() || super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == R.id.action_set_currency) {
            showCurrencyDialog();
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

    private void showCurrencyDialog() {
        CurrencyDialog dialog = new CurrencyDialog();
        dialog.setListener(currency -> {
            // TODO: Save selected currency as default
            Toast.makeText(this, "Selected currency: " + currency, Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "currency_dialog");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
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