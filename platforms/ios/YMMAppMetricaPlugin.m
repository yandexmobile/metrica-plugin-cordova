/*
 * Version for Cordova/PhoneGap
 * © 2017 YANDEX
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://yandex.com/legal/appmetrica_sdk_agreement/
 */

#import "YMMAppMetricaPlugin.h"

#import <YandexMobileMetrica/YandexMobileMetrica.h>
#import <CoreLocation/CoreLocation.h>

static bool gYMMIsAppMetricaActivated = false;

@implementation YMMAppMetricaPlugin

- (void)handleOpenURL:(NSNotification *)notification
{
    [super handleOpenURL:notification];

    NSURL *URL = [notification object];
    if ([URL isKindOfClass:[NSURL class]]) {
        [self dispatchAsync:^{
            [YMMYandexMetrica handleOpenURL:URL];
        }];
    }
}

#pragma mard - Commands

- (void)activate:(CDVInvokedUrlCommand *)command
{
    NSDictionary *configurationDictionary = [command argumentAtIndex:0 withDefault:nil andClass:[NSDictionary class]];

    [self dispatchAsync:^{
        [[self class] activateWithConfigurationDictionary:configurationDictionary];
    }];
}

- (void)reportEvent:(CDVInvokedUrlCommand *)command
{
    NSString *eventName = [command argumentAtIndex:0 withDefault:nil andClass:[NSString class]];
    NSDictionary *eventParams = [command argumentAtIndex:1 withDefault:nil andClass:[NSDictionary class]];

    [self dispatchAsync:^{
        [YMMYandexMetrica reportEvent:eventName
                           parameters:eventParams
                            onFailure:[self failureCallbackForCommand:command]];
    }];
}

- (void)reportError:(CDVInvokedUrlCommand *)command
{
    NSString *errorName = [command argumentAtIndex:0 withDefault:nil andClass:[NSString class]];
    NSString *errorReason = [command argumentAtIndex:1 withDefault:nil andClass:[NSString class]];

    [self dispatchAsync:^{
        NSException *exception = [NSException exceptionWithName:errorName reason:errorReason userInfo:nil];
        [YMMYandexMetrica reportError:errorName
                            exception:exception
                            onFailure:[self failureCallbackForCommand:command]];
    }];
}

- (void)setCustomAppVersion:(CDVInvokedUrlCommand *)command
{
    NSString *appVersion = [command argumentAtIndex:0 withDefault:nil andClass:[NSString class]];

    [self dispatchAsync:^{
        [YMMYandexMetrica setCustomAppVersion:appVersion];
    }];
}

- (void)setLocation:(CDVInvokedUrlCommand *)command
{
    NSDictionary *locationDictionary = [command argumentAtIndex:0 withDefault:nil andClass:[NSDictionary class]];

    [self dispatchAsync:^{
        CLLocation *location = [[self class] locationForDictionary:locationDictionary];
        [YMMYandexMetrica setLocation:location];
    }];
}

- (void)setTrackLocationEnabled:(CDVInvokedUrlCommand *)command
{
    NSNumber *enabledValue = [command argumentAtIndex:0 withDefault:nil andClass:[NSNumber class]];

    if (enabledValue != nil) {
        [self dispatchAsync:^{
            [YMMYandexMetrica setTrackLocationEnabled:enabledValue.boolValue];
        }];
    }
}

- (void)setEnvironmentValue:(CDVInvokedUrlCommand *)command
{
    NSString *key = [command argumentAtIndex:0 withDefault:nil andClass:[NSString class]];
    NSString *value = [command argumentAtIndex:1 withDefault:nil andClass:[NSString class]];

    [self dispatchAsync:^{
        [YMMYandexMetrica setEnvironmentValue:value forKey:key];
    }];
}

- (void)setSessionTimeout:(CDVInvokedUrlCommand *)command
{
    NSNumber *sessionTimeoutValue = [command argumentAtIndex:0 withDefault:nil andClass:[NSNumber class]];

    if (sessionTimeoutValue != nil) {
        [self dispatchAsync:^{
            [YMMYandexMetrica setSessionTimeout:sessionTimeoutValue.unsignedIntegerValue];
        }];
    }
}

- (void)setReportCrashesEnabled:(CDVInvokedUrlCommand *)command
{
    NSNumber *enabledValue = [command argumentAtIndex:0 withDefault:nil andClass:[NSNumber class]];

    if (enabledValue != nil) {
        [self dispatchAsync:^{
            [YMMYandexMetrica setReportCrashesEnabled:enabledValue.boolValue];
        }];
    }
}

- (void)setLoggingEnabled:(CDVInvokedUrlCommand *)command
{
    [self dispatchAsync:^{
        [YMMYandexMetrica setLoggingEnabled:YES];
    }];
}

- (void)setCollectInstalledAppsEnabled:(CDVInvokedUrlCommand *)command
{
    // Do nothing. Not available for iOS platform.
}

#pragma mark - Utils

- (void (^)(NSError *error))failureCallbackForCommand:(CDVInvokedUrlCommand *)command
{
    return ^(NSError * _Nonnull error) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                    messageAsString:error.description];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    };
}

+ (YMMYandexMetricaConfiguration *)configurationForDictionary:(NSDictionary *)configurationDictionary
{
    NSString *apiKey = configurationDictionary[@"apiKey"];
    YMMYandexMetricaConfiguration *configuration = [[YMMYandexMetricaConfiguration alloc] initWithApiKey:apiKey];

    NSNumber *handleFirstActivationAsUpdateEnabled = configurationDictionary[@"handleFirstActivationAsUpdateEnabled"];
    NSNumber *trackLocationEnabled = configurationDictionary[@"trackLocationEnabled"];
    NSNumber *sessionTimeout = configurationDictionary[@"sessionTimeout"];
    NSNumber *reportCrashesEnabled = configurationDictionary[@"reportCrashesEnabled"];
    NSString *customAppVersion = configurationDictionary[@"appVersion"];
    NSNumber *loggingEnabled = configurationDictionary[@"loggingEnabled"];
    NSDictionary *customLocationDictionary = configurationDictionary[@"location"];
    NSDictionary *preloadInfoDictionary = configurationDictionary[@"preloadInfo"];

    if (handleFirstActivationAsUpdateEnabled != nil) {
        configuration.handleFirstActivationAsUpdateEnabled = [handleFirstActivationAsUpdateEnabled boolValue];
    }
    if (trackLocationEnabled != nil) {
        configuration.trackLocationEnabled = [trackLocationEnabled boolValue];
    }
    if (sessionTimeout != nil) {
        configuration.sessionTimeout = [sessionTimeout unsignedIntegerValue];
    }
    if (reportCrashesEnabled != nil) {
        configuration.reportCrashesEnabled = [reportCrashesEnabled boolValue];
    }
    if (customAppVersion != nil) {
        configuration.customAppVersion = customAppVersion;
    }
    if (loggingEnabled != nil) {
        configuration.loggingEnabled = [loggingEnabled boolValue];
    }
    if (customLocationDictionary != nil) {
        configuration.location = [self locationForDictionary:customLocationDictionary];;
    }
    if (preloadInfoDictionary != nil) {
        NSString *trackingID = preloadInfoDictionary[@"trackingId"];
        YMMYandexMetricaPreloadInfo *preloadInfo =
            [[YMMYandexMetricaPreloadInfo alloc] initWithTrackingIdentifier:trackingID];
        NSDictionary *additionalInfo = preloadInfoDictionary[@"additionalInfo"];
        for (NSString *key in additionalInfo) {
            [preloadInfo setAdditionalInfo:additionalInfo[key] forKey:key];
        }
        configuration.preloadInfo = preloadInfo;
    }

    return configuration;
}

+ (CLLocation *)locationForDictionary:(NSDictionary *)locationDictionary
{
    if (locationDictionary == nil) {
        return nil;
    }

    NSNumber *latitude = locationDictionary[@"latitude"];
    NSNumber *longitude = locationDictionary[@"longitude"];
    NSNumber *altitude = locationDictionary[@"altitude"];
    NSNumber *horizontalAccuracy = locationDictionary[@"accuracy"];
    NSNumber *verticalAccuracy = locationDictionary[@"verticalAccuracy"];
    NSNumber *course = locationDictionary[@"course"];
    NSNumber *speed = locationDictionary[@"speed"];
    NSNumber *timestamp = locationDictionary[@"timestamp"];

    NSDate *locationDate = timestamp != nil ? [NSDate dateWithTimeIntervalSince1970:timestamp.doubleValue] : [NSDate date];
    CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake(latitude.doubleValue, longitude.doubleValue);
    CLLocation *location = [[CLLocation alloc] initWithCoordinate:coordinate
                                                         altitude:altitude.doubleValue
                                               horizontalAccuracy:horizontalAccuracy.doubleValue
                                                 verticalAccuracy:verticalAccuracy.doubleValue
                                                           course:course.doubleValue
                                                            speed:speed.doubleValue
                                                        timestamp:locationDate];
    return location;
}

- (void)dispatchAsync:(dispatch_block_t)block
{
    [self.commandDelegate runInBackground:block];
}

+ (void)activateWithConfigurationDictionary:(NSDictionary *)configuration
{
    YMMYandexMetricaConfiguration *config = [[self class] configurationForDictionary:configuration];
    [YMMYandexMetrica activateWithConfiguration:config];
    gYMMIsAppMetricaActivated = true;
}

+ (bool)isAppMetricaActivated
{
    return gYMMIsAppMetricaActivated;
}

@end
