package com.example.imamin.keypadproject;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
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
    StringBuilder line1StrBuilder = new StringBuilder();
    public static final char space = ' ';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        tvInputLine1.setTypeface(OptimizedTypeface.get(this, "fonts/erbos-draco-1st.open-nbp-regular.ttf"));
        tvInputLine2.setTypeface(OptimizedTypeface.get(this, "fonts/erbos-draco-1st.open-nbp-regular.ttf"));


        resetLikeInit();
        setGlyph();
    }

    private String generateLine1String(CharSequence digit, boolean reset) {
        if (reset) {
            line1StrBuilder = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                line1StrBuilder.append(space);
            }
            line1StrBuilder.append("Time");
        } else {
            for (int i = 0; i < line1StrBuilder.length(); i++) {
                if (line1StrBuilder.charAt(i) == space) {
                    line1StrBuilder.setCharAt(i, digit.charAt(0));
                    break;
                }
            }
            for (int i = 0; i < (12 - line1StrBuilder.length()); i++) {
                line1StrBuilder.append(space);
            }
            line1StrBuilder.append("Time");
        }

        return line1StrBuilder.toString();
    }

    @Override
    protected void onDestroy() {
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
        btnKeypadFire.setTypeface(OptimizedTypeface.get(this, "fonts/seguisym.ttf"));
        btnKeypadWarning.setTypeface(OptimizedTypeface.get(this, "fonts/seguisym.ttf"));
        btnKeypadShield.setTypeface(OptimizedTypeface.get(this, "fonts/seguisym.ttf"));

        btnKeypadFire.setText("\uD83D\uDD25");
        btnKeypadWarning.setText("\u26A0");
        btnKeypadShield.setText("\uD83D\uDEE1");
    }

    @SuppressWarnings("ResourceType")
    public void onNumericKeyPressed(View view) {
        CharSequence digit = getResources().getText(view.getId());
        tvInputLine1.setText(generateLine1String(digit, false));
    }

    public void onControlKeyPressed(View view) {
        switch (view.getId()) {
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

    private void resetLikeInit() {
        tvInputLine1.setText(generateLine1String(null, true));
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
}
