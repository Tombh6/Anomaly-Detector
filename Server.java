package test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {
    private Integer port;
    private Integer clientLimit;
    private ClientHandler ch;

    public interface ClientHandler {
        void handleClient(InputStream inFromClient, OutputStream outToClient);
    }

    volatile boolean stop;

    public Server() {
        stop = false;
    }

    private void startServer(int port, ClientHandler ch) {
        // implement here the server...
        this.port = port;
        this.ch = ch;

        try {
            ServerSocket server = null;
            while(!stop) {
                server = new ServerSocket(port);
                server.setSoTimeout(1000);
                Socket aClient = server.accept(); // blocking call

                ch.handleClient(aClient.getInputStream(),aClient.getOutputStream());

                aClient.getInputStream().close();
                aClient.getOutputStream().close();
                aClient.close();
            }

            server.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // runs the server in its own thread
    public void start(int port, ClientHandler ch) {
        new Thread(() -> startServer(port, ch)).start();
    }

    public void stop() {
        stop = true;
    }
}
