package com.kontakt.sample.loader;

import android.content.Context;

import com.kontakt.sdk.android.common.model.IPreset;
import com.kontakt.sdk.android.http.HttpResult;
import com.kontakt.sdk.android.http.KontaktApiClient;
import com.kontakt.sdk.android.http.exception.ClientException;
import com.kontakt.sdk.android.http.interfaces.ConfigurationApiAccessor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ProfilesLoader extends AbstractLoader<List<IPreset>> {

    private final DisableableContentObserver observer;

    private final ConfigurationApiAccessor profilesApi;

    public ProfilesLoader(Context context) {
        super(context);
        observer = new DisableableContentObserver(new ForceLoadContentObserver());
        profilesApi = new KontaktApiClient();
    }

    @Override
    protected void onStartLoading() {
        observer.setEnabled(true);
        super.onStartLoading();
    }

    @Override
    protected void onAbandon() {
        observer.setEnabled(false);
    }

    @Override
    protected void onReset() {
        super.onReset();
        observer.setEnabled(false);
        try {
            profilesApi.close();
        } catch (IOException ignored) { }
    }

    @Override
    public List<IPreset> loadInBackground() {
        try {
            HttpResult<List<IPreset>> profilesResult = profilesApi.listPresets();
            return profilesResult.isPresent() ? profilesResult.get() : Collections.<IPreset>emptyList();
        } catch (ClientException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
