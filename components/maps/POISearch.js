import { NativeModules, Platform } from 'react-native';

const isIOS = Platform.OS == 'ios';
const RNPOISearch = NativeModules.POISearch;
export default class POISearch {

    // 搜索附近
    static searchPoiByCenterCoordinate(options) {
        // 
        return new Promise((resolve, reject) => {
            RNPOISearch.searchPoiByCenterCoordinate(options).then(result => {
                // console.log(result);
                if (isIOS) {
                    resolve(result.resultList);
                } else {
                    resolve(result);
                }

            }).catch(error => {
                reject(error);
            })
        })
    }

    /**
     * @description  根据关键字搜索
     * @static  
     * @param {any} options 
     * {
     *   keyword: '',
     *   types: '',
     *   city: '',
     *   cityLimit: '',
     *   requireExtension
     * }
     * @returns 
     * @memberof POISearch
     */
    static searchPoiByKeyword(options) {
        if (!isIOS) {
            // Android端采用 searchPoiByCenterCoordinate方法
            return this.searchPoiByCenterCoordinate(options);
        }
        // iOS 采用 searchPoiByKeyword 方法
        return new Promise((resolve, reject) => {
            RNPOISearch.searchPoiByKeyword(options).then(result => {
                if (isIOS) {
                    resolve(result.resultList);
                } else {
                    resolve(result);
                }
            }).catch(error => {
                reject(error);
            })
        })
    }
}