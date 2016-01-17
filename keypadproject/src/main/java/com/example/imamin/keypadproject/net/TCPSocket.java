package com.example.imamin.keypadproject.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by imamin on 1/7/2016.
 */
public class TCPSocket {
    Socket clientSocket;
    boolean isClientSocketConfigured, isServerSocketConfigured = false;


    private TCPSocket configureClientSocket(String host, String port) {
        try {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
            clientSocket = new Socket(InetAddress.getByName(host), Integer.parseInt(port));
            isClientSocketConfigured = true;
            Log.i("Configured", "Client socket configured");
        } catch (IOException e) {
            isClientSocketConfigured = false;
            Log.w("Configuration Error", "Couldn't configure client socket\n\t" + e.getMessage());
        }
        return this;
    }

    public String readMessageFromServer(String host, String port) throws IOException {
        String msg = null;

        configureClientSocket(host, port);
        if (isClientSocketConfigured) {
            InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            msg = bufferedReader.readLine();
        }
        return msg;
    }
}
