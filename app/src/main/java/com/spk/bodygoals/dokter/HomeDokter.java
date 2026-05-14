package com.spk.bodygoals.dokter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.R;
import com.spk.bodygoals.admin.DataBMI;
import com.spk.bodygoals.admin.DataMakanan;
import com.spk.bodygoals.admin.DataPasien;
import com.spk.bodygoals.admin.RulesPenyakit;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;

public class HomeDokter extends AppCompatActivity {
    TextView namaDokter, jmlKurus, jmlIdeal, jmlGemuk;
    SessionManager session;
    LinearLayout menuMakanan, menuPasien, menuPenyakit, menuBMI;
    private AlertDialog loadingDialog;
    ImageView profil;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_dokter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHomeDokter), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        namaDokter = findViewById(R.id.namaDokter);
        namaDokter.setText(session.getName());

        menuBMI = findViewById(R.id.menuBMIs);
        menuPenyakit = findViewById(R.id.menuPenyakits);
        menuMakanan = findViewById(R.id.menuMakanans);
        menuPasien = findViewById(R.id.menuPasiens);

        jmlKurus = findViewById(R.id.jmlKurus);
        jmlIdeal = findViewById(R.id.jmlIdeal);
        jmlGemuk = findViewById(R.id.jmlGemuk);

        profil = findViewById(R.id.ivAvatarDoctor);

        profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeDokter.this, ProfilDokter.class));
                finish();
            }
        });

        fetchData();

        menuMakanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeDokter.this, DataMakanan.class));
                finish();
            }
        });

        menuPasien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeDokter.this, DataPasien.class));
                finish();
            }
        });

        menuPenyakit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeDokter.this, RulesPenyakit.class));
                finish();
            }
        });

        menuBMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeDokter.this, DataBMI.class));
                finish();
            }
        });
    }

    private void fetchData() {
        showLoading();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.DOKTER_DASHBOARD,
                null,
                response -> {
                    hideLoading();

                    try {
                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            int jumlahKurus = response.optInt("jumlah_kurus", 0);
                            int jumlahIdeal = response.optInt("jumlah_ideal", 0);
                            int jumlahGemuk = response.optInt("jumlah_gemuk", 0);

                            jmlKurus.setText(String.valueOf(jumlahKurus));
                            jmlIdeal.setText(String.valueOf(jumlahIdeal));
                            jmlGemuk.setText(String.valueOf(jumlahGemuk));

                        } else {
                            String message = response.optString("message", "Gagal mengambil data");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    hideLoading();

                    String message = "Gagal terhubung ke server";

                    if (error.networkResponse != null) {
                        message = "Error: " + error.networkResponse.statusCode;
                    } else if (error.getMessage() != null) {
                        message = error.getMessage();
                    }

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void showLoading() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false);

        loadingDialog = builder.create();
        loadingDialog.show();

        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}