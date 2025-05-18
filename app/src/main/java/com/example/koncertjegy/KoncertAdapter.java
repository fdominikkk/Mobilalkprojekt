package com.example.koncertjegy;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.koncertjegy.EditKoncertActivity;
import com.example.koncertjegy.R;
import com.example.koncertjegy.Koncert;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KoncertAdapter extends RecyclerView.Adapter<KoncertAdapter.KoncertViewHolder> {
    private List<Koncert> koncertek;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isAdmin;

    public KoncertAdapter(List<Koncert> koncertek) {
        this.koncertek = koncertek;
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.isAdmin = false;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KoncertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_koncert, parent, false);
        return new KoncertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KoncertViewHolder holder, int position) {
        Koncert koncert = koncertek.get(position);
        holder.textViewNev.setText(koncert.getNev());
        holder.textViewHelyszin.setText(koncert.getHelyszin());
        holder.textViewDatum.setText(koncert.getDatum());
        holder.textViewAra.setText(String.format("%.2f Ft", koncert.getJegyAra()));

        // Kép betöltése Glide-dal
        if (koncert.getKepUrl() != null && !koncert.getKepUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(koncert.getKepUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.imageViewKoncert);
        } else {
            holder.imageViewKoncert.setImageResource(R.drawable.placeholder_image);
        }

        if (isAdmin) {
            holder.buttonEdit.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonBuyTicket.setVisibility(View.GONE);

            holder.buttonEdit.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), EditKoncertActivity.class);
                intent.putExtra("koncertId", koncert.getId());
                holder.itemView.getContext().startActivity(intent);
            });

            holder.buttonDelete.setOnClickListener(v -> {
                db.collection("koncertek")
                        .document(koncert.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(holder.itemView.getContext(), koncert.getNev() + " törölve!", Toast.LENGTH_SHORT).show();
                            koncertek.remove(position);
                            notifyItemRemoved(position);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(holder.itemView.getContext(), "Hiba a törléskor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            holder.buttonEdit.setVisibility(View.GONE);
            holder.buttonDelete.setVisibility(View.GONE);
            holder.buttonBuyTicket.setVisibility(View.VISIBLE);

            holder.buttonBuyTicket.setOnClickListener(v -> {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(holder.itemView.getContext(), "Kérlek, jelentkezz be!", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("koncertek")
                        .document(koncert.getId())
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists() && document.getLong("elerhetoJegyek") > 0) {
                                String userId = mAuth.getCurrentUser().getUid();
                                Map<String, Object> kosarItem = new HashMap<>();
                                kosarItem.put("userId", userId);
                                kosarItem.put("koncertId", koncert.getId());
                                kosarItem.put("koncertNev", koncert.getNev());
                                kosarItem.put("koncertDatum", koncert.getDatum());
                                kosarItem.put("jegyAra", koncert.getJegyAra());
                                kosarItem.put("createdAt", com.google.firebase.Timestamp.now());

                                db.collection("kosar")
                                        .add(kosarItem)
                                        .addOnSuccessListener(docRef -> {
                                            db.collection("koncertek")
                                                    .document(koncert.getId())
                                                    .update("elerhetoJegyek", document.getLong("elerhetoJegyek") - 1)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(holder.itemView.getContext(), koncert.getNev() + " hozzáadva a kosárhoz!", Toast.LENGTH_SHORT).show();
                                                        // Navigáció a CartFragment-re
                                                        Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                                        intent.putExtra("navigateTo", "cart");
                                                        holder.itemView.getContext().startActivity(intent);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(holder.itemView.getContext(), "Hiba a jegyek frissítésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(holder.itemView.getContext(), "Hiba a kosárba helyezéskor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(holder.itemView.getContext(), "Nincs elérhető jegy ehhez a koncerthez!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(holder.itemView.getContext(), "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        }
    }

    @Override
    public int getItemCount() {
        return koncertek.size();
    }

    public static class KoncertViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNev, textViewHelyszin, textViewDatum, textViewAra;
        ImageView imageViewKoncert;
        Button buttonBuyTicket, buttonEdit, buttonDelete;

        public KoncertViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNev = itemView.findViewById(R.id.textViewNev);
            textViewHelyszin = itemView.findViewById(R.id.textViewHelyszin);
            textViewDatum = itemView.findViewById(R.id.textViewDatum);
            textViewAra = itemView.findViewById(R.id.textViewAra);
            imageViewKoncert = itemView.findViewById(R.id.imageViewKoncert);
            buttonBuyTicket = itemView.findViewById(R.id.buttonBuyTicket);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}