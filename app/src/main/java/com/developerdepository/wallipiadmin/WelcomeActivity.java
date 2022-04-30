package com.developerdepository.wallipiadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    private TextView welcomeFeedback;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        firebaseAuth = FirebaseAuth.getInstance();

        welcomeFeedback = findViewById(R.id.welcome_feedback);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            welcomeFeedback.setText("Creating Account..");
            firebaseAuth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    welcomeFeedback.setText("Account Created!");
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(WelcomeActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }
    }
}