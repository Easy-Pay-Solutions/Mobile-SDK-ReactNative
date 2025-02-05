//
//  EasyPayModule.swift
//  
//
//  
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(EasyPayModule, NSObject)

RCT_EXTERN_METHOD(configureSecrets:(NSString *) apiKey
                  hmacSecret:(NSString *) hmacSecret
                  sentryKey:(nullable NSString *) sentryKey
                  isProduction:(BOOL) isProduction);

RCT_EXTERN_METHOD(getCertificateStatus:(RCTResponseSenderBlock)callback);

RCT_EXTERN_METHOD(loadCertificate);

RCT_EXTERN_METHOD(manageAndSelect:     (NSDictionary *) config
                  user:                (NSDictionary *) user
                  payment:             (NSDictionary *) payment
                  address:             (NSDictionary *) address
                  resolve:             (RCTPromiseResolveBlock) resolve
                  reject:              (RCTPromiseRejectBlock) reject);

RCT_EXTERN_METHOD(pay:                 (NSDictionary *) config
                  user:                (NSDictionary *) user
                  payment:             (NSDictionary *) payment
                  address:             (NSDictionary *) address
                  resolve:             (RCTPromiseResolveBlock) resolve
                  reject:              (RCTPromiseRejectBlock) reject);

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

@end
