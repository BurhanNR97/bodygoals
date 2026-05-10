package com.spk.bodygoals;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.spk.bodygoals.service.ApiConfig;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Registrasi extends AppCompatActivity {

    TextInputLayout inNik, inNama, inTglLahir, inTelp, inAlamat, inEmail, inPassword, inKonfirPassword;
    TextInputEditText edNik, edNama, edTglLahir, edTelp, edAlamat, edEmail, edPassword, edKonfirPassword;
    RadioGroup rgJenisKelamin;
    RadioButton rbLaki, rbPerempuan;
    AppCompatButton btnRegister;
    TextView tvKeLogin;

    Calendar calendar = Calendar.getInstance();
    String tglLahirDb = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);
        View view = findViewById(R.id.mainRegistrasi);
        final int initialPaddingLeft = view.getPaddingLeft();
        final int initialPaddingTop = view.getPaddingTop();
        final int initialPaddingRight = view.getPaddingRight();
        final int initialPaddingBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRegistrasi), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    initialPaddingLeft + systemBars.left,
                    initialPaddingTop + systemBars.top,
                    initialPaddingRight + systemBars.right,
                    initialPaddingBottom + systemBars.bottom
            );
            return insets;
        });

        initView();

        edTglLahir.setOnClickListener(v -> showDatePicker());

        btnRegister.setOnClickListener(v -> prosesRegister());

        tvKeLogin.setOnClickListener(v -> {
            startActivity(new Intent(Registrasi.this, LoginActivity.class));
            finish();
        });
    }

    private void initView() {
        inNik = findViewById(R.id.inNik);
        inNama = findViewById(R.id.inNama);
        inTglLahir = findViewById(R.id.inTglLahir);
        inTelp = findViewById(R.id.inTelp);
        inAlamat = findViewById(R.id.inAlamat);
        inEmail = findViewById(R.id.inEmail);
        inPassword = findViewById(R.id.inPassword);
        inKonfirPassword = findViewById(R.id.inKonfirPassword);

        edNik = findViewById(R.id.edNik);
        edNama = findViewById(R.id.edNama);
        edTglLahir = findViewById(R.id.edTglLahir);
        edTelp = findViewById(R.id.edTelp);
        edAlamat = findViewById(R.id.edAlamat);
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        edKonfirPassword = findViewById(R.id.edKonfirPassword);

        rgJenisKelamin = findViewById(R.id.rgJenisKelamin);
        rbLaki = findViewById(R.id.rbLaki);
        rbPerempuan = findViewById(R.id.rbPerempuan);

        btnRegister = findViewById(R.id.btnDaftar);
        tvKeLogin = findViewById(R.id.tvKeLogin);
    }

    private void resetError() {
        inNik.setError(null);
        inNama.setError(null);
        inTglLahir.setError(null);
        inTelp.setError(null);
        inAlamat.setError(null);
        inEmail.setError(null);
        inPassword.setError(null);
        inKonfirPassword.setError(null);

        inNik.setErrorEnabled(false);
        inNama.setErrorEnabled(false);
        inTglLahir.setErrorEnabled(false);
        inTelp.setErrorEnabled(false);
        inAlamat.setErrorEnabled(false);
        inEmail.setErrorEnabled(false);
        inPassword.setErrorEnabled(false);
        inKonfirPassword.setErrorEnabled(false);
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Registrasi.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    SimpleDateFormat formatView = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
                    SimpleDateFormat formatDb = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                    edTglLahir.setText(formatView.format(calendar.getTime()));
                    tglLahirDb = formatDb.format(calendar.getTime());
                },
                year,
                month,
                day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void prosesRegister() {
        resetError();

        String nik = Objects.requireNonNull(edNik.getText()).toString().trim();
        String nama = Objects.requireNonNull(edNama.getText()).toString().trim();
        String telp = Objects.requireNonNull(edTelp.getText()).toString().trim();
        String alamat = Objects.requireNonNull(edAlamat.getText()).toString().trim();
        String email = Objects.requireNonNull(edEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(edPassword.getText()).toString().trim();
        String konfirPassword = Objects.requireNonNull(edKonfirPassword.getText()).toString().trim();

        int selectedGenderId = rgJenisKelamin.getCheckedRadioButtonId();

        if (nik.isEmpty()) {
            inNik.setErrorEnabled(true);
            inNik.setError("NIK wajib diisi");
            edNik.requestFocus();
            return;
        }

        if (nik.length() < 16) {
            inNik.setErrorEnabled(true);
            inNik.setError("NIK harus 16 digit");
            edNik.requestFocus();
            return;
        }

        if (nama.isEmpty()) {
            inNama.setErrorEnabled(true);
            inNama.setError("Nama wajib diisi");
            edNama.requestFocus();
            return;
        }

        if (selectedGenderId == -1) {
            Toast.makeText(this, "Jenis kelamin wajib dipilih", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tglLahirDb.isEmpty()) {
            inTglLahir.setErrorEnabled(true);
            inTglLahir.setError("Tanggal lahir wajib dipilih");
            edTglLahir.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            inEmail.setErrorEnabled(true);
            inEmail.setError("Email wajib diisi");
            edEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inEmail.setErrorEnabled(true);
            inEmail.setError("Format email tidak valid");
            edEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            inPassword.setErrorEnabled(true);
            inPassword.setError("Password wajib diisi");
            edPassword.requestFocus();
            return;
        }

        if (password.length() < 5) {
            inPassword.setErrorEnabled(true);
            inPassword.setError("Password minimal 5 karakter");
            edPassword.requestFocus();
            return;
        }

        if (konfirPassword.isEmpty()) {
            inKonfirPassword.setErrorEnabled(true);
            inKonfirPassword.setError("Konfirmasi password wajib diisi");
            edKonfirPassword.requestFocus();
            return;
        }

        if (!password.equals(konfirPassword)) {
            inKonfirPassword.setErrorEnabled(true);
            inKonfirPassword.setError("Konfirmasi password tidak sama");
            edKonfirPassword.requestFocus();
            return;
        }

        String jenisKelamin = selectedGenderId == R.id.rbLaki ? "L" : "P";

        registerKeServer(nik, nama, jenisKelamin, tglLahirDb, telp, alamat, email, password, konfirPassword);
    }

    private void registerKeServer(
            String nik,
            String nama,
            String jenisKelamin,
            String tglLahir,
            String telp,
            String alamat,
            String email,
            String password,
            String konfirPassword
    ) {
        String url = ApiConfig.getUrl("register");

        btnRegister.setEnabled(false);
        btnRegister.setText("Menyimpan...");

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Daftar");

                    try {
                        JSONObject json = new JSONObject(response);
                        boolean status = json.getBoolean("status");

                        if (status) {
                            Toast.makeText(this, "Registrasi berhasil, silakan login", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Registrasi.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, json.optString("message", "Registrasi gagal"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Response error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Daftar");

                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data);

                        try {
                            JSONObject json = new JSONObject(body);

                            if (error.networkResponse.statusCode == 422) {
                                tampilkanErrorValidasi(json);
                            } else {
                                Toast.makeText(this, json.optString("message", "Registrasi gagal"), Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + body, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Tidak dapat terhubung ke server", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("nik", nik);
                params.put("nama", nama);
                params.put("jenis_kelamin", jenisKelamin);
                params.put("tgl_lahir", tglLahir);
                params.put("telp", telp);
                params.put("alamat", alamat);
                params.put("email", email);
                params.put("password", password);
                params.put("password_confirmation", konfirPassword);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void tampilkanErrorValidasi(JSONObject json) {
        try {
            JSONObject errors = json.getJSONObject("errors");

            if (errors.has("nik")) {
                inNik.setErrorEnabled(true);
                inNik.setError(errors.getJSONArray("nik").getString(0));
                edNik.requestFocus();
            }

            if (errors.has("nama")) {
                inNama.setErrorEnabled(true);
                inNama.setError(errors.getJSONArray("nama").getString(0));
                edNama.requestFocus();
            }

            if (errors.has("email")) {
                inEmail.setErrorEnabled(true);
                inEmail.setError(errors.getJSONArray("email").getString(0));
                edEmail.requestFocus();
            }

            if (errors.has("jenis_kelamin")) {
                Toast.makeText(this, errors.getJSONArray("jenis_kelamin").getString(0), Toast.LENGTH_SHORT).show();
            }

            if (errors.has("tgl_lahir")) {
                inTglLahir.setErrorEnabled(true);
                inTglLahir.setError(errors.getJSONArray("tgl_lahir").getString(0));
                edTglLahir.requestFocus();
            }

            if (errors.has("password")) {
                inPassword.setErrorEnabled(true);
                inPassword.setError(errors.getJSONArray("password").getString(0));
                edPassword.requestFocus();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Validasi gagal", Toast.LENGTH_SHORT).show();
        }
    }
}