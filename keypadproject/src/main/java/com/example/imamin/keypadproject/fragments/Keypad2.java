package com.example.imamin.keypadproject.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.imamin.keypadproject.MainActivity;
import com.example.imamin.keypadproject.OptimizedTypeface;
import com.example.imamin.keypadproject.R;
import com.example.imamin.keypadproject.controllers.OnControlKeyPressedListener;
import com.example.imamin.keypadproject.controllers.OnNumericKeyPressedListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by imamin on 12/23/2015.
 */
public class Keypad2 extends Fragment implements OnNumericKeyPressedListener, OnControlKeyPressedListener {
    @Bind(R.id.btnBack)
    Button btnBack;
    @Bind(R.id.tvInputLine1)
    TextView tvInputLine1;
    @Bind(R.id.tvInputLine2)
    TextView tvInputLine2;

    private Handler handler = new Handler(Looper.getMainLooper());
    StringBuilder lcdStrBuilder = new StringBuilder();
    public static final char space = ' ';

    public Keypad2() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View keypad2View = inflater.inflate(R.layout.fragment_keypad2, container, false);
        ButterKnife.bind(this, keypad2View);
        return keypad2View;
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

    private void resetLikeInit() {
        tvInputLine1.setText(generateLCDString(null, true));
        handler.removeCallbacksAndMessages(null);


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

    @Override
    public void onControlKeyPressed(int controlKeyId) {

    }

    @Override
    public void onNumericKeyPressed(int numericKeyId) {
        CharSequence digit = getResources().getText(numericKeyId);
        tvInputLine1.setText(generateLCDString(digit, false));
    }
}
