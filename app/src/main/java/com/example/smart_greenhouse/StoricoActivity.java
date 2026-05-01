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

public class StoricoActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private LogAdapter adapter;
    ArrayList<LogEvento> listaLog=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
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
                overridePendingTransition(0, 0); // Effetto tab iOS
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
    private void importaEventi(DataSnapshot snapshot){
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
