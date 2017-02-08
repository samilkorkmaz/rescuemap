package com.rescuemap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    EditText etChild7YearsOldSpeed_mps;
    EditText etChild15YearsOldSpeed_mps;
    EditText etAdult30YearsOldSpeed_mps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        etChild7YearsOldSpeed_mps = (EditText) findViewById(R.id.etChild7YearsOldSpeed_mps);
        etChild15YearsOldSpeed_mps = (EditText) findViewById(R.id.etChild15YearsOldSpeed_mps);
        etAdult30YearsOldSpeed_mps = (EditText) findViewById(R.id.etAdult30YearsOldSpeed_mps);
        showSettings();
    }

    private void showSettings() {
        etChild7YearsOldSpeed_mps.setText(String.valueOf(MySettings.getChild7YearsOldSpeedMps()));
        etChild15YearsOldSpeed_mps.setText(String.valueOf(MySettings.getChild15YearsOldSpeedMps()));
        etAdult30YearsOldSpeed_mps.setText(String.valueOf(MySettings.getAdult30YearsOldSpeedMps()));
    }

    public void btnSaveClick(View view) {
        MySettings.setChild7YearsOldSpeedMps(Float.parseFloat(etChild7YearsOldSpeed_mps.getText().toString()));
        MySettings.setChild15YearsOldSpeedMps(Float.parseFloat(etChild15YearsOldSpeed_mps.getText().toString()));
        MySettings.setAdult30YearsOldSpeedMps(Float.parseFloat(etAdult30YearsOldSpeed_mps.getText().toString()));
        finish();
    }

    public void btnCancelClick(View view) {
        finish();
    }

    public void btnRestoreDefaultsClick(View view) {
        etChild7YearsOldSpeed_mps.setText(String.valueOf(MySettings.getChild7YearsOldDefaultSpeedMps()));
        etChild15YearsOldSpeed_mps.setText(String.valueOf(MySettings.getChild15YearsOldDefaultSpeedMps()));
        etAdult30YearsOldSpeed_mps.setText(String.valueOf(MySettings.getAdult30YearsOldDefaultSpeedMps()));
    }
}
