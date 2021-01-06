package com.example.jlibtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import com.intelligt.modbus.jlibmodbus.msg.request.WriteMultipleRegistersRequest;
import com.intelligt.modbus.jlibmodbus.msg.response.ReadHoldingRegistersResponse;
import com.intelligt.modbus.jlibmodbus.msg.response.WriteMultipleRegistersResponse;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryTcpClient;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryTcpServer;
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.example.jlibtest.Util.getHexContent;
import static com.example.jlibtest.Util.printArchivesCfg;
import static com.example.jlibtest.Util.printDateTime;
import static com.example.jlibtest.Util.printModel;
import static com.example.jlibtest.Util.printSerNumber;

public class MainActivity extends AppCompatActivity {
    static ArrayList<String> msgs;
    static ArrayAdapter<String> adapter;
    static CSVCreator creator;
    File filesDir;
    Date start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Task()).start();
        ListView listView = (ListView) findViewById(R.id.lw);
        msgs = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, msgs);
        listView.setAdapter(adapter);
        start = new Date(2020, 11,1);
    }

    public class Task extends Thread{
        private ModbusMaster master;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
        ArchivesConfig cfg;

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void run(){
            filesDir = getApplicationContext().getFilesDir();
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
                int model = 0;
                String sn;
                ReadHoldingRegistersResponse getModel = ResponseFromClassicRequest(0x0708, Integer.parseInt("2",16) / 2,"Get Model");
                ReadHoldingRegistersResponse getDateTime = null;
                if (getModel != null) {
                    model = printModel(0x0062, getModel);
                    getMsgToUI("Device model: " + model);
                    getDateTime = ResponseFromClassicRequest(0x0062, Integer.parseInt("8",16) / 2, "Get DateTime");
                }
                else Log.d("response", "Failed getModel");
                ReadHoldingRegistersResponse getSerNumber = null;
                if (getDateTime != null) {
                    Calendar c = printDateTime(getDateTime);
                    getMsgToUI("Date & Time: " + dateFormat.format(c.getTime()));
                    getSerNumber = ResponseFromClassicRequest(0x0101, Integer.parseInt("36",16) / 2, "Get Serial Number");
                }
                else Log.d("response", "Failed getDateTime");
                ReadHoldingRegistersResponse getArchivesCfg = null;
                if (getSerNumber != null){
                    sn = printSerNumber(getSerNumber);
                    getMsgToUI("Serial Number: " + sn);
                    creator = new CSVCreator(filesDir, String.valueOf(model), sn);
                    getArchivesCfg = ResponseFromClassicRequest(0x0106, Integer.parseInt("38",16) / 2, "Get Archives Config");
                }
                WriteMultipleRegistersResponse dateTime10Response = null;
                if (getArchivesCfg != null){
                    String cfgStr = getHexContent(getArchivesCfg);
                    cfg = new ArchivesConfig(cfgStr);
                    printArchivesCfg(cfg);
                    dateTime10Response = Response10DateTime(start);
                }

                /*
                ПОМЕСЯЧНЫЙ
                 */

                if (dateTime10Response != null){
                    ReadHoldingRegistersResponse row = ResponseFromClassicRequest(0x0020, Integer.parseInt("F0",16) / 2, "Get Month Row");
                    RecordRow recordRow = new RecordRow(cfg, getHexContent(row));
                    //recordRow.getOtherFieldsFloat();
                    Log.d("Record Row", recordRow.getRowDate());
                    creator.printRow(new String[]{"Помесячный архив"});
                    creator.printRow(cfg.getTitles());
                    creator.printRow(recordRow.getRowArray());
                    recordRow.getVf();
                    recordRow.getTf();
                    while (true){
                        if ((String.valueOf(getHexContent(row).charAt(0)) + getHexContent(row).charAt(1)).equals("ff")) {
                            getMsgToUI("С помесячным архивом покончено");
                            break;
                        }
                        else {
                            row = ResponseFromClassicRequest(0x0025, Integer.parseInt("F0", 16) / 2, "Get Month Row");
                            recordRow = new RecordRow(cfg, getHexContent(row));
                            creator.printRow(recordRow.getRowArray());
                        }
                    }
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

        private WriteMultipleRegistersResponse Response10DateTime(Date start) throws ModbusNumberException, ModbusProtocolException, ModbusIOException {
            WriteMultipleRegistersResponse response = null;
            WriteMultipleRegistersRequest test = new WriteMultipleRegistersRequest();
            test.setServerAddress(1);
            test.setStartAddress(0x0060);
            test.setByteCount(4);
            Log.d("date", String.valueOf(start.getDate()));
            byte day = Byte.parseByte(Integer.toHexString(start.getDate()), 16);
            Log.d("date", String.valueOf(start.getMonth() + 1));
            byte month = Byte.parseByte(Integer.toHexString(start.getMonth() + 1), 16);
            Log.d("date", String.valueOf(start.getYear() - 2000));
            byte year = Byte.parseByte(Integer.toHexString(start.getYear() - 2000), 16);
            test.setBytes(new byte[]{0x00, day, month, year});
            master.processRequest(test);
            response = (WriteMultipleRegistersResponse) test.getResponse();
            return response;
        }

        private void getMsgToUI(String msg) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    msgs.add(msg);
                    adapter.notifyDataSetChanged();
                }
            });
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


    }
}

