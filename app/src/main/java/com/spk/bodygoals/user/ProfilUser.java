package com.spk.bodygoals.user;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.LoginActivity;
import com.spk.bodygoals.R;
import com.spk.bodygoals.admin.ProfilAdmin;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfilUser extends AppCompatActivity {
    TextView tvNama, tvEmail, tvAlamat, tvTglLahir, tvTelp, tvBB, tvTB, tvJK;
    SessionManager session;
    ImageView kembali;
    LinearLayout btnEditProfil;
    String profilNama = "";
    String profilAlamat = "";
    String profilTelp = "";
    String profilTglLahir = "";
    String profilJenisKelamin = "";
    String profilBB = "";
    String profilTB = "";
    AppCompatButton keluar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profil_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainProfilUser), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvNama = findViewById(R.id.tvNamaUser);
        tvEmail = findViewById(R.id.tvEmailUser);

        tvBB = findViewById(R.id.profile_user_bb);
        tvTB = findViewById(R.id.profile_user_tb);
        tvJK = findViewById(R.id.profile_user_jk);
        tvAlamat = findViewById(R.id.profile_user_alamat);
        tvTelp = findViewById(R.id.profile_user_telp);
        tvTglLahir = findViewById(R.id.profile_user_tglLahir);
        btnEditProfil = findViewById(R.id.menuEditProfilUser);

        session = new SessionManager(this);

        tvNama.setText(session.getName());
        tvEmail.setText(session.getEmail());

        kembali = findViewById(R.id.btnBackProfilUser);
        kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfilUser.this, HomeUser.class));
                finish();
            }
        });

        getData();

        btnEditProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfilDialog();
            }
        });

        keluar = findViewById(R.id.btnLogoutUser);
        keluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signout();
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
                    ApiConfig.USER_PROFIL,
                    body,
                    response -> {
                        try {
                            boolean status = response.getBoolean("status");

                            if (status) {
                                JSONObject data = response.getJSONObject("data");

                                String bb = data.isNull("bb") ? "-" : data.optString("bb");
                                String tb = data.isNull("tb") ? "-" : data.optString("tb");
                                String telp = data.isNull("telp") ? "-" : data.optString("telp");
                                String alamat = data.isNull("alamat") ? "-" : data.optString("alamat");
                                String tglLahir = data.isNull("tgl_lahir") ? "-" : convTgl(data.optString("tgl_lahir"));
                                String jk = data.isNull("jk") ? "-" : data.optString("jk");

                                tvBB.setText((int) Float.parseFloat(bb) + " kg");
                                tvTB.setText((int) Float.parseFloat(tb) + " cm");
                                tvTelp.setText(telp);
                                tvAlamat.setText(alamat);
                                tvTglLahir.setText(tglLahir);
                                tvJK.setText(jk);

                                profilNama = safeValue(data, "nama");
                                profilAlamat = safeValue(data, "alamat");
                                profilTelp = safeValue(data, "telp");
                                profilTglLahir = safeValue(data, "tgl_lahir");
                                profilBB = safeValue(data, "bb");
                                profilTB = safeValue(data, "tb");

                                String jkText = safeValue(data, "jk");

                                if (jkText.equalsIgnoreCase("Laki-laki")) {
                                    profilJenisKelamin = "L";
                                } else if (jkText.equalsIgnoreCase("Perempuan")) {
                                    profilJenisKelamin = "P";
                                } else {
                                    profilJenisKelamin = "";
                                }
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

        Intent intent = new Intent(ProfilUser.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String convTgl(String tanggal) {
        try {
            if (tanggal == null || tanggal.equals("null") || tanggal.trim().isEmpty()) {
                return "-";
            }

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));

            return outputFormat.format(inputFormat.parse(tanggal));

        } catch (Exception e) {
            return tanggal;
        }
    }

    private void showEditProfilDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_profil_user);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText edNama = dialog.findViewById(R.id.edEditNama);
        EditText edTglLahir = dialog.findViewById(R.id.edEditTglLahir);
        EditText edTelp = dialog.findViewById(R.id.edEditTelp);
        EditText edAlamat = dialog.findViewById(R.id.edEditAlamat);
        EditText edTB = dialog.findViewById(R.id.edEditTB);
        EditText edBB = dialog.findViewById(R.id.edEditBB);

        RadioGroup rgJK = dialog.findViewById(R.id.rgEditJenisKelamin);
        RadioButton rbLaki = dialog.findViewById(R.id.rbEditLaki);
        RadioButton rbPerempuan = dialog.findViewById(R.id.rbEditPerempuan);

        AppCompatButton btnSimpan = dialog.findViewById(R.id.btnSimpanEditProfil);
        AppCompatButton btnBatal = dialog.findViewById(R.id.btnBatalEditProfil);

        // Isi data kalau ada. Kalau null/kosong, biarkan kosong.
        if (!profilNama.isEmpty()) {
            edNama.setText(profilNama);
        }

        if (!profilTelp.isEmpty()) {
            edTelp.setText(profilTelp);
        }

        if (!profilAlamat.isEmpty()) {
            edAlamat.setText(profilAlamat);
        }

        if (!profilBB.isEmpty()) {
            edBB.setText(profilBB);
        }

        if (!profilTB.isEmpty()) {
            edTB.setText(profilTB);
        }

        if (!profilTglLahir.isEmpty()) {
            edTglLahir.setText(convTgl(profilTglLahir));
        }

        if (profilJenisKelamin.equals("L")) {
            rbLaki.setChecked(true);
        } else if (profilJenisKelamin.equals("P")) {
            rbPerempuan.setChecked(true);
        }

        final String[] tglLahirDb = {profilTglLahir};

        edTglLahir.setFocusable(false);
        edTglLahir.setClickable(true);

        edTglLahir.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            if (!tglLahirDb[0].isEmpty()) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date date = inputFormat.parse(tglLahirDb[0]);

                    if (date != null) {
                        calendar.setTime(date);
                    }
                } catch (Exception ignored) {
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ProfilUser.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat viewFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
                        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                        edTglLahir.setText(viewFormat.format(calendar.getTime()));
                        tglLahirDb[0] = dbFormat.format(calendar.getTime());
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            String nama = edNama.getText().toString().trim();
            String telp = edTelp.getText().toString().trim();
            String alamat = edAlamat.getText().toString().trim();
            String bb = edBB.getText().toString().trim();
            String tb = edTB.getText().toString().trim();

            int selectedId = rgJK.getCheckedRadioButtonId();

            if (nama.isEmpty()) {
                edNama.setError("Nama wajib diisi");
                edNama.requestFocus();
                return;
            }

            if (tglLahirDb[0].isEmpty()) {
                edTglLahir.setError("Tanggal lahir wajib dipilih");
                edTglLahir.requestFocus();
                return;
            }

            if (selectedId == -1) {
                Toast.makeText(this, "Jenis kelamin wajib dipilih", Toast.LENGTH_SHORT).show();
                return;
            }

            String jk = selectedId == R.id.rbEditLaki ? "L" : "P";

            updateProfil(dialog, nama, alamat, telp, tglLahirDb[0], jk, tb, bb);
        });

        dialog.show();

        // Ini bagian penting agar popup tidak kecil
        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = (int) (metrics.widthPixels * 0.90);

            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void updateProfil(Dialog dialog, String nama, String alamat, String telp, String tglLahir, String jk, String tb, String bb) {
        String token = session.getToken();
        String email = session.getEmail();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.USER_PROFIL_UPDATE,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean status = json.optBoolean("status", false);

                        if (status) {
                            Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();

                            profilNama = nama;
                            profilAlamat = alamat;
                            profilTelp = telp;
                            profilTglLahir = tglLahir;
                            profilJenisKelamin = jk;

                            tvNama.setText(nama);
                            tvEmail.setText(email);
                            tvAlamat.setText(alamat.isEmpty() ? "-" : alamat);
                            tvTelp.setText(telp.isEmpty() ? "-" : telp);
                            tvTglLahir.setText(convTgl(tglLahir));
                            tvJK.setText(jk.equals("L") ? "Laki-laki" : "Perempuan");

                            getData();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, json.optString("message", "Gagal memperbarui profil"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Response tidak sesuai", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data);
                        Toast.makeText(this, "Error: " + body, Toast.LENGTH_LONG).show();
                        Log.e("SQL ku", body.toString());
                    } else {
                        Toast.makeText(this, "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("nama", nama);
                params.put("alamat", alamat);
                params.put("telp", telp);
                params.put("tgl_lahir", tglLahir);
                params.put("jenis_kelamin", jk);
                params.put("tb", tb);
                params.put("bb", bb);
                return params;
            }

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

    private String safeValue(JSONObject data, String key) {
        if (data == null || data.isNull(key)) {
            return "";
        }

        String value = data.optString(key, "");

        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }
    private void signout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfilUser.this, R.style.AlertDialogTheme);
        builder.setCancelable(false);
        View popup = LayoutInflater.from(ProfilUser.this).inflate(R.layout.dialog_keluar, (ConstraintLayout) findViewById(R.id.layoutDialogContainerKeluar));
        builder.setView(popup);
        final AlertDialog alertDialog = builder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

        popup.findViewById(R.id.btnYaKeluar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
                alertDialog.cancel();
                finish();
            }
        });

        popup.findViewById(R.id.btnTidakKeluar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    private void logout() {
        String url = ApiConfig.getUrl("logout");

        SessionManager session = new SessionManager(this);
        String token = session.getToken();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    session.logout();

                    Toast.makeText(ProfilUser.this, "Logout berhasil", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ProfilUser.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    // Kalau token sudah expired atau server error, tetap hapus session lokal
                    session.logout();

                    Toast.makeText(ProfilUser.this, "Sesi dihapus", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ProfilUser.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}