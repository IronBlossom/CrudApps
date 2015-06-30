package com.napcosecurity.android.ilock.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.db.AppDatabaseAdapter;
import com.napcosecurity.android.ilock.db.AppDatabaseFields;
import com.napcosecurity.android.ilock.db.AppDatabaseIntegrator;
import com.napcosecurity.android.ilock.lock.Lock;

public class SettingsLockDetailsActivity extends BaseActivity implements AppDatabaseIntegrator, View.OnClickListener {
    TextView tvLockName, tvLockId, tvLockMac, tvLockSlot;
    EditText etLockName, etLockPass;
    CheckBox ckLockSavePassword, ckLockEnableKeypad, ckOperateUnattended;
    Spinner spUnattendedTimeout;
    Button btnSaveChanges, btnDeleteLock;
    ImageButton ibBack, ibHome;
    AppDatabaseAdapter db;
    Lock lock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_lockdetails);

        initViews();
        setViewControllers();
        setLockData();

        requestDb(this);
    }

    private void initViews() {
        tvLockName = (TextView) findViewById(R.id.tvLockName);
        tvLockId = (TextView) findViewById(R.id.tvLockId);
        tvLockMac = (TextView) findViewById(R.id.tvLockMac);
        tvLockSlot = (TextView) findViewById(R.id.tvLockSlot);

        etLockName = (EditText) findViewById(R.id.etLockName);
        etLockPass = (EditText) findViewById(R.id.etLockPass);

        ckLockSavePassword = (CheckBox) findViewById(R.id.ck_savepass);
        ckLockEnableKeypad = (CheckBox) findViewById(R.id.ck_keypad);
        ckOperateUnattended = (CheckBox) findViewById(R.id.ck_opUnattended);

        spUnattendedTimeout = (Spinner) findViewById(R.id.sp_unattendedTimeout);

        btnSaveChanges = (Button) findViewById(R.id.btn_save_changes);
        btnDeleteLock = (Button) findViewById(R.id.btn_delete_lock);

        ibBack = (ImageButton) findViewById(R.id.ibBack);
        ibHome = (ImageButton) findViewById(R.id.ibHome);
    }

    private void setViewControllers() {
        ckOperateUnattended.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSpinnerEnabled(spUnattendedTimeout, isChecked);
            }
        });

        btnSaveChanges.setOnClickListener(this);
        btnDeleteLock.setOnClickListener(this);

        ibBack.setOnClickListener(this);
        ibHome.setOnClickListener(this);
    }

    private void setLockData() {
        lock = getIntent().getParcelableExtra(getPackageName() + AppDatabaseFields.FIELD_LOCK_NAME);
        tvLockName.setText(lock.lockName);
        tvLockId.setText("Lock ID -" + lock.lockId);
        tvLockMac.setText("Lock MAC - " + lock.lockMac);
        tvLockSlot.setText("Lock Slot - " + lock.lockSlot);

        etLockName.setText(lock.lockName);
        etLockPass.setText(lock.lockPassword);

        ckLockSavePassword.setChecked(lock.lockSettingsSavePassword == 1);
        ckLockEnableKeypad.setChecked(lock.lockSettingsKeypad == 1);
        ckOperateUnattended.setChecked(lock.lockSettingsOperateUnattended == 1);

        spUnattendedTimeout.setSelection(lock.lockSettingsUnattendedTimeoutPosition);
        setSpinnerEnabled(spUnattendedTimeout, ckOperateUnattended.isChecked());

    }

    private void setSpinnerEnabled(Spinner spinner, boolean enabled) {
        spinner.setEnabled(enabled);
        spinner.setAlpha(enabled ? 1.0f : 0.4f);
    }

    @Override
    public void onAppDatabaseIntegrated(AppDatabaseAdapter db) {
        this.db = db;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save_changes)
            if (etLockName.getText().toString().trim().isEmpty())
                Toast.makeText(this, "Please add a lock name.", Toast.LENGTH_SHORT).show();
            else if (etLockPass.getText().toString().trim().isEmpty())
                Toast.makeText(this, "Please enter password.", Toast.LENGTH_SHORT).show();
            else
                updateLockDetails();
        if (v.getId() == R.id.btn_delete_lock)
            deleteLock();

        if (v.getId() == R.id.ibBack)
            finish();
        if (v.getId() == R.id.ibHome) {
            finish();
        }
    }

    private void updateLockDetails() {
        db.updateLock(lock._id, etLockName.getText().toString(), etLockPass.getText().toString(), lock.lockMac, lock.lockId, lock.lockSlot, ckLockSavePassword.isChecked() ? 1 : 0, ckLockEnableKeypad.isChecked() ? 1 : 0, ckOperateUnattended.isChecked() ? 1 : 0, spUnattendedTimeout.getSelectedItemPosition(),lock.lockBatteryPercent);

        finish();
    }

    private void deleteLock() {
        AlertDialog deletionAlert = new AlertDialog.Builder(this).setTitle("Delete Lock?")
                .setMessage("Are you sure you want to delete this lock?").setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean s = db.deleteLock(lock._id);
                        Log.v("Deleted", "=" + s);
                        dialog.dismiss();
                        finish();
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        deletionAlert.show();

    }
}
