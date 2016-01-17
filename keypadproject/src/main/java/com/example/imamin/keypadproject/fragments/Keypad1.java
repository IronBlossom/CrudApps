package com.example.imamin.keypadproject.fragments;


import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.imamin.keypadproject.MainActivity;
import com.example.imamin.keypadproject.OptimizedTypeface;
import com.example.imamin.keypadproject.R;
import com.example.imamin.keypadproject.uicontrollers.OnControlKeyPressedListener;
import com.example.imamin.keypadproject.uicontrollers.OnNumericKeyPressedListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class Keypad1 extends Fragment implements OnNumericKeyPressedListener, OnControlKeyPressedListener {
    @Bind(R.id.btnBack)
    Button btnBack;
    @Bind(R.id.tvInputLine1)
    TextView tvInputLine1;
    @Bind(R.id.tvInputLine2)
    TextView tvInputLine2;

    @Bind(R.id.btnKeypadFire)
    Button btnKeypadFire;
    @Bind(R.id.btnKeypadWarning)
    Button btnKeypadWarning;
    @Bind(R.id.btnKeypadShield)
    Button btnKeypadShield;

    @Bind(R.id.ivLedCheckMark)
    ImageView ivLedCheckMark;
    @Bind(R.id.ivLedLock)
    ImageView ivLedLock;
    @Bind(R.id.ivLedWarning)
    ImageView ivLedWarning;
    @Bind(R.id.ivLedAC)
    ImageView ivLedAC;
    private Handler handler = new Handler(Looper.getMainLooper());
    StringBuilder lcdStrBuilder = new StringBuilder();
    public static final char space = ' ';

    public Keypad1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View keypad1View = inflater.inflate(R.layout.fragment_keypad1, container, false);
        ButterKnife.bind(this, keypad1View);
        return keypad1View;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvInputLine1.setTypeface(OptimizedTypeface.get(this.getContext(), "fonts/erbos-draco-1st.open-nbp-regular.ttf"));
        tvInputLine2.setTypeface(OptimizedTypeface.get(this.getContext(), "fonts/erbos-draco-1st.open-nbp-regular.ttf"));
        btnBack.setTypeface(OptimizedTypeface.get(this.getContext(), "fonts/Quivira.otf"));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).onBackButtonPressed();
            }
        });

        resetLikeInit();
        setGlyph();
    }

    private String generateLCDString(CharSequence digit, boolean reset) {
        if (reset) {
            lcdStrBuilder = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                lcdStrBuilder.append(space);
            }
            lcdStrBuilder.append("Time");
        } else {
            for (int i = 0; i < lcdStrBuilder.length(); i++) {
                if (lcdStrBuilder.charAt(i) == space) {
                    lcdStrBuilder.setCharAt(i, digit.charAt(0));
                    break;
                }
            }
            for (int i = 0; i < (12 - lcdStrBuilder.length()); i++) {
                lcdStrBuilder.append(space);
            }
            lcdStrBuilder.append("Time");
        }

        return lcdStrBuilder.toString();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void initAndStartAnimatingDrawable() {
        AnimationDrawable animationDrawable = (AnimationDrawable) ivLedCheckMark.getDrawable();
        animationDrawable.start();

        AnimationDrawable animationDrawable1 = (AnimationDrawable) ivLedLock.getDrawable();
        animationDrawable1.start();

        AnimationDrawable animationDrawable2 = (AnimationDrawable) ivLedWarning.getDrawable();
        animationDrawable2.start();

        AnimationDrawable animationDrawable3 = (AnimationDrawable) ivLedAC.getDrawable();
        animationDrawable3.start();
    }

    private void setGlyph() {
        btnKeypadFire.setTypeface(OptimizedTypeface.get(this.getContext(), "fonts/seguisym.ttf"));
        btnKeypadWarning.setTypeface(OptimizedTypeface.get(this.getContext(), "fonts/seguisym.ttf"));
        btnKeypadShield.setTypeface(OptimizedTypeface.get(this.getContext(), "fonts/seguisym.ttf"));

        btnKeypadFire.setText("\uD83D\uDD25");
        btnKeypadWarning.setText("\u26A0");
        btnKeypadShield.setText("\uD83D\uDEE1");
    }


    private void resetLikeInit() {
        tvInputLine1.setText(generateLCDString(null, true));
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initAndStartAnimatingDrawable();
            }
        }, 4000);

        handler.post(dateTimeUpdater);
    }

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM hh:mm a");
    Runnable dateTimeUpdater = new Runnable() {
        @Override
        public void run() {
            tvInputLine2.setText("" + simpleDateFormat.format(new Date()));
            handler.postDelayed(dateTimeUpdater, 1000);
        }
    };

    @SuppressWarnings("ResourceType")
    @Override
    public void onNumericKeyPressed(int numericKeyId) {
        CharSequence digit = getResources().getText(numericKeyId);
        tvInputLine1.setText(generateLCDString(digit, false));
    }

    @Override
    public void onControlKeyPressed(int controlKeyId) {
        switch (controlKeyId) {
            case R.id.btnKeypadStay:
                break;
            case R.id.btnKeypadAway:
                break;
            case R.id.btnKeypadChime:
                break;
            case R.id.btnKeypadReset:
                resetLikeInit();
                break;
            case R.id.btnKeypadExit:
                break;
        }
    }
}
