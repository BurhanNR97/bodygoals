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
                String[] menu = {"Lihat Data", "Hapus"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(model.getNama());
                builder.setItems(menu, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(context, DetailDataPetugas.class);
                        intent.putExtra("id", model.getId());
                        intent.putExtra("nama", model.getNama());
                        intent.putExtra("role", model.getRole());
                        context.startActivity(intent);
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    } else if (which == 1) {
                        konfirmasiHapus(model.getId(), model.getNama(), position);
                    }
                });
                builder.show();
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
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogTheme);
        builder.setCancelable(false);
        View popup = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        builder.setView(popup);
        final AlertDialog alertDialog = builder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

        TextView info = popup.findViewById(R.id.txtTanyaInfo);
        info.setText("Anda yakin ingin menghapus data ini ?\n" + nama);

        popup.findViewById(R.id.btnYaInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position >= 0 && position < list.size()) {
                    hapusPetugasDariDatabase(id, position, alertDialog);
                }
                alertDialog.cancel();
            }
        });

        popup.findViewById(R.id.btnTidakInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

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
                    Toast.makeText(context, "Gagal menghapus data dari database", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
