/*
 * Version for Cordova/PhoneGap
 * © 2017 YANDEX
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://yandex.com/legal/appmetrica_sdk_agreement/
 */

"use strict";

function appMetricaExec(methodName, argsArray) {
    var className = 'AppMetrica';
    cordova.exec(
        function () {},
        function (err) {
            console.warn('AppMetrica:cordova.exec(' +
                className + '.' + methodName + '): ' + err)
        },
        className,
        methodName,
        argsArray
    );
}

module.exports = {
    /**
     * Activates AppMetrica with API key and configuration.
     *
     * @param {Object} config
     *      Object with activation properties:
     *          apiKey {String} GUID-like API key from AppMetrica web interface.
     *          handleFirstActivationAsUpdateEnabled {Boolean?} First app launch with the AppMetrica SDK should be treated
     *                                                          as the first launch of the updated app version, and not as
     *                                                          the very first launch. Default: false.
     *          trackLocationEnabled {Boolean?} Allow or forbid the library to send device location data in reports.
     *                                          Default: true.
     *          sessionTimeout {Number?} Session timeout in seconds. Default: 10.
     *          reportCrashesEnabled {Boolean?} Monitor app crashes. Default: true.
     *          appVersion {String?} Custom app version. Default: null.
     *          loggingEnabled {Boolean?} Logging. Default: false.
     *          location {Object?} Custom location (default: null):
     *              latitude {Number} Latitude.
     *              longitude {Number} Longitude.
     *              altitude {Number?} Altitude.
     *              accuracy {Number?} Horizontal accuracy in meters.
     *              verticalAccuracy {Number?} Vertical accuracy in meters.
     *              course {Number?} Movement course in degrees.
     *              speed {Number?} Movement speed in km/h.
     *              timestamp {Number?} Unix time stamp of location.
     *          preloadInfo {Object?} Information for tracking pre-installed apps (Default: null):
     *              trackingId {String} Identifier of tracker.
     *              additionalInfo {Object?} Additional information in key-value form.
     * @see https://tech.yandex.com/appmetrica/doc/mobile-sdk-dg/concepts/mobilesdk-about-docpage/
     */
    activate: function (config) {
        appMetricaExec('activate', [config]);
        cordova.fireDocumentEvent('metricaconfigurationupdate', {"config": config});
    },
    /**
     * Reports event with name and parameters.
     *
     * @param {String} eventName
     *      Short name or description of the event.
     * @param {Object?} params
     *      Object of name/value pairs that should be sent to the server.
     */
    reportEvent: function (eventName, params) {
        appMetricaExec('reportEvent', [eventName, params]);
    },
    /**
     * Reports error with name and reason.
     *
     * @param {String} errorName
     *      Short name or description of the error.
     * @param {String?} reason
     *      Reason of the error.
     */
    reportError: function (errorName, reason) {
        appMetricaExec('reportError', [errorName, reason]);
    },
    /**
     * Setting the arbitrary application version.
     *
     * @param {String?} appVersion
     *      Application version to be reported.
     */
    setCustomAppVersion: function (appVersion) {
        appMetricaExec('setCustomAppVersion', [appVersion]);
    },
    /**
     * Custom geolocation of device.
     *
     * @param {Object?} location
     *      Custom device location to be reported.
     * @see See 'location' parameter from 'config' object of 'activate' method.
     */
    setLocation: function (location) {
        appMetricaExec('setLocation', [location]);
    },
    /**
     * Enable/disable location reporting to AppMetrica.
     *
     * @param {Boolean} enabled
     *      Flag indicating if reporting location to AppMetrica enabled.
     */
    setTrackLocationEnabled: function (enabled) {
        appMetricaExec('setTrackLocationEnabled', [enabled]);
    },
    /**
     * Setting key - value data to be used as additional information, associated with future unhandled exception.
     * If value is null, previously set key-value is removed. Does nothing if key hasn't been added.
     *
     * @param {String} key
     *      The error environment key.
     * @param {String?} value
     *      The error environment value.
     */
    setEnvironmentValue: function (key, value) {
        appMetricaExec('setEnvironmentValue', [key, value]);
    },
    /**
     * Setting session timeout (in seconds).
     *
     * @param {Number} sessionTimeoutSeconds
     *      Time limit before the application is considered inactive.
     *      By default, the session times out if the application is in background for 10 seconds.
     *      Minimum accepted value is 10 seconds. All passed values below 10 seconds automatically become 10 seconds.
     */
    setSessionTimeout: function (sessionTimeoutSeconds) {
        appMetricaExec('setSessionTimeout', [sessionTimeoutSeconds]);
    },
    /**
     * Enable/disable app crashes tracking.
     *
     * @param {Boolean} enabled
     *      Flag indicating if reporting crashes to AppMetrica enabled.
     */
    setReportCrashesEnabled: function (enabled) {
        appMetricaExec('setReportCrashesEnabled', [enabled]);
    },
    /**
     * Enable logging.
     */
    setLoggingEnabled: function () {
        appMetricaExec('setLoggingEnabled', []);
    },
    /**
     * Enable/disable transmission of data about installed apps on the device.
     * By default, sending data about installed apps is disabled.
     * (Android only)
     *
     * @param {Boolean} enabled
     *      Flag indicating if reporting installed apps to AppMetrica enabled.
     */
    setCollectInstalledAppsEnabled: function (enabled) {
        appMetricaExec('setCollectInstalledAppsEnabled', [enabled]);
    }
}
