/*
 * Version for Cordova/PhoneGap
 * © 2017-2019 YANDEX
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
     *          handleFirstActivationAsUpdate {Boolean?} First app launch with the AppMetrica SDK should be treated
     *                                                   as the first launch of the updated app version, and not as
     *                                                   the very first launch. Default: false.
     *          locationTracking {Boolean?} Allow or forbid the library to send device location data in reports.
     *                                      Default: true.
     *          sessionTimeout {Number?} Session timeout in seconds. Default: 10.
     *          crashReporting {Boolean?} Monitor app crashes. Default: true.
     *          appVersion {String?} Custom app version. Default: null.
     *          logs {Boolean?} Logging. Default: false.
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
     *              additionalParams {Object?} Additional information in key-value form.
     * @see https://appmetrica.yandex.com/docs/mobile-sdk-dg/concepts/mobilesdk-about.html
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
    setLocationTracking: function (enabled) {
        appMetricaExec('setLocationTracking', [enabled]);
    }
};
