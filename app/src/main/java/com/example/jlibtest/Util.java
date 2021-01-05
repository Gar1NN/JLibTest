package com.example.jlibtest;

import android.util.Log;

import com.intelligt.modbus.jlibmodbus.msg.response.ReadHoldingRegistersResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {
    public static int printModel(int address, ReadHoldingRegistersResponse response) {
        byte[] responsebytes = response.getBytes();
        for (int value : response.getHoldingRegisters()) {
            Log.d("response", ("Address: " + address++ + ", Value: " + value));
        }
        int[] registers = response.getHoldingRegisters().getRegisters();
        String t = Integer.toHexString(registers[0]);
        Log.d("response", t);
        String text = t.charAt(2) +
                String.valueOf(t.charAt(3)) +
                t.charAt(0) +
                t.charAt(1);
        int integ = Integer.parseInt(text, 16);
        Log.d("response", String.valueOf(integ));
        return integ;
    }

    public static Calendar printDateTime(ReadHoldingRegistersResponse response){
        int[] dates = response.getHoldingRegisters().getRegisters();
        int year = getInt(dates[3]);
        Log.d("Year",String.valueOf(year));
        int[] weekAndMonth = getTwoInt(dates[2]);
        Log.d("Месяц и день недели", String.valueOf(weekAndMonth[0]) +" " + String.valueOf(weekAndMonth[1]));

        int[] dateAndHour = getTwoInt(dates[1]);
        Log.d("День месяца и час", String.valueOf(dateAndHour[0]) +" " + String.valueOf(dateAndHour[1]));

        int[] minAndSeconds = getTwoInt(dates[0]);
        Log.d("Минуты и секунды", String.valueOf(minAndSeconds[0]) +" " + String.valueOf(minAndSeconds[1]));

        Calendar c = Calendar.getInstance();
        c.set(year, weekAndMonth[0] - 1, dateAndHour[0], dateAndHour[1],minAndSeconds[0], minAndSeconds[1]);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
        Log.d("Дата и время", dateFormat.format(c.getTime()));
        return c;
    }

    private static int getInt(int bytes){
        String str = Integer.toHexString(bytes);
        return Integer.parseInt(String.valueOf(str.charAt(2)) +str.charAt(3) + str.charAt(0) + str.charAt(1), 16);
    }

    private static int[] getTwoInt(int bytes){
        String str = Integer.toHexString(bytes);
        if (str.length() == 3)
            str = "0" + str;
        int [] result = new int[2];
        result[0] = Integer.parseInt(String.valueOf(str.charAt(2)) +str.charAt(3), 16);
        result[1] = Integer.parseInt(String.valueOf(str.charAt(0)) +str.charAt(1), 16);
        return result;
    }

    public static String getHexContent(ReadHoldingRegistersResponse response){
        int[] bytes = response.getHoldingRegisters().getRegisters();
        StringBuilder sb = new StringBuilder();

        for (int b : bytes) {
            String hex = Integer.toHexString(b);
            //Log.d("hex data", hex);
            if (hex.length() == 3) {
                sb.append("0").append(hex.charAt(0)).append(" ");
                sb.append(hex.charAt(1)).append(hex.charAt(2)).append(" ");
            }
            else if (hex.length() == 2) {
                sb.append("0").append("0").append(" ");
                sb.append(hex.charAt(0)).append(hex.charAt(1)).append(" ");
            }
            else if (hex.length() == 1) {
                sb.append("0").append("0").append(" ");
                sb.append("0").append(hex.charAt(0)).append(" ");
            }
            else if (hex.length() == 0) {
                sb.append("0").append("0").append(" ");
                sb.append("0").append("0").append(" ");
            }
            else {
                sb.append(hex.charAt(0)).append(hex.charAt(1)).append(" ");
                sb.append(hex.charAt(2)).append(hex.charAt(3)).append(" ");
            }
        }
        return sb.toString();
    }

    public static String printSerNumber(ReadHoldingRegistersResponse response){
        String hexData = getHexContent(response);
        String[] hdArray = hexData.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 9; i++){
            sb.append(Integer.parseInt(hdArray[i]) - 30);
        }
        Log.d("Сер. номер", sb.toString());
        return sb.toString();
    }

    public static void printArchivesCfg(ArchivesConfig cfg){
        Log.d("Конфиг V", cfg.getV().toString());
        Log.d("Конфиг T", cfg.getT().toString());
        Log.d("Конфиг P", cfg.getP().toString());
        Log.d("Конфиг", "And nothing else matters...");
    }
}
