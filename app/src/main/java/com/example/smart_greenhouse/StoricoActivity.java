package com.example.smart_greenhouse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Activity dedicata alla visualizzazione dello storico degli eventi.
 * Si collega al nodo 'eventi' del Realtime Database per recuperare
 * e mostrare in ordine cronologico tutte le azioni passate (accensione
 * luci o pompa, attivazione modalità automatica e notifiche ricevute),
 * permettendo all'utente di tener traccia dei cambiamenti della serra.
 */
public class StoricoActivity extends AppCompatActivity{
    private RecyclerView recyclerView;
    private LogAdapter adapter;
    ArrayList<LogEvento> listaLog=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        /**
         * Inizializza l'interfaccia grafica (RecyclerView) e avvia
         * il caricamento dei log da Firebase, ordinandoli per mostrarli a schermo.
         *
         * @param saveInstanceState Stato precedente salvato dell'applicazione.
         */
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_storico);

        recyclerView=findViewById(R.id.recyclerViewStorico);
        adapter=new LogAdapter(listaLog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        attivaListener();


        FrameLayout btnSerra=findViewById(R.id.btnSerra);
        if (btnSerra!=null){
            btnSerra.setOnClickListener(v -> {
                Intent intent=new Intent(StoricoActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_storico), (v, insets) -> {
            Insets systemBars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void attivaListener(){
        /**
         * Si collega al Firebase Realtime Database e imposta il listener
         * che rimane in ascolto continuo, sul nodo 'eventi. Ogni volta che c'è un cambiamento
         * di stato aggiorna l'interfaccia dell'app in tempo reale, attraverso il metodo
         * importaEventi()
         */
        DatabaseReference refEventi=FirebaseDatabase.getInstance().getReference("eventi");
        Query limite=refEventi.orderByKey().limitToLast(250);
        limite.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){
                importaEventi(snapshot);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot){}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void importaEventi(@NonNull DataSnapshot snapshot){
        /**
         * Attraverso lo snapshot ricevuto in ingresso, ricava i dati degli eventi
         * sottoforma di HashMap da Firebase, crea un nuovo un nuovo logEvento usando
         * il metodo statico creaLogDaHashMap(), lo inserisce nella lista dei log (ArrayList<LogEvento)
         * e notifica l'adapter che un nuovo elemento è stato inserito in cima.
         *
         * @param snapshot Il pacchetto di dati aggiornato ricevuto dal database.
         */
        if(snapshot.exists()){
            String chiave=snapshot.getKey();
            HashMap<String, Object> hm=(HashMap<String, Object>) snapshot.getValue();
            LogEvento nuovo=LogEvento.creaLogDaHashMap(snapshot.getKey(), hm);
            if(nuovo!=null){
                listaLog.add(0, nuovo);
            }
            adapter.notifyItemInserted(0);
        }
    }

}
