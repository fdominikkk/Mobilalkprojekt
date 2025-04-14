package com.example.koncertjegy;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();

        MaterialCardView cardWelcome = view.findViewById(R.id.cardWelcome);
        MaterialCardView cardContent = view.findViewById(R.id.cardContent);
        TextView textViewWelcome = view.findViewById(R.id.textViewWelcome);

        String fullName = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getDisplayName() : null;
        if (fullName != null && !fullName.isEmpty()) {
            textViewWelcome.setText("Üdvözöljük, " + fullName + "!");
        } else {
            textViewWelcome.setText("Üdvözöljük a Koncertjegy App-ban!");
        }

        animateCard(cardWelcome, 0);
        animateCard(cardContent, 200);

        return view;
    }

    private void animateCard(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(100f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(500),
                ObjectAnimator.ofFloat(view, "translationY", 100f, 0f).setDuration(500)
        );
        animatorSet.setStartDelay(delay);
        animatorSet.start();
    }
}