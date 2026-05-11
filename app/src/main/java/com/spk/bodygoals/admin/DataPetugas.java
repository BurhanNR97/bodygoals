package com.spk.bodygoals.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spk.bodygoals.R;
import com.spk.bodygoals.adapter.adpPetugas;
import com.spk.bodygoals.model.mPetugas;
import com.spk.bodygoals.service.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DataPetugas extends AppCompatActivity {
    private TextView btnFilter, tvJumlahData, tvKosong;
    private RecyclerView rvPetugas;
    private EditText edtCari;
    private adpPetugas adapter;
    private ArrayList<mPetugas> listPetugasFull;
    private ArrayList<mPetugas> listPetugasFilter;
    private AlertDialog loadingDialog;
    private String filterRole = "Semua";
    ImageView kembali, tambah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data_petugas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDataPetugas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listPetugasFull = new ArrayList<>();
        listPetugasFilter = new ArrayList<>();

        edtCari = findViewById(R.id.edtCariPetugas);
        btnFilter = findViewById(R.id.btnFilterPegawai);
        kembali = findViewById(R.id.btnBackDataPetugas);
        tambah = findViewById(R.id.btnAddDataPetugas);
        tvJumlahData = findViewById(R.id.tvJumlahData);
        tvKosong = findViewById(R.id.tvKosong);

        adapter = new adpPetugas(this, listPetugasFilter);
        rvPetugas = findViewById(R.id.rvDataPetugas);
        rvPetugas.setLayoutManager(new LinearLayoutManager(this));
        rvPetugas.setAdapter(adapter);

        kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DataPetugas.this, HomeAdmin.class));
                finish();
            }
        });

        setupCariDanFilter();
        ambilDataPetugas();
    }

    private void setupCariDanFilter() {
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnFilter.setOnClickListener(v -> tampilPopupFilter());
    }

    private void tampilPopupFilter() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_filter_petugas, null);

        LinearLayout menuSemua = view.findViewById(R.id.menuSemua);
        LinearLayout menuDokter = view.findViewById(R.id.menuDokter);
        LinearLayout menuAhliGizi = view.findViewById(R.id.menuAhliGizi);

        TextView checkSemua = view.findViewById(R.id.checkSemua);
        TextView checkDokter = view.findViewById(R.id.checkDokter);
        TextView checkAhliGizi = view.findViewById(R.id.checkAhliGizi);

        checkSemua.setVisibility(filterRole.equals("Semua") ? View.VISIBLE : View.GONE);
        checkDokter.setVisibility(filterRole.equals("Dokter") ? View.VISIBLE : View.GONE);
        checkAhliGizi.setVisibility(filterRole.equals("Ahli Gizi") ? View.VISIBLE : View.GONE);

        menuSemua.setOnClickListener(v -> {
            filterRole = "Semua";
            btnFilter.setText("Filter: Semua");
            filterData();
            bottomSheetDialog.dismiss();
        });

        menuDokter.setOnClickListener(v -> {
            filterRole = "Dokter";
            btnFilter.setText("Filter: Dokter");
            filterData();
            bottomSheetDialog.dismiss();
        });

        menuAhliGizi.setOnClickListener(v -> {
            filterRole = "Ahli Gizi";
            btnFilter.setText("Filter: Ahli Gizi");
            filterData();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void filterData() {
        String keyword = edtCari.getText().toString().trim().toLowerCase();

        listPetugasFilter.clear();

        for (mPetugas petugas : listPetugasFull) {
            String id = petugas.getId() == null ? "" : petugas.getId().toLowerCase();
            String nama = petugas.getNama() == null ? "" : petugas.getNama().toLowerCase();
            String role = petugas.getRole() == null ? "" : petugas.getRole();

            boolean cocokKeyword =
                    id.contains(keyword) ||
                            nama.contains(keyword);

            boolean cocokRole =
                    filterRole.equals("Semua") ||
                            role.equalsIgnoreCase(filterRole);

            if (cocokKeyword && cocokRole) {
                listPetugasFilter.add(petugas);
            }
        }

        adapter.notifyDataSetChanged();

        if (listPetugasFilter.isEmpty()) {
            tvKosong.setVisibility(View.VISIBLE);
            rvPetugas.setVisibility(View.GONE);
        } else {
            tvKosong.setVisibility(View.GONE);
            rvPetugas.setVisibility(View.VISIBLE);
        }

        if (filterRole.equals("Semua")) {
            tvJumlahData.setText("Menampilkan " + listPetugasFilter.size() + " data petugas");
        } else {
            tvJumlahData.setText("Menampilkan " + listPetugasFilter.size() + " data " + filterRole);
        }
    }

    private void ambilDataPetugas() {
        showLoading();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.ADMIN_PETUGAS_FETCH,
                null,
                response -> {
                    try {
                        listPetugasFull.clear();

                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            JSONArray data = response.optJSONArray("data");

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);

                                String idPetugas = obj.optString("nik", "");
                                String namaPetugas = obj.optString("nama_lengkap", "");
                                String role = obj.optString("role", "");

                                mPetugas model = new mPetugas();
                                model.setId(idPetugas);
                                model.setNama(namaPetugas);
                                model.setRole(role);

                                listPetugasFull.add(model);
                            }

                            filterData();

                        } else {
                            listPetugasFull.clear();
                            filterData();

                            listPetugasFull.clear();
                            filterData();

                            tvKosong.setText("Data petugas belum tersedia");
                            tvJumlahData.setText("Menampilkan 0 data petugas");

                            tvKosong.setText(response.optString("message", "Data petugas belum tersedia"));
                            tvKosong.setVisibility(View.VISIBLE);
                            rvPetugas.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                DataPetugas.this,
                                "Format data tidak sesuai: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    } finally {
                        hideLoading();
                    }
                },
                error -> {
                    hideLoading();

                    Toast.makeText(
                            DataPetugas.this,
                            "Gagal mengambil data petugas",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void showLoading() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
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