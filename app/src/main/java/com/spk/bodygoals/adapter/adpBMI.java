package com.spk.bodygoals.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.R;
import com.spk.bodygoals.model.mBMI;
import com.spk.bodygoals.model.mPasien;
import com.spk.bodygoals.service.ApiConfig;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class adpBMI extends RecyclerView.Adapter<adpBMI.ViewHolder> {
    private Context context;
    private ArrayList<mBMI> list;

    public adpBMI(Context context, ArrayList<mBMI> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public adpBMI.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bmi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull adpBMI.ViewHolder holder, int position) {
        mBMI model = list.get(position);

        holder.id.setText((model.getId()));
        holder.kategori.setText(model.getNama());
        holder.nilai.setText(model.getBmi());

        holder.cardView.setOnClickListener(v -> tampilDialogPasien(model, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView id, kategori, nilai;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardBMI);
            id = itemView.findViewById(R.id.bmi_id);
            kategori = itemView.findViewById(R.id.bmi_kategori);
            nilai = itemView.findViewById(R.id.bmi_nilai);
        }
    }

    private void tampilDialogPasien(mBMI model, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        builder.setCancelable(false);

        View popup = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        builder.setView(popup);

        final AlertDialog alertDialog = builder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        TextView info = popup.findViewById(R.id.txtTanyaInfo);
        info.setText(model.getNama() + "\n" + model.getBmi());
        AppCompatButton ya = popup.findViewById(R.id.btnYaInfo);

        ya.setText("Hapus");

        popup.findViewById(R.id.btnYaInfo).setOnClickListener(v -> {
            alertDialog.dismiss();
            konfirmasiHapus(model.getId(), model.getNama(), position);
        });

        popup.findViewById(R.id.btnTidakInfo).setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void konfirmasiHapus(String id, String nama, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        builder.setCancelable(false);

        View popup = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        builder.setView(popup);

        final AlertDialog alertDialog = builder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        TextView info = popup.findViewById(R.id.txtTanyaInfo);
        info.setText("Anda yakin ingin menghapus data ini?\n" + nama);

        popup.findViewById(R.id.btnYaInfo).setOnClickListener(v -> {
            if (id == null || id.trim().isEmpty()) {
                Toast.makeText(context, "ID kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            hapusPasienDariDatabase(id, position, alertDialog);
        });

        popup.findViewById(R.id.btnTidakInfo).setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void hapusPasienDariDatabase(String id, int position, AlertDialog alertDialog) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADMIN_BMI_HAPUS,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        boolean status = jsonObject.optBoolean("status", false);
                        String message = jsonObject.optString("message", "Terjadi kesalahan");

                        if (status) {
                            if (position >= 0 && position < list.size()) {
                                list.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, list.size());
                            }

                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(context, "Response tidak valid: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String pesan = "Gagal menghapus data pasien";

                    if (error.networkResponse != null) {
                        pesan = "Gagal menghapus data pasien. HTTP " + error.networkResponse.statusCode;
                    }

                    Toast.makeText(context, pesan, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }
}