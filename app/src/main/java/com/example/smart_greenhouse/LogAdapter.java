package com.example.smart_greenhouse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    static ArrayList<LogEvento> listaLog;
    public LogAdapter(ArrayList<LogEvento> listaLog){ this.listaLog=listaLog; }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log_evento, parent, false);
        return new LogViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position){
        LogEvento log=listaLog.get(position);
        holder.txtData.setText(log.getTimestap());
        if(log.getTipoEvento().equals(Funzione.IRRIGAZIONE)){
            holder.txtDettagli.setText("STATO IRRIGAZIONE - "+log.getStato());
            holder.imgEvento.setImageResource(R.drawable.ic_water_drop);
        }
        else{
            holder.txtDettagli.setText("STATO AUTOMATICO - "+log.getStato());
            holder.imgEvento.setImageResource(R.drawable.ic_automatic_mode);
        }
    }

    @Override
    public int getItemCount(){ return listaLog.size(); }

    static class LogViewHolder extends RecyclerView.ViewHolder{
        TextView txtData, txtDettagli;
        ImageView imgEvento;
        public LogViewHolder(@NonNull View itemView){
            super(itemView);
            txtData=itemView.findViewById(R.id.txtData);
            txtDettagli=itemView.findViewById(R.id.txtDettagli);
            imgEvento=itemView.findViewById(R.id.imgEvento);
        }
    }
}
