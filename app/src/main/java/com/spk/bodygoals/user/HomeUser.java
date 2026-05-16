package com.spk.bodygoals.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import androidx.cardview.widget.CardView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.LoginActivity;
import com.spk.bodygoals.R;
import com.spk.bodygoals.admin.HomeAdmin;
import com.spk.bodygoals.admin.ProfilAdmin;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;
import com.spk.bodygoals.BmiGaugeView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeUser extends AppCompatActivity {
    SessionManager session;
    TextView namaUser, tvBB, tvTB, tvUpdate;
    ImageView profil;
    private BmiGaugeView bmiGaugeUser;
    LinearLayout menuRekomen;
    CardView cardRekomendasiHariIni;
    TextView tvTanggalRekomendasiHariIni, tvTotalRekomendasiHariIni;
    LinearLayout layoutIsiRekomendasiHariIni;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHomeUser), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        Log.d("token", session.getToken());

        namaUser = findViewById(R.id.namaUser);
        namaUser.setText(session.getName());
        profil = findViewById(R.id.ivAvatar);
        tvBB = findViewById(R.id.tv_user_bb);
        tvTB = findViewById(R.id.tv_user_tb);
        bmiGaugeUser = findViewById(R.id.bmiGaugeUser);
        tvUpdate = findViewById(R.id.tv_user_update);
        menuRekomen = findViewById(R.id.menuRekomendasi);

        cardRekomendasiHariIni = findViewById(R.id.cardRekomendasiHariIni);
        tvTanggalRekomendasiHariIni = findViewById(R.id.tvTanggalRekomendasiHariIni);
        tvTotalRekomendasiHariIni = findViewById(R.id.tvTotalRekomendasiHariIni);
        layoutIsiRekomendasiHariIni = findViewById(R.id.layoutIsiRekomendasiHariIni);

        if (cardRekomendasiHariIni != null) {
            cardRekomendasiHariIni.setVisibility(View.GONE);
            cardRekomendasiHariIni.setOnClickListener(v -> {
                startActivity(new Intent(HomeUser.this, Rekomendasi.class));
                finish();
            });
        }

        profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeUser.this, ProfilUser.class);
                startActivity(intent);
                finish();
            }
        });

        getData();
        getRekomendasiHariIni();

        menuRekomen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeUser.this, Rekomendasi.class));
                finish();
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
                    ApiConfig.USER_DASHBOARD,
                    body,
                    response -> {
                        try {
                            boolean status = response.getBoolean("status");

                            if (status) {
                                JSONObject data = response.getJSONObject("data");

                                double bb = data.optDouble("bb", 0);
                                double tb = data.optDouble("tb", 0);

                                tvBB.setText(String.format("%.0f kg", bb));
                                tvTB.setText(String.format("%.0f cm", tb));

                                String tglUkur = data.optString("tgl_ukur", "");

                                if (tglUkur == null || tglUkur.trim().isEmpty() || tglUkur.equalsIgnoreCase("null")) {
                                    tvUpdate.setText("Terakhir diperbarui -");
                                } else {
                                    try {
                                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));

                                        Date date = inputFormat.parse(tglUkur);
                                        tvUpdate.setText("Terakhir diperbarui " + outputFormat.format(date));

                                    } catch (Exception e) {
                                        tvUpdate.setText("Terakhir diperbarui " + tglUkur);
                                    }
                                }

                                if (bb > 0 && tb > 0) {
                                    double bmi = hitungBMI(bb, tb);
                                    setupBmiGauge(bmi);
                                } else {
                                    setupBmiGauge(0);
                                }

                            } else {
                                tvBB.setText("0 kg");
                                tvTB.setText("0 cm");

                                setupBmiGauge(0);
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

        Intent intent = new Intent(HomeUser.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void getRekomendasiHariIni() {
        String token = session.getToken();
        String email = session.getEmail();

        if (token == null || token.trim().isEmpty()) {
            sembunyikanRekomendasiHariIni();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.REKOMENDASI_STATUS,
                    body,
                    response -> {
                        boolean status = response.optBoolean("status", false);

                        if (!status || !response.optBoolean("sudah_rekomendasi", false)) {
                            sembunyikanRekomendasiHariIni();
                            return;
                        }

                        JSONArray rekomendasi7Hari = response.optJSONArray("rekomendasi_7_hari");

                        if (rekomendasi7Hari == null || rekomendasi7Hari.length() == 0) {
                            sembunyikanRekomendasiHariIni();
                            return;
                        }

                        tampilkanRekomendasiHariIni(rekomendasi7Hari);
                    },
                    error -> sembunyikanRekomendasiHariIni()
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
            sembunyikanRekomendasiHariIni();
        }
    }

    private void tampilkanRekomendasiHariIni(JSONArray rekomendasi7Hari) {
        String tanggalHariIni = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        JSONObject hariIniObj = null;
        JSONObject opsiUtama = null;

        for (int i = 0; i < rekomendasi7Hari.length(); i++) {
            JSONObject hari = rekomendasi7Hari.optJSONObject(i);

            if (hari == null) {
                continue;
            }

            String tanggal = hari.optString("tanggal", "");

            if (!tanggalHariIni.equals(tanggal)) {
                continue;
            }

            JSONArray rekomendasi = hari.optJSONArray("rekomendasi");

            if (rekomendasi == null || rekomendasi.length() == 0) {
                continue;
            }

            hariIniObj = hari;
            opsiUtama = rekomendasi.optJSONObject(0);
            break;
        }

        if (hariIniObj == null || opsiUtama == null) {
            sembunyikanRekomendasiHariIni();
            return;
        }

        if (layoutIsiRekomendasiHariIni != null) {
            layoutIsiRekomendasiHariIni.removeAllViews();
        }

        int hariKe = hariIniObj.optInt("hari_ke", 0);
        String tanggal = hariIniObj.optString("tanggal", tanggalHariIni);

        if (cardRekomendasiHariIni != null) {
            cardRekomendasiHariIni.setVisibility(View.VISIBLE);
        }

        if (tvTanggalRekomendasiHariIni != null) {
            tvTanggalRekomendasiHariIni.setText("Hari " + hariKe + " • " + formatTanggalIndonesia(tanggal));
        }

        tambahMenuGiziHariIni(
                layoutIsiRekomendasiHariIni,
                "🍳",
                "Sarapan",
                opsiUtama,
                "sarapan"
        );

        tambahMenuGiziHariIni(
                layoutIsiRekomendasiHariIni,
                "🍱",
                "Makan Siang",
                opsiUtama,
                "makan_siang"
        );

        tambahMenuGiziHariIni(
                layoutIsiRekomendasiHariIni,
                "🍽️",
                "Makan Malam",
                opsiUtama,
                "makan_malam"
        );

        tambahMenuGiziHariIni(
                layoutIsiRekomendasiHariIni,
                "🍎",
                "Cemilan",
                opsiUtama,
                "snack"
        );

        if (tvTotalRekomendasiHariIni != null) {
            tvTotalRekomendasiHariIni.setText(formatKkal(opsiUtama.optDouble("total_kkal", 0)) + " kkal");
        }
    }

    private void tambahMenuGiziHariIni(LinearLayout parent, String icon, String label, JSONObject data, String prefix) {
        if (parent == null || data == null) {
            return;
        }

        String namaMenu = data.optString(prefix, "-");
        double kkal = data.optDouble(prefix + "_kkal", 0);
        double protein = data.optDouble(prefix + "_protein", 0);
        double karbohidrat = data.optDouble(prefix + "_karbohidrat", 0);
        double lemak = data.optDouble(prefix + "_lemak", 0);
        double serat = data.optDouble(prefix + "_serat", 0);
        double gula = data.optDouble(prefix + "_gula", 0);
        double natrium = data.optDouble(prefix + "_natrium", 0);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        box.setBackground(buatRoundDrawable(0xFFF9FAFB, dpToPx(18)));

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        boxParams.setMargins(0, 0, 0, dpToPx(10));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvIcon = new TextView(this);
        tvIcon.setText(icon);
        tvIcon.setTextSize(21);
        tvIcon.setGravity(android.view.Gravity.CENTER);

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(38),
                dpToPx(38)
        );

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setPadding(dpToPx(10), 0, dpToPx(8), 0);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(13);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setTextColor(getResources().getColor(R.color.text_primary));

        TextView tvMenu = new TextView(this);
        tvMenu.setText(namaMenu == null || namaMenu.trim().isEmpty() ? "-" : namaMenu);
        tvMenu.setTextSize(12);
        tvMenu.setTextColor(getResources().getColor(R.color.text_secondary));
        tvMenu.setPadding(0, dpToPx(3), 0, 0);
        tvMenu.setMaxLines(2);

        textBox.addView(tvLabel);
        textBox.addView(tvMenu);

        TextView tvKkal = new TextView(this);
        tvKkal.setText(formatKkal(kkal) + "\nkkal");
        tvKkal.setTextSize(10);
        tvKkal.setTypeface(null, Typeface.BOLD);
        tvKkal.setGravity(android.view.Gravity.CENTER);
        tvKkal.setTextColor(getResources().getColor(R.color.user_primary));
        tvKkal.setPadding(dpToPx(9), dpToPx(6), dpToPx(9), dpToPx(6));
        tvKkal.setBackground(buatRoundDrawable(0xFFEFF6FF, dpToPx(14)));

        topRow.addView(tvIcon, iconParams);
        topRow.addView(textBox, textParams);
        topRow.addView(tvKkal);

        box.addView(topRow);

        TextView tvRincian = new TextView(this);
        tvRincian.setText("Rincian gizi");
        tvRincian.setTextSize(10);
        tvRincian.setTypeface(null, Typeface.BOLD);
        tvRincian.setTextColor(getResources().getColor(R.color.text_primary));
        tvRincian.setPadding(0, dpToPx(10), 0, dpToPx(7));
        box.addView(tvRincian);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setWeightSum(3);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setWeightSum(3);
        row2.setPadding(0, dpToPx(7), 0, 0);

        tambahChipGiziKecil(row1, "Protein", formatSatuAngka(protein) + " g");
        tambahChipGiziKecil(row1, "Karbo", formatSatuAngka(karbohidrat) + " g");
        tambahChipGiziKecil(row1, "Lemak", formatSatuAngka(lemak) + " g");

        tambahChipGiziKecil(row2, "Serat", formatSatuAngka(serat) + " g");
        tambahChipGiziKecil(row2, "Gula", formatSatuAngka(gula) + " g");
        tambahChipGiziKecil(row2, "Natrium", formatSatuAngka(natrium) + " mg");

        box.addView(row1);
        box.addView(row2);

        parent.addView(box, boxParams);
    }

    private void tambahChipGiziKecil(LinearLayout parent, String label, String value) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.VERTICAL);
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setPadding(dpToPx(6), dpToPx(7), dpToPx(6), dpToPx(7));
        chip.setBackground(buatRoundDrawable(0xFFFFFFFF, dpToPx(12)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        params.setMargins(dpToPx(3), 0, dpToPx(3), 0);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(9);
        tvLabel.setGravity(android.view.Gravity.CENTER);
        tvLabel.setTextColor(getResources().getColor(R.color.text_secondary));

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextSize(10);
        tvValue.setTypeface(null, Typeface.BOLD);
        tvValue.setGravity(android.view.Gravity.CENTER);
        tvValue.setTextColor(getResources().getColor(R.color.text_primary));
        tvValue.setPadding(0, dpToPx(2), 0, 0);

        chip.addView(tvLabel);
        chip.addView(tvValue);

        parent.addView(chip, params);
    }

    private void sembunyikanRekomendasiHariIni() {
        if (cardRekomendasiHariIni != null) {
            cardRekomendasiHariIni.setVisibility(View.GONE);
        }

        if (layoutIsiRekomendasiHariIni != null) {
            layoutIsiRekomendasiHariIni.removeAllViews();
        }
    }

    private GradientDrawable buatRoundDrawable(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private String formatKkal(double value) {
        return String.format(Locale.US, "%.0f", value);
    }

    private String formatSatuAngka(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private String formatTanggalIndonesia(String tanggal) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));

            Date date = inputFormat.parse(tanggal);

            if (date == null) {
                return tanggal;
            }

            return outputFormat.format(date);

        } catch (Exception e) {
            return tanggal;
        }
    }

    private void setupBmiGauge(double bmi) {
        if (bmiGaugeUser != null) {
            bmiGaugeUser.setBmi((float) bmi);
        }
    }

    private double hitungBMI(double bb, double tbCm) {
        if (bb <= 0 || tbCm <= 0) {
            return 0;
        }

        double tbMeter = tbCm / 100.0;
        return bb / (tbMeter * tbMeter);
    }

    private android.graphics.drawable.Drawable getSelectableItemBackground() {
        android.util.TypedValue outValue = new android.util.TypedValue();

        getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue,
                true
        );

        return getResources().getDrawable(outValue.resourceId, getTheme());
    }
}