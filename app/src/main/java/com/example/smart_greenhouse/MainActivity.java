package com.example.smart_greenhouse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.util.HashMap;

public class MainActivity extends AppCompatActivity{
    ImageView imgMascotte, imgStatoPianta, imgIrrigazione, imgAuto, imgLuci;
    //Button btnHome, btnStorico;
    TextView txtUmidita, txtStatoPianta;
    MaterialCardView btnIrrigazione, btnAuto, btnLuci, btnLogout;
    FrameLayout btnStorico;

    boolean buio_status=false;
    int percentuale;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            Intent intent=new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("serra_alert").addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d("notifiche", "iscritto al canale notifiche");
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                    }
                }
            }
        });

        btnIrrigazione=findViewById(R.id.btnIrrigazioneContainer);
        btnAuto=findViewById(R.id.btnAutoContainer);
        btnLuci=findViewById(R.id.btnLuciContainer);

        imgIrrigazione=findViewById(R.id.imgIrrigazione);
        imgAuto=findViewById(R.id.imgAuto);
        imgLuci=findViewById(R.id.imgLuci);

        btnLogout =findViewById(R.id.btnLogout);

        imgMascotte=findViewById(R.id.imageViewMascotte);
        imgStatoPianta=findViewById(R.id.imageStatus);
        txtUmidita=findViewById(R.id.textPercentualeUmid);
        txtStatoPianta=findViewById(R.id.statusText);

        attivaListener();

        btnIrrigazione.setOnClickListener(v -> {
            DatabaseReference refPompa=FirebaseDatabase.getInstance().getReference("umidita/pompa_status");
            refPompa.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot dataSnapshot=task.getResult();
                    if(dataSnapshot.exists()){
                        String status=dataSnapshot.getValue(String.class);
                        Log.d("firebase", "auto: "+status);
                        String nextStatus="";
                        switch(status){
                            case "OFF":
                                nextStatus="ON";
                                refPompa.setValue(nextStatus); // Accendi l'automazione
                                //imgIrrigazione.setImageResource(R.drawable.ic_pause);
                                imgIrrigazione.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                                btnIrrigazione.setCardBackgroundColor(Color.WHITE);
                                btnIrrigazione.setCardElevation(4f);
                                break;

                            case "ON":
                                nextStatus="OFF";
                                refPompa.setValue("OFF"); // Accendi l'automazione
                                //imgIrrigazione.setImageResource(R.drawable.ic_water_drop);
                                imgIrrigazione.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                                btnIrrigazione.setCardBackgroundColor(Color.parseColor("#0F8B7D"));
                                btnIrrigazione.setCardElevation(0f);
                                break;
                        }
                        HashMap<String, Object> hm=new LogEvento(Funzione.IRRIGAZIONE, nextStatus).toHashMap();
                        mandaEventoDb(hm);

                    }
                    else Log.d("firebase", "errore database: nessun valore trovato");
                }
                else Log.d("firebase", "errore di connessione", task.getException());
            });
        });

        btnAuto.setOnClickListener(v -> {
            DatabaseReference refAuto=FirebaseDatabase.getInstance().getReference("auto_status");
            refAuto.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot dataSnapshot=task.getResult();
                    if(dataSnapshot.exists()){
                        boolean status=dataSnapshot.getValue(Boolean.class);
                        Log.d("firebase", "auto: "+status);
                        String nextStatus="";
                        if(status){
                            nextStatus="OFF";
                            refAuto.setValue(false); // Accendi l'automazione
                            //imgIrrigazione.setImageResource(R.drawable.ic_pause);
                            imgAuto.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                            btnAuto.setCardBackgroundColor(Color.WHITE);
                            btnAuto.setCardElevation(4f);
                        }
                        else{
                            nextStatus="ON";
                            refAuto.setValue(true); // Spegni l'automazione
                            //imgIrrigazione.setImageResource(R.drawable.ic_water_drop);
                            imgAuto.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                            btnAuto.setCardBackgroundColor(Color.parseColor("#33FFFFFF"));
                            btnAuto.setCardElevation(0f);
                        }
                        HashMap<String, Object> hm=new LogEvento(Funzione.AUTO, nextStatus).toHashMap();
                        mandaEventoDb(hm);

                    }
                    else Log.d("firebase", "errore database: nessun valore trovato");
                }
                else Log.d("firebase", "errore di connessione", task.getException());
            });
        });

        btnLuci.setOnClickListener(v -> {
            DatabaseReference refLuci=FirebaseDatabase.getInstance().getReference("luci/led_status");
            refLuci.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot dataSnapshot=task.getResult();
                    if(dataSnapshot.exists()){
                        String status=dataSnapshot.getValue(String.class);
                        Log.d("firebase", "luci: "+status);
                        String nextStatus="";
                        switch(status){
                            case "OFF":
                                nextStatus="ON";
                                refLuci.setValue(nextStatus); // Accendi l'automazione
                                imgLuci.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                                btnLuci.setCardBackgroundColor(Color.WHITE);
                                btnLuci.setCardElevation(4f);
                                break;

                            case "ON":
                                nextStatus="OFF";
                                refLuci.setValue("OFF"); // Accendi l'automazione
                                //imgIrrigazione.setImageResource(R.drawable.ic_water_drop);
                                imgLuci.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                                btnLuci.setCardBackgroundColor(Color.parseColor("#0F8B7D"));
                                btnLuci.setCardElevation(0f);
                                break;
                        }
                        HashMap<String, Object> hm=new LogEvento(Funzione.LUCI, nextStatus).toHashMap();
                        mandaEventoDb(hm);

                    }
                    else Log.d("firebase", "errore database: nessun valore trovato");
                }
                else Log.d("firebase", "errore di connessione", task.getException());
            });
        });

        btnLogout.setOnClickListener(view -> {
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

        //barra
        btnStorico=findViewById(R.id.btnStorico);
        btnStorico.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this, StoricoActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
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
                        imgAuto.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                        btnAuto.setCardBackgroundColor(Color.WHITE);
                        btnAuto.setCardElevation(4f);
                        Log.d("firebase", "auto on");
                    }
                    else{
                        imgAuto.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                        btnAuto.setCardBackgroundColor(Color.parseColor("#33FFFFFF"));
                        btnAuto.setCardElevation(0f);
                        Log.d("firebase", "auto off");
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
                    percentuale=snapshot.getValue(Integer.class);
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
                            imgIrrigazione.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                            btnIrrigazione.setCardBackgroundColor(Color.WHITE);
                            btnIrrigazione.setCardElevation(4f);
                            Log.d("firebase", "irrigazione on");
                            imgMascotte.setImageResource(R.mipmap.irrigazione);
                            break;
                        case "OFF":
                            imgIrrigazione.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                            btnIrrigazione.setCardBackgroundColor(Color.parseColor("#33FFFFFF"));
                            btnIrrigazione.setCardElevation(0f);
                            Log.d("firebase", "irrigazione off");
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
                            imgLuci.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                            btnLuci.setCardBackgroundColor(Color.WHITE);
                            btnLuci.setCardElevation(4f);
                            Log.d("firebase", "luci on");
                            break;
                        case "OFF":
                            if(buio_status) imgMascotte.setImageResource(R.mipmap.notte);
                            imgLuci.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                            btnLuci.setCardBackgroundColor(Color.parseColor("#33FFFFFF"));
                            btnLuci.setCardElevation(0f);
                            Log.d("firebase", "luci off");
                            break;
                    }
            }
        }
    }
    private void mandaEventoDb(HashMap<String, Object> evento){
        DatabaseReference refEventi=FirebaseDatabase.getInstance().getReference("eventi");
        String id=refEventi.push().getKey();
        refEventi.child(id).setValue(evento);
    }
}