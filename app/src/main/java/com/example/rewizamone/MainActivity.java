package com.example.rewizamone;

// Importações necessárias
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WITHDRAW = 1; // Código para identificar a atividade de saque
    private RewardedAd rewardedAd; // Objeto para anúncios recompensados
    private Button watchAdButton, menuButton; // Botões para assistir a anúncios e abrir o menu
    private TextView earningsTextView; // TextView para mostrar os ganhos do usuário
    private DrawerLayout drawerLayout; // Menu deslizante para a navegação
    private double userEarnings = 0.00; // Variável para armazenar os ganhos do usuário

    // Constantes para SharedPreferences (armazenamento de ganhos do usuário)
    private static final String PREFS_NAME = "user_earnings";
    private static final String KEY_EARNINGS = "earnings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialização do MobileAds para uso do AdMob
        MobileAds.initialize(this, initializationStatus -> {});

        // Referências aos elementos da interface
        drawerLayout = findViewById(R.id.drawerLayout);
        watchAdButton = findViewById(R.id.watchAdButton);
        menuButton = findViewById(R.id.menuButton);
        earningsTextView = findViewById(R.id.earningsTextView);

        // Configuração do menu de navegação
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Carrega e exibe os ganhos do usuário na interface
        loadUserEarnings();
        earningsTextView.setText(String.format("Earnings: $%.2f", userEarnings));

        // Carrega o anúncio recompensado
        loadRewardedAd();

        // Configura o botão para assistir a anúncios
        watchAdButton.setOnClickListener(v -> {
            if (rewardedAd != null) { // Verifica se o anúncio está pronto
                rewardedAd.show(this, rewardItem -> {
                    // Calcular o valor da recompensa para o usuário e para a plataforma
                    double rewardValue = rewardItem.getAmount(); // Quantia total da recompensa
                    double userShare = rewardValue * 0.35; // 35% para o usuário
                    double platformShare = rewardValue * 0.65; // 65% para a plataforma

                    // Atualizar o saldo do usuário com a parte dele
                    userEarnings += userShare;
                    earningsTextView.setText(String.format("Earnings: $%.2f", userEarnings));

                    // Mensagem informando o ganho do usuário
                    Toast.makeText(this, "You earned $" + String.format("%.3f", userShare) + "!", Toast.LENGTH_SHORT).show();

                    // Salva os ganhos do usuário e carrega um novo anúncio
                    saveUserEarnings();
                    loadRewardedAd();
                });
            } else {
                // Mensagem de erro caso o anúncio não esteja pronto
                Toast.makeText(this, "Ad not ready. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });

        // Configura o botão para abrir o menu lateral
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    // Gerencia a navegação do menu lateral
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_saque) {
            // Inicia a atividade de saque
            Intent intent = new Intent(MainActivity.this, WithdrawActivity.class);
            startActivityForResult(intent, REQUEST_CODE_WITHDRAW);
        } else if (id == R.id.nav_logout) {
            // Redireciona para a tela de login e finaliza a atividade atual
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        // Fecha o menu
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Recebe o resultado de atividades chamadas, como a de saque
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_WITHDRAW && resultCode == RESULT_OK) {
            if (data != null) {
                userEarnings = data.getDoubleExtra("updatedEarnings", userEarnings);
                earningsTextView.setText(String.format("Earnings: $%.2f", userEarnings));
                saveUserEarnings();
            }
        }
    }

    // Carrega um anúncio recompensado do AdMob
    private void loadRewardedAd() {
        String adUnitId = "ca-app-pub-3940256099942544/5224354917"; // ID de teste do AdMob
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, adUnitId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                rewardedAd = ad;
                setAdCallbacks(); // Configura os callbacks do anúncio carregado
                Toast.makeText(MainActivity.this, "Ad is ready!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                rewardedAd = null;
                Toast.makeText(MainActivity.this, "Failed to load ad: " + adError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Configura os callbacks para eventos de exibição do anúncio
    private void setAdCallbacks() {
        if (rewardedAd != null) {
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedAd = null;
                    Toast.makeText(MainActivity.this, "Ad closed.", Toast.LENGTH_SHORT).show();
                    loadRewardedAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    rewardedAd = null;
                    Toast.makeText(MainActivity.this, "Failed to show ad.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Toast.makeText(MainActivity.this, "Ad started.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Carrega os ganhos do usuário das preferências compartilhadas
    private void loadUserEarnings() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userEarnings = preferences.getFloat(KEY_EARNINGS, 0.00f);
    }

    // Salva os ganhos do usuário nas preferências compartilhadas
    private void saveUserEarnings() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(KEY_EARNINGS, (float) userEarnings);
        editor.apply();
    }

    // Gerencia o botão de "voltar" para fechar o menu, caso esteja aberto
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
