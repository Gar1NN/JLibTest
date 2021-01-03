package com.example.jlibtest;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;


public class CSVCreator {
    public String fname;
    public String DevName;
    public String SerNum;
    public ArrayList<String> archivesNames;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public CSVCreator(File filesDir, String devName, String serNum){
        this.DevName = devName;
        this.SerNum = serNum;
        this.fname = DevName + "_" + SerNum + "_" +  LocalDateTime.now();
        try (

                Writer writer = Files.newBufferedWriter(Paths.get(filesDir + fname + ".csv"));
                //Log.d("Files", filesDir + fname + ".csv");
                //Log.d("Files", filesDir.getAbsolutePath());
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {
            //String[] headerRecord = {"Name", "Email", "Phone", "Country"};
            String[] ss = {"Карат-" + DevName};
            csvWriter.writeNext(ss);
            ss = new String[]{SerNum};
            csvWriter.writeNext(ss);

            //csvWriter.writeNext(new String[]{"Sundar Pichai ♥", "sundar.pichai@gmail.com", "+1-1111111111", "India"});
            //csvWriter.writeNext(new String[]{"Satya Nadella", "satya.nadella@outlook.com", "+1-1111111112", "India"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void AddArchivesName(String an){
        archivesNames.add(an);
    }
}
