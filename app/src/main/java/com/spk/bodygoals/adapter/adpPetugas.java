package com.spk.bodygoals.adapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.spk.bodygoals.R;
import com.spk.bodygoals.admin.DetailDataPetugas;
import com.spk.bodygoals.admin.ProfilAdmin;
import com.spk.bodygoals.model.mPetugas;
import com.spk.bodygoals.service.ApiConfig;

import java.util.ArrayList;

public class adpPetugas extends RecyclerView.Adapter<adpPetugas.ViewHolder> {
    private Context context;
    private ArrayList<mPetugas> list;

    public adpPetugas(Context context, ArrayList<mPetugas> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public adpPetugas.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_petugas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull adpPetugas.ViewHolder holder, int position) {
        mPetugas model = list.get(position);

        holder.nik.setText(model.getId());
        holder.nama.setText(model.getNama());
        holder.role.setText(model.getRole());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setCancelable(false);

                View popup = LayoutInflater.from(context).inflate(R.layout.dialog_konfirmasi, null);
                builder.setView(popup);

                final AlertDialog alertDialog = builder.create();

                if (alertDialog.getWindow() != null) {
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                TextView info = popup.findViewById(R.id.txtTanyaKonfirmasi);
                info.setText(model.getNama() +"\n"+ model.getRole());

                popup.findViewById(R.id.btnLihatKonfirmasi).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                popup.findViewById(R.id.btnHapusKonfirmasi).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        konfirmasiHapus(model.getId(), model.getNama(), position);
                        alertDialog.cancel();
                    }
                });

                popup.findViewById(R.id.btnBatalKonfirmasi).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });

                alertDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nik, nama, role;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardPetugas);
            nik = itemView.findViewById(R.id.petugas_nik);
            nama = itemView.findViewById(R.id.petugas_nama);
            role = itemView.findViewById(R.id.petugas_role);
        }
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
        info.setText("Anda yakin ingin menghapus data ini ?\n" + nama);

        popup.findViewById(R.id.btnYaInfo).setOnClickListener(v -> {
            if (id == null || id.trim().isEmpty()) {
                Toast.makeText(context, "ID/NIK petugas kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            if (position >= 0 && position < list.size()) {
                hapusPetugasDariDatabase(id, position, alertDialog);
            }
        });

        popup.findViewById(R.id.btnTidakInfo).setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void hapusPetugasDariDatabase(String id, int position, AlertDialog alertDialog) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.ADMIN_PETUGAS_HAPUS,
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
                    String pesan = "Gagal menghapus data dari database";

                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;

                        if (error.networkResponse.data != null) {
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");
                                android.util.Log.e("HAPUS_PETUGAS", "HTTP " + code + " BODY: " + body);

                                JSONObject obj = new JSONObject(body);
                                pesan = obj.optString("message", pesan);
                            } catch (Exception e) {
                                pesan = "Gagal menghapus data. HTTP " + code;
                            }
                        } else {
                            pesan = "Gagal menghapus data. HTTP " + code;
                        }
                    } else {
                        android.util.Log.e("HAPUS_PETUGAS", String.valueOf(error));
                        pesan = "Tidak dapat terhubung ke server";
                    }

                    Toast.makeText(context, pesan, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);

                android.util.Log.d("HAPUS_PETUGAS_ID", id);
                android.util.Log.d("HAPUS_PETUGAS_URL", ApiConfig.ADMIN_PETUGAS_HAPUS);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
