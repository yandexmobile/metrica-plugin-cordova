/*
 * Version for Cordova/PhoneGap
 * © 2017-2019 YANDEX
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://yandex.com/legal/appmetrica_sdk_agreement/
 */

package com.yandex.metrica.plugin.cordova;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yandex.metrica.PreloadInfo;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMetricaPlugin extends CordovaPlugin {

    private final Object mLock = new Object();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private boolean mActivityPaused = true;
    private boolean mAppMetricaActivated = false;

    @Override
    public boolean execute(final String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        getAppMetricaExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if ("activate".equals(action)) {
                        activate(args, callbackContext);
                    } else if ("reportEvent".equals(action)) {
                        reportEvent(args, callbackContext);
                    } else if ("reportError".equals(action)) {
                        reportError(args, callbackContext);
                    } else if ("setLocation".equals(action)) {
                        setLocation(args, callbackContext);
                    } else if ("setLocationTracking".equals(action)) {
                        setLocationTracking(args, callbackContext);
                    } else {
                        callbackContext.error("Unknown action: " + action);
                    }
                } catch (JSONException ex) {
                    callbackContext.error(ex.getMessage());
                }
            }
        });
        return true;
    }

    @Override
    public void onPause(final boolean multitasking) {
        onPauseSession();
    }

    @Override
    public void onResume(final boolean multitasking) {
        onResumeSession();
    }

    @Override
    public void onNewIntent(final Intent intent) {
        getAppMetricaExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (mAppMetricaActivated) {
                    YandexMetrica.reportAppOpen(getActivity());
                }
            }
        });
    }

    private Activity getActivity() {
        return cordova.getActivity();
    }

    private ExecutorService getAppMetricaExecutor() {
        return mExecutor;
    }

    private void onPauseSession() {
        synchronized (mLock) {
            mActivityPaused = true;
            if (mAppMetricaActivated) {
                YandexMetrica.pauseSession(getActivity());
            }
        }
    }

    private void onResumeSession() {
        synchronized (mLock) {
            mActivityPaused = false;
            if (mAppMetricaActivated) {
                YandexMetrica.resumeSession(getActivity());
            }
        }
    }

    public static Location toLocation(final JSONObject locationObj) throws JSONException {
        final Location location = new Location("Custom");

        if (locationObj.has("latitude")) {
            location.setLatitude(locationObj.getDouble("latitude"));
        }
        if (locationObj.has("longitude")) {
            location.setLongitude(locationObj.getDouble("longitude"));
        }
        if (locationObj.has("altitude")) {
            location.setAltitude(locationObj.getDouble("altitude"));
        }
        if (locationObj.has("accuracy")) {
            location.setAccuracy((float) locationObj.getDouble("accuracy"));
        }
        if (locationObj.has("course")) {
            location.setBearing((float) locationObj.getDouble("course"));
        }
        if (locationObj.has("speed")) {
            location.setSpeed((float) locationObj.getDouble("speed"));
        }
        if (locationObj.has("timestamp")) {
            location.setTime(locationObj.getLong("timestamp"));
        }

        return location;
    }

    public static YandexMetricaConfig toConfig(final JSONObject configObj) throws JSONException {
        final String apiKey = configObj.getString("apiKey");
        final YandexMetricaConfig.Builder builder = YandexMetricaConfig.newConfigBuilder(apiKey);

        if (configObj.has("handleFirstActivationAsUpdate")) {
            builder.handleFirstActivationAsUpdate(configObj.getBoolean("handleFirstActivationAsUpdate"));
        }
        if (configObj.has("locationTracking")) {
            builder.withLocationTracking(configObj.getBoolean("locationTracking"));
        }
        if (configObj.has("sessionTimeout")) {
            builder.withSessionTimeout(configObj.getInt("sessionTimeout"));
        }
        if (configObj.has("crashReporting")) {
            builder.withCrashReporting(configObj.getBoolean("crashReporting"));
        }
        if (configObj.has("appVersion")) {
            builder.withAppVersion(configObj.getString("appVersion"));
        }
        if (configObj.optBoolean("logs", false)) {
            builder.withLogs();
        }
        if (configObj.has("location")) {
            final Location location = toLocation(configObj.getJSONObject("location"));
            builder.withLocation(location);
        }
        if (configObj.has("preloadInfo")) {
            final JSONObject preloadInfoObj = configObj.getJSONObject("preloadInfo");
            final PreloadInfo.Builder infoBuilder = PreloadInfo.newBuilder(preloadInfoObj.getString("trackingId"));
            final JSONObject additionalInfoObj = preloadInfoObj.optJSONObject("additionalParams");
            if (additionalInfoObj != null) {
                for (Iterator<String> keyIterator = additionalInfoObj.keys(); keyIterator.hasNext(); ) {
                    final String key = keyIterator.next();
                    final String value = additionalInfoObj.getString(key);
                    infoBuilder.setAdditionalParams(key, value);
                }
            }
            builder.withPreloadInfo(infoBuilder.build());
        }

        return builder.build();
    }

    private void activate(final JSONArray args,
                          final CallbackContext callbackContext) throws JSONException {
        final JSONObject configObj = args.getJSONObject(0);
        final YandexMetricaConfig config = toConfig(configObj);

        final Context context = getActivity().getApplicationContext();
        YandexMetrica.activate(context, config);

        synchronized (mLock) {
            if (mAppMetricaActivated == false) {
                YandexMetrica.reportAppOpen(getActivity());
                if (mActivityPaused == false) {
                    YandexMetrica.resumeSession(getActivity());
                }
            }
            mAppMetricaActivated = true;
        }
    }

    private void reportEvent(final JSONArray args,
                             final CallbackContext callbackContext) throws JSONException {
        final String eventName = args.getString(0);
        String eventParametersJSONString = null;
        try {
            final JSONObject eventParametersObj = args.getJSONObject(1);
            eventParametersJSONString = eventParametersObj.toString();
        } catch (JSONException ignored) {}

        if (eventParametersJSONString != null) {
            YandexMetrica.reportEvent(eventName, eventParametersJSONString);
        } else {
            YandexMetrica.reportEvent(eventName);
        }
    }

    private void reportError(final JSONArray args,
                             final CallbackContext callbackContext) throws JSONException {
        final String errorName = args.getString(0);
        Throwable errorThrowable = null;
        try {
            final String errorReason = args.getString(1);
            errorThrowable = new Throwable(errorReason);
        } catch (JSONException ignored) {}

        YandexMetrica.reportError(errorName, errorThrowable);
    }

    private void setLocation(final JSONArray args,
                             final CallbackContext callbackContext) throws JSONException {
        final JSONObject locationObj = args.getJSONObject(0);

        final Location location = toLocation(locationObj);
        YandexMetrica.setLocation(location);
    }

    private void setLocationTracking(final JSONArray args,
                                     final CallbackContext callbackContext) throws JSONException {
        final boolean enabled = args.getBoolean(0);

        YandexMetrica.setLocationTracking(enabled);
    }
}
