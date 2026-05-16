package com.spk.bodygoals.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.view.Gravity;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.Dialog;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.appcompat.widget.AppCompatButton;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.R;
import com.spk.bodygoals.admin.ProfilAdmin;
import com.spk.bodygoals.model.MakananItem;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;
import android.widget.HorizontalScrollView;
import android.graphics.Typeface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Rekomendasi extends AppCompatActivity {

    private SessionManager session;

    private ImageView btnBackRekomendasi;

    private TextView tvStatusInputHarian;
    private TextView tvSarapanHariIni, tvMakanSiangHariIni, tvMakanMalamHariIni, tvSnackHariIni;
    private TextView tvKaloriSarapan, tvKaloriMakanSiang, tvKaloriMakanMalam, tvKaloriSnack;
    private TextView btnIsiDataHarian, btnSaranMenu;

    private TextView tvTotalKaloriHariIni;

    private ArrayList<MakananItem> listMakanan = new ArrayList<>();

    private JSONArray listPenyakit = new JSONArray();
    private ArrayList<Integer> selectedPenyakitIds = new ArrayList<>();

    private ArrayList<MakananItem> sarapanList = new ArrayList<>();
    private ArrayList<MakananItem> makanSiangList = new ArrayList<>();
    private ArrayList<MakananItem> makanMalamList = new ArrayList<>();
    private ArrayList<MakananItem> snackList = new ArrayList<>();

    private double totalSarapan = 0;
    private double totalMakanSiang = 0;
    private double totalMakanMalam = 0;
    private double totalSnack = 0;

    private TextView tvJudulCardRekomendasi, tvTanggalCatatan;
    private LinearLayout llRekomendasi7Hari;

    private boolean rekomendasiSudahAda = false;
    private JSONObject dataPerhitunganManual = null;
    private JSONArray dataRekomendasi7Hari = null;

    private TextView btnUlangiRekomendasi;

    private TextView tvTBHariIni, tvBBHariIni, tvBMIHariIni, tvKategoriBMIHariIni;
    private TextView tvPenyakitHariIni;

    private double tbHariIni = 0;
    private double bbHariIni = 0;
    private double bmiHariIni = 0;
    private String kategoriBmiHariIni = "-";

    interface OnMakananSelectedListener {
        void onSelected(ArrayList<MakananItem> selected);
    }

    private JSONArray semuaPenyakitArray = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rekomendasi);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRekomendasi), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        initView();
        aturTombolAksi(false);

        btnBackRekomendasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Rekomendasi.this, HomeUser.class));
                finish();
            }
        });

        btnIsiDataHarian.setOnClickListener(v -> {
            if (!lockButton(btnIsiDataHarian)) {
                return;
            }

            if (listMakanan.isEmpty()) {
                getMakanan(() -> {
                    unlockButton(btnIsiDataHarian);
                    showInputMakanDialog();
                });

                unlockButtonDelay(btnIsiDataHarian, 3000);

            } else {
                unlockButton(btnIsiDataHarian);
                showInputMakanDialog();
            }
        });

        btnSaranMenu.setOnClickListener(v -> {
            if (!lockButton(btnSaranMenu)) {
                return;
            }

            if (rekomendasiSudahAda) {
                showDialogPerhitunganManual();

                unlockButtonDelay(btnSaranMenu, 800);
                return;
            }

            if (tbHariIni <= 0 || bbHariIni <= 0) {
                Toast.makeText(this, "Isi tinggi badan dan berat badan terlebih dahulu", Toast.LENGTH_SHORT).show();
                unlockButton(btnSaranMenu);
                showInputMakanDialog();
                return;
            }

            if (getTotalKaloriHariIni() <= 0) {
                Toast.makeText(this, "Isi makanan hari ini terlebih dahulu", Toast.LENGTH_SHORT).show();
                unlockButton(btnSaranMenu);
                return;
            }

            simpanInputMakanHarianDanHitungRekomendasi();
        });

        btnUlangiRekomendasi.setOnClickListener(v -> {
            if (!lockButton(btnUlangiRekomendasi)) {
                return;
            }

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Rekomendasi.this, R.style.AlertDialogTheme);
            builder.setCancelable(false);
            View popup = LayoutInflater.from(Rekomendasi.this).inflate(R.layout.dialog_info, (ConstraintLayout) findViewById(R.id.layoutDialogContainerInfo));
            builder.setView(popup);
            final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            alertDialog.show();

            TextView tanya = popup.findViewById(R.id.txtTanyaInfo);
            tanya.setText("Semua input makanan dan hasil rekomendasi saat ini akan dihapus. Setelah itu kamu bisa input data baru lagi.\nYakin ulangi isian ?");

            popup.findViewById(R.id.btnYaInfo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetRekomendasi();
                    alertDialog.cancel();
                }
            });

            popup.findViewById(R.id.btnTidakInfo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                    unlockButton(btnUlangiRekomendasi);
                }
            });
        });

        getMakanan(null);
        getPenyakit(() -> getStatusRekomendasi());
    }

    private void initView() {
        btnBackRekomendasi = findViewById(R.id.btnBackRekomendasi);

        tvStatusInputHarian = findViewById(R.id.tvStatusInputHarian);

        tvSarapanHariIni = findViewById(R.id.tvSarapanHariIni);
        tvMakanSiangHariIni = findViewById(R.id.tvMakanSiangHariIni);
        tvMakanMalamHariIni = findViewById(R.id.tvMakanMalamHariIni);
        tvSnackHariIni = findViewById(R.id.tvSnackHariIni);

        tvKaloriSarapan = findViewById(R.id.tvKaloriSarapan);
        tvKaloriMakanSiang = findViewById(R.id.tvKaloriMakanSiang);
        tvKaloriMakanMalam = findViewById(R.id.tvKaloriMakanMalam);
        tvKaloriSnack = findViewById(R.id.tvKaloriSnack);

        btnIsiDataHarian = findViewById(R.id.btnIsiDataHarian);
        btnSaranMenu = findViewById(R.id.btnSaranMenu);

        tvTotalKaloriHariIni = findViewById(R.id.tvTotalKaloriHariIni);
        tvJudulCardRekomendasi = findViewById(R.id.tvJudulCardRekomendasi);
        tvTanggalCatatan = findViewById(R.id.tvTanggalCatatan);
        llRekomendasi7Hari = findViewById(R.id.llRekomendasi7Hari);

        btnUlangiRekomendasi = findViewById(R.id.btnUlangiRekomendasi);

        tvTBHariIni = findViewById(R.id.tvTinggiHariIni);
        tvBBHariIni = findViewById(R.id.tvBeratHariIni);
        tvBMIHariIni = findViewById(R.id.tvBMIHariIni);
        tvKategoriBMIHariIni = findViewById(R.id.tvKategoriBMIHariIni);
        tvPenyakitHariIni = findViewById(R.id.tvPenyakitHariIni);

        TextView btnEditPenyakit = findViewById(R.id.btnEditPenyakit);
        if (btnEditPenyakit != null) {
            btnEditPenyakit.setOnClickListener(v -> {
                if (listPenyakit.length() == 0) {
                    getPenyakit(() -> showPenyakitMultiSelectDialog(true, null));
                } else {
                    showPenyakitMultiSelectDialog(true, null);
                }
            });
        }
    }

    private void showInputMakanDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input_makan_harian);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView edSarapan = dialog.findViewById(R.id.edDialogSarapan);
        TextView edMakanSiang = dialog.findViewById(R.id.edDialogMakanSiang);
        TextView edMakanMalam = dialog.findViewById(R.id.edDialogMakanMalam);
        TextView edSnack = dialog.findViewById(R.id.edDialogSnack);

        TextView tvDialogKaloriSarapan = dialog.findViewById(R.id.tvDialogKaloriSarapan);
        TextView tvDialogKaloriMakanSiang = dialog.findViewById(R.id.tvDialogKaloriMakanSiang);
        TextView tvDialogKaloriMakanMalam = dialog.findViewById(R.id.tvDialogKaloriMakanMalam);
        TextView tvDialogKaloriSnack = dialog.findViewById(R.id.tvDialogKaloriSnack);
        TextView tvDialogTotalKalori = dialog.findViewById(R.id.tvDialogTotalKalori);

        AppCompatButton btnBatal = dialog.findViewById(R.id.btnBatalInputMakan);
        AppCompatButton btnSimpan = dialog.findViewById(R.id.btnSimpanInputMakan);

        Runnable refreshDialog = () -> {
            edSarapan.setText(formatNamaMakananFull(sarapanList, "Pilih makanan sarapan"));
            edMakanSiang.setText(formatNamaMakananFull(makanSiangList, "Pilih makanan siang"));
            edMakanMalam.setText(formatNamaMakananFull(makanMalamList, "Pilih makanan malam"));
            edSnack.setText(formatNamaMakananFull(snackList, "Pilih snack / cemilan"));

            tvDialogKaloriSarapan.setText(formatKkal(hitungTotalKkal(sarapanList)) + " kkal");
            tvDialogKaloriMakanSiang.setText(formatKkal(hitungTotalKkal(makanSiangList)) + " kkal");
            tvDialogKaloriMakanMalam.setText(formatKkal(hitungTotalKkal(makanMalamList)) + " kkal");
            tvDialogKaloriSnack.setText(formatKkal(hitungTotalKkal(snackList)) + " kkal");

            double total = hitungTotalKkal(sarapanList)
                    + hitungTotalKkal(makanSiangList)
                    + hitungTotalKkal(makanMalamList)
                    + hitungTotalKkal(snackList);

            tvDialogTotalKalori.setText(formatKkal(total) + " kkal");
        };

        refreshDialog.run();

        edSarapan.setOnClickListener(v ->
                showSearchableMultiSelect("Sarapan", "sarapan", sarapanList, selected -> {
                    sarapanList = selected;
                    refreshDialog.run();
                })
        );

        edMakanSiang.setOnClickListener(v ->
                showSearchableMultiSelect("Makan Siang", "makan_siang", makanSiangList, selected -> {
                    makanSiangList = selected;
                    refreshDialog.run();
                })
        );

        edMakanMalam.setOnClickListener(v ->
                showSearchableMultiSelect("Makan Malam", "makan_malam", makanMalamList, selected -> {
                    makanMalamList = selected;
                    refreshDialog.run();
                })
        );

        edSnack.setOnClickListener(v ->
                showSearchableMultiSelect("Snack / Cemilan", "snack", snackList, selected -> {
                    snackList = selected;
                    refreshDialog.run();
                })
        );

        EditText edTB = dialog.findViewById(R.id.edDialogTB);
        EditText edBB = dialog.findViewById(R.id.edDialogBB);
        TextView tvDialogBMIInfo = dialog.findViewById(R.id.tvDialogBMIInfo);
        TextView edPenyakit = dialog.findViewById(R.id.edDialogPenyakit);

        if (tbHariIni > 0) {
            edTB.setText(formatAngka(tbHariIni));
        }

        if (bbHariIni > 0) {
            edBB.setText(formatAngka(bbHariIni));
        }

        if (bmiHariIni > 0) {
            tvDialogBMIInfo.setText("BMI terakhir: " + String.format(Locale.US, "%.2f", bmiHariIni) + " • " + kategoriBmiHariIni);
        } else {
            tvDialogBMIInfo.setText("BMI akan dihitung setelah disimpan");
        }

        if (edPenyakit != null) {
            edPenyakit.setText(formatPenyakitSelected("Pilih riwayat penyakit"));
            edPenyakit.setOnClickListener(v -> {
                if (listPenyakit.length() == 0) {
                    getPenyakit(() -> showPenyakitMultiSelectDialog(false, () -> edPenyakit.setText(formatPenyakitSelected("Pilih riwayat penyakit"))));
                } else {
                    showPenyakitMultiSelectDialog(false, () -> edPenyakit.setText(formatPenyakitSelected("Pilih riwayat penyakit")));
                }
            });
        }

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            double tb = parseDouble(edTB.getText().toString());
            double bb = parseDouble(edBB.getText().toString());

            if (tb <= 0) {
                edTB.setError("Tinggi badan wajib diisi");
                edTB.requestFocus();
                return;
            }

            if (bb <= 0) {
                edBB.setError("Berat badan wajib diisi");
                edBB.requestFocus();
                return;
            }

            tbHariIni = tb;
            bbHariIni = bb;

            hitungDanTampilkan();
            tampilkanInfoFisikLocal();
            updatePenyakitUser(false);

            dialog.dismiss();
        });

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = (int) (metrics.widthPixels * 0.92);

            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private double parseDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }

            return Double.parseDouble(value.trim().replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

    private void showSearchableMultiSelect(
            String title,
            String waktuMakan,
            ArrayList<MakananItem> currentSelected,
            OnMakananSelectedListener listener
    ) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 20, 24, 20);
        root.setBackgroundColor(Color.WHITE);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(getResources().getColor(R.color.text_primary));
        tvTitle.setPadding(0, 0, 0, 12);
        root.addView(tvTitle);

        SearchView searchView = new SearchView(this);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Cari makanan...");
        root.addView(searchView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        ListView listView = new ListView(this);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        listParams.topMargin = 12;
        root.addView(listView, listParams);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setWeightSum(2);
        buttonLayout.setPadding(0, 16, 0, 0);

        AppCompatButton btnBatal = new AppCompatButton(this);
        btnBatal.setText("BATAL");

        AppCompatButton btnPilih = new AppCompatButton(this);
        btnPilih.setText("PILIH");

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        btnParams.setMargins(8, 0, 8, 0);

        buttonLayout.addView(btnBatal, btnParams);
        buttonLayout.addView(btnPilih, btnParams);

        root.addView(buttonLayout);

        dialog.setContentView(root);

        ArrayList<MakananItem> sourceItems = getMakananByWaktu(waktuMakan);
        ArrayList<MakananItem> filteredItems = new ArrayList<>(sourceItems);
        ArrayList<MakananItem> selectedTemp = new ArrayList<>(currentSelected);

        ArrayAdapter<MakananItem> adapter = new ArrayAdapter<MakananItem>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                filteredItems
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                MakananItem item = getItem(position);

                if (item != null) {
                    text.setText(item.nama + " (" + formatKkal(item.kkal) + " kkal)");
                    text.setTextSize(13);
                    text.setPadding(8, 18, 8, 18);
                }

                return view;
            }

            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        ArrayList<MakananItem> filtered = new ArrayList<>();

                        String keyword = constraint == null ? "" : constraint.toString().toLowerCase().trim();

                        if (keyword.isEmpty()) {
                            filtered.addAll(sourceItems);
                        } else {
                            for (MakananItem item : sourceItems) {
                                if (item.nama.toLowerCase().contains(keyword)) {
                                    filtered.add(item);
                                }
                            }
                        }

                        results.values = filtered;
                        results.count = filtered.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        filteredItems.clear();
                        filteredItems.addAll((ArrayList<MakananItem>) results.values);
                        notifyDataSetChanged();

                        listView.post(() -> {
                            for (int i = 0; i < filteredItems.size(); i++) {
                                listView.setItemChecked(i, isSelected(selectedTemp, filteredItems.get(i).id));
                            }
                        });
                    }
                };
            }
        };

        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.post(() -> {
            for (int i = 0; i < filteredItems.size(); i++) {
                listView.setItemChecked(i, isSelected(selectedTemp, filteredItems.get(i).id));
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MakananItem item = filteredItems.get(position);

            if (listView.isItemChecked(position)) {
                if (!isSelected(selectedTemp, item.id)) {
                    selectedTemp.add(item);
                }
            } else {
                removeSelected(selectedTemp, item.id);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnPilih.setOnClickListener(v -> {
            listener.onSelected(new ArrayList<>(selectedTemp));
            dialog.dismiss();
        });

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = (int) (metrics.widthPixels * 0.92);
            int height = (int) (metrics.heightPixels * 0.75);

            window.setLayout(width, height);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
    }

    private String formatNamaMakananFull(ArrayList<MakananItem> list, String placeholder) {
        if (list == null || list.isEmpty()) {
            return placeholder;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(list.get(i).nama);
        }

        return sb.toString();
    }

    private ArrayList<MakananItem> getMakananByWaktu(String waktuMakan) {
        ArrayList<MakananItem> hasil = new ArrayList<>();

        for (MakananItem item : listMakanan) {

            if (waktuMakan.equals("snack")) {
                int kategori = item.kategoriId;

                if (kategori == 5 || kategori == 6 || kategori == 7) {
                    hasil.add(item);
                }

            } else {
                hasil.add(item);
            }
        }

        return hasil;
    }

    private void getMakanan(Runnable afterLoaded) {
        String token = session.getToken();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.MAKANAN,
                null,
                response -> {
                    try {
                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            JSONArray array = response.getJSONArray("data");
                            listMakanan.clear();

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);

                                listMakanan.add(new MakananItem(
                                        o.optInt("id"),
                                        o.optInt("kategori_id"),
                                        o.optString("nama"),
                                        o.optDouble("kkal", 0),
                                        o.optDouble("protein", 0),
                                        o.optDouble("karbohidrat", 0),
                                        o.optDouble("lemak", 0),
                                        o.optDouble("serat", 0),
                                        o.optDouble("gula", 0),
                                        o.optDouble("natrium", 0)
                                ));
                            }

                            if (afterLoaded != null) {
                                afterLoaded.run();
                            }
                        } else {
                            Toast.makeText(this, "Gagal mengambil makanan", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        Toast.makeText(this, "Format data makanan tidak sesuai", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
        ) {
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

    private void showPilihWaktuMakanDialog() {
        String[] waktuMakan = {
                "Sarapan",
                "Makan Siang",
                "Makan Malam",
                "Snack / Cemilan"
        };

        new AlertDialog.Builder(this)
                .setTitle("Pilih Waktu Makan")
                .setItems(waktuMakan, (dialog, which) -> {
                    if (which == 0) {
                        showMultiSelectMakanan("sarapan");
                    } else if (which == 1) {
                        showMultiSelectMakanan("makan_siang");
                    } else if (which == 2) {
                        showMultiSelectMakanan("makan_malam");
                    } else {
                        showMultiSelectMakanan("snack");
                    }
                })
                .show();
    }

    private void showMultiSelectMakanan(String waktu) {
        String[] namaMakanan = new String[listMakanan.size()];
        boolean[] checkedItems = new boolean[listMakanan.size()];

        ArrayList<MakananItem> selectedSementara = new ArrayList<>(getListByWaktu(waktu));

        for (int i = 0; i < listMakanan.size(); i++) {
            MakananItem item = listMakanan.get(i);

            namaMakanan[i] = item.nama + " (" + formatKkal(item.kkal) + " kkal)";
            checkedItems[i] = isSelected(selectedSementara, item.id);
        }

        new AlertDialog.Builder(this)
                .setTitle("Pilih Makanan - " + labelWaktu(waktu))
                .setMultiChoiceItems(namaMakanan, checkedItems, (dialog, which, isChecked) -> {
                    MakananItem item = listMakanan.get(which);

                    if (isChecked) {
                        if (!isSelected(selectedSementara, item.id)) {
                            selectedSementara.add(item);
                        }
                    } else {
                        removeSelected(selectedSementara, item.id);
                    }
                })
                .setPositiveButton("Simpan", (dialog, which) -> {
                    setListByWaktu(waktu, selectedSementara);
                    hitungDanTampilkan();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private ArrayList<MakananItem> getListByWaktu(String waktu) {
        switch (waktu) {
            case "sarapan":
                return sarapanList;
            case "makan_siang":
                return makanSiangList;
            case "makan_malam":
                return makanMalamList;
            case "snack":
                return snackList;
            default:
                return new ArrayList<>();
        }
    }

    private void setListByWaktu(String waktu, ArrayList<MakananItem> data) {
        switch (waktu) {
            case "sarapan":
                sarapanList = new ArrayList<>(data);
                break;
            case "makan_siang":
                makanSiangList = new ArrayList<>(data);
                break;
            case "makan_malam":
                makanMalamList = new ArrayList<>(data);
                break;
            case "snack":
                snackList = new ArrayList<>(data);
                break;
        }
    }

    private boolean isSelected(ArrayList<MakananItem> list, int id) {
        for (MakananItem item : list) {
            if (item.id == id) {
                return true;
            }
        }
        return false;
    }

    private void removeSelected(ArrayList<MakananItem> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == id) {
                list.remove(i);
                return;
            }
        }
    }

    private void hitungDanTampilkan() {
        totalSarapan = hitungTotalKkal(sarapanList);
        totalMakanSiang = hitungTotalKkal(makanSiangList);
        totalMakanMalam = hitungTotalKkal(makanMalamList);
        totalSnack = hitungTotalKkal(snackList);

        tvSarapanHariIni.setText(formatNamaMakanan(sarapanList));
        tvMakanSiangHariIni.setText(formatNamaMakanan(makanSiangList));
        tvMakanMalamHariIni.setText(formatNamaMakanan(makanMalamList));
        tvSnackHariIni.setText(formatNamaMakanan(snackList));

        tvKaloriSarapan.setText(formatKkal(totalSarapan) + " kkal");
        tvKaloriMakanSiang.setText(formatKkal(totalMakanSiang) + " kkal");
        tvKaloriMakanMalam.setText(formatKkal(totalMakanMalam) + " kkal");
        tvKaloriSnack.setText(formatKkal(totalSnack) + " kkal");

        tvTotalKaloriHariIni.setText(formatKkal(getTotalKaloriHariIni()) + " kkal");

        if (getTotalKaloriHariIni() > 0) {
            tvStatusInputHarian.setText("Sudah input");
        } else {
            tvStatusInputHarian.setText("Belum lengkap");
        }
    }

    private double hitungTotalKkal(ArrayList<MakananItem> list) {
        double total = 0;

        for (MakananItem item : list) {
            total += item.kkal;
        }

        return total;
    }

    private double getTotalKaloriHariIni() {
        return totalSarapan + totalMakanSiang + totalMakanMalam + totalSnack;
    }

    private String formatNamaMakanan(ArrayList<MakananItem> list) {
        if (list == null || list.isEmpty()) {
            return "-";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            if (i < 2) {
                sb.append(list.get(i).nama);
            } else {
                sb.append("+").append(list.size() - 2).append(" lainnya");
                break;
            }
        }

        return sb.toString();
    }

    private String formatKkal(double value) {
        return String.format(Locale.US, "%.0f", value);
    }

    private String labelWaktu(String waktu) {
        switch (waktu) {
            case "sarapan":
                return "Sarapan";
            case "makan_siang":
                return "Makan Siang";
            case "makan_malam":
                return "Makan Malam";
            case "snack":
                return "Snack / Cemilan";
            default:
                return waktu;
        }
    }

    private JSONArray toJsonArray(ArrayList<MakananItem> list) throws JSONException {
        JSONArray array = new JSONArray();

        for (MakananItem item : list) {
            JSONObject obj = new JSONObject();
            obj.put("makanan_id", item.id);
            obj.put("nama", item.nama);
            obj.put("kkal", item.kkal);
            obj.put("protein", item.protein);
            obj.put("karbohidrat", item.karbohidrat);
            obj.put("lemak", item.lemak);
            obj.put("serat", item.serat);
            obj.put("gula", item.gula);
            obj.put("natrium", item.natrium);
            array.put(obj);
        }

        return array;
    }

    private void simpanInputMakanHarianDanHitungRekomendasi() {
        String token = session.getToken();

        Dialog loadingDialog = showLoadingPerhitunganDialog();

        try {
            JSONObject body = new JSONObject();
            body.put("email", session.getEmail());
            body.put("tb", tbHariIni);
            body.put("bb", bbHariIni);

            JSONArray penyakitIds = new JSONArray();
            for (Integer id : selectedPenyakitIds) {
                penyakitIds.put(id);
            }
            body.put("penyakit_ids", penyakitIds);

            body.put("sarapan", toJsonArray(sarapanList));
            body.put("makan_siang", toJsonArray(makanSiangList));
            body.put("makan_malam", toJsonArray(makanMalamList));
            body.put("snack", toJsonArray(snackList));
            body.put("total_kkal", getTotalKaloriHariIni());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.INPUT_MAKAN_HARIAN,
                    body,
                    response -> {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            loadingDialog.dismiss();

                            boolean status = response.optBoolean("status", false);

                            if (status) {
                                rekomendasiSudahAda = true;

                                dataPerhitunganManual = response.optJSONObject("perhitungan");
                                dataRekomendasi7Hari = response.optJSONArray("rekomendasi_7_hari");

                                JSONObject rataRata = response.optJSONObject("rata_rata_7_hari");

                                tampilkanRataRata7Hari(rataRata);
                                tampilkanCardRekomendasi7Hari(dataRekomendasi7Hari);

                                JSONObject infoFisik = response.optJSONObject("info_fisik");
                                tampilkanInfoFisikDariServer(infoFisik);
                                tampilkanPenyakitDariServer(response.optJSONArray("penyakit"));

                                aturTombolAksi(true);
                                getStatusRekomendasi();

                                Toast.makeText(this, "Rekomendasi 7 hari berhasil dihitung", Toast.LENGTH_SHORT).show();
                                unlockButton(btnSaranMenu);
                            } else {
                                Toast.makeText(this, response.optString("message", "Gagal menghitung rekomendasi"), Toast.LENGTH_SHORT).show();
                                unlockButton(btnSaranMenu);
                            }

                        }, 800);
                    },
                    error -> {
                        loadingDialog.dismiss();
                        unlockButton(btnSaranMenu);

                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);

                            android.util.Log.e("INPUT_MAKAN_ERROR", "Code: " + error.networkResponse.statusCode);
                            android.util.Log.e("INPUT_MAKAN_ERROR", errorBody);

                            Toast.makeText(
                                    this,
                                    "Error server: " + error.networkResponse.statusCode + "\n" + errorBody,
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            android.util.Log.e("INPUT_MAKAN_ERROR", String.valueOf(error));
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
            loadingDialog.dismiss();
            unlockButton(btnSaranMenu);
            Toast.makeText(this, "Gagal membuat data input", Toast.LENGTH_SHORT).show();
        }
    }

    private Dialog showLoadingPerhitunganDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 32, 36, 32);
        root.setBackgroundColor(Color.WHITE);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Menghitung Rekomendasi");
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(getResources().getColor(R.color.text_primary));
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        root.addView(tvTitle);

        TextView tvStep = new TextView(this);
        tvStep.setText("Menyiapkan data pasien...");
        tvStep.setTextSize(13);
        tvStep.setTextColor(getResources().getColor(R.color.text_secondary));
        tvStep.setGravity(Gravity.CENTER);
        tvStep.setPadding(0, 16, 0, 8);
        root.addView(tvStep);

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        root.addView(progressBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                16
        ));

        TextView tvPercent = new TextView(this);
        tvPercent.setText("0%");
        tvPercent.setTextSize(14);
        tvPercent.setTextColor(getResources().getColor(R.color.user_primary));
        tvPercent.setTypeface(null, android.graphics.Typeface.BOLD);
        tvPercent.setGravity(Gravity.CENTER);
        tvPercent.setPadding(0, 10, 0, 0);
        root.addView(tvPercent);

        dialog.setContentView(root);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = (int) (metrics.widthPixels * 0.85);
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        String[] tahap = {
                "Mengambil data pasien...",
                "Mengambil data fisik terbaru...",
                "Membaca riwayat penyakit...",
                "Menghitung BMI dan kategori status gizi...",
                "Mengambil referensi kebutuhan gizi...",
                "Menghitung target gizi harian...",
                "Menganalisis input makanan hari ini...",
                "Menghitung koreksi kebutuhan untuk besok...",
                "Mengambil kandidat makanan dari database...",
                "Melakukan filtering makanan berdasarkan batasan...",
                "Menghitung skor setiap kombinasi menu...",
                "Menyusun rekomendasi 7 hari...",
                "Menyimpan hasil rekomendasi...",
                "Menyiapkan tabel perhitungan manual..."
        };

        Handler handler = new Handler(Looper.getMainLooper());

        for (int i = 0; i < tahap.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                int percent = (int) (((index + 1) / (double) tahap.length) * 95);

                tvStep.setText(tahap[index]);
                progressBar.setProgress(percent);
                tvPercent.setText(percent + "%");

            }, i * 500L);
        }

        dialog.setOnDismissListener(d -> handler.removeCallbacksAndMessages(null));

        return dialog;
    }

    private void tampilkanRataRata7Hari(JSONObject rataRata) {
        if (rataRata == null) {
            return;
        }

        tvJudulCardRekomendasi.setText("Rata-rata Rekomendasi 7 Hari");
        tvTanggalCatatan.setText("Hasil perhitungan rekomendasi menu makanan mingguan");
        tvStatusInputHarian.setText("Selesai");

        double avgSarapan = rataRata.optDouble("sarapan_kkal", 0);
        double avgSiang = rataRata.optDouble("makan_siang_kkal", 0);
        double avgMalam = rataRata.optDouble("makan_malam_kkal", 0);
        double avgSnack = rataRata.optDouble("snack_kkal", 0);
        double avgTotal = rataRata.optDouble("total_kkal", 0);

        tvSarapanHariIni.setText("Rata-rata sarapan");
        tvMakanSiangHariIni.setText("Rata-rata makan siang");
        tvMakanMalamHariIni.setText("Rata-rata makan malam");
        tvSnackHariIni.setText("Rata-rata snack");

        tvKaloriSarapan.setText(formatKkal(avgSarapan) + " kkal");
        tvKaloriMakanSiang.setText(formatKkal(avgSiang) + " kkal");
        tvKaloriMakanMalam.setText(formatKkal(avgMalam) + " kkal");
        tvKaloriSnack.setText(formatKkal(avgSnack) + " kkal");
        tvTotalKaloriHariIni.setText(formatKkal(avgTotal) + " kkal");
    }

    private void tampilkanCardRekomendasi7Hari(JSONArray array) {
        llRekomendasi7Hari.removeAllViews();

        if (array == null || array.length() == 0) {
            llRekomendasi7Hari.setVisibility(View.GONE);
            return;
        }

        llRekomendasi7Hari.setVisibility(View.VISIBLE);

        for (int i = 0; i < array.length(); i++) {
            JSONObject hariObj = array.optJSONObject(i);

            if (hariObj == null) {
                continue;
            }

            JSONArray rekomendasiArray = hariObj.optJSONArray("rekomendasi");

            if (rekomendasiArray == null || rekomendasiArray.length() == 0) {
                continue;
            }

            JSONObject utama = rekomendasiArray.optJSONObject(0);

            if (utama == null) {
                continue;
            }

            int hariKe = hariObj.optInt("hari_ke", i + 1);

            androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
            card.setRadius(dpToPx(22));
            card.setCardElevation(dpToPx(3));
            card.setUseCompatPadding(true);
            card.setCardBackgroundColor(Color.WHITE);
            card.setClickable(true);
            card.setForeground(getSelectableItemBackground());

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, dpToPx(6), 0, dpToPx(14));
            card.setLayoutParams(cardParams);

            LinearLayout root = new LinearLayout(this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setBackgroundColor(Color.WHITE);

            // Header warna
            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.VERTICAL);
            header.setPadding(dpToPx(18), dpToPx(16), dpToPx(18), dpToPx(14));
            header.setBackground(buatRoundDrawable(0xFFEFF6FF, dpToPx(22)));

            LinearLayout rowHeader = new LinearLayout(this);
            rowHeader.setOrientation(LinearLayout.HORIZONTAL);
            rowHeader.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout titleBox = new LinearLayout(this);
            titleBox.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams titleBoxParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );

            TextView tvHari = new TextView(this);
            tvHari.setText("Hari " + hariKe);
            tvHari.setTextSize(18);
            tvHari.setTypeface(null, Typeface.BOLD);
            tvHari.setTextColor(getResources().getColor(R.color.text_primary));

            TextView tvTanggal = new TextView(this);
            tvTanggal.setText(hariObj.optString("tanggal", "-"));
            tvTanggal.setTextSize(12);
            tvTanggal.setTextColor(getResources().getColor(R.color.text_secondary));
            tvTanggal.setPadding(0, dpToPx(3), 0, 0);

            titleBox.addView(tvHari);
            titleBox.addView(tvTanggal);
            rowHeader.addView(titleBox, titleBoxParams);

            TextView badge = new TextView(this);
            badge.setText("Utama");
            badge.setTextSize(11);
            badge.setTypeface(null, Typeface.BOLD);
            badge.setTextColor(getResources().getColor(R.color.user_primary));
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
            badge.setBackground(buatRoundStrokeDrawable(0xFFFFFFFF, dpToPx(30), 0xFFBFDBFE, dpToPx(1)));
            rowHeader.addView(badge);

            header.addView(rowHeader);

            TextView tvInfo = new TextView(this);
            tvInfo.setText("Ketuk card untuk melihat 5 pilihan rekomendasi");
            tvInfo.setTextSize(12);
            tvInfo.setTextColor(getResources().getColor(R.color.user_primary));
            tvInfo.setPadding(0, dpToPx(12), 0, 0);
            header.addView(tvInfo);

            root.addView(header);

            // Body
            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(16));

            tambahBarisMenuModern(body, "🍳", "Sarapan", utama, "sarapan");
            tambahBarisMenuModern(body, "🍱", "Makan Siang", utama, "makan_siang");
            tambahBarisMenuModern(body, "🍽️", "Makan Malam", utama, "makan_malam");
            tambahBarisMenuModern(body, "🍎", "Cemilan", utama, "snack");

            LinearLayout totalBox = new LinearLayout(this);
            totalBox.setOrientation(LinearLayout.HORIZONTAL);
            totalBox.setGravity(Gravity.CENTER_VERTICAL);
            totalBox.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            totalBox.setBackground(buatRoundDrawable(0xFFF0FDF4, dpToPx(16)));

            LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            totalParams.setMargins(0, dpToPx(12), 0, 0);

            TextView tvTotalLabel = new TextView(this);
            tvTotalLabel.setText("Total energi");
            tvTotalLabel.setTextSize(13);
            tvTotalLabel.setTypeface(null, Typeface.BOLD);
            tvTotalLabel.setTextColor(getResources().getColor(R.color.text_primary));

            LinearLayout.LayoutParams labelTotalParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );

            TextView tvTotalValue = new TextView(this);
            tvTotalValue.setText(formatKkal(utama.optDouble("total_kkal", 0)) + " kkal");
            tvTotalValue.setTextSize(15);
            tvTotalValue.setTypeface(null, Typeface.BOLD);
            tvTotalValue.setTextColor(getResources().getColor(R.color.user_primary));

            totalBox.addView(tvTotalLabel, labelTotalParams);
            totalBox.addView(tvTotalValue);
            body.addView(totalBox, totalParams);

            tambahRingkasanGizi(body, utama);

            root.addView(body);
            card.addView(root);

            final JSONObject finalHariObj = hariObj;
            card.setOnClickListener(v -> showDialogOpsiRekomendasiHari(finalHariObj));

            llRekomendasi7Hari.addView(card);
        }
    }

    private void tambahBarisMenuModern(LinearLayout parent, String icon, String label, JSONObject data, String prefix) {
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
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvIcon = new TextView(this);
        tvIcon.setText(icon);
        tvIcon.setTextSize(22);
        tvIcon.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(40),
                dpToPx(40)
        );

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setPadding(dpToPx(10), 0, dpToPx(8), 0);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
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

        titleBox.addView(tvLabel);
        titleBox.addView(tvMenu);

        TextView tvKkal = new TextView(this);
        tvKkal.setText(formatKkal(kkal) + "\nkkal");
        tvKkal.setTextSize(11);
        tvKkal.setTypeface(null, Typeface.BOLD);
        tvKkal.setGravity(Gravity.CENTER);
        tvKkal.setTextColor(getResources().getColor(R.color.user_primary));
        tvKkal.setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6));
        tvKkal.setBackground(buatRoundDrawable(0xFFEFF6FF, dpToPx(14)));

        topRow.addView(tvIcon, iconParams);
        topRow.addView(titleBox, titleParams);
        topRow.addView(tvKkal);

        box.addView(topRow);

        View divider = new View(this);
        divider.setBackgroundColor(0xFFE5E7EB);

        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1)
        );
        dividerParams.setMargins(0, dpToPx(10), 0, dpToPx(10));
        box.addView(divider, dividerParams);

        TextView tvDetailTitle = new TextView(this);
        tvDetailTitle.setText("Rincian gizi");
        tvDetailTitle.setTextSize(11);
        tvDetailTitle.setTypeface(null, Typeface.BOLD);
        tvDetailTitle.setTextColor(getResources().getColor(R.color.text_primary));
        tvDetailTitle.setPadding(0, 0, 0, dpToPx(8));
        box.addView(tvDetailTitle);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setWeightSum(3);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setWeightSum(3);
        row2.setPadding(0, dpToPx(8), 0, 0);

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
        chip.setGravity(Gravity.CENTER);
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
        tvLabel.setGravity(Gravity.CENTER);
        tvLabel.setTextColor(getResources().getColor(R.color.text_secondary));

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextSize(11);
        tvValue.setTypeface(null, Typeface.BOLD);
        tvValue.setGravity(Gravity.CENTER);
        tvValue.setTextColor(getResources().getColor(R.color.text_primary));
        tvValue.setPadding(0, dpToPx(2), 0, 0);

        chip.addView(tvLabel);
        chip.addView(tvValue);

        parent.addView(chip, params);
    }

    private void tambahRingkasanGizi(LinearLayout parent, JSONObject data) {
        double protein = data.optDouble("total_protein", 0);
        double karbohidrat = data.optDouble("total_karbohidrat", 0);
        double lemak = data.optDouble("total_lemak", 0);
        double serat = data.optDouble("total_serat", 0);

        if (protein <= 0 && karbohidrat <= 0 && lemak <= 0 && serat <= 0) {
            return;
        }

        TextView title = new TextView(this);
        title.setText("Ringkasan gizi");
        title.setTextSize(12);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        title.setPadding(0, dpToPx(12), 0, dpToPx(8));
        parent.addView(title);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setWeightSum(2);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setWeightSum(2);
        row2.setPadding(0, dpToPx(8), 0, 0);

        tambahChipGizi(row1, "Protein", formatSatuAngka(protein) + " g");
        tambahChipGizi(row1, "Karbo", formatSatuAngka(karbohidrat) + " g");
        tambahChipGizi(row2, "Lemak", formatSatuAngka(lemak) + " g");
        tambahChipGizi(row2, "Serat", formatSatuAngka(serat) + " g");

        parent.addView(row1);
        parent.addView(row2);
    }

    private void tambahChipGizi(LinearLayout parent, String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
        box.setBackground(buatRoundDrawable(0xFFF8FAFC, dpToPx(14)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(10);
        tvLabel.setTextColor(getResources().getColor(R.color.text_secondary));

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextSize(13);
        tvValue.setTypeface(null, Typeface.BOLD);
        tvValue.setTextColor(getResources().getColor(R.color.text_primary));
        tvValue.setPadding(0, dpToPx(2), 0, 0);

        box.addView(tvLabel);
        box.addView(tvValue);

        parent.addView(box, params);
    }

    private android.graphics.drawable.GradientDrawable buatRoundDrawable(int color, int radius) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private android.graphics.drawable.GradientDrawable buatRoundStrokeDrawable(int color, int radius, int strokeColor, int strokeWidth) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    private void tambahBarisMenu(LinearLayout parent, String label, String namaMenu, double kkal) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, 8, 0, 8);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label + ":");
        tvLabel.setTextSize(13);
        tvLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        tvLabel.setTextColor(getResources().getColor(R.color.text_primary));
        box.addView(tvLabel);

        TextView tvMenu = new TextView(this);
        tvMenu.setText(namaMenu == null || namaMenu.trim().isEmpty() ? "-" : namaMenu);
        tvMenu.setTextSize(12);
        tvMenu.setTextColor(getResources().getColor(R.color.text_secondary));
        tvMenu.setPadding(0, 4, 0, 2);
        box.addView(tvMenu);

        TextView tvKkal = new TextView(this);
        tvKkal.setText(formatKkal(kkal) + " kkal");
        tvKkal.setTextSize(11);
        tvKkal.setTypeface(null, android.graphics.Typeface.BOLD);
        tvKkal.setTextColor(getResources().getColor(R.color.user_primary));
        tvKkal.setGravity(Gravity.END);
        box.addView(tvKkal);

        parent.addView(box);
    }

    private void showDialogPerhitunganManual() {
        if (dataPerhitunganManual == null) {
            Toast.makeText(this, "Data perhitungan belum tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(24, 22, 24, 22);
        outer.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Perhitungan Manual Rekomendasi");
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 16);
        outer.addView(title);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        JSONArray tahapan = dataPerhitunganManual.optJSONArray("tahapan");

        if (tahapan != null) {
            for (int i = 0; i < tahapan.length(); i++) {
                JSONObject tahap = tahapan.optJSONObject(i);
                if (tahap != null) {
                    tambahTahapPerhitungan(root, i + 1, tahap);
                }
                View divider = new View(this);
                divider.setBackgroundColor(0xFFE5E7EB);

                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                );
                dividerParams.setMargins(0, 12, 0, 8);

                root.addView(divider, dividerParams);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText(dataPerhitunganManual.toString());
            tv.setTextSize(12);
            tv.setTextColor(getResources().getColor(R.color.text_secondary));
            root.addView(tv);
        }

        scrollView.addView(root);
        outer.addView(scrollView, scrollParams);

        AppCompatButton btnTutup = new AppCompatButton(this);
        btnTutup.setText("TUTUP");
        btnTutup.setTextColor(Color.WHITE);
        btnTutup.setBackgroundResource(R.drawable.btn_biru);

        LinearLayout.LayoutParams btnParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParam.setMargins(0, 16, 0, 0);
        outer.addView(btnTutup, btnParam);

        btnTutup.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(outer);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = (int) (metrics.widthPixels * 0.94);
            int height = (int) (metrics.heightPixels * 0.88);

            window.setLayout(width, height);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void tambahTahapPerhitungan(LinearLayout root, int nomor, JSONObject tahap) {
        TextView tvJudul = new TextView(this);
        tvJudul.setText(nomor + ". " + tahap.optString("judul", "Tahap Perhitungan"));
        tvJudul.setTextSize(15);
        tvJudul.setTypeface(null, android.graphics.Typeface.BOLD);
        tvJudul.setTextColor(getResources().getColor(R.color.text_primary));
        tvJudul.setPadding(0, 14, 0, 4);
        root.addView(tvJudul);

        String deskripsi = tahap.optString("deskripsi", "");
        if (!deskripsi.isEmpty()) {
            TextView tvDesc = new TextView(this);
            tvDesc.setText(deskripsi);
            tvDesc.setTextSize(12);
            tvDesc.setTextColor(getResources().getColor(R.color.text_secondary));
            tvDesc.setPadding(0, 0, 0, 8);
            root.addView(tvDesc);
        }

        JSONArray tabel = tahap.optJSONArray("tabel");

        if (tabel != null && tabel.length() > 0) {
            tambahTabelPerhitungan(root, tabel);
        }

        String rumus = tahap.optString("rumus", "");
        if (!rumus.isEmpty()) {
            TextView tvRumus = new TextView(this);
            tvRumus.setText("Cara hitung:\n" + rumus);
            tvRumus.setTextSize(12);
            tvRumus.setTextColor(getResources().getColor(R.color.user_primary));
            tvRumus.setPadding(14, 10, 14, 10);
            tvRumus.setBackgroundColor(0xFFEFF6FF);
            root.addView(tvRumus);
        }

        String hasil = tahap.optString("hasil", "");
        if (!hasil.isEmpty()) {
            TextView tvHasil = new TextView(this);
            tvHasil.setText(hasil);
            tvHasil.setTextSize(12);
            tvHasil.setTypeface(null, Typeface.BOLD);
            tvHasil.setTextColor(getResources().getColor(R.color.text_primary));
            tvHasil.setPadding(14, 10, 14, 10);
            tvHasil.setBackgroundColor(0xFFF0FDF4);
            root.addView(tvHasil);
        }
    }

    private void tambahTabelPerhitungan(LinearLayout root, JSONArray tabel) {
        if (tabel == null || tabel.length() == 0) {
            return;
        }

        JSONObject first = tabel.optJSONObject(0);
        if (first == null) {
            return;
        }

        JSONArray keys = first.names();
        if (keys == null) {
            return;
        }

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        horizontalScrollView.setHorizontalScrollBarEnabled(true);
        horizontalScrollView.setFillViewport(false);
        horizontalScrollView.setPadding(0, 4, 0, 8);

        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setStretchAllColumns(false);
        tableLayout.setShrinkAllColumns(false);
        tableLayout.setPadding(0, 4, 0, 8);

        TableRow header = new TableRow(this);
        header.setBackgroundColor(0xFFE5E7EB);

        for (int i = 0; i < keys.length(); i++) {
            String key = keys.optString(i);
            TextView cell = buatCellTabel(key, true);
            header.addView(cell);
        }

        tableLayout.addView(header);

        for (int r = 0; r < tabel.length(); r++) {
            JSONObject rowObj = tabel.optJSONObject(r);

            if (rowObj == null) {
                continue;
            }

            TableRow row = new TableRow(this);

            if (r % 2 == 1) {
                row.setBackgroundColor(0xFFF9FAFB);
            }

            for (int c = 0; c < keys.length(); c++) {
                String key = keys.optString(c);
                String value = rowObj.optString(key, "-");

                row.addView(buatCellTabel(value, false));
            }

            tableLayout.addView(row);
        }

        horizontalScrollView.addView(tableLayout);

        root.addView(horizontalScrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }

    private TextView buatCellTabel(String text, boolean header) {
        TextView tv = new TextView(this);

        String safeText = text == null || text.trim().isEmpty() ? "-" : text;
        tv.setText(safeText);

        tv.setTextSize(header ? 11 : 10);
        tv.setTextColor(getResources().getColor(R.color.text_primary));
        tv.setPadding(14, 10, 14, 10);
        tv.setGravity(Gravity.CENTER_VERTICAL);

        if (header) {
            tv.setTypeface(null, Typeface.BOLD);
            tv.setBackgroundColor(0xFFE5E7EB);
        }

        boolean angka = isTextAngkaPendek(safeText);
        boolean teksPendek = safeText.length() <= 18;

        if (angka || teksPendek) {
            tv.setSingleLine(true);
            tv.setMinWidth(dpToPx(105));
        } else {
            tv.setSingleLine(false);
            tv.setMaxLines(3);
            tv.setMinWidth(dpToPx(180));
        }

        return tv;
    }

    private boolean isTextAngkaPendek(String text) {
        if (text == null) {
            return false;
        }

        String value = text.trim();

        return value.matches("^-?\\d+(\\.\\d+)?$") ||
                value.matches("^-?\\d+(\\.\\d+)?\\s?(kkal|g|mg|mL|kg|cm|%)$") ||
                value.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private boolean lockButton(TextView button) {
        if (button == null) {
            return false;
        }

        if (!button.isEnabled()) {
            return false;
        }

        button.setEnabled(false);
        button.setAlpha(0.55f);
        return true;
    }

    private void unlockButton(TextView button) {
        if (button == null) {
            return;
        }

        button.setEnabled(true);
        button.setAlpha(1f);
    }

    private void unlockButtonDelay(TextView button, long delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> unlockButton(button), delayMillis);
    }

    private void aturTombolAksi(boolean sudahAdaData) {
        LinearLayout.LayoutParams paramsKiri = new LinearLayout.LayoutParams(
                0,
                dpToPx(42),
                1f
        );
        paramsKiri.setMargins(0, 0, dpToPx(6), 0);

        LinearLayout.LayoutParams paramsKanan = new LinearLayout.LayoutParams(
                0,
                dpToPx(42),
                1f
        );
        paramsKanan.setMargins(dpToPx(6), 0, 0, 0);

        btnSaranMenu.setLayoutParams(paramsKiri);

        if (sudahAdaData) {
            btnIsiDataHarian.setVisibility(View.GONE);
            btnUlangiRekomendasi.setVisibility(View.VISIBLE);
            btnUlangiRekomendasi.setLayoutParams(paramsKanan);

            btnSaranMenu.setText("Lihat Perhitungan");
        } else {
            btnIsiDataHarian.setVisibility(View.VISIBLE);
            btnUlangiRekomendasi.setVisibility(View.GONE);
            btnIsiDataHarian.setLayoutParams(paramsKanan);

            btnSaranMenu.setText("Saran Menu");
        }
    }

    private void getPenyakit(Runnable afterLoaded) {
        String token = session.getToken();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.PENYAKIT,
                null,
                response -> {
                    boolean status = response.optBoolean("status", false);

                    if (status) {
                        listPenyakit = response.optJSONArray("data");
                        if (listPenyakit == null) {
                            listPenyakit = new JSONArray();
                        }

                        if (afterLoaded != null) {
                            afterLoaded.run();
                        }
                    } else {
                        Toast.makeText(this, response.optString("message", "Gagal mengambil data penyakit"), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Gagal mengambil data penyakit", Toast.LENGTH_SHORT).show()
        ) {
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

    private void showPenyakitMultiSelectDialog(boolean saveImmediately, Runnable afterSelected) {
        if (listPenyakit == null || listPenyakit.length() == 0) {
            Toast.makeText(this, "Data penyakit belum tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 22, 24, 22);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Pilih Riwayat Penyakit");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 14);
        root.addView(title);

        EditText edCari = new EditText(this);
        edCari.setHint("Cari penyakit, contoh: diabetes, hipertensi...");
        edCari.setSingleLine(true);
        edCari.setTextSize(13);
        edCari.setPadding(16, 0, 16, 0);
        edCari.setBackgroundResource(R.drawable.bg_input);

        LinearLayout.LayoutParams cariParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(48)
        );
        root.addView(edCari, cariParams);

        TextView tvJumlah = new TextView(this);
        tvJumlah.setText("Dipilih: " + selectedPenyakitIds.size());
        tvJumlah.setTextSize(12);
        tvJumlah.setTextColor(getResources().getColor(R.color.user_primary));
        tvJumlah.setPadding(0, 10, 0, 6);
        root.addView(tvJumlah);

        ScrollView scrollView = new ScrollView(this);

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(listContainer);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        scrollParams.setMargins(0, 8, 0, 12);
        root.addView(scrollView, scrollParams);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setWeightSum(2);

        AppCompatButton btnBatal = new AppCompatButton(this);
        btnBatal.setText("BATAL");
        btnBatal.setTextColor(getResources().getColor(R.color.text_primary));
        btnBatal.setBackgroundResource(R.drawable.bg_input);

        AppCompatButton btnSimpan = new AppCompatButton(this);
        btnSimpan.setText("SIMPAN");
        btnSimpan.setTextColor(Color.WHITE);
        btnSimpan.setBackgroundResource(R.drawable.btn_biru);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0,
                dpToPx(48),
                1f
        );
        btnParams.setMargins(6, 0, 6, 0);

        buttonLayout.addView(btnBatal, btnParams);
        buttonLayout.addView(btnSimpan, btnParams);

        root.addView(buttonLayout);

        ArrayList<Integer> selectedTemp = new ArrayList<>(selectedPenyakitIds);

        Runnable renderAwal = () -> tampilkanListPenyakitCheckbox(
                listContainer,
                selectedTemp,
                "",
                tvJumlah
        );

        renderAwal.run();

        edCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tampilkanListPenyakitCheckbox(
                        listContainer,
                        selectedTemp,
                        s.toString(),
                        tvJumlah
                );
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            selectedPenyakitIds = new ArrayList<>(selectedTemp);

            tampilkanPenyakitLocal();

            if (afterSelected != null) {
                afterSelected.run();
            }

            if (saveImmediately) {
                updatePenyakitUser(true);
            }

            dialog.dismiss();
        });

        dialog.setContentView(root);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = (int) (metrics.widthPixels * 0.94);
            int height = (int) (metrics.heightPixels * 0.82);

            window.setLayout(width, height);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void tampilkanListPenyakitCheckbox(
            LinearLayout parent,
            ArrayList<Integer> selectedTemp,
            String keyword,
            TextView tvJumlah
    ) {
        parent.removeAllViews();

        String cari = keyword == null ? "" : keyword.toLowerCase().trim();
        int jumlahTampil = 0;

        for (int i = 0; i < listPenyakit.length(); i++) {
            JSONObject item = listPenyakit.optJSONObject(i);

            if (item == null) {
                continue;
            }

            int id = item.optInt("id", 0);
            String nama = item.optString("nama_penyakit", "-");
            String kataKunci = item.optString("kata_kunci", "");
            String anjuran = item.optString("anjuran", "");
            String hindari = item.optString("hindari", "");
            String disarankan = item.optString("disarankan", "");
            String risiko = item.optString("tingkat_risiko", "");

            boolean cocok = cari.isEmpty()
                    || nama.toLowerCase().contains(cari)
                    || kataKunci.toLowerCase().contains(cari)
                    || anjuran.toLowerCase().contains(cari)
                    || hindari.toLowerCase().contains(cari)
                    || disarankan.toLowerCase().contains(cari);

            if (!cocok) {
                continue;
            }

            jumlahTampil++;

            LinearLayout itemBox = new LinearLayout(this);
            itemBox.setOrientation(LinearLayout.VERTICAL);
            itemBox.setPadding(12, 10, 12, 10);
            itemBox.setBackgroundResource(R.drawable.bg_card_white);

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(0, 0, 0, 8);
            itemBox.setLayoutParams(itemParams);

            CheckBox cb = new CheckBox(this);

            String label = nama;
            if (risiko != null && !risiko.trim().isEmpty()) {
                label += " (" + risiko + ")";
            }

            cb.setText(label);
            cb.setTextSize(13);
            cb.setTextColor(getResources().getColor(R.color.text_primary));
            cb.setTypeface(null, Typeface.BOLD);
            cb.setChecked(selectedTemp.contains(id));

            TextView tvDetail = new TextView(this);
            tvDetail.setText("Hindari: " + (hindari.isEmpty() ? "-" : hindari));
            tvDetail.setTextSize(11);
            tvDetail.setTextColor(getResources().getColor(R.color.text_secondary));
            tvDetail.setPadding(42, 0, 0, 0);

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedTemp.contains(id)) {
                        selectedTemp.add(id);
                    }
                } else {
                    selectedTemp.remove(Integer.valueOf(id));
                }

                tvJumlah.setText("Dipilih: " + selectedTemp.size());
            });

            itemBox.setOnClickListener(v -> cb.setChecked(!cb.isChecked()));

            itemBox.addView(cb);
            itemBox.addView(tvDetail);

            parent.addView(itemBox);
        }

        if (jumlahTampil == 0) {
            TextView kosong = new TextView(this);
            kosong.setText("Penyakit tidak ditemukan");
            kosong.setTextSize(13);
            kosong.setTextColor(getResources().getColor(R.color.text_secondary));
            kosong.setGravity(Gravity.CENTER);
            kosong.setPadding(0, 24, 0, 24);
            parent.addView(kosong);
        }

        tvJumlah.setText("Dipilih: " + selectedTemp.size());
    }

    private void updatePenyakitUser(boolean showToast) {
        String token = session.getToken();

        try {
            JSONObject body = new JSONObject();
            body.put("email", session.getEmail());

            JSONArray penyakitIds = new JSONArray();
            for (Integer id : selectedPenyakitIds) {
                penyakitIds.put(id);
            }
            body.put("penyakit_ids", penyakitIds);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.UPDATE_PENYAKIT_USER,
                    body,
                    response -> {
                        boolean status = response.optBoolean("status", false);
                        if (status) {
                            tampilkanPenyakitDariServer(response.optJSONArray("penyakit"));
                            if (showToast) {
                                Toast.makeText(this, "Riwayat penyakit berhasil diperbarui", Toast.LENGTH_SHORT).show();
                            }
                        } else if (showToast) {
                            Toast.makeText(this, response.optString("message", "Gagal menyimpan penyakit"), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        if (showToast) {
                            Toast.makeText(this, "Gagal menyimpan riwayat penyakit", Toast.LENGTH_SHORT).show();
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
            if (showToast) {
                Toast.makeText(this, "Gagal membuat request penyakit", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void tampilkanPenyakitDariServer(JSONArray penyakitArray) {
        if (penyakitArray == null) {
            tampilkanPenyakitLocal();
            return;
        }

        selectedPenyakitIds.clear();

        for (int i = 0; i < penyakitArray.length(); i++) {
            JSONObject item = penyakitArray.optJSONObject(i);

            if (item == null) {
                continue;
            }

            int id = item.optInt("id", item.optInt("penyakit_id", 0));

            if (id > 0 && !selectedPenyakitIds.contains(id)) {
                selectedPenyakitIds.add(id);
            }
        }

        tampilkanPenyakitLocal();
    }

    private void tampilkanPenyakitLocal() {
        if (tvPenyakitHariIni != null) {
            tvPenyakitHariIni.setText(formatPenyakitSelected("Tidak ada"));
        }
    }

    private String formatPenyakitSelected(String emptyText) {
        if (selectedPenyakitIds == null || selectedPenyakitIds.isEmpty()) {
            return emptyText;
        }

        ArrayList<String> nama = new ArrayList<>();

        for (Integer id : selectedPenyakitIds) {
            String n = getNamaPenyakitById(id);
            if (n != null && !n.trim().isEmpty()) {
                nama.add(n);
            }
        }

        if (nama.isEmpty()) {
            return emptyText;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nama.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(nama.get(i));

            if (i == 2 && nama.size() > 3) {
                sb.append(" +").append(nama.size() - 3).append(" lainnya");
                break;
            }
        }

        return sb.toString();
    }

    private String getNamaPenyakitById(int id) {
        if (listPenyakit != null) {
            for (int i = 0; i < listPenyakit.length(); i++) {
                JSONObject item = listPenyakit.optJSONObject(i);
                if (item != null && item.optInt("id", 0) == id) {
                    return item.optString("nama_penyakit", "-");
                }
            }
        }

        return "Penyakit ID " + id;
    }

    private void getStatusRekomendasi() {
        String token = session.getToken();

        try {
            JSONObject body = new JSONObject();
            body.put("email", session.getEmail());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.REKOMENDASI_STATUS,
                    body,
                    response -> {
                        boolean status = response.optBoolean("status", false);

                        if (!status) {
                            return;
                        }

                        boolean sudahInput = response.optBoolean("sudah_input", false);
                        boolean sudahRekomendasi = response.optBoolean("sudah_rekomendasi", false);

                        rekomendasiSudahAda = sudahRekomendasi;
                        aturTombolAksi(sudahInput || sudahRekomendasi);

                        JSONObject inputHarian = response.optJSONObject("input_harian");

                        if (sudahInput && inputHarian != null) {
                            tampilkanInputHarianDariServer(inputHarian);
                        }

                        JSONObject infoFisik = response.optJSONObject("info_fisik");
                        tampilkanInfoFisikDariServer(infoFisik);

                        tampilkanPenyakitDariServer(response.optJSONArray("penyakit"));

                        if (sudahRekomendasi) {
                            rekomendasiSudahAda = true;

                            dataPerhitunganManual = response.optJSONObject("perhitungan");
                            dataRekomendasi7Hari = response.optJSONArray("rekomendasi_7_hari");

                            JSONObject rataRata = response.optJSONObject("rata_rata_7_hari");

                            tampilkanRataRata7Hari(rataRata);
                            tampilkanCardRekomendasi7Hari(dataRekomendasi7Hari);

                            btnSaranMenu.setText("Lihat Perhitungan");
                            btnUlangiRekomendasi.setVisibility(View.VISIBLE);
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Toast.makeText(this, "Status error: " + error.networkResponse.statusCode, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Gagal mengambil status rekomendasi", Toast.LENGTH_SHORT).show();
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
        }
    }

    private void tampilkanInputHarianDariServer(JSONObject inputHarian) {
        try {
            sarapanList = jsonArrayToMakananList(inputHarian.optJSONArray("sarapan"));
            makanSiangList = jsonArrayToMakananList(inputHarian.optJSONArray("makan_siang"));
            makanMalamList = jsonArrayToMakananList(inputHarian.optJSONArray("makan_malam"));
            snackList = jsonArrayToMakananList(inputHarian.optJSONArray("snack"));

            hitungDanTampilkan();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<MakananItem> jsonArrayToMakananList(JSONArray array) {
        ArrayList<MakananItem> list = new ArrayList<>();

        if (array == null) {
            return list;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);

            if (o == null) {
                continue;
            }

            list.add(new MakananItem(
                    o.optInt("makanan_id", o.optInt("id", 0)),
                    o.optInt("kategori_id"),
                    o.optString("nama", o.optString("nama_makanan", "-")),
                    o.optDouble("kkal", 0),
                    o.optDouble("protein", 0),
                    o.optDouble("karbohidrat", 0),
                    o.optDouble("lemak", 0),
                    o.optDouble("serat", 0),
                    o.optDouble("gula", 0),
                    o.optDouble("natrium", 0)
            ));
        }

        return list;
    }

    private android.graphics.drawable.Drawable getSelectableItemBackground() {
        android.util.TypedValue outValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        return getResources().getDrawable(outValue.resourceId);
    }

    private void showDialogOpsiRekomendasiHari(JSONObject hariObj) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(18));
        outer.setBackgroundColor(Color.WHITE);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dpToPx(12));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams titleBoxParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );

        TextView title = new TextView(this);
        title.setText("Rekomendasi Hari " + hariObj.optInt("hari_ke", 0));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary));

        TextView subtitle = new TextView(this);
        subtitle.setText(hariObj.optString("tanggal", "-") + " • 5 pilihan menu");
        subtitle.setTextSize(12);
        subtitle.setTextColor(getResources().getColor(R.color.text_secondary));
        subtitle.setPadding(0, dpToPx(3), 0, 0);

        titleBox.addView(title);
        titleBox.addView(subtitle);

        TextView close = new TextView(this);
        close.setText("✕");
        close.setTextSize(18);
        close.setGravity(Gravity.CENTER);
        close.setTextColor(getResources().getColor(R.color.text_secondary));
        close.setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6));
        close.setOnClickListener(v -> dialog.dismiss());

        header.addView(titleBox, titleBoxParams);
        header.addView(close);

        outer.addView(header);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        JSONArray rekomendasiArray = hariObj.optJSONArray("rekomendasi");

        if (rekomendasiArray != null) {
            for (int i = 0; i < rekomendasiArray.length(); i++) {
                JSONObject opsi = rekomendasiArray.optJSONObject(i);

                if (opsi == null) {
                    continue;
                }

                tambahCardOpsiDialog(content, opsi, i == 0);
            }
        }

        scrollView.addView(content);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );

        outer.addView(scrollView, scrollParams);

        dialog.setContentView(outer);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = (int) (metrics.widthPixels * 0.94);
            int height = (int) (metrics.heightPixels * 0.88);

            window.setLayout(width, height);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void tambahCardOpsiDialog(LinearLayout parent, JSONObject opsi, boolean utama) {
        androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
        card.setRadius(dpToPx(18));
        card.setCardElevation(dpToPx(2));
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvOpsi = new TextView(this);
        tvOpsi.setText("Opsi " + opsi.optInt("rekomendasi_ke", 0));
        tvOpsi.setTextSize(15);
        tvOpsi.setTypeface(null, Typeface.BOLD);
        tvOpsi.setTextColor(getResources().getColor(R.color.text_primary));

        LinearLayout.LayoutParams opsiParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );

        top.addView(tvOpsi, opsiParams);

        if (utama) {
            TextView badge = new TextView(this);
            badge.setText("Terbaik");
            badge.setTextSize(10);
            badge.setTypeface(null, Typeface.BOLD);
            badge.setTextColor(0xFF047857);
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));
            badge.setBackground(buatRoundDrawable(0xFFD1FAE5, dpToPx(20)));
            top.addView(badge);
        }
        box.addView(top);

        tambahBarisMenuModern(box, "🍳", "Sarapan", opsi, "sarapan");
        tambahBarisMenuModern(box, "🍱", "Makan Siang", opsi, "makan_siang");
        tambahBarisMenuModern(box, "🍽️", "Makan Malam", opsi, "makan_malam");
        tambahBarisMenuModern(box, "🍎", "Cemilan", opsi, "snack");

        LinearLayout totalBox = new LinearLayout(this);
        totalBox.setOrientation(LinearLayout.HORIZONTAL);
        totalBox.setGravity(Gravity.CENTER_VERTICAL);
        totalBox.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
        totalBox.setBackground(buatRoundDrawable(utama ? 0xFFF0FDF4 : 0xFFEFF6FF, dpToPx(16)));

        LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        totalParams.setMargins(0, dpToPx(8), 0, 0);

        TextView label = new TextView(this);
        label.setText("Total");
        label.setTextSize(13);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(getResources().getColor(R.color.text_primary));

        TextView value = new TextView(this);
        value.setText(formatKkal(opsi.optDouble("total_kkal", 0)) + " kkal");
        value.setTextSize(14);
        value.setTypeface(null, Typeface.BOLD);
        value.setTextColor(getResources().getColor(R.color.user_primary));

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );

        totalBox.addView(label, labelParams);
        totalBox.addView(value);

        box.addView(totalBox, totalParams);

        tambahRingkasanGizi(box, opsi);

        double skor = opsi.optDouble("skor", 0);
        TextView tvSkor = new TextView(this);
        tvSkor.setText("Loss Score: " + String.format(Locale.US, "%.4f", skor));
        tvSkor.setTextSize(11);
        tvSkor.setTextColor(getResources().getColor(R.color.text_secondary));
        tvSkor.setGravity(Gravity.END);
        tvSkor.setPadding(0, dpToPx(8), 0, 0);
        box.addView(tvSkor);

        card.addView(box);
        parent.addView(card, cardParams);
    }

    private void tampilkanInfoFisikDariServer(JSONObject infoFisik) {
        if (infoFisik == null) {
            return;
        }

        tbHariIni = infoFisik.optDouble("tb", tbHariIni);
        bbHariIni = infoFisik.optDouble("bb", bbHariIni);
        bmiHariIni = infoFisik.optDouble("bmi", 0);
        kategoriBmiHariIni = infoFisik.optString("kategori_bmi", "-");

        tampilkanInfoFisikLocal();
    }

    private void tampilkanInfoFisikLocal() {
        tvTBHariIni.setText(tbHariIni > 0 ? formatAngka(tbHariIni) + " cm" : "- cm");
        tvBBHariIni.setText(bbHariIni > 0 ? formatAngka(bbHariIni) + " kg" : "- kg");

        if (bmiHariIni > 0) {
            tvBMIHariIni.setText(String.format(Locale.US, "%.2f", bmiHariIni));
            tvKategoriBMIHariIni.setText(kategoriBmiHariIni);
        } else if (tbHariIni > 0 && bbHariIni > 0) {
            double tbMeter = tbHariIni / 100.0;
            double bmi = bbHariIni / (tbMeter * tbMeter);

            tvBMIHariIni.setText(String.format(Locale.US, "%.2f", bmi));
            tvKategoriBMIHariIni.setText("Dihitung setelah disimpan");
        } else {
            tvBMIHariIni.setText("-");
            tvKategoriBMIHariIni.setText("-");
        }
    }

    private String formatAngka(double value) {
        if (value == (long) value) {
            return String.format(Locale.US, "%d", (long) value);
        }

        return String.format(Locale.US, "%.1f", value);
    }

    private void resetRekomendasi() {
        String token = session.getToken();

        try {
            JSONObject body = new JSONObject();
            body.put("email", session.getEmail());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.RESET_REKOMENDASI,
                    body,
                    response -> {
                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            rekomendasiSudahAda = false;
                            dataPerhitunganManual = null;
                            dataRekomendasi7Hari = null;

                            sarapanList.clear();
                            makanSiangList.clear();
                            makanMalamList.clear();
                            snackList.clear();

                            totalSarapan = 0;
                            totalMakanSiang = 0;
                            totalMakanMalam = 0;
                            totalSnack = 0;

                            tvJudulCardRekomendasi.setText("Input Makanan Hari Ini");
                            tvTanggalCatatan.setText("Pilih makanan yang sudah dikonsumsi, bisa lebih dari satu makanan");
                            tvStatusInputHarian.setText("Belum lengkap");

                            tvSarapanHariIni.setText("-");
                            tvMakanSiangHariIni.setText("-");
                            tvMakanMalamHariIni.setText("-");
                            tvSnackHariIni.setText("-");

                            tvKaloriSarapan.setText("0 kkal");
                            tvKaloriMakanSiang.setText("0 kkal");
                            tvKaloriMakanMalam.setText("0 kkal");
                            tvKaloriSnack.setText("0 kkal");
                            tvTotalKaloriHariIni.setText("0 kkal");

                            llRekomendasi7Hari.removeAllViews();
                            llRekomendasi7Hari.setVisibility(View.GONE);

                            btnSaranMenu.setText("Saran Menu");
                            btnUlangiRekomendasi.setVisibility(View.GONE);

                            unlockButton(btnUlangiRekomendasi);
                            aturTombolAksi(false);

                            Toast.makeText(this, "Data berhasil diulang. Silakan input baru.", Toast.LENGTH_SHORT).show();
                        } else {
                            unlockButton(btnUlangiRekomendasi);
                            Toast.makeText(this, response.optString("message", "Gagal mengulang data"), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        unlockButton(btnUlangiRekomendasi);
                        if (error.networkResponse != null) {
                            Toast.makeText(this, "Error server: " + error.networkResponse.statusCode, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Gagal membuat request reset", Toast.LENGTH_SHORT).show();
            unlockButton(btnSaranMenu);
        }
    }

    private void showDialogPilihPenyakit() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_pilih_penyakit);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        EditText edCariPenyakit = dialog.findViewById(R.id.edCariPenyakit);
        LinearLayout llListPenyakit = dialog.findViewById(R.id.llListPenyakit);
        TextView btnBatal = dialog.findViewById(R.id.btnBatalPenyakit);
        TextView btnSimpan = dialog.findViewById(R.id.btnSimpanPenyakit);

        ArrayList<Integer> selectedTemp = new ArrayList<>(selectedPenyakitIds);

        tampilkanListPenyakitCheckbox(llListPenyakit, semuaPenyakitArray, selectedTemp, "");

        edCariPenyakit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tampilkanListPenyakitCheckbox(
                        llListPenyakit,
                        semuaPenyakitArray,
                        selectedTemp,
                        s.toString()
                );
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            selectedPenyakitIds.clear();
            selectedPenyakitIds.addAll(selectedTemp);

            simpanPenyakitUser(selectedPenyakitIds);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void tambahInfoGizi(LinearLayout parent, JSONObject data) {
        double protein = data.optDouble("total_protein", 0);
        double karbohidrat = data.optDouble("total_karbohidrat", 0);
        double lemak = data.optDouble("total_lemak", 0);
        double serat = data.optDouble("total_serat", 0);
        double gula = data.optDouble("total_gula", 0);
        double natrium = data.optDouble("total_natrium", 0);

        TextView tvGizi = new TextView(this);
        tvGizi.setText(
                "Protein: " + formatSatuAngka(protein) + " g\n" +
                        "Karbohidrat: " + formatSatuAngka(karbohidrat) + " g\n" +
                        "Lemak: " + formatSatuAngka(lemak) + " g\n" +
                        "Serat: " + formatSatuAngka(serat) + " g\n" +
                        "Gula: " + formatSatuAngka(gula) + " g\n" +
                        "Natrium: " + formatSatuAngka(natrium) + " mg"
        );
        tvGizi.setTextSize(11);
        tvGizi.setTextColor(getResources().getColor(R.color.text_secondary));
        tvGizi.setPadding(12, 10, 12, 10);
        tvGizi.setBackgroundColor(0xFFF9FAFB);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 0);

        parent.addView(tvGizi, params);
    }

    private String formatSatuAngka(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private void tampilkanListPenyakitCheckbox(
            LinearLayout parent,
            JSONArray data,
            ArrayList<Integer> selectedTemp,
            String keyword
    ) {
        parent.removeAllViews();

        String cari = keyword == null ? "" : keyword.toLowerCase().trim();

        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.optJSONObject(i);

            if (item == null) {
                continue;
            }

            int id = item.optInt("id");
            String nama = item.optString("nama_penyakit", "-");
            String kataKunci = item.optString("kata_kunci", "");
            String anjuran = item.optString("anjuran", "");
            String hindari = item.optString("hindari", "");

            boolean cocok = cari.isEmpty()
                    || nama.toLowerCase().contains(cari)
                    || kataKunci.toLowerCase().contains(cari)
                    || anjuran.toLowerCase().contains(cari)
                    || hindari.toLowerCase().contains(cari);

            if (!cocok) {
                continue;
            }

            CheckBox cb = new CheckBox(this);
            cb.setText(nama);
            cb.setTextSize(13);
            cb.setTextColor(getResources().getColor(R.color.text_primary));
            cb.setPadding(4, 8, 4, 8);
            cb.setChecked(selectedTemp.contains(id));

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedTemp.contains(id)) {
                        selectedTemp.add(id);
                    }
                } else {
                    selectedTemp.remove(Integer.valueOf(id));
                }
            });

            parent.addView(cb);
        }

        if (parent.getChildCount() == 0) {
            TextView kosong = new TextView(this);
            kosong.setText("Penyakit tidak ditemukan");
            kosong.setTextSize(13);
            kosong.setTextColor(getResources().getColor(R.color.text_secondary));
            kosong.setPadding(8, 16, 8, 16);
            parent.addView(kosong);
        }
    }

    private void getPenyakit() {
        String token = session.getToken();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.PENYAKIT,
                null,
                response -> {
                    boolean status = response.optBoolean("status", false);

                    if (status) {
                        semuaPenyakitArray = response.optJSONArray("data");

                        if (semuaPenyakitArray == null) {
                            semuaPenyakitArray = new JSONArray();
                        }
                    } else {
                        Toast.makeText(this, response.optString("message", "Gagal mengambil penyakit"), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        String errorBody = new String(error.networkResponse.data);
                        android.util.Log.e("PENYAKIT_ERROR", errorBody);
                        Toast.makeText(this, "Gagal mengambil penyakit: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
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

    private void simpanPenyakitUser(ArrayList<Integer> penyakitIds) {
        String token = session.getToken();

        try {
            JSONObject body = new JSONObject();
            body.put("email", session.getEmail());

            JSONArray array = new JSONArray();
            for (int id : penyakitIds) {
                array.put(id);
            }

            body.put("penyakit_ids", array);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.UPDATE_PENYAKIT_USER,
                    body,
                    response -> {
                        boolean status = response.optBoolean("status", false);

                        if (status) {
                            Toast.makeText(this, "Riwayat penyakit berhasil disimpan", Toast.LENGTH_SHORT).show();

                            // Ambil ulang status agar card penyakit di halaman ikut update
                            getStatusRekomendasi();

                        } else {
                            Toast.makeText(
                                    this,
                                    response.optString("message", "Gagal menyimpan riwayat penyakit"),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);

                            android.util.Log.e("SIMPAN_PENYAKIT_ERROR", "Code: " + error.networkResponse.statusCode);
                            android.util.Log.e("SIMPAN_PENYAKIT_ERROR", errorBody);

                            Toast.makeText(
                                    this,
                                    "Gagal menyimpan penyakit: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            android.util.Log.e("SIMPAN_PENYAKIT_ERROR", String.valueOf(error));
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
            Toast.makeText(this, "Gagal membuat data penyakit", Toast.LENGTH_SHORT).show();
        }
    }
}