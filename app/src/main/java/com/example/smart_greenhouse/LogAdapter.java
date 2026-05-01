package com.example.smart_greenhouse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Adapter personalizzato per la RecyclerView dello Storico.
 * Agisce da "ponte" tra i dati grezzi scaricati da Firebase (LogEvento)
 * e gli elementi grafici dell'interfaccia. Genera e ricicla dinamicamente
 * le righe della lista, garantendo prestazioni fluide e risparmio di memoria.
 */

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    static ArrayList<LogEvento> listaLog;

    public LogAdapter(ArrayList<LogEvento> listaLog){
        /**
         * Costruttore dell'adapter.
         * Riceve i dati iniziali necessari per costruire l'elenco.
         *
         * @param listaLog La lista di oggetti LogEvento da mostrare a schermo.
         */
        this.listaLog=listaLog;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        /**
         * Viene chiamato quando l'interfaccia ha bisogno di creare una nuova "riga" vuota.
         * Prende il file XML della singola riga (layout) e fa inflating,
         * trasformandolo in un oggetto Java visibile a schermo.
         *
         * @param parent Il gruppo (ViewGroup) a cui questa nuova vista verrà aggiunta.
         * @param viewType Il tipo di vista.
         * @return Un nuovo contenitore (ViewHolder) con il layout della riga pronto.
         */

        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log_evento, parent, false);
        return new LogViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position){
        /**
         * Si occupa di compilare una singola riga grafica con i dati veri e propri.
         * Prende un evento specifico dalla lista e imposta la data, i dettagli e
         * l'icona corretta (es. goccia d'acqua, lampadina, automatico o notifica)
         * in base alla funzione in questione.
         *
         * @param holder Il contenitore grafico della riga da popolare.
         * @param position La posizione dell'evento corrente all'interno della lista.
         */

        LogEvento log=listaLog.get(position);
        holder.txtData.setText(log.getTimestap());
        if(log.getTipoEvento().equals(Funzione.IRRIGAZIONE)){
            holder.txtDettagli.setText("STATO IRRIGAZIONE - "+log.getStato());
            holder.imgEvento.setImageResource(R.drawable.ic_water_drop);
        }
        else if(log.getTipoEvento().equals(Funzione.AUTO)){
            holder.txtDettagli.setText("STATO AUTOMATICO - "+log.getStato());
            holder.imgEvento.setImageResource(R.drawable.ic_automatic_mode);
        }
        else if(log.getTipoEvento().equals(Funzione.LUCI)){
            holder.txtDettagli.setText("STATO LED - "+log.getStato());
            holder.imgEvento.setImageResource(R.drawable.ic_lightbulb);
        }
        else {
            holder.txtDettagli.setText("ALLARME UMIDITÀ - "+log.getStato()+"%");
            holder.imgEvento.setImageResource(R.drawable.ic_warning_circle);
        }
    }

    @Override
    public int getItemCount(){
        /**
         * Restituisce il numero totale di eventi presenti nello storico.
         * Permette alla RecyclerView di sapere esattamente quante righe dovrà scorrere.
         *
         * @return La dimensione effettiva della lista eventi.
         */

        return listaLog.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder{
        TextView txtData, txtDettagli;
        ImageView imgEvento;
        public LogViewHolder(@NonNull View itemView){
            /**
             * Classe interna (ViewHolder) che funge da "scatola" per i singoli elementi
             * grafici di una riga (TextView, ImageView). Salva i riferimenti a questi
             * elementi in modo da non dover chiamare continuamente findViewById(),
             * ottimizzando notevolmente la fluidità dello scorrimento.
             */
            super(itemView);
            txtData=itemView.findViewById(R.id.txtData);
            txtDettagli=itemView.findViewById(R.id.txtDettagli);
            imgEvento=itemView.findViewById(R.id.imgEvento);
        }
    }
}
