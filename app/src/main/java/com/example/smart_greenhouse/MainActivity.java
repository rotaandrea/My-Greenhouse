package com.example.smart_greenhouse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

/**
 * Activity principale dell'applicazione My Greénhouse.
 * Funge da pannello di controllo (dashboard) per l'utente,
 * mostrando in tempo reale il valore dell'umidità del terreno
 * della cultura, e permettendo il controllo manuale della pompa
 * di irrigazione, delle luci e della modalità automatica.
 *
 * @author Andrea Rota
 * @author Andrea Pedrali
 * @author Claudio Carminati
 * @author Filippo Vezzoli
 */
public class MainActivity extends AppCompatActivity{
    ImageView imgMascotte, imgStatoPianta, imgIrrigazione, imgAuto, imgLuci;
    TextView txtUmidita, txtStatoPianta;
    MaterialCardView btnIrrigazione, btnAuto, btnLuci, btnLogout;
    FrameLayout btnStorico;
    View loadingOverlay;

    boolean buio_status=false, luci_status=false, pompa_status=false;
    int percentuale;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        /**
         * Metodo chiamato alla creazione dell'Activity.
         * Inizializza l'interfaccia grafica e gestisce la schermata di caricamento.
         * Verifica l'autenticazione dell'utente tramite FirebaseAuth e configura
         * il canale delle notifiche.
         * Gestisce inflating collegando gli elementi UI (MaterialCardView, FrameLayout,
         * ImageView, TextView) e imposta i listener per le interazioni dell'utente (luci,
         * automazione, irrigazione e barra di navigazione inferiore).
         *
         * @param saveInstanceState Stato precedente salvato dell'applicazione.
         */
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
        btnStorico=findViewById(R.id.btnStorico);

        imgMascotte=findViewById(R.id.imageViewMascotte);
        imgStatoPianta=findViewById(R.id.imageStatus);
        txtUmidita=findViewById(R.id.textPercentualeUmid);
        txtStatoPianta=findViewById(R.id.statusText);

        loadingOverlay =findViewById(R.id.loadingOverlay);

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
                                pompa_status=true;
                                refPompa.setValue(nextStatus);
                                imgIrrigazione.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                                btnIrrigazione.setCardBackgroundColor(Color.WHITE);
                                btnIrrigazione.setCardElevation(4f);
                                break;

                            case "ON":
                                nextStatus="OFF";
                                pompa_status=false;
                                refPompa.setValue("OFF");
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
                                refLuci.setValue(nextStatus);

                                aggiornaMascotte();

                                imgLuci.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                                btnLuci.setCardBackgroundColor(Color.WHITE);
                                btnLuci.setCardElevation(4f);
                                break;

                            case "ON":
                                nextStatus="OFF";
                                refLuci.setValue("OFF");

                                aggiornaMascotte();

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
        /**
         * Si collega al Firebase Realtime Database e imposta dei listener
         * che rimangono in ascolto continuo. Ogni volta che c'è un cambiamento di stato
         * (es. variazione dell'umidità, accensione delle luci o stato automazione),
         * aggiorna l'interfaccia dell'app in tempo reale.
         */
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
    private void cambiamentiUmidita(@NonNull DataSnapshot snapshot){
        /**
         * Gestisce i dati in arrivo da Firebase riguardanti il terreno.
         * Aggiorna il testo a schermo con la percentuale di umidità e cambia
         * l'espressione della mascotte (normale o secco) e l'icona di stato
         * se la percentuale scende sotto soglie di allarme (es. sotto il 60%).
         *
         * @param snapshot Il pacchetto di dati aggiornato ricevuto dal database.
         */

        nascondiCaricamento();
        if(snapshot.exists()){
            String chiave=snapshot.getKey();
            switch(chiave){
                case "sensore":
                    percentuale=snapshot.getValue(Integer.class);
                    txtUmidita.setText(percentuale+"%");
                    if(percentuale<60){
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
                        imgStatoPianta.setImageResource(R.drawable.ic_check_circle);
                        txtStatoPianta.setText("UMIDITA' BUONA");
                    }
                    aggiornaMascotte();
                    break;
                case "pompa_status":
                    String status=snapshot.getValue(String.class);
                    switch(status){
                        case "ON":
                            pompa_status=true;
                            imgIrrigazione.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                            btnIrrigazione.setCardBackgroundColor(Color.WHITE);
                            btnIrrigazione.setCardElevation(4f);
                            Log.d("firebase", "irrigazione on");
                            aggiornaMascotte();
                            break;
                        case "OFF":
                            pompa_status=false;
                            imgIrrigazione.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                            btnIrrigazione.setCardBackgroundColor(Color.parseColor("#33FFFFFF"));
                            btnIrrigazione.setCardElevation(0f);
                            Log.d("firebase", "irrigazione off");
                            aggiornaMascotte();
                            break;
                    }
            }
        }
    }
    private void cambiamentiLuci(@NonNull DataSnapshot snapshot){
        /**
         * Gestisce l'aggiornamento dell'interfaccia grafica in base allo stato
         * dell'illuminazione (LED) e al sensore di luminosità (buio).
         *
         * @param snapshot Il pacchetto di dati aggiornato ricevuto dal database.
         */

        if(snapshot.exists()){
            String chiave=snapshot.getKey();
            switch(chiave){
                case "buio_status":
                    buio_status=snapshot.getValue(Boolean.class);
                    aggiornaMascotte();
                    break;
                case "led_status":
                    String status=snapshot.getValue(String.class);
                    switch(status){
                        case "ON":
                            luci_status=true;

                            aggiornaMascotte();

                            imgLuci.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0F8B7D")));
                            btnLuci.setCardBackgroundColor(Color.WHITE);
                            btnLuci.setCardElevation(4f);
                            Log.d("firebase", "luci on");
                            break;
                        case "OFF":
                            luci_status=false;

                            aggiornaMascotte();

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
        /**
         * Salva un'azione compiuta dall'utente (es. accensione manuale della pompa)
         * nel nodo "eventi" del Realtime Database, per mantenere uno storico completo delle
         * attività della serra.
         *
         * @param evento Una mappa (HashMap) contenente i dettagli dell'azione da salvare.
         */

        DatabaseReference refEventi=FirebaseDatabase.getInstance().getReference("eventi");
        String id=refEventi.push().getKey();
        refEventi.child(id).setValue(evento);
    }
    private void nascondiCaricamento(){
        /**
         * Nasconde la rotella di progresso del caricamento, gestendo anche la
         * transizione in modo di non avere un risultato scattante
         */

        if(loadingOverlay!=null && loadingOverlay.getVisibility()==View.VISIBLE){
            loadingOverlay.animate().alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> loadingOverlay.setVisibility(View.GONE));
        }
    }
    private void aggiornaMascotte() {
        /**
         * Calcola l'interazione tra i valori della pompa, del buio, e dell'umidità
         * in modo da cambiare lo stato della mascotte in base a delle priorità.
         * Priorità 1: Se la pompa va, mostra l'irrigazione sempre.
         * Priorità 2: Se è buio e le luci sono spente, la mascotte dorme.
         * Priorità 3: C'è un'allerta siccità.
         * Priorità 4: Tutto regolare.
         */


        if (pompa_status) imgMascotte.setImageResource(R.mipmap.irrigazione);
        else if(buio_status && !luci_status) imgMascotte.setImageResource(R.mipmap.notte);
        else if(percentuale<60) imgMascotte.setImageResource(R.mipmap.secco);
        else imgMascotte.setImageResource(R.mipmap.normale);
    }

}