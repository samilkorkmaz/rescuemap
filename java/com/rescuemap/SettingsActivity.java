package com.rescuemap;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    EditText etChild7YearsOldSpeed_mps;
    EditText etChild15YearsOldSpeed_mps;
    EditText etAdult30YearsOldSpeed_mps;
    TextView tvAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        etChild7YearsOldSpeed_mps = (EditText) findViewById(R.id.etChild7YearsOldSpeed_mps);
        etChild15YearsOldSpeed_mps = (EditText) findViewById(R.id.etChild15YearsOldSpeed_mps);
        etAdult30YearsOldSpeed_mps = (EditText) findViewById(R.id.etAdult30YearsOldSpeed_mps);
        tvAbout = (TextView) findViewById(R.id.tvAbout);
        showSettings();
    }

    private void showSettings() {
        etChild7YearsOldSpeed_mps.setText(String.valueOf(MySettings.getChild7YearsOldSpeedMps()));
        etChild15YearsOldSpeed_mps.setText(String.valueOf(MySettings.getChild15YearsOldSpeedMps()));
        etAdult30YearsOldSpeed_mps.setText(String.valueOf(MySettings.getAdult30YearsOldSpeedMps()));
        tvAbout.setText("Version " + getVersionName() + ", copyright Şamil Korkmaz");
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

    public String getVersionName() {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionName;
    }
}
