package com.example.jlibtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters;
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataAddressException;
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataValueException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.msg.base.ModbusMessage;
import com.intelligt.modbus.jlibmodbus.msg.request.ReadHoldingRegistersRequest;
import com.intelligt.modbus.jlibmodbus.msg.request.ReadWriteMultipleRegistersRequest;
import com.intelligt.modbus.jlibmodbus.msg.response.ReadHoldingRegistersResponse;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryTcpClient;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryTcpServer;
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Task()).start();
    }

    public static class Task extends Thread{
        private ModbusMaster master;

        public void run(){
            try {
                TcpParameters tcpParameter = new TcpParameters();
                InetAddress host = InetAddress.getByName("192.168.0.106");
                tcpParameter.setHost(host);
                tcpParameter.setPort(50000);
                tcpParameter.setKeepAlive(true);
                SerialUtils.setSerialPortFactory(new SerialPortFactoryTcpServer(tcpParameter));
                SerialParameters serialParameter = new SerialParameters();
                serialParameter.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600);
                 //serialParameter.setDataBits(100);

                SerialUtils.setSerialPortFactory(new SerialPortFactoryTcpClient(tcpParameter));
                master = ModbusMasterFactory.createModbusMasterRTU(serialParameter);
                master.setResponseTimeout(10000);
                master.connect();

                /*
                 Опрос начат
                 - модель
                 - дата и время
                 - серийник
                 - конфигурация архивов
                 */

                ReadHoldingRegistersResponse getModel = ResponseFromClassicRequest(0x0708, Integer.parseInt("2",16) / 2,"Get Model");
                ReadHoldingRegistersResponse getDateTime = null;
                if (getModel != null) {
                    printModel(0x0062, getModel);
                    getDateTime = ResponseFromClassicRequest(0x0062, Integer.parseInt("8",16) / 2, "Get DateTime");
                }
                else Log.d("response", "Failed getModel");
                ReadHoldingRegistersResponse getSerNumber = null;
                if (getDateTime != null) {
                    printDateTime(getDateTime);
                    getSerNumber = ResponseFromClassicRequest(0x0101, Integer.parseInt("36",16) / 2, "Get Serial Number");
                }
                else Log.d("response", "Failed getDateTime");
                ReadHoldingRegistersResponse getArchivesCfg = null;
                if (getSerNumber != null){
                    printSerNumber(getSerNumber);
                    getArchivesCfg = ResponseFromClassicRequest(0x0106, Integer.parseInt("38",16) / 2, "Get Archives Config");
                }

                master.disconnect();
            } catch (SerialPortException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (ModbusIOException e) {
                e.printStackTrace();
            } catch (IllegalDataValueException e) {
                e.printStackTrace();
            } catch (IllegalDataAddressException e) {
                e.printStackTrace();
            } catch (ModbusNumberException e) {
                e.printStackTrace();
            } catch (ModbusProtocolException e) {
                e.printStackTrace();
            }
        }

        private ReadHoldingRegistersResponse ResponseFromClassicRequest(int offset, int quantity, String msg) throws ModbusNumberException, ModbusProtocolException, ModbusIOException {
            int slaveId = 1;
            //int quantity = 1;

            ReadHoldingRegistersResponse response = null;

            Log.d("response", ("Query: " + msg + ", Try: " + 0));
            ReadHoldingRegistersRequest readRequest = new ReadHoldingRegistersRequest();
            readRequest.setServerAddress(slaveId);
            readRequest.setStartAddress(offset);
            readRequest.setQuantity(quantity);

            master.processRequest(readRequest);
            response = (ReadHoldingRegistersResponse) readRequest.getResponse();

            Log.d("hex data", getHexContent(response));
            return response;
        }

        private int printModel(int address, ReadHoldingRegistersResponse response) {
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

        private Calendar printDateTime(ReadHoldingRegistersResponse response){
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
            c.set(year, weekAndMonth[0], dateAndHour[0], dateAndHour[1],minAndSeconds[0], minAndSeconds[1]);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
            Log.d("Дата и время", dateFormat.format(c.getTime()));
            return c;
        }

        public static int getInt(int bytes){
            String str = Integer.toHexString(bytes);
            return Integer.parseInt(String.valueOf(str.charAt(2)) +str.charAt(3) + str.charAt(0) + str.charAt(1), 16);
        }

        public static int[] getTwoInt(int bytes){
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
                if (hex.length() == 3) {
                    sb.append("0").append(hex.charAt(0)).append(" ");
                    sb.append(hex.charAt(1)).append(hex.charAt(2)).append(" ");
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
    }
}

