package com.company;

import java.net.*;
import java.io.*;

/**
 * REFERENCE:
 * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/networking/sockets/examples/KKMultiServer.java
 */
public class MultiHTTPServ {
    public static void main(String[] args) throws IOException {


        int portNumber = 8858;  //Port number that server runs on
        boolean actlisten = true; // Server is actively listening for new connections
        System.out.println("Server Started on port "+ portNumber);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)){
            while (actlisten) { //Create socket in new thread and listen for new connections
                new MultiThreadServ(serverSocket.accept()).start();
                System.out.println("Connection established on port# "+portNumber);
            }
        } catch (IOException e) { // Error check on socket creation
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}