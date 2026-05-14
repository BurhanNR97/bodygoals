package com.spk.bodygoals.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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

public class DetailRulePenyakit extends AppCompatActivity {
    private String idRule = "";

    private TextView tvNamaPenyakit, tvKataKunci, tvAnjuran, tvHindari, tvDisarankan;
    private TextView tvFokusPenalti, tvFaktorGula, tvFaktorKarbohidrat, tvFaktorLemak;
    private TextView tvFaktorProtein, tvFaktorSerat, tvBatasNatrium, tvTingkatRisiko, tvStatus;
    private TextView btnUbah, btnHapus;
    private ImageView kembali;

    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_rule_penyakit);
        View v = findViewById(R.id.mainDetailRulePenyakit);
        final int initialPaddingLeft = v.getPaddingLeft();
        final int initialPaddingTop = v.getPaddingTop();
        final int initialPaddingRight = v.getPaddingRight();
        final int initialPaddingBottom = v.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDetailRulePenyakit), (view, insets) -> {
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

        idRule = getIntent().getStringExtra("id");

        kembali = findViewById(R.id.btnBackDetailRulesPenyakit);

        tvNamaPenyakit = findViewById(R.id.tvNamaPenyakit);
        tvKataKunci = findViewById(R.id.tvKataKunci);
        tvAnjuran = findViewById(R.id.tvAnjuran);
        tvHindari = findViewById(R.id.tvHindari);
        tvDisarankan = findViewById(R.id.tvDisarankan);
        tvFokusPenalti = findViewById(R.id.tvFokusPenalti);
        tvFaktorGula = findViewById(R.id.tvFaktorGula);
        tvFaktorKarbohidrat = findViewById(R.id.tvFaktorKarbohidrat);
        tvFaktorLemak = findViewById(R.id.tvFaktorLemak);
        tvFaktorProtein = findViewById(R.id.tvFaktorProtein);
        tvFaktorSerat = findViewById(R.id.tvFaktorSerat);
        tvBatasNatrium = findViewById(R.id.tvBatasNatrium);
        tvTingkatRisiko = findViewById(R.id.tvTingkatRisiko);
        tvStatus = findViewById(R.id.tvStatus);

        btnUbah = findViewById(R.id.btnUbahRule);
        btnHapus = findViewById(R.id.btnHapusRule);

        kembali.setOnClickListener(view -> finish());

        btnUbah.setOnClickListener(view -> {
            Toast.makeText(this, "Fitur ubah nanti dibuat", Toast.LENGTH_SHORT).show();
        });

        btnHapus.setOnClickListener(view -> konfirmasiHapus());

        ambilDetail();
    }

    private void ambilDetail() {
        showLoading();

        String url = ApiConfig.ADMIN_RULES_PENYAKIT_DETAIL + idRule;

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
                                tvNamaPenyakit.setText(data.optString("nama_penyakit", "-"));
                                tvKataKunci.setText("Kata Kunci:\n" + data.optString("kata_kunci", "-"));
                                tvAnjuran.setText("Anjuran:\n" + data.optString("anjuran", "-"));
                                tvHindari.setText("Hindari:\n" + data.optString("hindari", "-"));
                                tvDisarankan.setText("Disarankan:\n" + data.optString("disarankan", "-"));
                                tvFokusPenalti.setText("Fokus Penalti:\n" + data.optString("fokus_penalti", "-"));
                                tvFaktorGula.setText("Faktor Gula:\n" + data.optString("faktor_gula", "-"));
                                tvFaktorKarbohidrat.setText("Faktor Karbohidrat:\n" + data.optString("faktor_karbohidrat", "-"));
                                tvFaktorLemak.setText("Faktor Lemak:\n" + data.optString("faktor_lemak", "-"));
                                tvFaktorProtein.setText("Faktor Protein:\n" + data.optString("faktor_protein", "-"));
                                tvFaktorSerat.setText("Faktor Serat:\n" + data.optString("faktor_serat", "-"));
                                tvBatasNatrium.setText("Batas Natrium Mg:\n" + data.optString("batas_natrium_mg", "-"));
                                tvTingkatRisiko.setText("Tingkat Risiko:\n" + data.optString("tingkat_risiko", "-"));
                                tvStatus.setText("Status:\n" + data.optString("status", "-"));
                            }
                        } else {
                            Toast.makeText(this, response.optString("message", "Data tidak ditemukan"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Format data tidak sesuai: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        hideLoading();
                    }
                },
                error -> {
                    hideLoading();
                    Toast.makeText(this, "Gagal mengambil detail rules penyakit", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void konfirmasiHapus() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Data")
                .setMessage("Yakin ingin menghapus rules penyakit ini?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (dialog, which) -> hapusData())
                .show();
    }

    private void hapusData() {
        showLoading();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADMIN_RULES_PENYAKIT_HAPUS,
                response -> {
                    hideLoading();

                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean status = obj.optBoolean("status", false);
                        String message = obj.optString("message", "Terjadi kesalahan");

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if (status) {
                            finish();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Response tidak valid", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    hideLoading();
                    Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", idRule);
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
}