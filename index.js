import MapView from './components/maps/MapView'
import Marker from './components/maps/Marker'
import Polyline from './components/maps/Polyline'
import Polygon from './components/maps/Polygon'
import Circle from './components/maps/Circle'
import MultiPoint from './components/maps/MultiPoint'
import Navigation from './components/navigation'
import MapUtils from './components/Utils'
import RNLocation from "./components/location/index";
import POISearch from './components/maps/POISearch';
MapView.Marker = Marker
MapView.Polyline = Polyline
MapView.Polygon = Polygon
MapView.Circle = Circle
MapView.MultiPoint = MultiPoint

export default MapView
export {
  MapView,
  Marker,
  Polyline,
  Polygon,
  Circle,
  MultiPoint,
  Navigation,
  MapUtils,
  RNLocation,
  POISearch
}
