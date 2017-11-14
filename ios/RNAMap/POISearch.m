//
//  POISearch.m
//  RNAMap
//
//  Created by Jason on 2017/11/9.
//  Copyright © 2017年 Jason. All rights reserved.
//

#import "POISearch.h"
#import <AMapFoundationKit/AMapFoundationKit.h>

#import <AMapSearchKit/AMapSearchKit.h>

@interface POISearch()<AMapSearchDelegate>

@property (nonatomic, strong) AMapSearchAPI *search;
@property (nonatomic, strong) RCTPromiseResolveBlock resolve;
@property (nonatomic, strong) RCTPromiseRejectBlock reject;

@end

@implementation POISearch

//+ (NSString *)moduleName {
//    return @"POISearch";
//}

RCT_EXPORT_MODULE(POISearch)

//#pragma mark - Lifecycle
- (void) dealloc {
//    self.search = [[AMapSearchAPI alloc] init];
    self.resolve = nil;
    self.reject = nil;
    self.search = nil;
//    self.search.delegate = self;
}

RCT_EXPORT_METHOD(searchPoiByCenterCoordinate: (NSDictionary *) params
                  resolve: (RCTPromiseResolveBlock)resolve
                  reject: (RCTPromiseRejectBlock) reject)
{
    if (self.search == nil) {
        self.search = [[AMapSearchAPI alloc] init];
        self.search.delegate = self;
    }
    self.resolve = resolve;
    self.reject = reject;
    AMapPOIAroundSearchRequest * request = [[AMapPOIAroundSearchRequest alloc] init];
    if (params != nil) {
        NSArray *keys = [params allKeys];
        if ([keys containsObject:@"types"]) {
            // 类型
            NSString *types = [params objectForKey:@"types"];
            request.types = types;
        }
        if([keys containsObject:@"sortrule"]) {
            // 排序规则
            int sortrule = [[params objectForKey:@"sortrule"] intValue];
            request.sortrule = sortrule;
        }
        if([keys containsObject:@"offset"]) {
            // 偏移量
            int offset = [[params objectForKey:@"offset"] intValue];
            request.offset = offset;
        }
        if([keys containsObject:@"page"]) {
            // 页码
            int page = [[params objectForKey:@"page"] intValue];
            request.page = page;
        }
        if([keys containsObject:@"requireExtension"]) {
            
            BOOL requireExtension = [[params objectForKey:@"requireExtension"] boolValue];
            request.requireExtension = requireExtension;
        }
        if([keys containsObject:@"requireSubPOIs"]) {
            BOOL requireSubPOIs = [[params objectForKey:@"requireSubPOIs"] boolValue];
            request.requireSubPOIs = requireSubPOIs;
        }
        
        if([keys containsObject:@"keywords"]) {
            NSString *keywords = [params objectForKey:@"keywords"];
            request.keywords = keywords;
        }
        if([keys containsObject:@"coordinate"]) {
            NSDictionary *coordinate = [params objectForKey:@"coordinate"];
            double latitude = [[coordinate objectForKey:@"latitude"] doubleValue];
            double longitude = [[coordinate objectForKey:@"longitude"] doubleValue];
            request.location = [AMapGeoPoint locationWithLatitude:latitude longitude:longitude];
        }
        if([keys containsObject:@"radius"]) {
            // 半径
            int *radius = [[params objectForKey:@"radius"] intValue];
            request.radius = radius;
        }
        // 发起搜索
        [self.search AMapPOIAroundSearch:request];
    }
}

#pragma mark - AMapSearchDelegate
/* 搜索失败 */
-(void)AMapSearchRequest:(id)request didFailWithError:(NSError *)error
{
    NSDictionary *result;
    NSString *errorCodeStr = [NSString stringWithFormat:@"%ld", (long)error.code];
    result = @{
               @"code": @(error.code),
               @"message": error.localizedDescription
               };
    
    if (self.reject != nil) {
        self.reject(errorCodeStr, error.localizedDescription, error);
    }
}
/* 搜索成功 */
-(void) onPOISearchDone:(AMapPOISearchBaseRequest *)request response:(AMapPOISearchResponse *)response
{
    NSDictionary *result;
    NSMutableArray *resultList;
    resultList = [NSMutableArray arrayWithCapacity:response.pois.count];
    
    if (response.pois.count > 0)
    {
        [response.pois enumerateObjectsUsingBlock:^(AMapPOI * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            [resultList addObject:@{
                                    @"uid": obj.uid,
                                    @"name": obj.name,
                                    @"type": obj.type,
                                    @"typecode": obj.typecode,
                                    @"latitude": @(obj.location.latitude),
                                    @"longitude": @(obj.location.longitude),
                                    @"address": obj.address,
                                    @"tel": obj.tel,
                                    @"distance": @(obj.distance)
                                    }];
        }];
        
        result = @{
                   @"resultList": resultList,
                   @"status": @"OK"
                   };
        if (self.resolve != nil) {
            self.resolve(result);
        }
    }
    
}


@end
