package com.example.imamin.keypadproject.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.example.imamin.keypadproject.InstantAutoComplete;
import com.example.imamin.keypadproject.MainActivity;
import com.example.imamin.keypadproject.R;

import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class Login extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    @Bind(R.id.autoTvUserName)
    InstantAutoComplete autoTvUserName;
    @Bind(R.id.etPassword)
    EditText etPassword;
    @Bind(R.id.btnSubmit)
    Button btnSubmit;
    @Bind(R.id.rgKeypadSelector)
    RadioGroup rgKeypadSelector;

    SharedPreferences sharedPreferences;
    Handler handler = new Handler(Looper.getMainLooper());
    Set<String> userNames;
    String[] userNamesAliasSet;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View loginView = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, loginView);
        return loginView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userNames = sharedPreferences.getStringSet("userNames", new HashSet<String>());
        if (userNames.size() != 0) {
            userNamesAliasSet = new String[userNames.size()];
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, userNames.toArray(userNamesAliasSet));
            autoTvUserName.setAdapter(adapter);
        }
        autoTvUserName.setOnItemClickListener(this);
        btnSubmit.setOnClickListener(this);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                autoTvUserName.showDropDown();
            }
        },1000);
    }

    @Override
    public void onClick(View v) {
        if (autoTvUserName.getText().toString().replaceAll(" ", "").length() != 0) {
            userNames.add(autoTvUserName.getText().toString());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("userNames").apply();
            editor.putStringSet("userNames", userNames).apply();

            ((MainActivity) getActivity()).onSuccessfulLogin(rgKeypadSelector.getCheckedRadioButtonId()==R.id.rbKeypad1);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        etPassword.setText(userNamesAliasSet[position]);
    }
}
