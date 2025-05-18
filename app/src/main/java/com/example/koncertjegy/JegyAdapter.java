package com.example.koncertjegy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.koncertjegy.R;
import com.example.koncertjegy.Jegy;
import java.util.List;

public class JegyAdapter extends RecyclerView.Adapter<JegyAdapter.JegyViewHolder> {
    private List<Jegy> jegyek;

    public JegyAdapter(List<Jegy> jegyek) {
        this.jegyek = jegyek;
    }

    @NonNull
    @Override
    public JegyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jegy, parent, false);
        return new JegyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JegyViewHolder holder, int position) {
        Jegy jegy = jegyek.get(position);
        holder.textViewKoncertId.setText("Koncert ID: " + jegy.getKoncertId());
        holder.textViewTipus.setText("Típus: " + jegy.getJegyTipus());
        holder.textViewAra.setText(String.format("Ár: %.2f Ft", jegy.getJegyAra()));
        holder.textViewDatum.setText("Vásárlás dátuma: " + jegy.getVasarlasDatuma());
    }

    @Override
    public int getItemCount() {
        return jegyek.size();
    }

    public static class JegyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewKoncertId, textViewTipus, textViewAra, textViewDatum;

        public JegyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewKoncertId = itemView.findViewById(R.id.textViewKoncertId);
            textViewTipus = itemView.findViewById(R.id.textViewTipus);
            textViewAra = itemView.findViewById(R.id.textViewAra);
            textViewDatum = itemView.findViewById(R.id.textViewDatum);
        }
    }
}