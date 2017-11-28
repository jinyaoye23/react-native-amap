package cn.qiuxiang.react.amap3d;

import android.support.annotation.Nullable;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2017/11/13.
 */

public class PIOSearchModule extends ReactContextBaseJavaModule implements PoiSearch.OnPoiSearchListener{
//    searchPoiByCenterCoordinate
    private Promise mPromise;
    private final ReactApplicationContext mReactContext;

    public PIOSearchModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mReactContext = reactContext;
    }

    @Override
    public String getName() {
        return "POISearch";
    }


    @ReactMethod
    public  void searchPoiByCenterCoordinate(@Nullable ReadableMap options, Promise promise) {
        mPromise = promise;

        if (options == null) {
            promise.resolve(null);
            return;
        }

        String types = "";
        String keywords = "";
        if (options.hasKey("types")) {
            types = options.getString("types");
        }
        if (options.hasKey("keyword")) {
            keywords = options.getString("keyword");
        }

        PoiSearch.Query query = new PoiSearch.Query(keywords, types, "");
        query.setPageSize(options.getInt("offset"));// 设置每页最多返回多少条poiitem
        query.setPageNum(0);//设置查询页码
        PoiSearch poiSearch = new PoiSearch(mReactContext, query);
        poiSearch.setOnPoiSearchListener(this);
        if (options.hasKey("radius")) {
            ReadableMap latlng = options.getMap("coordinate");

            PoiSearch.SearchBound bound = new PoiSearch.SearchBound(new LatLonPoint(
               latlng.getDouble("latitude"), latlng.getDouble("longitude")), options.getInt("radius"));
            poiSearch.setBound(bound);
        }
        poiSearch.searchPOIAsyn();

    }


    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        if (i == 1000) {
            ArrayList<PoiItem> list = poiResult.getPois();
            WritableArray result = Arguments.createArray();
            for(PoiItem poiItem : list) {
                result.pushMap(toMap(poiItem));
            }
            mPromise.resolve(result);
        } else {
            mPromise.reject(String.valueOf(i), "查询失败");
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {


    }

    public WritableMap toMap(PoiItem poiItem) {
        WritableMap retMap = Arguments.createMap();

        retMap.putString("uid", poiItem.getPoiId());
        retMap.putString("name", poiItem.toString());
        retMap.putString("type", poiItem.getTypeDes());
        retMap.putString("typecode", poiItem.getTypeCode());
        LatLonPoint latlng = poiItem.getLatLonPoint();
        retMap.putDouble("latitude", latlng.getLatitude());
        retMap.putDouble("longitude", latlng.getLongitude());
        retMap.putString("address", poiItem.getSnippet());
        retMap.putInt("distance", poiItem.getDistance());
        retMap.putString("tel", poiItem.getTel());
        return retMap;
    }
}
