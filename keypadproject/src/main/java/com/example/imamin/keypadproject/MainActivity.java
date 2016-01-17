package com.example.imamin.keypadproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.imamin.keypadproject.net.TCPSocket;
import com.example.imamin.keypadproject.uicontrollers.OnControlKeyPressedListener;
import com.example.imamin.keypadproject.uicontrollers.OnNumericKeyPressedListener;
import com.example.imamin.keypadproject.fragments.Keypad1;
import com.example.imamin.keypadproject.fragments.Keypad2;
import com.example.imamin.keypadproject.fragments.Login;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    //    public static boolean submitDone = false;
    public static boolean isKeypad1 = false;
    Keypad1 keypad1;
    Keypad2 keypad2;
    OnNumericKeyPressedListener onNumericKeyPressedListener;
    OnControlKeyPressedListener onControlKeyPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Login()).commit();

    }

    public void onSuccessfulLogin(boolean isKeypad1) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TCPSocket tcpSocket = new TCPSocket();
                try {
                    String s = tcpSocket.readMessageFromServer("172.16.205.157", "11000");
                    Log.v("Message", "=" + s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        if (isKeypad1) {
            keypad1 = new Keypad1();
            onNumericKeyPressedListener = keypad1;
            onControlKeyPressedListener = keypad1;
        } else {
            keypad2 = new Keypad2();
            onNumericKeyPressedListener = keypad2;
            onControlKeyPressedListener = keypad2;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, isKeypad1 ? keypad1 : keypad2).commit();
    }

    public void onNumericKeyPressed(View view) {
        onNumericKeyPressedListener.onNumericKeyPressed(view.getId());

    }

    public void onControlKeyPressed(View view) {
        onControlKeyPressedListener.onControlKeyPressed(view.getId());
    }

    public void onBackButtonPressed() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Login()).commit();

    }
}
