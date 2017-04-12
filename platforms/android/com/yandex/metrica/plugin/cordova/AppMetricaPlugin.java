/*
 *  AppMetricaPlugin.java
 *
 * This file is a part of the AppMetrica.
 *
 * Version for Android © 2017 YANDEX
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at https://yandex.com/legal/metrica_termsofuse/
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
                    } else if ("setCustomAppVersion".equals(action)) {
                        setCustomAppVersion(args, callbackContext);
                    } else if ("setLocation".equals(action)) {
                        setLocation(args, callbackContext);
                    } else if ("setTrackLocationEnabled".equals(action)) {
                        setTrackLocationEnabled(args, callbackContext);
                    } else if ("setEnvironmentValue".equals(action)) {
                        setEnvironmentValue(args, callbackContext);
                    } else if ("setSessionTimeout".equals(action)) {
                        setSessionTimeout(args, callbackContext);
                    } else if ("setReportCrashesEnabled".equals(action)) {
                        setReportCrashesEnabled(args, callbackContext);
                    } else if ("setLoggingEnabled".equals(action)) {
                        setLoggingEnabled(args, callbackContext);
                    } else if ("setCollectInstalledAppsEnabled".equals(action)) {
                        setCollectInstalledAppsEnabled(args, callbackContext);
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
    public void onPause(boolean multitasking) {
        onPauseActivity();
    }

    @Override
    public void onResume(boolean multitasking) {
        onResumeActivity();
    }

    @Override
    public void onNewIntent(Intent intent) {
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

    private void onPauseActivity() {
        synchronized (mLock) {
            mActivityPaused = true;
            if (mAppMetricaActivated) {
                YandexMetrica.onPauseActivity(getActivity());
            }
        }
    }

    private void onResumeActivity() {
        synchronized (mLock) {
            mActivityPaused = false;
            if (mAppMetricaActivated) {
                YandexMetrica.onResumeActivity(getActivity());
            }
        }
    }

    private Location toLocation(JSONObject locationObj) throws JSONException {
        Location location = new Location("Custom");

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

    private YandexMetricaConfig toConfig(JSONObject configObj) throws JSONException {
        String apiKey = configObj.getString("apiKey");
        YandexMetricaConfig.Builder builder = YandexMetricaConfig.newConfigBuilder(apiKey);

        if (configObj.has("handleFirstActivationAsUpdateEnabled")) {
            builder.handleFirstActivationAsUpdate(configObj.getBoolean("handleFirstActivationAsUpdateEnabled"));
        }
        if (configObj.has("trackLocationEnabled")) {
            builder.setTrackLocationEnabled(configObj.getBoolean("trackLocationEnabled"));
        }
        if (configObj.has("sessionTimeout")) {
            builder.setSessionTimeout(configObj.getInt("sessionTimeout"));
        }
        if (configObj.has("reportCrashesEnabled")) {
            builder.setReportCrashesEnabled(configObj.getBoolean("reportCrashesEnabled"));
        }
        if (configObj.has("appVersion")) {
            builder.setAppVersion(configObj.getString("appVersion"));
        }
        if (configObj.optBoolean("loggingEnabled", false)) {
            builder.setLogEnabled();
        }
        if (configObj.has("location")) {
            Location location = toLocation(configObj.getJSONObject("location"));
            builder.setLocation(location);
        }
        if (configObj.has("preloadInfo")) {
            JSONObject preloadInfoObj = configObj.getJSONObject("preloadInfo");
            PreloadInfo.Builder infoBuilder = PreloadInfo.newBuilder(preloadInfoObj.getString("trackingId"));
            JSONObject additionalInfoObj = preloadInfoObj.optJSONObject("additionalInfo");
            if (additionalInfoObj != null) {
                for (Iterator<String> keyIterator = additionalInfoObj.keys(); keyIterator.hasNext();) {
                    String key = keyIterator.next();
                    String value = additionalInfoObj.getString(key);
                    infoBuilder.setAdditionalParams(key, value);
                }
            }
            builder.setPreloadInfo(infoBuilder.build());
        }

        return builder.build();
    }

    private void activate(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final JSONObject configObj = args.getJSONObject(0);
        final YandexMetricaConfig config = toConfig(configObj);

        Context context = getActivity().getApplicationContext();
        YandexMetrica.activate(context, config);

        synchronized (mLock) {
            if (mAppMetricaActivated == false) {
                YandexMetrica.reportAppOpen(getActivity());
                if (mActivityPaused == false) {
                    onResumeActivity();
                }
            }
            mAppMetricaActivated = true;
        }
    }

    private void reportEvent(JSONArray args, CallbackContext callbackContext) throws JSONException {
        final String eventName = args.getString(0);
        String eventParametersJSONString = null;
        try {
            JSONObject eventParametersObj = args.getJSONObject(1);
            eventParametersJSONString = eventParametersObj.toString();
        } catch (JSONException ignored) {}

        if (eventParametersJSONString != null) {
            YandexMetrica.reportEvent(eventName, eventParametersJSONString);
        } else {
            YandexMetrica.reportEvent(eventName);
        }
    }

    private void reportError(JSONArray args, CallbackContext callbackContext) throws JSONException {
        final String errorName = args.getString(0);
        Throwable errorThrowable = null;
        try {
            String errorReason = args.getString(1);
            errorThrowable = new Throwable(errorReason);
        } catch (JSONException ignored) {}

        YandexMetrica.reportError(errorName, errorThrowable);
    }

    private void setCustomAppVersion(JSONArray args, CallbackContext callbackContext) throws JSONException {
        final String appVersion = args.getString(0);

        YandexMetrica.setCustomAppVersion(appVersion);
    }

    private void setLocation(JSONArray args, CallbackContext callbackContext) throws JSONException {
        final JSONObject locationObj = args.getJSONObject(0);

        Location location = toLocation(locationObj);
        YandexMetrica.setLocation(location);
    }

    private void setTrackLocationEnabled(JSONArray args, CallbackContext callbackContext) throws JSONException {
        final boolean enabled = args.getBoolean(0);

        YandexMetrica.setTrackLocationEnabled(enabled);
    }

    private void setEnvironmentValue(JSONArray args, CallbackContext callbackContext) throws JSONException {
        final String key = args.getString(0);
        final String value = args.getString(1);

        YandexMetrica.setEnvironmentValue(key, value);
    }

    private void setSessionTimeout(JSONArray args, CallbackContext callbackContext) throws JSONException {
        final int sessionTimeout = args.getInt(0);

        YandexMetrica.setSessionTimeout(sessionTimeout);
    }

    private void setReportCrashesEnabled(JSONArray args, CallbackContext callbackContext) throws JSONException {
        final boolean enabled = args.getBoolean(0);

        YandexMetrica.setReportCrashesEnabled(enabled);
    }

    private void setLoggingEnabled(JSONArray args, CallbackContext callbackContext) throws JSONException {
        YandexMetrica.setLogEnabled();
    }

    private void setCollectInstalledAppsEnabled(JSONArray args, CallbackContext callbackContext) throws JSONException {
        final boolean enabled = args.getBoolean(0);

        YandexMetrica.setCollectInstalledApps(enabled);
    }

}
