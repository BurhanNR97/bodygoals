package com.spk.bodygoals.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.R;
import com.spk.bodygoals.service.ApiConfig;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DetailMakanan extends AppCompatActivity {
    private String idMakanan = "";

    private ImageView kembali;
    private TextView tvNama, tvKategori, tvPorsi, tvKkal, tvProtein, tvKarbohidrat;
    private TextView tvEnergi, tvAir, tvLemak, tvSerat, tvGula, tvNatrium, tvKeterangan;
    private TextView btnUbah, btnHapus;

    private AlertDialog loadingDialog;
    private String currentKategoriId = "";
    private String currentNama = "";
    private String currentPorsi = "";
    private String currentKkal = "";
    private String currentProtein = "";
    private String currentKarbohidrat = "";
    private String currentEnergi = "";
    private String currentAir = "";
    private String currentLemak = "";
    private String currentSerat = "";
    private String currentGula = "";
    private String currentNatrium = "";
    private String currentKeterangan = "";

    private ArrayList<String> kategoriIds = new ArrayList<>();
    private ArrayList<String> kategoriNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_makanan);
        View view = findViewById(R.id.mainDetailMakanan);
        final int initialPaddingLeft = view.getPaddingLeft();
        final int initialPaddingTop = view.getPaddingTop();
        final int initialPaddingRight = view.getPaddingRight();
        final int initialPaddingBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDetailMakanan), (v, insets) -> {
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

        idMakanan = getIntent().getStringExtra("id");

        kembali = findViewById(R.id.btnBackDetailMakanan);

        tvNama = findViewById(R.id.tvDetailNamaMakanan);
        tvKategori = findViewById(R.id.tvDetailKategoriMakanan);
        tvPorsi = findViewById(R.id.tvDetailPorsi);
        tvKkal = findViewById(R.id.tvDetailKkal);
        tvProtein = findViewById(R.id.tvDetailProtein);
        tvKarbohidrat = findViewById(R.id.tvDetailKarbohidrat);
        tvEnergi = findViewById(R.id.tvDetailEnergi);
        tvAir = findViewById(R.id.tvDetailAir);
        tvLemak = findViewById(R.id.tvDetailLemak);
        tvSerat = findViewById(R.id.tvDetailSerat);
        tvGula = findViewById(R.id.tvDetailGula);
        tvNatrium = findViewById(R.id.tvDetailNatrium);
        tvKeterangan = findViewById(R.id.tvDetailKeterangan);

        btnUbah = findViewById(R.id.btnUbahMakanan);
        btnHapus = findViewById(R.id.btnHapusMakanan);

        kembali.setOnClickListener(v -> finish());

        btnUbah.setOnClickListener(v -> {
            tampilDialogUbahMakanan();
        });

        btnHapus.setOnClickListener(v -> konfirmasiHapus());

        ambilDetailMakanan();
    }

    private void ambilDetailMakanan() {
        showLoading();

        String url = ApiConfig.ADMIN_MAKANAN_DETAIL + idMakanan;

        android.util.Log.d("DETAIL_MAKANAN_URL", url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            JSONObject data = response.optJSONObject("data");

                            if (data != null) {
                                tvNama.setText(data.optString("nama", "-"));
                                tvKategori.setText(data.optString("kategori", "-"));

                                tvPorsi.setText("Porsi Gram:\n" + nilai(data.optString("porsi_gram", "")));
                                tvKkal.setText("Kkal:\n" + nilai(data.optString("kkal", "")));
                                tvProtein.setText("Protein:\n" + nilai(data.optString("protein", "")));
                                tvKarbohidrat.setText("Karbohidrat:\n" + nilai(data.optString("karbohidrat", "")));
                                tvEnergi.setText("Energi:\n" + nilai(data.optString("energi", "")));
                                tvAir.setText("Air:\n" + nilai(data.optString("air", "")));
                                tvLemak.setText("Lemak:\n" + nilai(data.optString("lemak", "")));
                                tvSerat.setText("Serat:\n" + nilai(data.optString("serat", "")));
                                tvGula.setText("Gula:\n" + nilai(data.optString("gula", "")));
                                tvNatrium.setText("Natrium:\n" + nilai(data.optString("natrium", "")));
                                tvKeterangan.setText("Keterangan:\n" + nilai(data.optString("keterangan", "")));
                            }
                            if (data != null) {
                                currentKategoriId = data.optString("kategori_id", "");
                                currentNama = data.optString("nama", "");
                                currentPorsi = data.optString("porsi_gram", "");
                                currentKkal = data.optString("kkal", "");
                                currentProtein = data.optString("protein", "");
                                currentKarbohidrat = data.optString("karbohidrat", "");
                                currentEnergi = data.optString("energi", "");
                                currentAir = data.optString("air", "");
                                currentLemak = data.optString("lemak", "");
                                currentSerat = data.optString("serat", "");
                                currentGula = data.optString("gula", "");
                                currentNatrium = data.optString("natrium", "");
                                currentKeterangan = data.optString("keterangan", "");

                                tvNama.setText(nilai(currentNama));
                                tvKategori.setText(nilai(data.optString("kategori", "")));

                                tvPorsi.setText("Porsi Gram:\n" + nilai(currentPorsi));
                                tvKkal.setText("Kkal:\n" + nilai(currentKkal));
                                tvProtein.setText("Protein:\n" + nilai(currentProtein));
                                tvKarbohidrat.setText("Karbohidrat:\n" + nilai(currentKarbohidrat));
                                tvEnergi.setText("Energi:\n" + nilai(currentEnergi));
                                tvAir.setText("Air:\n" + nilai(currentAir));
                                tvLemak.setText("Lemak:\n" + nilai(currentLemak));
                                tvSerat.setText("Serat:\n" + nilai(currentSerat));
                                tvGula.setText("Gula:\n" + nilai(currentGula));
                                tvNatrium.setText("Natrium:\n" + nilai(currentNatrium));
                                tvKeterangan.setText("Keterangan:\n" + nilai(currentKeterangan));
                            }
                        } else {
                            Toast.makeText(
                                    this,
                                    response.optString("message", "Data makanan tidak ditemukan"),
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                this,
                                "Format data tidak sesuai: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    } finally {
                        hideLoading();
                    }
                },
                error -> {
                    hideLoading();

                    String pesan = "Gagal mengambil detail makanan";

                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;

                        if (error.networkResponse.data != null) {
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");
                                android.util.Log.e("DETAIL_MAKANAN_ERROR", "HTTP " + code + " BODY: " + body);

                                JSONObject obj = new JSONObject(body);
                                pesan = obj.optString("message", pesan);
                            } catch (Exception e) {
                                pesan = "Gagal mengambil detail makanan. HTTP " + code;
                            }
                        } else {
                            pesan = "Gagal mengambil detail makanan. HTTP " + code;
                        }
                    }

                    Toast.makeText(this, pesan, Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private String nilai(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return "-";
        }

        return value;
    }

    private void tampilDialogUbahMakanan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ubah_makanan, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        Spinner spKategori = view.findViewById(R.id.spKategoriMakanan);
        EditText edtNama = view.findViewById(R.id.edtNamaMakanan);
        EditText edtPorsi = view.findViewById(R.id.edtPorsiGram);
        EditText edtKkal = view.findViewById(R.id.edtKkal);
        EditText edtProtein = view.findViewById(R.id.edtProtein);
        EditText edtKarbohidrat = view.findViewById(R.id.edtKarbohidrat);
        EditText edtEnergi = view.findViewById(R.id.edtEnergi);
        EditText edtAir = view.findViewById(R.id.edtAir);
        EditText edtLemak = view.findViewById(R.id.edtLemak);
        EditText edtSerat = view.findViewById(R.id.edtSerat);
        EditText edtGula = view.findViewById(R.id.edtGula);
        EditText edtNatrium = view.findViewById(R.id.edtNatrium);
        EditText edtKeterangan = view.findViewById(R.id.edtKeterangan);

        TextView btnBatal = view.findViewById(R.id.btnBatalUbahMakanan);
        TextView btnSimpan = view.findViewById(R.id.btnSimpanUbahMakanan);

        edtNama.setText(nilaiKosong(currentNama));
        edtPorsi.setText(nilaiKosong(currentPorsi));
        edtKkal.setText(nilaiKosong(currentKkal));
        edtProtein.setText(nilaiKosong(currentProtein));
        edtKarbohidrat.setText(nilaiKosong(currentKarbohidrat));
        edtEnergi.setText(nilaiKosong(currentEnergi));
        edtAir.setText(nilaiKosong(currentAir));
        edtLemak.setText(nilaiKosong(currentLemak));
        edtSerat.setText(nilaiKosong(currentSerat));
        edtGula.setText(nilaiKosong(currentGula));
        edtNatrium.setText(nilaiKosong(currentNatrium));
        edtKeterangan.setText(nilaiKosong(currentKeterangan));

        ambilKategoriUntukSpinner(spKategori);

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            String nama = edtNama.getText().toString().trim();
            String porsi = edtPorsi.getText().toString().trim();
            String kkal = edtKkal.getText().toString().trim();
            String protein = edtProtein.getText().toString().trim();
            String karbohidrat = edtKarbohidrat.getText().toString().trim();
            String energi = edtEnergi.getText().toString().trim();
            String air = edtAir.getText().toString().trim();
            String lemak = edtLemak.getText().toString().trim();
            String serat = edtSerat.getText().toString().trim();
            String gula = edtGula.getText().toString().trim();
            String natrium = edtNatrium.getText().toString().trim();
            String keterangan = edtKeterangan.getText().toString().trim();

            if (nama.isEmpty()) {
                edtNama.setError("Nama makanan wajib diisi");
                edtNama.requestFocus();
                return;
            }

            if (spKategori.getSelectedItemPosition() < 0 || kategoriIds.isEmpty()) {
                Toast.makeText(this, "Kategori belum tersedia", Toast.LENGTH_SHORT).show();
                return;
            }

            String kategoriId = kategoriIds.get(spKategori.getSelectedItemPosition());

            updateMakanan(
                    dialog,
                    kategoriId,
                    nama,
                    porsi,
                    kkal,
                    protein,
                    karbohidrat,
                    energi,
                    air,
                    lemak,
                    serat,
                    gula,
                    natrium,
                    keterangan
            );
        });

        dialog.show();
    }

    private void ambilKategoriUntukSpinner(Spinner spinner) {
        kategoriIds.clear();
        kategoriNames.clear();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.ADMIN_MAKANAN_KATEGORI,
                null,
                response -> {
                    try {
                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            JSONArray data = response.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject obj = data.getJSONObject(i);

                                    String id = obj.optString("id", "");
                                    String nama = obj.optString("nama", "");

                                    kategoriIds.add(id);
                                    kategoriNames.add(nama);
                                }
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                DetailMakanan.this,
                                android.R.layout.simple_spinner_item,
                                kategoriNames
                        );

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);

                        int selectedIndex = 0;

                        for (int i = 0; i < kategoriIds.size(); i++) {
                            if (kategoriIds.get(i).equals(currentKategoriId)) {
                                selectedIndex = i;
                                break;
                            }
                        }

                        if (!kategoriIds.isEmpty()) {
                            spinner.setSelection(selectedIndex);
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Format kategori tidak sesuai", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Gagal mengambil kategori", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void updateMakanan(
            AlertDialog dialog,
            String kategoriId,
            String nama,
            String porsi,
            String kkal,
            String protein,
            String karbohidrat,
            String energi,
            String air,
            String lemak,
            String serat,
            String gula,
            String natrium,
            String keterangan
    ) {
        showLoading();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADMIN_MAKANAN_UPDATE,
                response -> {
                    hideLoading();

                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean status = obj.optBoolean("status", false);
                        String message = obj.optString("message", "Terjadi kesalahan");

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if (status) {
                            dialog.dismiss();
                            ambilDetailMakanan();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Response tidak valid", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    hideLoading();

                    String pesan = "Gagal memperbarui data makanan";

                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;

                        if (error.networkResponse.data != null) {
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");
                                android.util.Log.e("UPDATE_MAKANAN", "HTTP " + code + " BODY: " + body);

                                JSONObject obj = new JSONObject(body);
                                pesan = obj.optString("message", pesan);

                                if (obj.has("errors")) {
                                    JSONObject errors = obj.getJSONObject("errors");
                                    StringBuilder detail = new StringBuilder();

                                    java.util.Iterator<String> keys = errors.keys();
                                    while (keys.hasNext()) {
                                        String key = keys.next();
                                        JSONArray arr = errors.getJSONArray(key);

                                        if (arr.length() > 0) {
                                            detail.append(arr.getString(0)).append("\n");
                                        }
                                    }

                                    if (detail.length() > 0) {
                                        pesan = detail.toString().trim();
                                    }
                                }

                            } catch (Exception e) {
                                pesan = "Gagal memperbarui data. HTTP " + code;
                            }
                        } else {
                            pesan = "Gagal memperbarui data. HTTP " + code;
                        }
                    }

                    Toast.makeText(this, pesan, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("id", idMakanan);
                params.put("kategori_id", kategoriId);
                params.put("nama", nama);
                params.put("porsi_gram", porsi);
                params.put("kkal", kkal);
                params.put("protein", protein);
                params.put("karbohidrat", karbohidrat);
                params.put("energi", energi);
                params.put("air", air);
                params.put("lemak", lemak);
                params.put("serat", serat);
                params.put("gula", gula);
                params.put("natrium", natrium);
                params.put("keterangan", keterangan);

                android.util.Log.d("UPDATE_MAKANAN_PARAMS", params.toString());

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void konfirmasiHapus() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Data")
                .setMessage("Yakin ingin menghapus data makanan ini?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (dialog, which) -> hapusData())
                .show();
    }

    private void hapusData() {
        showLoading();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADMIN_MAKANAN_HAPUS,
                response -> {
                    hideLoading();

                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean status = obj.optBoolean("status", false);
                        String message = obj.optString("message", "Terjadi kesalahan");

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if (status) {
                            Intent intent = new Intent(this, DataMakanan.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Response tidak valid", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    hideLoading();

                    String pesan = "Gagal menghapus data makanan";

                    if (error.networkResponse != null) {
                        pesan = "Gagal menghapus data makanan. HTTP " + error.networkResponse.statusCode;
                    }

                    Toast.makeText(this, pesan, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", idMakanan);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

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

    private String nilaiKosong(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value;
    }
}