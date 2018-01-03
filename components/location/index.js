'use strict';

var ReactNative = require('react-native');

var {
    NativeModules,
    DeviceEventEmitter,
    Platform,
} = ReactNative;

const RNLocation = NativeModules.RNLocation;
const onLocationChanged = 'onLocationChangedEvent';
const onContinuousLocationChangedEvent = 'onContinuousLocationChangedEvent';
// Android default options
// {
//     accuracy: 'HighAccuracy', // BatterySaving(低功耗定位模式), DeviceSensors(仅设备定位模式), HighAccuracy(高精度模式)
//     needAddress: true, // 设置是否返回地址信息
//     onceLocation: false, // 是否只定位一次
//     onceLocationLatest: false,//获取最近3s内精度最高的一次定位结果
//     wifiActiveScan: true, // 设置是否强制刷新WIFI，默认为强制刷新,模式为仅设备模式(Device_Sensors)时无效
//     mockEnable: false, // 设置是否允许模拟位置,默认为false，不允许模拟位置,模式为低功耗模式(Battery_Saving)时无效
//     interval: 2000, // 设置定位间隔,单位毫秒,默认为2000ms
//     httpTimeOut: 30000, // 设置联网超时时间(ms), 模式为仅设备模式(Device_Sensors)时无效,默认30000毫秒，建议超时时间不要低于8000毫秒,
//     protocol:'http', //用于设定网络定位时所采用的协议，提供http/https两种协议,默认值http
//     locationCacheEnable: false //true表示使用定位缓存策略；false表示不使用。默认是false
//   }

// iOS default options
// {
//     accuracy: 'kCLLocationAccuracyHundredMeters', // kCLLocationAccuracyHundredMeters, kCLLocationAccuracyBest, kCLLocationAccuracyNearestTenMeters,kCLLocationAccuracyKilometer,kCLLocationAccuracyThreeKilometers
//     onceLocation: false, // 是否只定位一次,
//     pausesLocationUpdatesAutomatically: true,//指定定位是否会被系统自动暂停。默认为YES
//     allowsBackgroundLocationUpdates: false,//是否允许后台定位。默认为NO。只在iOS 9.0及之后起作用。设置为YES的时候必须保证 Background Modes 中的 Location updates 处于选中状态，否则会抛出异常
//     locationTimeout: 10,//指定单次定位超时时间,默认为10s。最小值是2s。注意单次定位请求前设置
//     reGeocodeTimeout: 5,//指定单次定位逆地理超时时间,默认为5s。最小值是2s。注意单次定位请求前设置
//     locatingWithReGeocode: false,//连续定位是否返回逆地理信息，默认NO
//     distanceFilter: 'kCLDistanceFilterNone'//设定定位的最小更新距离。默认为 kCLDistanceFilterNone 
//   }
module.exports = {
    onContinuousLocationChangedEvent,
    onLocationChanged,
    startLocation: function (options) {
        RNLocation.startLocation(options);
    },
    stopLocation: function () {
        RNLocation.stopLocation();
    },
    destroyLocation: function () {
        RNLocation.destroyLocation();
    },
    /**
     * 开启持续定位
     * TODO: iOS 
     */
    startContinuousLocation: function (options) {
        RNLocation.startContinuousLocation(options);
        // if (Platform.OS == 'ios') {
        //     this.startLocation(options);
        // } else {
        //     RNLocation.startContinuousLocation(options);
        // }
    },
    /**
     * 停止持续定位
     * TODO: iOS 
     */
    stopContinuousLocation: function () {
        RNLocation.stopContinuousLocation();
        // if (Platform.OS == 'ios') {
        //     this.stopLocation();
        // } else {
        //     RNLocation.stopContinuousLocation();
        // }
    },
    /**
     * 销毁定位
     * TODO: iOS
     */
    destroyContinuousLocation: function () {
        RNLocation.destroyContinuousLocation();
        // if (Platform.OS == 'ios') {
        //     this.stopLocation();
        // } else {
        //     RNLocation.destroyContinuousLocation();
        // }
    },
    addEventListener: function (handler, eventName = onLocationChanged) {
        const listener = DeviceEventEmitter.addListener(
            eventName,
            handler
        );
        return listener;
    },
};
