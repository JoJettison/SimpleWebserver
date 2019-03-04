package com.company;

/**
 * Joseph Brewster
 * 1001049436
 * CSE 4344 Lab 1
 */
import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Date;

public class MultiThreadServ extends Thread {
    private  Socket mtsocket;  //socket for http requests

    /*
      @Param the socket connection from the MultiHTTPServ
      REFERENCE:
      https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/networking/sockets/examples/KKMultiServerThread.java
     */
    public MultiThreadServ(Socket mtsocket){
        super("MultiThreadServ");
        this.mtsocket = mtsocket; //Create a new thread containing a MultiThreadServ object with the specifed socket
    }
    /*
    Method implemented by children of the thread class
     */
    public void run(){
        Thread tre= Thread.currentThread();
        try (
                //InputStream is = mtsocket.getInputStream(); // the input stream of the  mtsocket
                OutputStream os = mtsocket.getOutputStream(); // the output stream of the mtsocket
                //PrintWriter out = new PrintWriter(mtsocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader( new InputStreamReader( mtsocket.getInputStream())); //the messages from the client
        ){
            String reqLine = null, headLine = null; // declare strings for navigating request and headerlines
            reqLine = in.readLine();  //Get the HTTP request line and store it
            System.out.println("ID: " +tre.getId());  //Current thread being executed
            System.out.println("---------------------");  //Seperator
            System.out.println(reqLine);  //Prints the requestline to the terminal
            while ((headLine = in.readLine()).length() !=  0) {
                System.out.println(headLine); // prints the header lines to the terminal
            }

            StringTokenizer tokenget = new StringTokenizer(reqLine); //Parses the requestline
            tokenget.nextToken(); //Skips over the GET substring
            String fileName = tokenget.nextToken();   // Stores the file name from the get request
            //System.out.println(fileName); // Used for debugging purposes

            httpResponseGen(fileName, os); //Method to generate and send http resonses

        } catch (IOException e) {
            e.printStackTrace();  // error handling
        }
    }

    /*
      Used for generating the http response to be sent to the server
      @Param flname, the fileName of the selected file, os the OutputStream of the socket
     */
    public void httpResponseGen(String flname, OutputStream os) throws IOException{
        flname = "." +flname;   //Says that the file is in the current directory
        String crlf = "\r\n";   // string for carriage return and line feed
        Date crntTime = new Date(); //The Date the response is being ,ade
        FileInputStream transmitter = null; //Used to read the file data
        boolean fileHere = true;  //Flag that indicates whether the selected file is present
        String statusLine,headerContent,contentTypeLine, contentBody; //Declare strings for http response
        contentTypeLine = null; //Needs to be initialized before use
        try{
            transmitter = new FileInputStream(flname); //Attach the stream to the selected file name
        }
        catch (FileNotFoundException exc){
            fileHere = false; //Change the flag to indicate file is not found
        }

        if (fileHere) { //200 OK response message
            statusLine = "HTTP/1.0 200 OK\r\n";
            headerContent ="Connection: close\r\nDate:"+crntTime+"\r\n";
            contentTypeLine = "Content-Type:" + getType(flname)+"\r\n";

        } //301 Moved Permanently message
        else if (flname.equals("./index1.html")){
            statusLine = "HTTP/1.0 301 Moved Permanently\r\n";
            headerContent ="Connection: close\r\nDate:"+crntTime+"\r\n";
            contentTypeLine = "Content-Type: text/html\r\n";
        }
        else{ // 404 Not found message
            statusLine = "HTTP/1.0 404 Not Found\r\n";
            headerContent ="Connection: close\r\nDate:"+crntTime+"\r\n";
            contentTypeLine = "Content-Type: text/html\r\n";
        }
        os.write(statusLine.getBytes());  //Send the status line to the client
        os.flush();                       // clear buffer
        os.write(headerContent.getBytes()); //Send the header content to the client
        os.flush();                       // clear buffer
        os.write(contentTypeLine.getBytes()); //Send the content type to the client
        os.flush();                       // clear buffer
        os.write(crlf.getBytes());        // Send the blank line to the client
        os.flush();                       //clear buffer before file transmission
        if (fileHere) { //200 OK response, send file
            sendFileAsBytes(transmitter, os);
            transmitter.close();
        }               //301 Moved Permanently response, send error page
        else if(flname.equals("./index1.html")){
            contentBody ="<html>" + "<head><title>Moved Permanently</title></head>"+"<body>Content has been moved to <a href=\"http://localhost:8858/index.html\">index.html</a></body></html>";
            os.write(contentBody.getBytes());
        }
        else{           //404 Not found response, send error page
            contentBody ="<html>" + "<head><title>Not Found</title></head>"+"<body>NOT FOUND!</body></html>";
            os.write(contentBody.getBytes());
        }
        os.close();     // close socket output stream
        mtsocket.close(); //close socket connection

    }
    /*
    Check what the file name expression is, and return the appropiate content type
    @Param flname the filename to check
    @Return  the string representing the content type
    */
    public String getType(String flname){

        if( flname.endsWith(".html")|| flname.endsWith(".htm") ){
            return "text/html";
        }
        if(flname.endsWith(".jpeg")){
            return "image/jpeg";
        }
        if(flname.endsWith(".png")){
            return "image/png";
        }
        return "";
    }

    /*
    Used for sending the file to the client
    @param(s) fs, the FileInputStream connected to the file name; os, the OutputStreamconnected to the Socket
    REFERENCE: https://www.cs.helsinki.fi/u/jakangas/Teaching/CBU/lab1_WebServer2.html
    */
    public void sendFileAsBytes(FileInputStream fs, OutputStream os) throws IOException{
        //Make a buffer of 100 bytes for file transfer
        byte[] buffer = new byte[1024]; // allocate buffer of 1000 bytes
        int bytecount = 0;              //used for counting number of bytes in the file
        while((bytecount = fs.read(buffer)) != -1){
            os.write(buffer, 0, bytecount);
        }
    }

}