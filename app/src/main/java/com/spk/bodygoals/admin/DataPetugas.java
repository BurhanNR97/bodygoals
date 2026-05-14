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
import android.app.DatePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.toolbox.StringRequest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
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
    private boolean nikSudahAda = false;
    private boolean emailSudahAda = false;
    private android.os.Handler handler = new android.os.Handler();

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
        tambah = findViewById(R.id.btnAddDataPetugas);

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

        tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tampilDialogTambahPetugas();
            }
        });
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

    private void tampilDialogTambahPetugas() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_petugas, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        EditText edtNik = view.findViewById(R.id.edtNik);
        EditText edtNamaLengkap = view.findViewById(R.id.edtNamaLengkap);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtAlamat = view.findViewById(R.id.edtAlamat);
        EditText edtTelp = view.findViewById(R.id.edtTelp);
        EditText edtTglLahir = view.findViewById(R.id.edtTglLahir);
        EditText edtPassword = view.findViewById(R.id.edtPassword);
        EditText edtKonfirmasiPassword = view.findViewById(R.id.edtKonfirmasiPassword);

        RadioGroup rgJenisKelamin = view.findViewById(R.id.rgJenisKelamin);
        RadioButton rbLaki = view.findViewById(R.id.rbLaki);
        RadioButton rbPerempuan = view.findViewById(R.id.rbPerempuan);
        RadioGroup rgJenisPetugas = view.findViewById(R.id.rgJenis);
        RadioButton rbDokter = view.findViewById(R.id.rbDokter);
        RadioButton rbGizi = view.findViewById(R.id.rbGizi);

        TextView btnBatal = view.findViewById(R.id.btnBatal);
        TextView btnSimpan = view.findViewById(R.id.btnSimpan);

        edtNik.addTextChangedListener(new TextWatcher() {
            Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nikSudahAda = false;

                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }

                String nik = s.toString().trim();

                if (nik.isEmpty()) {
                    edtNik.setError(null);
                    return;
                }

                runnable = () -> cekNikSudahAda(nik, edtNik);
                handler.postDelayed(runnable, 700);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtEmail.addTextChangedListener(new TextWatcher() {
            Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailSudahAda = false;

                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }

                String email = s.toString().trim();

                if (email.isEmpty()) {
                    edtEmail.setError(null);
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    edtEmail.setError("Format email tidak valid");
                    return;
                }

                runnable = () -> cekEmailSudahAda(email, edtEmail);
                handler.postDelayed(runnable, 700);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString().trim();

                if (!password.isEmpty() && password.length() < 5) {
                    edtPassword.setError("Password minimal 5 karakter");
                } else {
                    edtPassword.setError(null);
                }

                String konfirmasi = edtKonfirmasiPassword.getText().toString().trim();
                if (!konfirmasi.isEmpty() && !konfirmasi.equals(password)) {
                    edtKonfirmasiPassword.setError("Konfirmasi password tidak sama");
                } else {
                    edtKonfirmasiPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtKonfirmasiPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = edtPassword.getText().toString().trim();
                String konfirmasi = s.toString().trim();

                if (!konfirmasi.isEmpty() && !konfirmasi.equals(password)) {
                    edtKonfirmasiPassword.setError("Konfirmasi password tidak sama");
                } else {
                    edtKonfirmasiPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtTglLahir.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int tahun = calendar.get(Calendar.YEAR);
            int bulan = calendar.get(Calendar.MONTH);
            int hari = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    DataPetugas.this,
                    (datePicker, year, month, dayOfMonth) -> {
                        String tanggal = year + "-" +
                                String.format("%02d", month + 1) + "-" +
                                String.format("%02d", dayOfMonth);

                        edtTglLahir.setText(tanggal);
                    },
                    tahun,
                    bulan,
                    hari
            );

            datePickerDialog.show();
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            String nik = edtNik.getText().toString().trim();
            String namaLengkap = edtNamaLengkap.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String alamat = edtAlamat.getText().toString().trim();
            String telp = edtTelp.getText().toString().trim();
            String tglLahir = edtTglLahir.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String konfirmasiPassword = edtKonfirmasiPassword.getText().toString().trim();

            String jenisKelamin = rbLaki.isChecked() ? "L" : "P";
            String jenis = rbDokter.isChecked() ? "Dokter" : "Ahli Gizi";

            if (email.isEmpty()) {
                edtEmail.setError("Email wajib diisi");
                edtEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Format email tidak valid");
                edtEmail.requestFocus();
                return;
            }

            if (emailSudahAda) {
                edtEmail.setError("Email sudah digunakan");
                edtEmail.requestFocus();
                return;
            }

            if (namaLengkap.isEmpty()) {
                edtNamaLengkap.setError("Nama lengkap wajib diisi");
                edtNamaLengkap.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                edtEmail.setError("Email wajib diisi");
                edtEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Format email tidak valid");
                edtEmail.requestFocus();
                return;
            }

            if (!telp.isEmpty() && telp.length() < 8) {
                edtTelp.setError("Nomor telepon tidak valid");
                edtTelp.requestFocus();
                return;
            }

            if (tglLahir.isEmpty()) {
                edtTglLahir.setError("Tanggal lahir wajib diisi");
                edtTglLahir.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Password wajib diisi");
                edtPassword.requestFocus();
                return;
            }

            if (password.length() < 5) {
                edtPassword.setError("Password minimal 5 karakter");
                edtPassword.requestFocus();
                return;
            }

            if (konfirmasiPassword.isEmpty()) {
                edtKonfirmasiPassword.setError("Konfirmasi password wajib diisi");
                edtKonfirmasiPassword.requestFocus();
                return;
            }

            if (!konfirmasiPassword.equals(password)) {
                edtKonfirmasiPassword.setError("Konfirmasi password tidak sama");
                edtKonfirmasiPassword.requestFocus();
                return;
            }

            simpanPetugasDokter(
                    dialog,
                    jenis,
                    nik,
                    namaLengkap,
                    email,
                    alamat,
                    telp,
                    tglLahir,
                    jenisKelamin,
                    password,
                    konfirmasiPassword
            );
        });

        dialog.show();
    }

    private void cekNikSudahAda(String nik, EditText edtNik) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADMIN_CEK_NIK,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean exists = obj.optBoolean("exists", false);

                        nikSudahAda = exists;

                        if (exists) {
                            edtNik.setError("NIK sudah digunakan");
                        } else {
                            edtNik.setError(null);
                        }

                    } catch (Exception e) {
                        nikSudahAda = false;
                    }
                },
                error -> {
                    nikSudahAda = false;
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nik", nik);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void cekEmailSudahAda(String email, EditText edtEmail) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADMIN_CEK_EMAIL,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean exists = obj.optBoolean("exists", false);

                        emailSudahAda = exists;

                        if (exists) {
                            edtEmail.setError("Email sudah digunakan");
                        } else {
                            edtEmail.setError(null);
                        }

                    } catch (Exception e) {
                        emailSudahAda = false;
                    }
                },
                error -> {
                    emailSudahAda = false;
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void simpanPetugasDokter(
            AlertDialog dialog,
            String jenis,
            String nik,
            String namaLengkap,
            String email,
            String alamat,
            String telp,
            String tglLahir,
            String jenisKelamin,
            String password,
            String konfirmasiPassword
    ) {
        showLoading();

        android.util.Log.d("STORE_PETUGAS_URL", ApiConfig.ADMIN_PETUGAS_STORE);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADMIN_PETUGAS_STORE,
                response -> {
                    hideLoading();

                    android.util.Log.d("STORE_PETUGAS_RESPONSE", response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean status = jsonObject.optBoolean("status", false);
                        String message = jsonObject.optString("message", "Terjadi kesalahan");

                        if (status) {
                            Toast.makeText(DataPetugas.this, message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            ambilDataPetugas();
                        } else {
                            Toast.makeText(DataPetugas.this, message, Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                DataPetugas.this,
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

                                android.util.Log.e("STORE_PETUGAS_ERROR",
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
                                android.util.Log.e("STORE_PETUGAS_ERROR_PARSE", e.getMessage());
                            }
                        } else {
                            pesan = "Gagal menyimpan data. HTTP " + statusCode;
                        }
                    } else {
                        pesan = "Tidak dapat terhubung ke server";
                        android.util.Log.e("STORE_PETUGAS_ERROR", String.valueOf(error));
                    }

                    Toast.makeText(DataPetugas.this, pesan, Toast.LENGTH_LONG).show();
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

                params.put("nik", nik);
                params.put("nama_lengkap", namaLengkap);
                params.put("email", email);
                params.put("role", jenis);
                params.put("alamat", alamat);
                params.put("telp", telp);
                params.put("tgl_lahir", tglLahir);
                params.put("jenis_kelamin", jenisKelamin);
                params.put("nama", namaLengkap);
                params.put("password", password);
                params.put("password_confirmation", konfirmasiPassword);

                android.util.Log.d("STORE_PETUGAS_PARAMS", params.toString());

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}