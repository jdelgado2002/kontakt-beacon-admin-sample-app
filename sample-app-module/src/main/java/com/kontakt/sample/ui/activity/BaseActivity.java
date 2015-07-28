package com.kontakt.sample.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.kontakt.sdk.android.common.profile.DeviceProfile;

public class BaseActivity extends AppCompatActivity {

    public static final String[] SUPPORTED_SWITCHABLE_PROFILES = new String[] {
            DeviceProfile.EDDYSTONE.name(),
            DeviceProfile.IBEACON.name()
    };

    protected void setUpActionBar(final Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    protected void setUpActionBarTitle(final String title) {
        getSupportActionBar().setTitle(title);
    }

}
