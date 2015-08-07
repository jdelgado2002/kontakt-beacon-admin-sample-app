package com.kontakt.sample.ui.activity;

import android.os.Bundle;
import android.os.Parcel;

import com.kontakt.sample.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.kontakt.api_exp.ApiClient;
import io.kontakt.api_exp.model.Device;
import io.kontakt.api_exp.model.DeviceResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ApiExpActivity extends BaseActivity {

    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_exp_layout);
        ButterKnife.inject(this);
        apiClient = ApiClient.getInstance("android-apikey");
    }

    @OnClick(R.id.get_devices)
    void onDeviceClicked(){
        apiClient.getDevices(new Callback<DeviceResponse>() {
            @Override
            public void success(DeviceResponse deviceResponse, Response response) {
                System.out.println("device response " + deviceResponse);
                Device device = deviceResponse.getDevices().get(0);
                Parcel obtain = Parcel.obtain();
                device.writeToParcel(obtain, 0);
                obtain.setDataPosition(0);
                Device fromParcel = Device.CREATOR.createFromParcel(obtain);

            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
    }
}
