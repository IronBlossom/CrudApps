package com.example.imamin.keypadproject;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.imamin.keypadproject.controllers.OnControlKeyPressedListener;
import com.example.imamin.keypadproject.controllers.OnNumericKeyPressedListener;
import com.example.imamin.keypadproject.fragments.Keypad1;
import com.example.imamin.keypadproject.fragments.Keypad2;
import com.example.imamin.keypadproject.fragments.Login;

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

    public void onBackButtonPressed(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Login()).commit();

    }
}
