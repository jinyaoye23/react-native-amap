package cn.qiuxiang.react.amap3d.location;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class AMapLocationModule extends ReactContextBaseJavaModule implements AMapLocationListener, LifecycleEventListener{
    private static final String MODULE_NAME = "RNLocation";
    private AMapLocationClient mLocationClient;
    private AMapLocationClient multipLocationClient;
    private AMapLocationListener mLocationListener = this;
    private final ReactApplicationContext mReactContext;
    // 是否显示详细信息
    private boolean needDetail = false;
    private boolean multipNeedDetail = false;

    private NotificationManager notificationManager = null;
    boolean isCreateChannel = false;
    private static final String NOTIFICATION_CHANNEL_NAME = "AMapBackgroundLocation";
    private static final int CHANNEL_ID = 200001;

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        if (mReactContext != null) {
            mReactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    public AMapLocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mReactContext = reactContext;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        return constants;
    }

    @ReactMethod
    public void startLocation(@Nullable ReadableMap options) {
        mLocationClient = new AMapLocationClient(mReactContext);
        mLocationClient.setLocationListener(mLocationListener);
        mReactContext.addLifecycleEventListener(this);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        needDetail = true;
        if (options != null) {
            // if (options.hasKey("needDetail")) {
            //     needDetail = options.getBoolean("needDetail");
            // }
            if (options.hasKey("accuracy")) {
                //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
                switch (options.getString("accuracy")) {
                    case "BatterySaving":
                        mLocationOption.setLocationMode(AMapLocationMode.Battery_Saving);
                        break;
                    case "DeviceSensors":
                        mLocationOption.setLocationMode(AMapLocationMode.Device_Sensors);
                        break;
                    case "HighAccuracy":
                        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
                        break;
                    default:
                        break;
                }
            }
            if (options.hasKey("needAddress")) {
                //设置是否返回地址信息（默认返回地址信息）
                needDetail = options.getBoolean("needAddress");
                mLocationOption.setNeedAddress(options.getBoolean("needAddress"));
            }
            if (options.hasKey("onceLocation")) {
                //设置是否只定位一次,默认为false
                mLocationOption.setOnceLocation(options.getBoolean("onceLocation"));
            }
            if (options.hasKey("onceLocationLatest")) {
                //获取最近3s内精度最高的一次定位结果
                mLocationOption.setOnceLocationLatest(options.getBoolean("onceLocationLatest"));
            }
            if (options.hasKey("wifiActiveScan")) {
                //设置是否强制刷新WIFI，默认为强制刷新
                //模式为仅设备模式(Device_Sensors)时无效
                mLocationOption.setWifiActiveScan(options.getBoolean("wifiActiveScan"));
            }
            if (options.hasKey("mockEnable")) {
                //设置是否允许模拟位置,默认为false，不允许模拟位置
                //模式为低功耗模式(Battery_Saving)时无效
                mLocationOption.setMockEnable(options.getBoolean("mockEnable"));
            }
            if (options.hasKey("interval")) {
                //设置定位间隔,单位毫秒,默认为2000ms
                mLocationOption.setInterval(options.getInt("interval"));
            }
            if (options.hasKey("httpTimeOut")) {
                //设置联网超时时间
                //默认值：30000毫秒
                //模式为仅设备模式(Device_Sensors)时无效
                mLocationOption.setHttpTimeOut(options.getInt("httpTimeOut"));
            }
            if (options.hasKey("protocol")) {
                switch (options.getString("protocol")) {
                    case "http":
                        mLocationOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);
                        break;
                    case "https":
                        mLocationOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTPS);
                        break;
                    default:
                        break;
                }
            }
            if (options.hasKey("locationCacheEnable")) {
                mLocationOption.setLocationCacheEnable(options.getBoolean("locationCacheEnable"));
            }
        }
        this.mLocationClient.setLocationOption(mLocationOption);
        this.mLocationClient.startLocation();
    }

    @ReactMethod
    public void stopLocation() {
        if (this.mLocationClient != null) {
            this.mLocationClient.stopLocation();
        }
    }

    @ReactMethod
    public void destroyLocation() {
        if (this.mLocationClient != null) {
            this.mLocationClient.onDestroy();
        }
    }

    @ReactMethod
    public void startContinuousLocation(@Nullable ReadableMap options) {
        multipLocationClient = new AMapLocationClient(mReactContext);
        multipLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    sendEvent("onContinuousLocationChangedEvent", amapLocationToObject(aMapLocation, multipNeedDetail));
                }
            }
        });
        mReactContext.addLifecycleEventListener(this);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        multipNeedDetail = true;
        mLocationOption.setOnceLocation(false);
        if (options != null) {
            if (options.hasKey("accuracy")) {
                //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
                switch (options.getString("accuracy")) {
                    case "BatterySaving":
                        mLocationOption.setLocationMode(AMapLocationMode.Battery_Saving);
                        break;
                    case "DeviceSensors":
                        mLocationOption.setLocationMode(AMapLocationMode.Device_Sensors);
                        break;
                    case "HighAccuracy":
                        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
                        break;
                    default:
                        break;
                }
            }
            if (options.hasKey("needAddress")) {
                //设置是否返回地址信息（默认返回地址信息）
                multipNeedDetail = options.getBoolean("needAddress");
                mLocationOption.setNeedAddress(options.getBoolean("needAddress"));
            }
            if (options.hasKey("wifiActiveScan")) {
                //设置是否强制刷新WIFI，默认为强制刷新
                //模式为仅设备模式(Device_Sensors)时无效
                mLocationOption.setWifiActiveScan(options.getBoolean("wifiActiveScan"));
            }
            if (options.hasKey("mockEnable")) {
                //设置是否允许模拟位置,默认为false，不允许模拟位置
                //模式为低功耗模式(Battery_Saving)时无效
                mLocationOption.setMockEnable(options.getBoolean("mockEnable"));
            }
            if (options.hasKey("interval")) {
                //设置定位间隔,单位毫秒,默认为2000ms
                mLocationOption.setInterval(options.getInt("interval"));
            }
            if (options.hasKey("httpTimeOut")) {
                //设置联网超时时间
                //默认值：30000毫秒
                //模式为仅设备模式(Device_Sensors)时无效
                mLocationOption.setHttpTimeOut(options.getInt("httpTimeOut"));
            }
            // 默认是Https
            mLocationOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTPS);
            if (options.hasKey("protocol")) {
                switch (options.getString("protocol")) {
                    case "http":
                        mLocationOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);
                        break;
                    case "https":
                        mLocationOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTPS);
                        break;
                    default:
                        break;
                }
            }
            if (options.hasKey("locationCacheEnable")) {
                mLocationOption.setLocationCacheEnable(options.getBoolean("locationCacheEnable"));
            }
        }
        this.multipLocationClient.setLocationOption(mLocationOption);
        this.multipLocationClient.startLocation();
    }

    @ReactMethod
    public void stopContinuousLocation() {
        if (this.multipLocationClient != null) {
            if (Build.VERSION.SDK_INT >= 26) {
                this.multipLocationClient.disableBackgroundLocation(true);
            }
            this.multipLocationClient.stopLocation();
        }
    }
    @ReactMethod
    public void destroyContinuousLocation() {
        if (this.multipLocationClient != null) {
            this.multipLocationClient.onDestroy();
        }
    }
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            sendEvent("onLocationChangedEvent", amapLocationToObject(amapLocation, needDetail));
        }
    }

    private WritableMap amapLocationToObject(AMapLocation amapLocation, boolean needDetail) {
        WritableMap map = Arguments.createMap();
        Integer errorCode = amapLocation.getErrorCode();
        if (errorCode > 0) {
            map.putInt("errorCode", errorCode);
            map.putString("errorInfo", amapLocation.getErrorInfo());
        } else {
            Double latitude = amapLocation.getLatitude();
            Double longitude = amapLocation.getLongitude();
            map.putInt("locationType", amapLocation.getLocationType());
            map.putDouble("latitude", latitude);
            map.putDouble("longitude", longitude);
            if (needDetail) {
                // GPS Only
                map.putDouble("accuracy", amapLocation.getAccuracy());
                map.putDouble("altitude", amapLocation.getAltitude());
                map.putDouble("speed", amapLocation.getSpeed());
                map.putDouble("bearing", amapLocation.getBearing());
                map.putString("address", amapLocation.getAddress());
                map.putString("adCode", amapLocation.getAdCode());
                map.putString("country", amapLocation.getCountry());
                map.putString("province", amapLocation.getProvince());
                map.putString("poiName", amapLocation.getPoiName());
                map.putString("aoiName", amapLocation.getAoiName());
                map.putString("street", amapLocation.getStreet());
                map.putString("streetNum", amapLocation.getStreetNum());
                map.putString("city", amapLocation.getCity());
                map.putString("cityCode", amapLocation.getCityCode());
                map.putString("district", amapLocation.getDistrict());
                map.putInt("gpsStatus", amapLocation.getGpsAccuracyStatus());
                map.putString("locationDetail", amapLocation.getLocationDetail());
            }
        }
        return map;
    }

    @Override
    public void onHostResume() {

        if (Build.VERSION.SDK_INT >= 26) {
            multipLocationClient.disableBackgroundLocation(true);
        }
    }

    @Override
    public void onHostPause() {

        if (Build.VERSION.SDK_INT >= 26) {
            if (multipLocationClient != null && multipLocationClient.isStarted()) {
                // 如果已经开启了定位，并且进入后台，则添加提示
                multipLocationClient.enableBackgroundLocation(CHANNEL_ID, buildNotification());
            }
        }
    }

    @Override
    public void onHostDestroy() {
        if (this.mLocationClient != null) {
            this.mLocationClient.onDestroy();
        }
        if (this.multipLocationClient != null) {
            this.multipLocationClient.onDestroy();
        }
    }


    /**
     * 构建通知
     *
     * @return
     */
    @SuppressLint("NewApi")
    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;

        if (Build.VERSION.SDK_INT >= 26) {
            if (null == notificationManager) {
                notificationManager = (NotificationManager)  mReactContext.getSystemService(Context.NOTIFICATION_SERVICE);
            }

            String channelId = mReactContext.getPackageName();

            if (!isCreateChannel) {

                NotificationChannel notificationChannel = new NotificationChannel(channelId, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true); // //是否在桌面icon右上角展示小圆点
                notificationChannel.enableVibration(false);
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setVibrationPattern(new long[]{0, 0, 0, 0,0, 0, 0, 0, 0});
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(mReactContext, channelId);
        } else {
            builder =  new Notification.Builder(mReactContext);
        }

        try {
            builder.setSmallIcon(mReactContext.getPackageManager().getApplicationInfo(mReactContext.getPackageName(), PackageManager.GET_META_DATA).icon);
        } catch (Exception e) {

        }

        builder.setContentTitle(mReactContext.getApplicationInfo().loadLabel(mReactContext.getPackageManager()).toString())
                .setContentText("正在后台运行")
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;

    }
}
