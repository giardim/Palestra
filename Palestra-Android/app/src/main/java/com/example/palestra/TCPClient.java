package com.example.palestra;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

public class TCPClient extends Thread{

    final static String HOSTNAME = "192.168.131.254";
    final static int PORT = 8080;
    final static String TAG = "TCPClient";
    private Socket sockFD;
    private String serverMessage;
    private boolean status;
    private ArrayList<String> workoutNumbers = new ArrayList<String>();

    public TCPClient(){
        //do nothing
    }

    @Override
    public void run(){
        try{
            this.sockFD = new Socket(HOSTNAME, PORT);
            InputStreamReader in = new InputStreamReader(sockFD.getInputStream());
            BufferedReader bf = new BufferedReader(in);
            Log.d(TAG, "STATUS: " + status);
            while(true) {
                if (status){
                    sendMessage("START");
                    serverMessage = getMessage(bf);
                    workoutNumbers.add(serverMessage);
                }
                else{
                    workoutNumbers.clear();
                    sendMessage("QUIT");
                    serverMessage = getMessage(bf);
                }
                Log.d(TAG, "SERVER: " + serverMessage);
            }
        }
        catch(Exception e){
            Log.d(TAG, "***COULD NOT CONNECT TO HOST " + e + "***");
            this.status = false;
        }
    }

    void setStatus(boolean status){
        this.status = status;
    }

    public void sendMessage(String message){
        try {
            PrintWriter pr = new PrintWriter(sockFD.getOutputStream());
            pr.println(message);
            pr.flush();
        }
        catch (Exception e){
            Log.d(TAG, "***COULD NOT SEND MESSAGE TO HOST " + e +"***");
        }
    }

    public String getMessage(BufferedReader bf) {
        serverMessage = "DEFAULT";
        try {
            serverMessage = bf.readLine();
            Log.d("GETMESSAGE", "SERVER:" + serverMessage);
        } catch (Exception e) {
            Log.d(TAG, "***COULD NOT READ MESSAGE FROM HOST " + e + "***");
        }
        return serverMessage;
    }

    public ArrayList<String> getWorkoutStats(){
        return workoutNumbers;
    }

    public void shutdownSocket(){
        try{
            sockFD.close();
        }
        catch (Exception e){
            Log.d(TAG, "***COULD NOT CLOSE SOCKET " + e + " ***");
        }

    }
}
