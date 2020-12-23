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
import com.intelligt.modbus.jlibmodbus.msg.request.ReadHoldingRegistersRequest;
import com.intelligt.modbus.jlibmodbus.msg.response.ReadHoldingRegistersResponse;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryTcpClient;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryTcpServer;
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;


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
        public void run(){
            try {
                TcpParameters tcpParameter = new TcpParameters();
                InetAddress host = InetAddress.getByName("192.168.1.121");
                tcpParameter.setHost(host);
                tcpParameter.setPort(50000);
                tcpParameter.setKeepAlive(false);
                SerialUtils.setSerialPortFactory(new SerialPortFactoryTcpServer(tcpParameter));
                SerialParameters serialParameter = new SerialParameters();
                serialParameter.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600);
                ModbusHoldingRegisters holdingRegisters = new ModbusHoldingRegisters(1);

                for (int i = 0; i < holdingRegisters.getQuantity(); i++) {
                    holdingRegisters.set(i, i + 1);
                }

                SerialUtils.setSerialPortFactory(new SerialPortFactoryTcpClient(tcpParameter));
                ModbusMaster master = ModbusMasterFactory.createModbusMasterRTU(serialParameter);
                master.setResponseTimeout(10000);
                master.connect();
                int slaveId = 1;
                int offset = 0x0708;
                int quantity = 1;
                //you can invoke #connect method manually, otherwise it'll be invoked automatically
                // at next string we receive ten registers from a slave with id of 1 at offset of 0.
                int[] registerValues = master.readHoldingRegisters(slaveId, offset, quantity);
                // print values
                int address = offset;
                for (int value : registerValues) {
                    Log.d("registers", ("Address: " + address++ + ", Value: " + value));
                }

                Log.d("registers", ("Read " + quantity + " HoldingRegisters start from " + offset));

                /*
                 * The same thing using a request
                 */
                ReadHoldingRegistersRequest readRequest = new ReadHoldingRegistersRequest();
                readRequest.setServerAddress(slaveId);
                readRequest.setStartAddress(offset);
                readRequest.setQuantity(quantity);
                master.processRequest(readRequest);
                ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse)readRequest.getResponse();

                for (int value : response.getHoldingRegisters()) {
                    Log.d("response", ("Address: " + address++ + ", Value: " + value));
                }

                Log.d("response","Read " + quantity + " HoldingRegisters start from " + offset);

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
    }
}

