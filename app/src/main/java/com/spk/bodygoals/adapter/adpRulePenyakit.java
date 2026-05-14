package com.spk.bodygoals.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.spk.bodygoals.R;
import com.spk.bodygoals.admin.DetailRulePenyakit;
import com.spk.bodygoals.model.mRulePenyakit;
import java.util.ArrayList;

public class adpRulePenyakit extends RecyclerView.Adapter<adpRulePenyakit.ViewHolder> {
    private Context context;
    private ArrayList<mRulePenyakit> list;

    public adpRulePenyakit(Context context, ArrayList<mRulePenyakit> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public adpRulePenyakit.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rule_penyakit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull adpRulePenyakit.ViewHolder holder, int position) {
        mRulePenyakit model = list.get(position);

        holder.tvNamaPenyakit.setText(model.getNamaPenyakit());
        holder.tvKataKunci.setText(model.getKataKunci());

        holder.cardRulePenyakit.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailRulePenyakit.class);
            intent.putExtra("id", model.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardRulePenyakit;
        TextView tvNamaPenyakit, tvKataKunci;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardRulePenyakit = itemView.findViewById(R.id.cardRulePenyakit);
            tvNamaPenyakit = itemView.findViewById(R.id.tvNamaPenyakit);
            tvKataKunci = itemView.findViewById(R.id.tvKataKunci);
        }
    }
}