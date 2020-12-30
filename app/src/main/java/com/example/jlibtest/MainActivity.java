package com.example.jlibtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.intelligt.modbus.jlibmodbus.data.DataHolder;
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
import com.intelligt.modbus.jlibmodbus.msg.response.ReadWriteMultipleRegistersResponse;
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
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Task()).start();
    }

    public static class Task extends Thread{
        public void run(){
            try {
                TcpParameters tcpParameter = new TcpParameters();
                InetAddress host = InetAddress.getByName("192.168.1.121");
                tcpParameter.setHost(host);
                tcpParameter.setPort(50000);
                tcpParameter.setKeepAlive(true);
                SerialParameters serialParameter = new SerialParameters();
                serialParameter.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600);


                SerialUtils.setSerialPortFactory(new SerialPortFactoryTcpClient(tcpParameter));
                ModbusMaster master = ModbusMasterFactory.createModbusMasterRTU(serialParameter);
                master.setResponseTimeout(5000);
                master.connect();
                int slaveId = 1;
                int offset = 0x0708;
                int quantity = 1;
                int address = offset;

                ReadHoldingRegistersRequest readRequest = new ReadHoldingRegistersRequest();
                readRequest.setServerAddress(slaveId);
                readRequest.setStartAddress(offset);
                readRequest.setQuantity(quantity);

                master.processRequest(readRequest);
                ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse)readRequest.getResponse();
                byte[] responsebytes = response.getBytes();
                for (int value : response.getHoldingRegisters()) {
                    Log.d("response", ("Address: " + address++ + ", Value: " + value));

                }
                int[] registers = response.getHoldingRegisters().getRegisters();
                Log.d("Прибор:", String.valueOf(getInt(registers[0])));



                ReadHoldingRegistersRequest getDate = new ReadHoldingRegistersRequest();
                getDate.setServerAddress(slaveId);
                getDate.setStartAddress(0x0062);
                getDate.setQuantity(4);
                master.processRequest(getDate);
                ReadHoldingRegistersResponse date = (ReadHoldingRegistersResponse) getDate.getResponse();
                int[] dates = date.getHoldingRegisters().getRegisters();

                Log.d("Year",String.valueOf(getInt(dates[3])));




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
        public int getInt(int bytes){
            String str = Integer.toHexString(bytes);
            return Integer.parseInt(String.valueOf(str.charAt(2)) +str.charAt(3) + str.charAt(0) + str.charAt(1), 16);
        }


    }
}

