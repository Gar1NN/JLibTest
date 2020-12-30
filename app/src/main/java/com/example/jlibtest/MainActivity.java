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
                InetAddress host = InetAddress.getByName("192.168.0.71");
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
                ReadHoldingRegistersResponse getModel = ResponseFromClassicRequest(0x0708, 1,"Get Model");
                ReadHoldingRegistersResponse getDateTime = null;
                if (getModel != null) {
                    getDateTime = ResponseFromClassicRequest(0x0062, 4, "Get DateTime");
                    printModel(0x0062, getModel);
                }
                else Log.d("response", "Failed getModel");
                ReadHoldingRegistersResponse getSerNumber = null;
                if (getDateTime != null)
                    getSerNumber = ResponseFromClassicRequest(0x0101, 18, "Get Serial Number");
                else Log.d("response", "Failed getDateTime");

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

            return response;
        }

        private void printModel(int address, ReadHoldingRegistersResponse response) {
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
        }
    }
}

