package com.galihekasyaputra.studentsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.galihekasyaputra.studentsapp.model.LoginRequest;
import com.galihekasyaputra.studentsapp.model.LoginResponse;
import com.galihekasyaputra.studentsapp.service.ApiClient;
import com.galihekasyaputra.studentsapp.service.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Jika sudah login, langsung ke MainActivity
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (prefs.getString("token", null) != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);

        apiService = ApiClient.getService();

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {

        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.login(new LoginRequest(email, password))
                .enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(
                            Call<LoginResponse> call,
                            Response<LoginResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {

                            String token = response.body().getToken();

                            getSharedPreferences("app_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("token", token)
                                    .apply();

                            Toast.makeText(LoginActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this, "Email atau password salah!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}