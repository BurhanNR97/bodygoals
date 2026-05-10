package com.spk.bodygoals.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.LoginActivity;
import com.spk.bodygoals.R;
import com.spk.bodygoals.admin.HomeAdmin;
import com.spk.bodygoals.admin.ProfilAdmin;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;
import com.spk.bodygoals.BmiGaugeView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeUser extends AppCompatActivity {
    SessionManager session;
    TextView namaUser, tvBB, tvTB, tvUpdate;
    ImageView profil;
    private BmiGaugeView bmiGaugeUser;
    LinearLayout menuRekomen;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHomeUser), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        Log.d("token", session.getToken());

        namaUser = findViewById(R.id.namaUser);
        namaUser.setText(session.getName());
        profil = findViewById(R.id.ivAvatar);
        tvBB = findViewById(R.id.tv_user_bb);
        tvTB = findViewById(R.id.tv_user_tb);
        bmiGaugeUser = findViewById(R.id.bmiGaugeUser);
        tvUpdate = findViewById(R.id.tv_user_update);
        menuRekomen = findViewById(R.id.menuRekomendasi);

        profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeUser.this, ProfilUser.class);
                startActivity(intent);
                finish();
            }
        });

        getData();

        menuRekomen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeUser.this, Rekomendasi.class));
                finish();
            }
        });
    }

    private void getData() {
        String token = session.getToken();
        String email = session.getEmail();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show();
            logoutToLogin();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.USER_DASHBOARD,
                    body,
                    response -> {
                        try {
                            boolean status = response.getBoolean("status");

                            if (status) {
                                JSONObject data = response.getJSONObject("data");

                                double bb = data.optDouble("bb", 0);
                                double tb = data.optDouble("tb", 0);

                                tvBB.setText(String.format("%.0f kg", bb));
                                tvTB.setText(String.format("%.0f cm", tb));

                                String tglUkur = data.optString("tgl_ukur", "");

                                if (tglUkur == null || tglUkur.trim().isEmpty() || tglUkur.equalsIgnoreCase("null")) {
                                    tvUpdate.setText("Terakhir diperbarui -");
                                } else {
                                    try {
                                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));

                                        Date date = inputFormat.parse(tglUkur);
                                        tvUpdate.setText("Terakhir diperbarui " + outputFormat.format(date));

                                    } catch (Exception e) {
                                        tvUpdate.setText("Terakhir diperbarui " + tglUkur);
                                    }
                                }

                                if (bb > 0 && tb > 0) {
                                    double bmi = hitungBMI(bb, tb);
                                    setupBmiGauge(bmi);
                                } else {
                                    setupBmiGauge(0);
                                }

                            } else {
                                tvBB.setText("0 kg");
                                tvTB.setText("0 cm");

                                setupBmiGauge(0);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Format response dashboard tidak sesuai", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;

                            if (statusCode == 401) {
                                Toast.makeText(this, "Sesi berakhir. Silakan login ulang.", Toast.LENGTH_SHORT).show();
                                logoutToLogin();
                            } else {
                                Toast.makeText(this, "Error server: " + statusCode, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show();
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + token);

                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat request", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutToLogin() {
        session.logout();

        Intent intent = new Intent(HomeUser.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBmiGauge(double bmi) {
        if (bmiGaugeUser != null) {
            bmiGaugeUser.setBmi((float) bmi);
        }
    }

    private double hitungBMI(double bb, double tbCm) {
        if (bb <= 0 || tbCm <= 0) {
            return 0;
        }

        double tbMeter = tbCm / 100.0;
        return bb / (tbMeter * tbMeter);
    }
}