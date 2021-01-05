package com.example.jlibtest;

import java.util.ArrayList;

public class RecordRow {
    private ArrayList<Integer> v = new ArrayList<>();
    private ArrayList<Integer> t  = new ArrayList<>();
    private ArrayList<Integer> p  = new ArrayList<>();
    private ArrayList<Integer> ends = new ArrayList<>();
    private Integer Tmin;
    private Integer Tmax;
    private Integer Tdt;
    private Integer Tf;
    private Integer Errors;
    private Integer Narabotkas;

    public RecordRow(String response){
        String[] bytes = response.split(" ");
        int y = 0;
        for (String aByte : bytes) {
            switch (aByte.charAt(0)) {
                case '1':
                    v.add(Integer.parseInt(aByte, 16));
                    break;
                case '3':
                    t.add(Integer.parseInt(aByte, 16));
                    break;
                case '4':
                    p.add(Integer.parseInt(aByte, 16));
                    break;
                case 'd':
                    switch (y) {
                        case 0:
                            y++;
                            Tmin = Integer.parseInt(aByte, 16);
                            break;
                        case 1:
                            y++;
                            Tmax = Integer.parseInt(aByte, 16);
                            break;
                        case 2:
                            y++;
                            Tdt = Integer.parseInt(aByte, 16);
                            break;
                        case 3:
                            Tf = Integer.parseInt(aByte, 16);
                            break;
                    }
                case 'c':
                    Errors = Integer.parseInt(aByte, 16);
                    break;
                case 'b':
                    Narabotkas = Integer.parseInt(aByte, 16);
                    break;
                default:
                    ends.add(Integer.parseInt(aByte, 16));
                    break;
            }
        }

    }
}
