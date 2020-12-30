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
                ModbusHoldingRegisters holdingRegisters = new ModbusHoldingRegisters(1);

                for (int i = 0; i < holdingRegisters.getQuantity(); i++) {
                    holdingRegisters.set(i, i + 1);
                }

                SerialUtils.setSerialPortFactory(new SerialPortFactoryTcpClient(tcpParameter));
                master = ModbusMasterFactory.createModbusMasterRTU(serialParameter);
                master.setResponseTimeout(5000);
                master.connect();
                ReadHoldingRegistersResponse getModel = ResponseFromClassicRequest(master, 0x0708, "Get Model");
                ReadHoldingRegistersResponse getDateTime = null;
                if (getModel != null)
                    getDateTime = ResponseFromClassicRequest(master, 0x0062, "Get DateTime");
                else Log.d("response", "Failed getModel");
                ReadHoldingRegistersResponse getSerNumber = null;
                if (getDateTime != null)
                    getSerNumber = ResponseFromClassicRequest(master, 0x0101, "Get Serial Number");
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

        private ReadHoldingRegistersResponse ResponseFromClassicRequest(ModbusMaster master, int offset, String msg) throws ModbusNumberException, ModbusProtocolException, ModbusIOException {
            int slaveId = 1;
            int quantity = 1;

            ReadHoldingRegistersResponse response = null;

            for (int i = 0; i < 5 && response == null; i++) {
                Log.d("response", ("Query: " + msg + ", Try: " + i));
                ReadHoldingRegistersRequest readRequest = new ReadHoldingRegistersRequest();
                readRequest.setServerAddress(slaveId);
                readRequest.setStartAddress(offset);
                readRequest.setQuantity(quantity);

                master.processRequest(readRequest);
                response = (ReadHoldingRegistersResponse) readRequest.getResponse();
            }

            printResponseResults(offset, response);

            return response;
        }

        private void printResponseResults(int address, ReadHoldingRegistersResponse response) {
            byte[] responsebytes = response.getBytes();
            for (int value : response.getHoldingRegisters()) {
                Log.d("response", ("Address: " + address++ + ", Value: " + value));
            }
            int[] registers = response.getHoldingRegisters().getRegisters();
            String[] t = Integer.toHexString(registers[0]).split("");
            String text = t[2] + t[3] + t[0] + t[1];
            int integ = Integer.parseInt(text, 16);
            Log.d("response", String.valueOf(integ));
        }
    }
}

