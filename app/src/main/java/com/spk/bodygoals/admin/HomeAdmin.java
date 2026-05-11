package com.spk.bodygoals.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.spk.bodygoals.LoginActivity;
import com.spk.bodygoals.R;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeAdmin extends AppCompatActivity {

    LineChart lineChart;
    ImageView profil;
    SessionManager session;
    TextView tvTotalUser, tvTotalPasien, tvTotalDokter;
    LinearLayout menuPetugas;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_admin);
        View v = findViewById(R.id.layoutHomeAdmin);
        final int initialPaddingLeft = v.getPaddingLeft();
        final int initialPaddingTop = v.getPaddingTop();
        final int initialPaddingRight = v.getPaddingRight();
        final int initialPaddingBottom = v.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHomeAdmin), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    initialPaddingLeft + systemBars.left,
                    initialPaddingTop + systemBars.top,
                    initialPaddingRight + systemBars.right,
                    initialPaddingBottom + systemBars.bottom
            );
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        lineChart = findViewById(R.id.lineChart);

        tvTotalUser = findViewById(R.id.qty_user);
        tvTotalDokter = findViewById(R.id.qty_dokter);
        tvTotalPasien = findViewById(R.id.qty_pasien);

        menuPetugas = findViewById(R.id.menuPetugas);

        profil = findViewById(R.id.ivAvatar);
        profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeAdmin.this, ProfilAdmin.class);
                startActivity(intent);
                finish();
            }
        });

        menuPetugas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeAdmin.this, DataPetugas.class));
                finish();
            }
        });

        getDashboardAdmin();
    }

    private void setupUserChart(JSONArray array) throws JSONException {
        ArrayList<Entry> entries = new ArrayList<>();
        int maxTotalPasien = 0;

        for (int i=0; i<array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            int bulan = obj.getInt("bulan_angka") - 1;
            int totalPasien = obj.getInt("total_pasien");
            entries.add(new Entry(bulan, totalPasien));
            if (totalPasien > maxTotalPasien) {
                maxTotalPasien = totalPasien;
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "");

        dataSet.setColor(Color.parseColor("#FF7A00"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#FF7A00"));
        dataSet.setCircleRadius(5f);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFE0B2"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        String[] bulan = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des"};

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(bulan));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#6B7280"));
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(maxTotalPasien * 1f);
        leftAxis.setGranularity(5f);
        leftAxis.setTextColor(Color.parseColor("#6B7280"));
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setExtraOffsets(8, 8, 8, 8);

        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void logoutToLogin() {
        session.logout();

        Intent intent = new Intent(HomeAdmin.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void getDashboardAdmin() {
        String token = session.getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show();
            logoutToLogin();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.ADMIN_DASHBOARD,
                null,
                response -> {
                    try {
                        boolean status = response.getBoolean("status");

                        if (status) {
                            JSONArray users = response.getJSONArray("users");
                            JSONArray pasien = response.getJSONArray("pasien");
                            JSONArray dokter = response.getJSONArray("dokter");
                            JSONArray grafik = response.getJSONArray("grafik");

                            setupUserChart(grafik);

                            tvTotalUser.setText(String.valueOf(users.length()));
                            tvTotalPasien.setText(String.valueOf(pasien.length()));
                            tvTotalDokter.setText(String.valueOf(dokter.length()));
                        } else {
                            String message = response.optString("message", "Gagal mengambil data dashboard");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                headers.put("Authorization", "Bearer " + token);

                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}