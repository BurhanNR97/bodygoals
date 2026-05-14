package com.spk.bodygoals.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.spk.bodygoals.R;
import com.spk.bodygoals.admin.DetailMakanan;
import com.spk.bodygoals.model.mMakanan;

import java.util.ArrayList;

public class adpMakanan extends RecyclerView.Adapter<adpMakanan.ViewHolder> {
    private Context context;
    private ArrayList<mMakanan> list;

    public adpMakanan(Context context, ArrayList<mMakanan> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public adpMakanan.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_makanan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull adpMakanan.ViewHolder holder, int position) {
        mMakanan model = list.get(position);

        holder.tvNama.setText(model.getNama());
        holder.tvKategori.setText(model.getKategori());

        holder.cardView.setOnClickListener(v -> {
             Intent intent = new Intent(context, DetailMakanan.class);
             intent.putExtra("id", model.getId());
             context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvNama, tvKategori;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardMakanan);
            tvNama = itemView.findViewById(R.id.tvNamaMakanan);
            tvKategori = itemView.findViewById(R.id.tvKategoriMakanan);
        }
    }
}