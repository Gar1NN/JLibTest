package com.example.jlibtest;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecordRow {
    public int day, month, year;
    private ArrayList<ArrayList<String>> v = new ArrayList<>();
    private ArrayList<Float> vf = new ArrayList<>();
    private ArrayList<ArrayList<String>> t  = new ArrayList<>();
    private ArrayList<Float> tf = new ArrayList<>();
    private ArrayList<ArrayList<String>> p  = new ArrayList<>();
    private ArrayList<Float> pf = new ArrayList<>();
    private HashMap<String, ArrayList<String>> otherFields = new HashMap<>();
    private HashMap<String, Float> otherFieldsFloat = new HashMap<>();
    private ArchivesConfig cfg;

    public RecordRow(ArchivesConfig cfg, String response){
        String[] responseArray = response.split(" ");
        this.cfg = cfg;
        int i = 15;
        day = HexByteToInt(responseArray[i++]);
        month = HexByteToInt(responseArray[i++]);
        year = HexByteToInt(responseArray[i++]) + 2000;
        for (int z = 0; z < cfg.getV().size(); z++){
            v.add(new ArrayList<String>());
            for (int y = 0; y < 4; y++)
                v.get(z).add(responseArray[i+z*4+y]);
        }
        i += cfg.getV().size() * 4;
        for (int z = 0; z < cfg.getT().size(); z++){
            t.add(new ArrayList<String>());
            for (int y = 0; y < 4; y++)
                t.get(z).add(responseArray[i+z*4+y]);
        }
        i += cfg.getT().size() * 4;
        for (int z = 0; z < cfg.getP().size(); z++){
            p.add(new ArrayList<String>());
            for (int y = 0; y < 4; y++)
                p.get(z).add(responseArray[i+z*4+y]);
        }
        i += cfg.getP().size() * 4;

        otherFields.put("Tmin", new ArrayList<String>());
        for (int y = 0; y < 4; y++)
            otherFields.get("Tmin").add(responseArray[++i]);

        otherFields.put("Tmax", new ArrayList<String>());
        for (int y = 0; y < 4; y++)
            otherFields.get("Tmax").add(responseArray[++i]);

        otherFields.put("Tdt", new ArrayList<String>());
        for (int y = 0; y < 4; y++)
            otherFields.get("Tdt").add(responseArray[++i]);

        otherFields.put("Tf", new ArrayList<String>());
        for (int y = 0; y < 4; y++)
            otherFields.get("Tf").add(responseArray[++i]);

        otherFields.put("Tep", new ArrayList<String>());
        for (int y = 0; y < 4; y++)
            otherFields.get("Tep").add(responseArray[++i]);

        otherFields.put("Errors", new ArrayList<String>());
        for (int y = 0; y < 4; y++)
            otherFields.get("Errors").add(responseArray[++i]);

        otherFields.put("Narabotkas", new ArrayList<String>());
        for (int y = 0; y < 4; y++)
            otherFields.get("Narabotkas").add(responseArray[++i]);

        for (ArrayList<String> four: v)
            vf.add(FourByteStringToFloat(four));

        for (ArrayList<String> four: t)
            tf.add(FourByteStringToFloat(four));

        for (ArrayList<String> four: p)
            pf.add(FourByteStringToFloat(four));

        for (Map.Entry<String, ArrayList<String>> pair: otherFields.entrySet())
            otherFieldsFloat.put(pair.getKey(), FourByteStringToFloat(pair.getValue()));
    }


    public ArrayList<Float> getVf() {
        return vf;
    }

    private Float FourByteStringToFloat(ArrayList<String> four){
        String reverse = four.get(3) + four.get(2) + four.get(1) + four.get(0);
        Long i = Long.parseLong(reverse, 16);
        Float f = Float.intBitsToFloat(i.intValue());
        Log.d("Data Float", four.toString() + " ---> " + f.toString());
        return f;
    }

    public ArrayList<Float> getTf() {
        return tf;
    }

    public ArrayList<Float> getPf() {
        return pf;
    }


    public HashMap<String, Float> getOtherFieldsFloat() {
        return otherFieldsFloat;
    }

    public int HexByteToInt(String hex){
        return Integer.parseInt(hex, 16);
    }

    public String getRowDate(){
        return day + "." +  month + "." + year;
    }

    public String[] getRowArray(RecordRow r){
        ArrayList<String> res = new ArrayList<>();
        res.add(r.day + "." +  r.month + "." + r.year);
        for (Float f: r.getVf())
            res.add(f.toString());
        for (Float f: r.getTf())
            res.add(f.toString());
        for (Float f: r.getPf())
            res.add(f.toString());
        for (Float f: r.getOtherFieldsFloat().values())
            res.add(f.toString());
        return res.toArray(new String[0]);
    }
}
