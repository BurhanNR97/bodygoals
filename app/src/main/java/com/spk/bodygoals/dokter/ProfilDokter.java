package com.spk.bodygoals.dokter;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spk.bodygoals.LoginActivity;
import com.spk.bodygoals.R;
import com.spk.bodygoals.admin.ProfilAdmin;
import com.spk.bodygoals.service.ApiConfig;
import com.spk.bodygoals.service.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class ProfilDokter extends AppCompatActivity {
    ImageView btnBack;
    AppCompatButton logout;
    TextView nama, email;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profil_dokter);
        View v = findViewById(R.id.mainProfilDokter);
        final int initialPaddingLeft = v.getPaddingLeft();
        final int initialPaddingTop = v.getPaddingTop();
        final int initialPaddingRight = v.getPaddingRight();
        final int initialPaddingBottom = v.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainProfilDokter), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    initialPaddingLeft + systemBars.left,
                    initialPaddingTop + systemBars.top,
                    initialPaddingRight + systemBars.right,
                    initialPaddingBottom + systemBars.bottom
            );
            return insets;
        });

        session = new SessionManager(this);

        btnBack = findViewById(R.id.btnBackProfilDokter);
        logout = findViewById(R.id.btnLogouts);
        nama = findViewById(R.id.tvNamas);
        email = findViewById(R.id.tvEmails);

        nama.setText(session.getName());
        email.setText(session.getEmail());

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfilDokter.this, HomeDokter.class));
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signout();
            }
        });
    }

    private void signout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfilDokter.this, R.style.AlertDialogTheme);
        builder.setCancelable(false);
        View popup = LayoutInflater.from(ProfilDokter.this).inflate(R.layout.dialog_keluar, (ConstraintLayout) findViewById(R.id.layoutDialogContainerKeluar));
        builder.setView(popup);
        final AlertDialog alertDialog = builder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

        popup.findViewById(R.id.btnYaKeluar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
                alertDialog.cancel();
                finish();
            }
        });

        popup.findViewById(R.id.btnTidakKeluar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    private void logout() {
        String url = ApiConfig.getUrl("logout");

        SessionManager session = new SessionManager(this);
        String token = session.getToken();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    session.logout();

                    Toast.makeText(ProfilDokter.this, "Logout berhasil", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ProfilDokter.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    // Kalau token sudah expired atau server error, tetap hapus session lokal
                    session.logout();

                    Toast.makeText(ProfilDokter.this, "Sesi dihapus", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ProfilDokter.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}