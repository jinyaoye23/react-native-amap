//
//  RNLocation.m
//  RNLocation
//
//  Created by leo on 2016/11/16.
//  Copyright © 2016年 leo. All rights reserved.
//

#import "RNLocation.h"

#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>

#import <AMapFoundationKit/AMapFoundationKit.h>
#import <AMapLocationKit/AMapLocationKit.h>

static NSInteger const kDefaultLocationTimeout  = 10;
static NSInteger const kDefaultReGeocodeTimeout = 5;

static NSString * const LocationChangeEvent = @"onLocationChangedEvent";
static NSString * const ContinuousLocationChangeEvent = @"onContinuousLocationChangedEvent";

static NSString * const kErrorCodeKey = @"errorCode";
static NSString * const kErrorInfoKey = @"errorInfo";

@interface RNLocation () <AMapLocationManagerDelegate>

@property (nonatomic, strong) AMapLocationManager         *locationManager;
@property (nonatomic, copy  ) AMapLocatingCompletionBlock completionBlock;
// 持续定位对象
@property (nonatomic, strong) AMapLocationManager *continuousLocationManager;
@property (nonatomic) BOOL isContinuous;

@end

@implementation RNLocation

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE(RNLocation);

#pragma mark - Lifecycle
- (void)dealloc {
    self.locationManager = nil;
    self.completionBlock = nil;
    self.continuousLocationManager = nil;
    self.isContinuous = NO;
}

RCT_EXPORT_METHOD(startContinuousLocation: (NSDictionary *)options) {
    // set default value
    CLLocationAccuracy accuracy             = kCLLocationAccuracyHundredMeters;
    CLLocationDistance distanceFilter       = kCLDistanceFilterNone;
    BOOL allowsBackgroundLocationUpdates    = YES;
    BOOL locatingWithReGeocode              = YES;
    BOOL pausesLocationUpdatesAutomatically = NO;
    NSInteger locationTimeout               = kDefaultLocationTimeout;
    NSInteger reGeocodeTimeout              = kDefaultReGeocodeTimeout;
    
    if(options &&
       [options isKindOfClass:[NSDictionary class]]) {
        
        /**
         *  accuracy
         *  精度值设定，值类型若为字符串，则使用预设值
         *  若为数值，则使用具体数值，其他使用默认值
         */
        id accuracyValue = options[@"accuracy"];
        if ([accuracyValue isKindOfClass:[NSString class]]) {
            if ([accuracyValue isEqualToString:@"kCLLocationAccuracyBest"]) {
                accuracy = kCLLocationAccuracyBest;
            } else if ([accuracyValue isEqualToString:@"kCLLocationAccuracyNearestTenMeters"]) {
                accuracy = kCLLocationAccuracyNearestTenMeters;
            } else if ([accuracyValue isEqualToString:@"kCLLocationAccuracyHundredMeters"]) {
                accuracy = kCLLocationAccuracyHundredMeters;
            } else if ([accuracyValue isEqualToString:@"kCLLocationAccuracyKilometer"]) {
                accuracy = kCLLocationAccuracyKilometer;
            } else if ([accuracyValue isEqualToString:@"kCLLocationAccuracyThreeKilometers"]) {
                accuracy = kCLLocationAccuracyThreeKilometers;
            }
        } else if ([accuracyValue isKindOfClass:[NSNumber class]]) {
            accuracy = [accuracyValue doubleValue];
        }
        
        /**
         *  distanceFilter
         *  对于非数字，采用默认值 kCLDistanceFilterNone
         */
        id distanceFilterValue = options[@"distanceFilter"];
        if ([distanceFilterValue isKindOfClass:[NSNumber class]]) {
            distanceFilter = [distanceFilterValue doubleValue];
        }
        
        /**
         *  allowsBackgroundLocationUpdates
         *  针对 iOS 9.0+，允许后台位置更新，注意对应 background mode 配置
         */
        allowsBackgroundLocationUpdates = [options[@"allowsBackgroundLocationUpdates"] boolValue];
        
        /**
         *  locatingWithReGeocode
         *  是否逆地理位置编码
         */
        locatingWithReGeocode = [options[@"locatingWithReGeocode"] boolValue];
        
        
        /**
         *  pausesLocationUpdatesAutomatically
         *  是否自动停止更新
         */
        pausesLocationUpdatesAutomatically = [options[@"pausesLocationUpdatesAutomatically"] boolValue];
        
        /**
         *  locationTimeout
         *  是否逆地理位置定位超时
         */
        locationTimeout = [options[@"locationTimeout"] intValue];
        
        /**
         *  reGeocodeTimeout
         *  逆地址解析超时
         */
        reGeocodeTimeout = [options[@"reGeocodeTimeout"] intValue];
    }
    
    /**
     *  更新定位管理器设置
     */
    [self.continuousLocationManager setDesiredAccuracy:accuracy];
    [self.continuousLocationManager setDistanceFilter:distanceFilter];
    
    
    [self.continuousLocationManager setAllowsBackgroundLocationUpdates:allowsBackgroundLocationUpdates];
    [self.continuousLocationManager setLocatingWithReGeocode:YES];
    [self.continuousLocationManager setPausesLocationUpdatesAutomatically:pausesLocationUpdatesAutomatically];
    
    [self.continuousLocationManager setLocationTimeout:locationTimeout];
    [self.continuousLocationManager setReGeocodeTimeout:reGeocodeTimeout];
    
    // 先停止定位，在开始
    [self.continuousLocationManager stopUpdatingLocation];
    
    [self.continuousLocationManager startUpdatingLocation];
    
    self.isContinuous = YES;
    
}
/** 停止持续定位 */
RCT_EXPORT_METHOD(stopContinuousLocation) {
     self.isContinuous = NO;
    [self.continuousLocationManager stopUpdatingLocation];
}
/** 销毁持续定位 */
RCT_EXPORT_METHOD(destroyContinuousLocation) {
    self.isContinuous = NO;
    [self.continuousLocationManager stopUpdatingLocation];
    self.continuousLocationManager = nil;
}

RCT_EXPORT_METHOD(startLocation:(NSDictionary *)options) {
    // set default value
    CLLocationAccuracy accuracy             = kCLLocationAccuracyHundredMeters;
    CLLocationDistance distanceFilter       = kCLDistanceFilterNone;
    BOOL allowsBackgroundLocationUpdates    = YES;
    BOOL locatingWithReGeocode              = YES;
    BOOL onceLocation                       = NO;
    BOOL pausesLocationUpdatesAutomatically = NO;
    NSInteger locationTimeout               = kDefaultLocationTimeout;
    NSInteger reGeocodeTimeout              = kDefaultReGeocodeTimeout;
    
    if(options &&
       [options isKindOfClass:[NSDictionary class]]) {
        
        /**
         *  accuracy
         *  精度值设定，值类型若为字符串，则使用预设值
         *  若为数值，则使用具体数值，其他使用默认值
         */
        id accuracyValue = options[@"accuracy"];
        if ([accuracyValue isKindOfClass:[NSString class]]) {
            if ([accuracyValue isEqualToString:@"kCLLocationAccuracyBest"]) {
                accuracy = kCLLocationAccuracyBest;
            } else if ([accuracyValue isEqualToString:@"kCLLocationAccuracyNearestTenMeters"]) {
                accuracy = kCLLocationAccuracyNearestTenMeters;
            } else if ([accuracyValue isEqualToString:@"kCLLocationAccuracyHundredMeters"]) {
                accuracy = kCLLocationAccuracyHundredMeters;
            } else if ([accuracyValue isEqualToString:@"kCLLocationAccuracyKilometer"]) {
                accuracy = kCLLocationAccuracyKilometer;
            } else if ([accuracyValue isEqualToString:@"kCLLocationAccuracyThreeKilometers"]) {
                accuracy = kCLLocationAccuracyThreeKilometers;
            }
        } else if ([accuracyValue isKindOfClass:[NSNumber class]]) {
            accuracy = [accuracyValue doubleValue];
        }
        
        /**
         *  distanceFilter
         *  对于非数字，采用默认值 kCLDistanceFilterNone
         */
        id distanceFilterValue = options[@"distanceFilter"];
        if ([distanceFilterValue isKindOfClass:[NSNumber class]]) {
            distanceFilter = [distanceFilterValue doubleValue];
        }
        
        /**
         *  allowsBackgroundLocationUpdates
         *  针对 iOS 9.0+，允许后台位置更新，注意对应 background mode 配置
         */
        allowsBackgroundLocationUpdates = [options[@"allowsBackgroundLocationUpdates"] boolValue];
        
        /**
         *  locatingWithReGeocode
         *  是否逆地理位置编码
         */
        locatingWithReGeocode = [options[@"locatingWithReGeocode"] boolValue];
        
        /**
         *  locatingWithReGeocode
         *  是否逆地理位置编码
         */
        onceLocation = [options[@"onceLocation"] boolValue];
        
        /**
         *  locatingWithReGeocode
         *  是否逆地理位置编码
         */
        pausesLocationUpdatesAutomatically = [options[@"pausesLocationUpdatesAutomatically"] boolValue];
        
        /**
         *  locatingWithReGeocode
         *  是否逆地理位置编码
         */
        locationTimeout = [options[@"locationTimeout"] intValue];
        
        /**
         *  locatingWithReGeocode
         *  是否逆地理位置编码
         */
        reGeocodeTimeout = [options[@"reGeocodeTimeout"] intValue];
    }
    
    /**
     *  更新定位管理器设置
     */
    [self.locationManager setDesiredAccuracy:accuracy];
    [self.locationManager setDistanceFilter:distanceFilter];
    
    [self.locationManager setAllowsBackgroundLocationUpdates:allowsBackgroundLocationUpdates];
    [self.locationManager setLocatingWithReGeocode:YES];
    [self.locationManager setPausesLocationUpdatesAutomatically:pausesLocationUpdatesAutomatically];
    
    [self.locationManager setLocationTimeout:locationTimeout];
    [self.locationManager setReGeocodeTimeout:reGeocodeTimeout];
    
    /**
     *  是否单次定位，若多次调用当前方法
     *  且 onceLocation 更新为 true 时，若当前开启持续定位，则停止
     */
    if (onceLocation) {
        [self.locationManager stopUpdatingLocation];
        [self.locationManager requestLocationWithReGeocode:locatingWithReGeocode
                                           completionBlock:self.completionBlock];
    } else {
        [self.locationManager startUpdatingLocation];
    }
}

RCT_EXPORT_METHOD(stopLocation) {
    [self.locationManager stopUpdatingLocation];
}

RCT_EXPORT_METHOD(destroyLocation) {
    [self.locationManager stopUpdatingLocation];
    self.locationManager = nil;
}

- (NSDictionary *)constantsToExport {
    return nil;
}

#pragma mark - Setter & Getter
- (AMapLocationManager *)locationManager {
    if (!_locationManager) {
        _locationManager = [[AMapLocationManager alloc] init];
        _locationManager.delegate = self;
    }
    return _locationManager;
}
-(AMapLocationManager *) continuousLocationManager {
    if (!_continuousLocationManager) {
        _continuousLocationManager = [[AMapLocationManager alloc] init ];
        _continuousLocationManager.delegate = self;
    }
    return _continuousLocationManager;
}
-(BOOL) isContinuous {
    if (!_isContinuous) {
        _isContinuous = FALSE;
    }
    return _isContinuous;
}

- (AMapLocatingCompletionBlock)completionBlock {
    if (!_completionBlock) {
        _completionBlock = ^(CLLocation *location, AMapLocationReGeocode *regeocode, NSError *error) {
            NSMutableDictionary *resultDic = [NSMutableDictionary dictionary];
            if (error) {
                resultDic[kErrorCodeKey] = @(error.code);
                resultDic[kErrorInfoKey] = error.localizedDescription;
            } else {
                if (location) {
                    resultDic[@"horizontalAccuracy"] = @(location.horizontalAccuracy);
                    resultDic[@"verticalAccuracy"]   = @(location.verticalAccuracy);
                    resultDic[@"latitude"]           = @(location.coordinate.latitude);
                    resultDic[@"longitude"]          = @(location.coordinate.longitude);
                    resultDic[@"altitude"]           = @(location.altitude);
                    
                    if (regeocode) {
                        resultDic[@"address"]   = regeocode.formattedAddress ? : [NSNull null];
                        resultDic[@"country"]   = regeocode.country ? : [NSNull null];
                        resultDic[@"province"]  = regeocode.province ? : [NSNull null];
                        resultDic[@"city"]      = regeocode.city ? : [NSNull null];
                        resultDic[@"district"]  = regeocode.district ? : [NSNull null];
                        resultDic[@"cityCode"]  = regeocode.citycode ? : [NSNull null];
                        resultDic[@"adCode"]    = regeocode.adcode ? : [NSNull null];
                        resultDic[@"street"]    = regeocode.street ? : [NSNull null];
                        resultDic[@"number"]    = regeocode.number ? : [NSNull null];
                        resultDic[@"poiName"]   = regeocode.POIName ? : [NSNull null];
                        resultDic[@"aoiName"]   = regeocode.AOIName ? : [NSNull null];
                    }
                } else {
                    resultDic[kErrorCodeKey] = @(-1);
                    resultDic[kErrorInfoKey] = @"定位结果不存在";
                }
            }
            [self.bridge.eventDispatcher sendAppEventWithName:LocationChangeEvent
                                                         body:resultDic];
            if (self.isContinuous) {
                //  如果是持续定位，则发送持续定位事件
                [self.bridge.eventDispatcher sendAppEventWithName:ContinuousLocationChangeEvent
                                                             body:resultDic];
            }
        };
    }
    return _completionBlock;
}

#pragma mark - AMapLocationManagerDelegate
- (void)amapLocationManager:(AMapLocationManager *)manager didFailWithError:(NSError *)error {
    if (self.completionBlock) {
        self.completionBlock(nil, nil, error);
    }
}

- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateLocation:(CLLocation *)location {
    if (self.completionBlock) {
        self.completionBlock(location, nil, nil);
    }
}

- (void)amapLocationManager:(AMapLocationManager *)manager
          didUpdateLocation:(CLLocation *)location reGeocode:(AMapLocationReGeocode *)reGeocode {
    if (self.completionBlock) {
        self.completionBlock(location, reGeocode, nil);
    }
}

@end
