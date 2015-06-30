package com.napcosecurity.android.ilock.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.napcosecurity.android.ilock.R;

public class KeyboardActivity extends BaseActivity implements View.OnClickListener {
    TextView tvLockName;
    ImageButton ibBack, ibHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numeric_keyboard);
        initviews();
        setViewController();
        tvLockName.setText(getIntent().getStringExtra("lockName"));

    }

    private void initviews() {
        tvLockName = (TextView) findViewById(R.id.tvLockName);
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        ibHome = (ImageButton) findViewById(R.id.ibHome);
    }

    private void setViewController() {
        ibBack.setOnClickListener(this);
        ibHome.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibBack) {
            finish();
        }
        if (v.getId() == R.id.ibHome) {
            finish();
        }
    }
}
