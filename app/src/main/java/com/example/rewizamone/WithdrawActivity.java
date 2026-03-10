package com.example.rewizamone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WithdrawActivity extends AppCompatActivity {

    private EditText paypalEmailEditText, withdrawalAmountEditText;
    private Button confirmWithdrawalButton;
    private double userEarnings;

    private static final String PREFS_NAME = "user_earnings";
    private static final String KEY_EARNINGS = "earnings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        paypalEmailEditText = findViewById(R.id.paypalEmailEditText);
        withdrawalAmountEditText = findViewById(R.id.withdrawalAmountEditText);
        confirmWithdrawalButton = findViewById(R.id.confirmWithdrawalButton);

        // Carregar o saldo atual do usuário
        loadUserEarnings();

        confirmWithdrawalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paypalEmail = paypalEmailEditText.getText().toString().trim();
                String withdrawalAmountStr = withdrawalAmountEditText.getText().toString().trim();

                if (paypalEmail.isEmpty() || withdrawalAmountStr.isEmpty()) {
                    Toast.makeText(WithdrawActivity.this, "Please enter all details.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double withdrawalAmount = Double.parseDouble(withdrawalAmountStr);

                if (withdrawalAmount > userEarnings) {
                    Toast.makeText(WithdrawActivity.this, "Insufficient balance.", Toast.LENGTH_SHORT).show();
                } else {
                    userEarnings -= withdrawalAmount;
                    saveUserEarnings();

                    // Enviar saldo atualizado de volta para MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedEarnings", userEarnings);
                    setResult(RESULT_OK, resultIntent);

                    Toast.makeText(WithdrawActivity.this, "Withdrawal of $" + withdrawalAmount + " processed.", Toast.LENGTH_LONG).show();

                    finish();
                }
            }
        });
    }

    private void loadUserEarnings() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userEarnings = preferences.getFloat(KEY_EARNINGS, 0.00f);
    }

    private void saveUserEarnings() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(KEY_EARNINGS, (float) userEarnings);
        editor.apply();
    }
}
