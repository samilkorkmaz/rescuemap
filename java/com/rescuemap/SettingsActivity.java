package com.rescuemap;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    EditText etChild7YearsOldSpeed_kmph;
    EditText etChild15YearsOldSpeed_kmph;
    EditText etAdult30YearsOldSpeed_kmph;
    TextView tvAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        etChild7YearsOldSpeed_kmph = (EditText) findViewById(R.id.etChild7YearsOldSpeed_kmph);
        etChild15YearsOldSpeed_kmph = (EditText) findViewById(R.id.etChild15YearsOldSpeed_kmph);
        etAdult30YearsOldSpeed_kmph = (EditText) findViewById(R.id.etAdult30YearsOldSpeed_kmph);
        tvAbout = (TextView) findViewById(R.id.tvAbout);
        showSettings();
    }

    private void showSettings() {
        etChild7YearsOldSpeed_kmph.setText(String.valueOf(MySettings.getChild7YearsOldSpeedKmph()));
        etChild15YearsOldSpeed_kmph.setText(String.valueOf(MySettings.getChild15YearsOldSpeedKmph()));
        etAdult30YearsOldSpeed_kmph.setText(String.valueOf(MySettings.getAdult30YearsOldSpeedKmph()));
        tvAbout.setText("Version " + getVersionName() + ", copyright Şamil Korkmaz");
    }

    public void btnSaveClick(View view) {
        MySettings.setChild7YearsOldSpeedKmph(Float.parseFloat(etChild7YearsOldSpeed_kmph.getText().toString()));
        MySettings.setChild15YearsOldSpeedKmph(Float.parseFloat(etChild15YearsOldSpeed_kmph.getText().toString()));
        MySettings.setAdult30YearsOldSpeedKmph(Float.parseFloat(etAdult30YearsOldSpeed_kmph.getText().toString()));
        finish();
    }

    public void btnCancelClick(View view) {
        finish();
    }

    public void btnRestoreDefaultsClick(View view) {
        etChild7YearsOldSpeed_kmph.setText(String.valueOf(MySettings.getChild7YearsOldDefaultSpeedKmph()));
        etChild15YearsOldSpeed_kmph.setText(String.valueOf(MySettings.getChild15YearsOldDefaultSpeedKmph()));
        etAdult30YearsOldSpeed_kmph.setText(String.valueOf(MySettings.getAdult30YearsOldDefaultSpeedKmph()));
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
