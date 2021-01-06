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
import java.util.Arrays;


public class CSVCreator {
    public String fname;
    public File fileDir;
    CSVWriter csvWriter;
    //public ArrayList<String> archivesNames;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public CSVCreator(File filesDir, ArrayList<String[]> rows) throws IOException {
        this.fname = String.valueOf(LocalDateTime.now());
        this.fileDir = filesDir;
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(fileDir + fname + ".csv"));
        ) {
            csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            for (String[] row: rows)
                csvWriter.writeNext(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
