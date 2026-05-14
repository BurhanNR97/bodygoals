package com.spk.bodygoals.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.R;
import com.spk.bodygoals.adapter.adpPasien;
import com.spk.bodygoals.model.mPasien;
import com.spk.bodygoals.service.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DataPasien extends AppCompatActivity {
    private RecyclerView rvPasien;
    private EditText edtCari;
    private TextView tvJumlahData, tvKosong;
    private ImageView kembali;

    private adpPasien adapter;
    private ArrayList<mPasien> listPasienFull;
    private ArrayList<mPasien> listPasienFilter;
    private AlertDialog loadingDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data_pasien);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDataPasien), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listPasienFull = new ArrayList<>();
        listPasienFilter = new ArrayList<>();

        edtCari = findViewById(R.id.edtCariPasien);
        tvJumlahData = findViewById(R.id.tvJumlahDataPasien);
        tvKosong = findViewById(R.id.tvKosongPasien);
        kembali = findViewById(R.id.btnBackDataPasien);
        rvPasien = findViewById(R.id.rvDataPasien);

        kembali = findViewById(R.id.btnBackDataPasien);

        adapter = new adpPasien(this, listPasienFilter);
        rvPasien.setLayoutManager(new LinearLayoutManager(this));
        rvPasien.setAdapter(adapter);

        kembali.setOnClickListener(v -> {
            startActivity(new Intent(DataPasien.this, HomeAdmin.class));
            finish();
        });

        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ambilDataPasien();
    }

    private void filterData() {
        String keyword = edtCari.getText().toString().trim().toLowerCase();

        listPasienFilter.clear();

        for (mPasien pasien : listPasienFull) {
            String id = pasien.getId() == null ? "" : pasien.getId().toLowerCase();
            String nama = pasien.getNama() == null ? "" : pasien.getNama().toLowerCase();
            String email = pasien.getEmail() == null ? "" : pasien.getEmail().toLowerCase();

            boolean cocok =
                    id.contains(keyword) ||
                            nama.contains(keyword) ||
                            email.contains(keyword);

            if (cocok) {
                listPasienFilter.add(pasien);
            }
        }

        adapter.notifyDataSetChanged();

        if (listPasienFilter.isEmpty()) {
            tvKosong.setVisibility(android.view.View.VISIBLE);
            rvPasien.setVisibility(android.view.View.GONE);
        } else {
            tvKosong.setVisibility(android.view.View.GONE);
            rvPasien.setVisibility(android.view.View.VISIBLE);
        }

        tvJumlahData.setText("Menampilkan " + listPasienFilter.size() + " data pasien");
    }

    private void ambilDataPasien() {
        showLoading();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.ADMIN_PASIEN_FETCH,
                null,
                response -> {
                    try {
                        listPasienFull.clear();

                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            JSONArray data = response.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject obj = data.getJSONObject(i);

                                    mPasien model = new mPasien();
                                    model.setId(obj.optString("nik", ""));
                                    model.setNama(obj.optString("nama_lengkap", ""));
                                    model.setJk(obj.optString("jenis_kelamin", ""));
                                    model.setTglLahir(obj.optString("tgl_lahir", ""));
                                    model.setAlamat(obj.optString("alamat", ""));
                                    model.setEmail(obj.optString("email", ""));

                                    listPasienFull.add(model);
                                }
                            }

                            filterData();

                        } else {
                            listPasienFull.clear();
                            filterData();

                            tvKosong.setText(response.optString("message", "Data pasien belum tersedia"));
                            tvJumlahData.setText("Menampilkan 0 data pasien");
                            tvKosong.setVisibility(android.view.View.VISIBLE);
                            rvPasien.setVisibility(android.view.View.GONE);
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                DataPasien.this,
                                "Format data tidak sesuai: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    } finally {
                        hideLoading();
                    }
                },
                error -> {
                    hideLoading();
                    Toast.makeText(DataPasien.this, "Gagal mengambil data pasien", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
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