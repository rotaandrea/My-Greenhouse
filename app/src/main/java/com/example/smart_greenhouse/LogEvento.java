package com.example.smart_greenhouse;

import android.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.zip.DataFormatException;

public class LogEvento{
    /**
     * Classe modello (Data Class) che rappresenta un singolo evento nello storico.
     * Contiene le informazioni fondamentali: il tipo di funzione, il nuovo stato (es. ON/OFF)
     * e la data/ora esatta in cui è avvenuto.
     * Viene utilizzata per inviare e leggere i dati in modo strutturato da Firebase,
     * grazie al metodo statico creaLogDaHashMap().
     */

    private String stato;
    private Funzione tipoEvento;
    private LocalDateTime timestap;
    private String id;
    static DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


    public LogEvento(){}
    public LogEvento(Funzione tipoEvento, String evento){
        this.tipoEvento=tipoEvento;
        this.stato=evento;
        this.timestap=LocalDateTime.now();
    }

    public Funzione getTipoEvento(){ return tipoEvento; }
    public String getStato(){ return stato; }
    public String getTimestap(){
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return timestap.format(formatter);
    }
    public String getId(){ return id; }

    public void setStato(String stato){ this.stato=stato; }
    public void setTipoEvento(Funzione tipoEvento){ this.tipoEvento=tipoEvento; }
    public void setTimestap(LocalDateTime timestap){ this.timestap=timestap; }
    public void setId(String id){ this.id=id; }

    static LogEvento creaLogDaHashMap(String id, HashMap<String,Object> mappaEvento){
        try{
            LogEvento e=new LogEvento();
            if(mappaEvento.size()!=3) throw new DataFormatException();
            e.id=id;
            e.tipoEvento=Funzione.valueOf(mappaEvento.get("tipoEvento").toString());
            e.stato=mappaEvento.get("stato").toString();
            e.timestap=LocalDateTime.parse(mappaEvento.get("timestamp").toString(), formatter);
            return e;
        } catch (Exception e){
            Log.d("firebase", "errore conversione log: "+id);
        }
        return null;
    }
    public HashMap<String,Object> toHashMap(){
        HashMap<String,Object> hm=new HashMap<>();
        hm.put("stato", stato);
        hm.put("tipoEvento", tipoEvento.toString());
        hm.put("timestamp", timestap.format(formatter).toString());
        return hm;
    }
}
