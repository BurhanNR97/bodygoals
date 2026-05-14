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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.spk.bodygoals.R;
import com.spk.bodygoals.adapter.adpMakanan;
import com.spk.bodygoals.dokter.HomeDokter;
import com.spk.bodygoals.model.mMakanan;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DataMakanan extends AppCompatActivity {
    private RecyclerView rvMakanan;
    private EditText edtCari;
    private TextView tvJumlahData, tvKosong, btnFilter;
    private ImageView kembali;

    private adpMakanan adapter;
    private ArrayList<mMakanan> listFull;
    private ArrayList<mMakanan> listFilter;
    private AlertDialog loadingDialog;
    SessionManager session;
    private String filterKategori = "Semua";
    private HashMap<String, String> mapKategori = new HashMap<>();
    private ArrayList<String> listKategoriNama = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data_makanan);
        View v = findViewById(R.id.mainDataMakanan);
        final int initialPaddingLeft = v.getPaddingLeft();
        final int initialPaddingTop = v.getPaddingTop();
        final int initialPaddingRight = v.getPaddingRight();
        final int initialPaddingBottom = v.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDataMakanan), (view, insets) -> {
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

        listFull = new ArrayList<>();
        listFilter = new ArrayList<>();

        edtCari = findViewById(R.id.edtCariMakanan);
        btnFilter = findViewById(R.id.btnFilterMakanan);
        tvJumlahData = findViewById(R.id.tvJumlahDataMakanan);
        tvKosong = findViewById(R.id.tvKosongMakanan);
        kembali = findViewById(R.id.btnBackDataMakanan);
        rvMakanan = findViewById(R.id.rvDataMakanan);

        session = new SessionManager(DataMakanan.this);

        adapter = new adpMakanan(this, listFilter);
        rvMakanan.setLayoutManager(new LinearLayoutManager(this));
        rvMakanan.setAdapter(adapter);

        kembali.setOnClickListener(view -> {
            String role = session.getRole().toLowerCase();

            if (role.equals("admin")) {
                startActivity(new Intent(DataMakanan.this, HomeAdmin.class));
            } else
            if (role.equals("dokter")) {
                startActivity(new Intent(DataMakanan.this, HomeDokter.class));
            }
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

        btnFilter.setOnClickListener(view -> tampilFilterKategori());

        ambilKategori();
        ambilDataMakanan();
    }

    private void filterData() {
        String keyword = edtCari.getText().toString().trim().toLowerCase();

        listFilter.clear();

        for (mMakanan makanan : listFull) {
            String nama = makanan.getNama() == null ? "" : makanan.getNama().toLowerCase();
            String kategori = makanan.getKategori() == null ? "" : makanan.getKategori();

            boolean cocokKeyword = nama.contains(keyword);

            boolean cocokKategori =
                    filterKategori.equals("Semua") ||
                            kategori.equalsIgnoreCase(filterKategori);

            if (cocokKeyword && cocokKategori) {
                listFilter.add(makanan);
            }
        }

        adapter.notifyDataSetChanged();

        if (listFilter.isEmpty()) {
            tvKosong.setVisibility(View.VISIBLE);
            rvMakanan.setVisibility(View.GONE);
        } else {
            tvKosong.setVisibility(View.GONE);
            rvMakanan.setVisibility(View.VISIBLE);
        }

        if (filterKategori.equals("Semua")) {
            tvJumlahData.setText("Menampilkan " + listFilter.size() + " data makanan");
        } else {
            tvJumlahData.setText("Menampilkan " + listFilter.size() + " data " + filterKategori);
        }
    }

    private void tampilFilterKategori() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 24, 32, 24);

        TextView title = new TextView(this);
        title.setText("Filter Kategori");
        title.setTextSize(18);
        title.setTextColor(android.graphics.Color.BLACK);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 20);
        container.addView(title);

        tambahItemFilter(container, "Semua", bottomSheetDialog);

        for (String kategori : listKategoriNama) {
            tambahItemFilter(container, kategori, bottomSheetDialog);
        }

        bottomSheetDialog.setContentView(container);
        bottomSheetDialog.show();
    }

    private void tambahItemFilter(LinearLayout container, String kategori, BottomSheetDialog dialog) {
        TextView item = new TextView(this);
        item.setText(kategori.equals(filterKategori) ? "✓  " + kategori : kategori);
        item.setTextSize(16);
        item.setTextColor(android.graphics.Color.rgb(30, 30, 30));
        item.setPadding(0, 22, 0, 22);

        item.setOnClickListener(v -> {
            filterKategori = kategori;
            btnFilter.setText("Filter: " + kategori);
            filterData();
            dialog.dismiss();
        });

        container.addView(item);
    }

    private void ambilKategori() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.ADMIN_MAKANAN_KATEGORI,
                null,
                response -> {
                    try {
                        listKategoriNama.clear();
                        mapKategori.clear();

                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            JSONArray data = response.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject obj = data.getJSONObject(i);

                                    String id = obj.optString("id", "");
                                    String nama = obj.optString("nama", "");

                                    listKategoriNama.add(nama);
                                    mapKategori.put(id, nama);
                                }
                            }
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Format kategori tidak sesuai", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Gagal mengambil kategori", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void ambilDataMakanan() {
        showLoading();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.ADMIN_MAKANAN_FETCH,
                null,
                response -> {
                    try {
                        listFull.clear();

                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            JSONArray data = response.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject obj = data.getJSONObject(i);

                                    mMakanan model = new mMakanan();
                                    model.setId(obj.optString("id", ""));
                                    model.setKategoriId(obj.optString("kategori_id", ""));
                                    model.setKategori(obj.optString("kategori", ""));
                                    model.setNama(obj.optString("nama", ""));
                                    model.setPorsiGram(obj.optString("porsi_gram", ""));
                                    model.setKkal(obj.optString("kkal", ""));
                                    model.setProtein(obj.optString("protein", ""));
                                    model.setKarbohidrat(obj.optString("karbohidrat", ""));
                                    model.setEnergi(obj.optString("energi", ""));
                                    model.setAir(obj.optString("air", ""));
                                    model.setLemak(obj.optString("lemak", ""));
                                    model.setSerat(obj.optString("serat", ""));
                                    model.setGula(obj.optString("gula", ""));
                                    model.setNatrium(obj.optString("natrium", ""));
                                    model.setKeterangan(obj.optString("keterangan", ""));

                                    listFull.add(model);
                                }
                            }

                            filterData();

                        } else {
                            listFull.clear();
                            filterData();

                            tvKosong.setText(response.optString("message", "Data makanan belum tersedia"));
                            tvJumlahData.setText("Menampilkan 0 data makanan");
                            tvKosong.setVisibility(View.VISIBLE);
                            rvMakanan.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                DataMakanan.this,
                                "Format data tidak sesuai: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    } finally {
                        hideLoading();
                    }
                },
                error -> {
                    hideLoading();

                    String pesan = "Gagal mengambil data makanan";

                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;

                        if (error.networkResponse.data != null) {
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");
                                android.util.Log.e("FETCH_MAKANAN", "HTTP " + code + " BODY: " + body);

                                JSONObject obj = new JSONObject(body);
                                pesan = obj.optString("message", pesan);
                            } catch (Exception e) {
                                pesan = "Gagal mengambil data makanan. HTTP " + code;
                            }
                        } else {
                            pesan = "Gagal mengambil data makanan. HTTP " + code;
                        }
                    }

                    Toast.makeText(DataMakanan.this, pesan, Toast.LENGTH_LONG).show();
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