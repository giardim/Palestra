package com.example.palestra;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

public class TCPClient extends Thread{

    final static String HOSTNAME = "192.168.104.254";
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
            }
        }
        catch(Exception e){
            this.status = false;
            shutdownSocket();
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
            shutdownSocket();
        }
    }

    public String getMessage(BufferedReader bf) {
        serverMessage = "DEFAULT";
        try {
            serverMessage = bf.readLine();
        } catch (Exception e) {
            shutdownSocket();
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
            //Do something
        }

    }
}
