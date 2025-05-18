package com.example.koncertjegy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PurchasedTicketsAdapter extends RecyclerView.Adapter<PurchasedTicketsAdapter.TicketViewHolder> {
    private List<DocumentSnapshot> purchasedTickets;
    private FirebaseFirestore db;

    public PurchasedTicketsAdapter(List<DocumentSnapshot> purchasedTickets) {
        this.purchasedTickets = purchasedTickets;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchased_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        DocumentSnapshot item = purchasedTickets.get(position);
        String koncertNev = item.getString("koncertNev");
        String koncertDatum = item.getString("koncertDatum");
        double jegyAra = item.getDouble("jegyAra");
        String koncertId = item.getString("koncertId");

        holder.textViewTicketName.setText(koncertNev);
        holder.textViewTicketDatum.setText(koncertDatum != null ? koncertDatum : "Nincs dátum");
        holder.textViewTicketPrice.setText(String.format("%.2f Ft", jegyAra));

        // Kép betöltése
        db.collection("koncertek").document(koncertId)
                .get()
                .addOnSuccessListener(document -> {
                    String kepUrl = document.getString("kepUrl");
                    if (kepUrl != null && !kepUrl.isEmpty()) {
                        Glide.with(holder.itemView.getContext())
                                .load(kepUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image)
                                .into(holder.imageViewTicket);
                    } else {
                        holder.imageViewTicket.setImageResource(R.drawable.placeholder_image);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return purchasedTickets.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewTicket;
        TextView textViewTicketName, textViewTicketDatum, textViewTicketPrice;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewTicket = itemView.findViewById(R.id.imageViewTicket);
            textViewTicketName = itemView.findViewById(R.id.textViewTicketName);
            textViewTicketDatum = itemView.findViewById(R.id.textViewTicketDatum);
            textViewTicketPrice = itemView.findViewById(R.id.textViewTicketPrice);
        }
    }
}