package com.napcosecurity.android.ilock.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.db.AppDatabaseAdapter;
import com.napcosecurity.android.ilock.db.AppDatabaseIntegrator;
import com.napcosecurity.android.ilock.lock.Lock;

import java.util.Random;

public class AddLockActivity extends BaseActivity implements AppDatabaseIntegrator, View.OnClickListener {
    EditText etLockName, etLockPass;
    CheckBox ckSavePass, ckEnableKeypad;
    Button btnAddLock;
    ImageButton ibBack, ibHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);
        initViews();
        setViewControllers();
    }

    private void initViews() {
        etLockName = (EditText) findViewById(R.id.etLockName);
        etLockPass = (EditText) findViewById(R.id.etLockPass);
        ckSavePass = (CheckBox) findViewById(R.id.ck_savepass);
        ckEnableKeypad = (CheckBox) findViewById(R.id.ck_keypad);

        btnAddLock = (Button) findViewById(R.id.btn_add_lock);

        ibBack = (ImageButton) findViewById(R.id.ibBack);
        ibHome = (ImageButton) findViewById(R.id.ibHome);
    }

    private void setViewControllers() {
        btnAddLock.setOnClickListener(this);
        ibBack.setOnClickListener(this);
        ibHome.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_add_lock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* int id = item.getItemId();
        if (id == R.id.action_add) {
            new ProgressAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }*/
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibBack) {
            finish();
        }
        if (v.getId() == R.id.ibHome) {
            finish();
        }
        if (v.getId() == R.id.btn_add_lock) {

            if (etLockName.getText().toString().trim().isEmpty())
                Toast.makeText(this, "Please add a lock name.", Toast.LENGTH_SHORT).show();
            else if (etLockPass.getText().toString().trim().isEmpty())
                Toast.makeText(this, "Please enter password.", Toast.LENGTH_SHORT).show();
            else
                new ProgressAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    private class ProgressAsync extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(AddLockActivity.this);
            progressDialog.setTitle("Adding Lock");
            progressDialog.setMessage("Step 1 of 3");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("Step 2 of 3");
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("Step 3 of 3");
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            progressDialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            showAddingIndeterminate();
        }
    }

    private void showAddingIndeterminate() {
        AlertDialog alertDialog;
        Random random = new Random();
        if (random.nextBoolean()) {
            alertDialog = new AlertDialog.Builder(AddLockActivity.this)
                    .setCancelable(false).setTitle("Successful")
                    .setMessage("Lock has successfully added.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).create();
            requestDb(this);
        } else {
            alertDialog = new AlertDialog.Builder(AddLockActivity.this)
                    .setTitle("Error").setMessage("Would you like to try again?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            new ProgressAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).create();
        }
        alertDialog.show();
    }

    @Override
    public void onAppDatabaseIntegrated(AppDatabaseAdapter db) {
        Lock lock = createLock();
        Random random = new Random();
        /*db.insertLock(lock.getLockId(), lock.getLockName(), lock.getLockStatus(), lock.getLockConnectionStatus(), lock.getLockSettingsPassword(), lock.getLockSettingsKeypad(), lock.getLockPassword(), "" + random.nextInt(80) + "%");*/
    }

    private Lock createLock() {
      Lock lock = new Lock();
        /*  lock.setLockName(etLockName.getText().toString());
        lock.setLockStatus("locked");
        lock.setLockConnectionStatus("connected");
        lock.setLockSettingsPassword(ckSavePass.isChecked() ? "enable" : "disable");
        lock.setLockSettingsKeypad(ckEnableKeypad.isChecked() ? "enable" : "disable");
        lock.setLockPassword(etLockPass.getText().toString());*/
        return lock;
    }
}
