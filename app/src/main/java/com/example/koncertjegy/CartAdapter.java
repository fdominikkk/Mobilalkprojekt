package com.example.koncertjegy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
import com.example.koncertjegy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<DocumentSnapshot> cartItems;
    private FirebaseFirestore db;
    private OnCartChangedListener onCartChangedListener;

    public interface OnCartChangedListener {
        void onCartChanged(double totalPrice);
    }

    public CartAdapter(List<DocumentSnapshot> cartItems, OnCartChangedListener listener) {
        this.cartItems = cartItems;
        this.db = FirebaseFirestore.getInstance();
        this.onCartChangedListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        DocumentSnapshot item = cartItems.get(position);
        String koncertNev = item.getString("koncertNev");
        String koncertDatum = item.getString("koncertDatum");
        double jegyAra = item.getDouble("jegyAra");
        String koncertId = item.getString("koncertId");

        holder.textViewCartItemName.setText(koncertNev);
        holder.textViewCartItemPrice.setText(String.format("%.2f Ft", jegyAra));
        holder.textViewCartItemDatum.setText(koncertDatum != null ? koncertDatum : "Nincs dátum");

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
                                .into(holder.imageViewCartItem);
                    } else {
                        holder.imageViewCartItem.setImageResource(R.drawable.placeholder_image);
                    }
                });

        holder.buttonRemove.setOnClickListener(v -> {
            // Alarm törlése
            cancelReminder(holder.itemView.getContext(), item.getId());

            // Kosár elem törlése
            db.collection("kosar").document(item.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        cartItems.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(holder.itemView.getContext(), koncertNev + " eltávolítva a kosárból!", Toast.LENGTH_SHORT).show();
                        updateTotalPrice();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "Hiba a törléskor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private void updateTotalPrice() {
        double totalPrice = 0;
        for (DocumentSnapshot item : cartItems) {
            totalPrice += item.getDouble("jegyAra");
        }
        if (onCartChangedListener != null) {
            onCartChangedListener.onCartChanged(totalPrice);
        }
    }

    private void cancelReminder(Context context, String itemId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, KoncertReminderReceiver.class);
        int requestCode = itemId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCartItem;
        TextView textViewCartItemName, textViewCartItemPrice, textViewCartItemDatum;
        Button buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCartItem = itemView.findViewById(R.id.imageViewCartItem);
            textViewCartItemName = itemView.findViewById(R.id.textViewCartItemName);
            textViewCartItemPrice = itemView.findViewById(R.id.textViewCartItemPrice);
            textViewCartItemDatum = itemView.findViewById(R.id.textViewCartItemDatum);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }
    }
}