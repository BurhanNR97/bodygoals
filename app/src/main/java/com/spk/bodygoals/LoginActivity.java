package com.spk.bodygoals;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.spk.bodygoals.admin.HomeAdmin;
import com.spk.bodygoals.dokter.HomeDokter;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;
import com.spk.bodygoals.user.HomeUser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout lyEmail, lyPass;
    TextInputEditText etEmail, etPass;
    AppCompatButton btnLogin;
    SessionManager session;
    TextView daftar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        lyEmail = findViewById(R.id.layoutEmail);
        lyPass = findViewById(R.id.layoutPassword);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        daftar = findViewById(R.id.daftarDisini);

        new Handler().postDelayed(() -> {
            if (session.isLogin()) {
                String role = session.getRole();

                if (role.equalsIgnoreCase("admin")) {
                    startActivity(new Intent(this, HomeAdmin.class));
                } else if (role.equalsIgnoreCase("dokter")) {
                    startActivity(new Intent(this, HomeDokter.class));
                } else {
                    startActivity(new Intent(this, HomeUser.class));
                }
                finish();
            }
        }, 0);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
                String pass = Objects.requireNonNull(etPass.getText()).toString().trim();
                String url = ApiConfig.getUrl("login");

                if (email.isEmpty()) {
                    lyEmail.setError("Masukkan email");
                    return;
                }
                if (pass.isEmpty()) {
                    lyPass.setError("Masukkan password");
                    return;
                }

                btnLogin.setEnabled(false);
                btnLogin.setText("Loading...");

                StringRequest request = new StringRequest(
                        Request.Method.POST,
                        url,
                        response -> {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Login");

                            try {
                                JSONObject json = new JSONObject(response);
                                boolean status = json.getBoolean("status");

                                if (status) {
                                    JSONObject data = json.getJSONObject("data");
                                    String role = data.getString("role");

                                    int id = data.getInt("id");
                                    String name = data.getString("name");
                                    String emailUser = data.getString("email");
                                    String roles = data.getString("role");
                                    String token = json.getString("token");
                                    session.saveLogin(id, name, emailUser, roles, token);

                                    Toast.makeText(LoginActivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();

                                    if (role.equalsIgnoreCase("admin")) {
                                        startActivity(new Intent(LoginActivity.this, HomeAdmin.class));
                                    } else if (role.equalsIgnoreCase("dokter")) {
                                        startActivity(new Intent(LoginActivity.this, HomeDokter.class));
                                    } else {
                                        startActivity(new Intent(LoginActivity.this, HomeUser.class));
                                    }
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Response error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Login");

                            String msg = "Login gagal";

                            if (error.networkResponse != null) {
                                msg += " | Code: " + error.networkResponse.statusCode;
                                msg += " | Response: " + new String(error.networkResponse.data);
                            } else if (error.getMessage() != null) {
                                msg += " | " + error.getMessage();
                            }

                            if (error.networkResponse.statusCode == 401) {
                                Toast.makeText(LoginActivity.this, "Email atau password salah", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", email);
                        params.put("password", pass);
                        return params;
                    }
                };

                Volley.newRequestQueue(LoginActivity.this).add(request);
            }
        });

        daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, Registrasi.class));
                finish();
            }
        });
    }
}