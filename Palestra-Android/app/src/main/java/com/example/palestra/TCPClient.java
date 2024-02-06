package com.example.palestra;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.nfc.Tag;
import android.util.Log;

public class TCPClient extends Thread{
    private PrintWriter bufferSend;
    final static String HOSTNAME = "192.168.131.246";
    final static int PORT = 8080;
    private boolean status;
    private Socket sockFD;

    final static String TAG = "TCPClient";
    public TCPClient(){
        //do nothing
    }

    @Override
    public void run(){
        try{
            this.sockFD = new Socket(HOSTNAME, PORT);
            this.status = true;
        }
        catch(Exception e){
            Log.d(TAG, "***COULD NOT CONNECT TO HOST " + e + "***");
            this.status = false;
        }
    }

    public void sendMessage(String message){
        try {
            PrintWriter pr = new PrintWriter(sockFD.getOutputStream());
            pr.println(message);
            pr.flush();
        }
        catch (Exception e){
            Log.d(TAG, "***COULD NOT SEND MESSAGE TO HOST " + e.getMessage() +"***");
        }
    }

    public String getMessage(){
        String serverMessage = "DEFAULT";
        try{
            InputStreamReader in = new InputStreamReader(sockFD.getInputStream());
            BufferedReader bf = new BufferedReader(in);
            serverMessage = bf.readLine();
        }
        catch (Exception e){
            Log.d(TAG, "***COULD NOT READ MESSAGE FROM HOST " + e.getMessage() +"***");
        }
        return serverMessage;
    }

    public void shutdownSocket(){
        try{
            sockFD.close();
        }
        catch (Exception e){
            Log.d(TAG, "***COULD NOT CLOSE SOCKET " + e + " ***");
        }

    }

    public boolean getStatus(){
        return status;
    }
}
