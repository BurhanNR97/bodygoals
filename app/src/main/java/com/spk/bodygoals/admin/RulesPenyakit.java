package com.spk.bodygoals.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
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
import com.spk.bodygoals.adapter.adpRulePenyakit;
import com.spk.bodygoals.dokter.HomeDokter;
import com.spk.bodygoals.model.mRulePenyakit;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class RulesPenyakit extends AppCompatActivity {
    private RecyclerView rvRulesPenyakit;
    private EditText edtCari;
    private TextView tvJumlah, tvKosong;
    private ImageView kembali;

    private adpRulePenyakit adapter;
    private ArrayList<mRulePenyakit> listFull;
    private ArrayList<mRulePenyakit> listFilter;
    private AlertDialog loadingDialog;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rules_penyakit);
        View v = findViewById(R.id.mainRulesPenyakit);
        final int initialPaddingLeft = v.getPaddingLeft();
        final int initialPaddingTop = v.getPaddingTop();
        final int initialPaddingRight = v.getPaddingRight();
        final int initialPaddingBottom = v.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRulesPenyakit), (view, insets) -> {
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

        rvRulesPenyakit = findViewById(R.id.rvRulesPenyakit);
        edtCari = findViewById(R.id.edtCariRulesPenyakit);
        tvJumlah = findViewById(R.id.tvJumlahRulesPenyakit);
        tvKosong = findViewById(R.id.tvKosongRulesPenyakit);
        kembali = findViewById(R.id.btnBackRulesPenyakit);

        session = new SessionManager(this);

        adapter = new adpRulePenyakit(this, listFilter);
        rvRulesPenyakit.setLayoutManager(new LinearLayoutManager(this));
        rvRulesPenyakit.setAdapter(adapter);

        kembali.setOnClickListener(view -> {
            String role = session.getRole().toLowerCase();

            if (role.equals("admin")) {
                startActivity(new Intent(RulesPenyakit.this, HomeAdmin.class));
            } else
            if (role.equals("dokter")) {
                startActivity(new Intent(RulesPenyakit.this, HomeDokter.class));
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

        ambilDataRulesPenyakit();
    }

    private void filterData() {
        String keyword = edtCari.getText().toString().trim().toLowerCase();

        listFilter.clear();

        for (mRulePenyakit item : listFull) {
            String nama = item.getNamaPenyakit() == null ? "" : item.getNamaPenyakit().toLowerCase();
            String kataKunci = item.getKataKunci() == null ? "" : item.getKataKunci().toLowerCase();

            if (nama.contains(keyword) || kataKunci.contains(keyword)) {
                listFilter.add(item);
            }
        }

        adapter.notifyDataSetChanged();

        if (listFilter.isEmpty()) {
            tvKosong.setVisibility(android.view.View.VISIBLE);
            rvRulesPenyakit.setVisibility(android.view.View.GONE);
        } else {
            tvKosong.setVisibility(android.view.View.GONE);
            rvRulesPenyakit.setVisibility(android.view.View.VISIBLE);
        }

        tvJumlah.setText("Menampilkan " + listFilter.size() + " data rules penyakit");
    }

    private void ambilDataRulesPenyakit() {
        showLoading();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.ADMIN_RULES_PENYAKIT_FETCH,
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

                                    mRulePenyakit model = new mRulePenyakit();

                                    model.setId(obj.optString("id", ""));
                                    model.setNamaPenyakit(obj.optString("nama_penyakit", ""));
                                    model.setKataKunci(obj.optString("kata_kunci", ""));
                                    model.setAnjuran(obj.optString("anjuran", ""));
                                    model.setHindari(obj.optString("hindari", ""));
                                    model.setDisarankan(obj.optString("disarankan", ""));
                                    model.setFokusPenalti(obj.optString("fokus_penalti", ""));
                                    model.setFaktorGula(obj.optString("faktor_gula", ""));
                                    model.setFaktorKarbohidrat(obj.optString("faktor_karbohidrat", ""));
                                    model.setFaktorLemak(obj.optString("faktor_lemak", ""));
                                    model.setFaktorProtein(obj.optString("faktor_protein", ""));
                                    model.setFaktorSerat(obj.optString("faktor_serat", ""));
                                    model.setBatasNatriumMg(obj.optString("batas_natrium_mg", ""));
                                    model.setTingkatRisiko(obj.optString("tingkat_risiko", ""));
                                    model.setStatus(obj.optString("status", ""));

                                    listFull.add(model);
                                }
                            }

                            filterData();

                        } else {
                            listFull.clear();
                            filterData();

                            tvKosong.setText(response.optString("message", "Data rules penyakit belum tersedia"));
                            tvJumlah.setText("Menampilkan 0 data rules penyakit");
                            tvKosong.setVisibility(android.view.View.VISIBLE);
                            rvRulesPenyakit.setVisibility(android.view.View.GONE);
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                RulesPenyakit.this,
                                "Format data tidak sesuai: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    } finally {
                        hideLoading();
                    }
                },
                error -> {
                    hideLoading();

                    String pesan = "Gagal mengambil data rules penyakit";

                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;

                        if (error.networkResponse.data != null) {
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");
                                android.util.Log.e("FETCH_RULES_PENYAKIT", "HTTP " + code + " BODY: " + body);

                                JSONObject obj = new JSONObject(body);
                                pesan = obj.optString("message", pesan);
                            } catch (Exception e) {
                                pesan = "Gagal mengambil data rules penyakit. HTTP " + code;
                            }
                        } else {
                            pesan = "Gagal mengambil data rules penyakit. HTTP " + code;
                        }
                    }

                    Toast.makeText(RulesPenyakit.this, pesan, Toast.LENGTH_LONG).show();
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