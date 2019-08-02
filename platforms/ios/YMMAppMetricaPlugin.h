/*
 * Version for Cordova/PhoneGap
 * © 2017-2019 YANDEX
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://yandex.com/legal/appmetrica_sdk_agreement/
 */

#import <Cordova/CDVPlugin.h>

@interface YMMAppMetricaPlugin : CDVPlugin <UIApplicationDelegate>

- (void)activate:(CDVInvokedUrlCommand *)command;
- (void)reportEvent:(CDVInvokedUrlCommand *)command;
- (void)reportError:(CDVInvokedUrlCommand *)command;
- (void)setLocation:(CDVInvokedUrlCommand *)command;
- (void)setLocationTracking:(CDVInvokedUrlCommand *)command;
+ (void)activateWithConfigurationDictionary:(NSDictionary *)configuration;
+ (bool)isAppMetricaActivated;

@end
