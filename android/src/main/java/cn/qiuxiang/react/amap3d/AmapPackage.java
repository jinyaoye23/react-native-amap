package cn.qiuxiang.react.amap3d;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.List;

import cn.qiuxiang.react.amap3d.location.AMapLocationModule;
import cn.qiuxiang.react.amap3d.maps.AMapCircleManager;
import cn.qiuxiang.react.amap3d.maps.AMapHeatMapManager;
import cn.qiuxiang.react.amap3d.maps.AMapInfoWindowManager;
import cn.qiuxiang.react.amap3d.maps.AMapMarkerManager;
import cn.qiuxiang.react.amap3d.maps.AMapMultiPointManager;
import cn.qiuxiang.react.amap3d.maps.AMapPolygonManager;
import cn.qiuxiang.react.amap3d.maps.AMapPolylineManager;
import cn.qiuxiang.react.amap3d.maps.AMapViewManager;
import cn.qiuxiang.react.amap3d.navigation.AMapDriveManager;
import cn.qiuxiang.react.amap3d.navigation.AMapRideManager;
import cn.qiuxiang.react.amap3d.navigation.AMapWalkManager;

/**
 * Created by Jason on 2017/11/13.
 */

public class AmapPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new AMapUtilsModule(reactContext));
        modules.add(new AMapLocationModule(reactContext));
        modules.add(new PIOSearchModule(reactContext));

        return modules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {

        List<ViewManager> viewManagers = new ArrayList<>();
        viewManagers.add(new AMapViewManager());
        viewManagers.add(new AMapMarkerManager());
        viewManagers.add(new AMapInfoWindowManager());
        viewManagers.add(new AMapCircleManager());
        viewManagers.add(new AMapMultiPointManager());
        viewManagers.add(new AMapPolygonManager());
        viewManagers.add(new AMapPolylineManager());
        viewManagers.add(new AMapHeatMapManager());
        viewManagers.add(new AMapDriveManager());
        viewManagers.add(new AMapWalkManager());
        viewManagers.add(new AMapRideManager());
//        AMapViewManager(),
//                AMapMarkerManager(),
//                AMapInfoWindowManager(),
//                AMapPolylineManager(),
//                AMapPolygonManager(),
//                AMapCircleManager(),
//                AMapHeatMapManager(),
//                AMapMultiPointManager(),
//                AMapDriveManager(),
//                AMapWalkManager(),
//                AMapRideManager()
        return viewManagers;
    }
}
