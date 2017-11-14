import { NativeModules, Platform } from 'react-native';

const isIOS = Platform.OS == 'ios';
const RNPOISearch = NativeModules.POISearch;
export default class POISearch {

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
}