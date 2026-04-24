package com.example.smart_greenhouse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity{
    Button btnIrrigazioneON, btnIrrigazioneOFF, btnAutoOFF, btnAutoON;
    ImageView imgMascotte, imgStatoPianta;
    TextView txtUmidita, txtStatoPianta;
    MaterialCardView btnLog;
    boolean buio_status=false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            Intent intent=new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        btnIrrigazioneON=findViewById(R.id.btnIrrigazioneON);
        btnIrrigazioneOFF=findViewById(R.id.btnIrrigazioneOFF);

        btnAutoOFF=findViewById(R.id.btnAutoOFF);
        btnAutoON=findViewById(R.id.btnAutoON);
        btnLog=findViewById(R.id.btnLogout);

        imgMascotte=findViewById(R.id.imageViewMascotte);
        imgStatoPianta=findViewById(R.id.imageStatus);
        txtUmidita=findViewById(R.id.textPercentualeUmid);
        txtStatoPianta=findViewById(R.id.statusText);

        attivaListener();

        btnLog.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        //attiva irrigazione
        btnIrrigazioneOFF.setOnClickListener(view -> {
            DatabaseReference refPompa=FirebaseDatabase.getInstance().getReference("umidita/pompa_status");
            refPompa.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot dataSnapshot=task.getResult();
                    if(dataSnapshot.exists()){
                        String status=dataSnapshot.getValue(String.class);
                        Log.d("firebase", "Auto: "+status);
                        refPompa.setValue("OFF"); // Accendi l'automazione
                        btnIrrigazioneOFF.setVisibility(View.GONE);  // Nascondi questo bottone
                        btnIrrigazioneON.setVisibility(View.VISIBLE); // Fai apparire la Pausa Rossa
                    }
                    else Log.d("firebase", "Errore database: nessun valore trovato");
                }
                else Log.d("firebase", "Errore di connessione", task.getException());
            });
        });
        //disattiva irrigazione
        btnIrrigazioneON.setOnClickListener(view -> {
            DatabaseReference refPompa=FirebaseDatabase.getInstance().getReference("umidita/pompa_status");
            refPompa.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot dataSnapshot=task.getResult();
                    if(dataSnapshot.exists()){
                        String status=dataSnapshot.getValue(String.class);
                        Log.d("firebase", "Auto: "+status);
                        refPompa.setValue("ON"); // Accendi l'automazione
                        btnIrrigazioneON.setVisibility(View.GONE);  // Nascondi questo bottone
                        btnIrrigazioneOFF.setVisibility(View.VISIBLE); // Fai apparire la Pausa Rossa
                    }
                    else Log.d("firebase", "Errore database: nessun valore trovato");
                }
                else Log.d("firebase", "Errore di connessione", task.getException());
            });
        });

        //attiva auto
        btnAutoOFF.setOnClickListener(view -> {
            DatabaseReference refAuto=FirebaseDatabase.getInstance().getReference("auto_status");
            refAuto.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot dataSnapshot=task.getResult();
                    if(dataSnapshot.exists()){
                        boolean status=dataSnapshot.getValue(Boolean.class);
                        Log.d("firebase", "Auto: "+status);
                        refAuto.setValue(true); // Accendi l'automazione
                        btnAutoOFF.setVisibility(View.GONE);  // Nascondi questo bottone
                        btnAutoON.setVisibility(View.VISIBLE); // Fai apparire la Pausa Rossa
                    }
                    else Log.d("firebase", "Errore database: nessun valore trovato");
                }
                else Log.d("firebase", "Errore di connessione", task.getException());
            });
        });
        //disattiva auto
        btnAutoON.setOnClickListener(view -> {
            DatabaseReference refAuto=FirebaseDatabase.getInstance().getReference("auto_status");
            refAuto.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot dataSnapshot=task.getResult();
                    if(dataSnapshot.exists()){
                        boolean status=dataSnapshot.getValue(Boolean.class);
                        Log.d("firebase", "Auto: "+status);
                        refAuto.setValue(false); // Spegni l'automazione (ERRORE CORRETTO: prima c'era true)
                        btnAutoON.setVisibility(View.GONE); // Nascondi la pausa rossa
                        btnAutoOFF.setVisibility(View.VISIBLE); // Fai riapparire il bianco
                    }
                    else Log.d("firebase", "Errore database: nessun valore trovato");
                }
                else Log.d("firebase", "Errore di connessione", task.getException());
            });
        });

        //logout
        btnLog.setOnClickListener(view -> {
            try{
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (Exception e){
                Log.e("firebase", "Errore durante il logout: " + e.getMessage());
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
     private void attivaListener(){
        DatabaseReference refAuto=FirebaseDatabase.getInstance().getReference("auto_status");
        DatabaseReference refUmidita=FirebaseDatabase.getInstance().getReference("umidita");
        DatabaseReference refLuci=FirebaseDatabase.getInstance().getReference("luci");

        refAuto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot){
                if (isFinishing() || isDestroyed() || FirebaseAuth.getInstance().getCurrentUser()==null) return;
                if(snapshot.exists()){
                    boolean status=snapshot.getValue(Boolean.class);
                    if(status){
                        btnAutoOFF.setVisibility(View.GONE);
                        btnAutoON.setVisibility(View.VISIBLE);
                        Log.d("firebase", "auto on");
                    }
                    else{
                        btnAutoOFF.setVisibility(View.VISIBLE);
                        btnAutoON.setVisibility(View.GONE);
                        Log.d("firebase", "auto on");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error){}
        });
        refUmidita.addChildEventListener(new ChildEventListener(){
             @Override
             public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){
                 if (isFinishing() || isDestroyed() || FirebaseAuth.getInstance().getCurrentUser()==null) return;
                 cambiamentiUmidita(snapshot);
             }
             @Override
             public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){
                 if (isFinishing() || isDestroyed() || FirebaseAuth.getInstance().getCurrentUser()==null) return;
                 cambiamentiUmidita(snapshot);
             }
             @Override
             public void onChildRemoved(@NonNull DataSnapshot snapshot){}
             @Override
             public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){}
             @Override
             public void onCancelled(@NonNull DatabaseError error){}
         });
        refLuci.addChildEventListener(new ChildEventListener(){
             @Override
             public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){
                 if (isFinishing() || isDestroyed() || FirebaseAuth.getInstance().getCurrentUser()==null) return;
                 cambiamentiLuci(snapshot);
             }
             @Override
             public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){
                 if (isFinishing() || isDestroyed() || FirebaseAuth.getInstance().getCurrentUser()==null) return;
                 cambiamentiLuci(snapshot);
             }
             @Override
             public void onChildRemoved(@NonNull DataSnapshot snapshot){}
             @Override
             public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){}
             @Override
             public void onCancelled(@NonNull DatabaseError error){}
         });
    }
    private void cambiamentiUmidita(DataSnapshot snapshot){
        if(snapshot.exists()){
            String chiave=snapshot.getKey();
            switch(chiave){
                case "sensore":
                    int percentuale=snapshot.getValue(Integer.class);
                    txtUmidita.setText(percentuale+"%");
                    if(percentuale<60){
                        imgMascotte.setImageResource(R.mipmap.secco);
                        if(percentuale<40){
                            imgStatoPianta.setImageResource(R.drawable.ic_error_circle);
                            txtStatoPianta.setText("UMIDITA' MOLTO BASSA");
                        }
                        else if(percentuale>40 && percentuale<60){
                            imgStatoPianta.setImageResource(R.drawable.ic_warning_circle);
                            txtStatoPianta.setText("UMIDITA' BASSA");
                        }
                    }
                    else{
                        imgMascotte.setImageResource(R.mipmap.normale);
                        imgStatoPianta.setImageResource(R.drawable.ic_check_circle);
                        txtStatoPianta.setText("UMIDITA' BUONA");
                    }
                    break;
                case "pompa_status":
                    String status=snapshot.getValue(String.class);
                    switch(status){
                        case "ON":
                            btnIrrigazioneON.setVisibility(View.GONE);
                            btnIrrigazioneOFF.setVisibility(View.VISIBLE);
                            Log.d("firebase", "irrigazione on");
                            imgMascotte.setImageResource(R.mipmap.irrigazione);
                            break;
                        case "OFF":
                            btnIrrigazioneOFF.setVisibility(View.GONE);
                            btnIrrigazioneON.setVisibility(View.VISIBLE);
                            Log.d("firebase", "irrigazione off");
                            String soloNumeri=txtUmidita.getText().toString().replace("%", "").trim();
                            try{
                                percentuale=Integer.parseInt(soloNumeri);
                                Log.d("debug", "percentuale: "+percentuale);
                            }catch (NumberFormatException e){
                                percentuale=-1;
                                Log.e("errore", "impossibile convertire: "+soloNumeri);
                            }
                            if(percentuale<60) imgMascotte.setImageResource(R.mipmap.secco);
                            else imgMascotte.setImageResource(R.mipmap.normale);
                            break;
                    }
            }
        }
    }
    private void cambiamentiLuci(DataSnapshot snapshot){
        if(snapshot.exists()){
            String chiave=snapshot.getKey();
            switch(chiave){
                case "buio_status":
                    buio_status=snapshot.getValue(Boolean.class);
                    if(buio_status) imgMascotte.setImageResource(R.mipmap.notte);
                    break;
                case "led_status":
                    String status=snapshot.getValue(String.class);
                    switch(status){
                        case "ON":
                            imgMascotte.setImageResource(R.mipmap.normale);
                            break;
                        case "OFF":
                            if(buio_status) imgMascotte.setImageResource(R.mipmap.notte);
                            break;
                    }
            }
        }
    }
}