package com.spk.bodygoals.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.R;
import com.spk.bodygoals.adapter.adpBMI;
import com.spk.bodygoals.dokter.HomeDokter;
import com.spk.bodygoals.model.mBMI;
import com.spk.bodygoals.model.mPetugas;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;
import com.spk.bodygoals.user.HomeUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataBMI extends AppCompatActivity {
    ImageView kembali, tambah;
    TextView tvJumlah, tvKosong;
    RecyclerView rvData;
    adpBMI adapter;
    ArrayList<mBMI> list;
    private AlertDialog loadingDialog;
    SessionManager session;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data_bmi);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDataBMI), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        kembali = findViewById(R.id.btnBackDataBMI);
        tambah = findViewById(R.id.btnAddDataBMI);
        tvJumlah = findViewById(R.id.tvJumlahDataBMI);
        tvKosong = findViewById(R.id.tvKosongBMI);
        rvData = findViewById(R.id.rvDataBMI);

        session = new SessionManager(this);

        list = new ArrayList<>();
        adapter = new adpBMI(DataBMI.this, list);

        rvData.setLayoutManager(new LinearLayoutManager(this));
        rvData.setAdapter(adapter);

        kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String role = session.getRole().toLowerCase();

                if (role.equals("admin")) {
                    startActivity(new Intent(DataBMI.this, HomeAdmin.class));
                } else
                if (role.equals("dokter")) {
                    startActivity(new Intent(DataBMI.this, HomeDokter.class));
                }
                finish();
            }
        });

        tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tampilDialogTambah();
            }
        });

        fetchData();
    }

    private void fetchData() {
        showLoading();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.ADMIN_BMI_FETCH,
                null,
                response -> {
                    try {
                        list.clear();

                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            JSONArray data = response.optJSONArray("data");

                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject obj = data.getJSONObject(i);

                                    String id = obj.optString("id", "");
                                    String kategori = obj.optString("kategori", "");
                                    String min = obj.optString("bmi_min", "");
                                    String max = obj.optString("bmi_max", "");

                                    mBMI model = new mBMI();
                                    model.setId(id);
                                    model.setNama(kategori);

                                    boolean minKosong = min == null || min.trim().isEmpty() || min.equalsIgnoreCase("null");
                                    boolean maxKosong = max == null || max.trim().isEmpty() || max.equalsIgnoreCase("null");

                                    if (minKosong && !maxKosong) {
                                        model.setBmi("<= " + max);
                                    } else if (!minKosong && maxKosong) {
                                        model.setBmi(">= " + min);
                                    } else if (!minKosong && !maxKosong) {
                                        model.setBmi(min + " - " + max);
                                    } else {
                                        model.setBmi("-");
                                    }

                                    list.add(model);
                                }
                            }

                            adapter.notifyDataSetChanged();

                            if (list.isEmpty()) {
                                tvKosong.setText("Data belum tersedia");
                                tvKosong.setVisibility(View.VISIBLE);
                                rvData.setVisibility(View.GONE);
                            } else {
                                tvKosong.setVisibility(View.GONE);
                                rvData.setVisibility(View.VISIBLE);
                            }

                            tvJumlah.setText("Menampilkan " + list.size() + " data");

                        } else {
                            list.clear();
                            adapter.notifyDataSetChanged();

                            tvKosong.setText(response.optString("message", "Data belum tersedia"));
                            tvJumlah.setText("Menampilkan 0 data");
                            tvKosong.setVisibility(View.VISIBLE);
                            rvData.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                DataBMI.this,
                                "Format data tidak sesuai: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    } finally {
                        hideLoading();
                    }
                },
                error -> {
                    hideLoading();

                    String pesan = "Gagal mengambil data BMI";

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;

                        if (error.networkResponse.data != null) {
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");

                                android.util.Log.e(
                                        "FETCH_BMI_ERROR",
                                        "HTTP " + statusCode + " BODY: " + body
                                );

                                JSONObject obj = new JSONObject(body);
                                pesan = obj.optString("message", pesan);

                                if (obj.has("error")) {
                                    pesan = pesan + "\n" + obj.optString("error");
                                }

                            } catch (Exception e) {
                                pesan = "Gagal mengambil data BMI. HTTP " + statusCode;
                                android.util.Log.e("FETCH_BMI_PARSE", e.getMessage());
                            }
                        } else {
                            pesan = "Gagal mengambil data BMI. HTTP " + statusCode;
                        }
                    } else {
                        pesan = "Tidak dapat terhubung ke server";
                        android.util.Log.e("FETCH_BMI_ERROR", String.valueOf(error));
                    }

                    Toast.makeText(
                            DataBMI.this,
                            pesan,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
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

    private void tampilDialogTambah() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_bmi, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        EditText edtKategori = view.findViewById(R.id.edtBmiKategori);
        EditText edtMin= view.findViewById(R.id.edtBmiMin);
        EditText edtMax = view.findViewById(R.id.edtBmiMax);

        TextView btnBatal = view.findViewById(R.id.btnBMIBatal);
        TextView btnSimpan = view.findViewById(R.id.btnBMISimpan);

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtKategori.getText().toString().isEmpty()) {
                    edtKategori.requestFocus();
                    edtKategori.setError("Masukkan kategori BMI");
                    return;
                } else
                if (edtMin.getText().toString().isEmpty()) {
                    edtMin.requestFocus();
                    edtMin.setError("Masukkan Skala Min");
                    return;
                } else
                if (edtMax.getText().toString().isEmpty()) {
                    edtMax.requestFocus();
                    edtMax.setError("Masukkan Skala Max");
                    return;
                } else {
                    simpanData(
                            dialog,
                            edtKategori.getText().toString().trim(),
                            edtMin.getText().toString().trim(),
                            edtMax.getText().toString().trim()
                    );
                }
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void simpanData(AlertDialog dialog, String kategori, String min, String max) {
        showLoading();
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADMIN_BMI_STORE,
                response -> {
                    hideLoading();

                    android.util.Log.d("STORE_PETUGAS_RESPONSE", response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean status = jsonObject.optBoolean("status", false);
                        String message = jsonObject.optString("message", "Terjadi kesalahan");

                        if (status) {
                            Toast.makeText(DataBMI.this, message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            fetchData();
                        } else {
                            Toast.makeText(DataBMI.this, message, Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                DataBMI.this,
                                "Format response tidak sesuai: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                error -> {
                    hideLoading();

                    String pesan = "Gagal menyimpan data";

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;

                        if (error.networkResponse.data != null) {
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");

                                android.util.Log.e("STORE_DATA_ERROR",
                                        "HTTP " + statusCode + " BODY: " + body);

                                JSONObject obj = new JSONObject(body);

                                pesan = obj.optString("message", pesan);

                                if (obj.has("error")) {
                                    pesan = pesan + "\n" + obj.optString("error");
                                }

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
                                pesan = "Gagal menyimpan data. HTTP " + statusCode;
                                android.util.Log.e("STORE_DATA_ERROR_PARSE", e.getMessage());
                            }
                        } else {
                            pesan = "Gagal menyimpan data. HTTP " + statusCode;
                        }
                    } else {
                        pesan = "Tidak dapat terhubung ke server";
                        android.util.Log.e("STORE_PETUGAS_ERROR", String.valueOf(error));
                    }

                    Toast.makeText(DataBMI.this, pesan, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("kategori", kategori);
                params.put("min", min);
                params.put("max", max);

                android.util.Log.d("STORE_DATA_PARAMS", params.toString());

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}