package com.technosales.net.buslocationannouncement.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.network.RegisterDevice;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_reg;
    private TextInputEditText reg_device_number;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewIniialize();

        databaseHelper = new DatabaseHelper(this);

        if (databaseHelper.routeStationLists().size() > 1) {
            startActivity(new Intent(this, AnnounceActivity.class));
        }

        sharedPreferences = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0);


    }

    private void viewIniialize() {
        reg_device_number = findViewById(R.id.reg_device_number);
        btn_reg = findViewById(R.id.btn_reg);

        btn_reg.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reg:

                if (reg_device_number.getText().toString().trim().length() > 9) {
                    RegisterDevice.RegisterDevice(this, reg_device_number.getText().toString().trim());
                    sharedPreferences.edit().putString(UtilStrings.DEVICE_ID, reg_device_number.getText().toString().trim()).apply();
                } else {
                    reg_device_number.setError("Enter Device Number");
                }
                break;

        }
    }
}
