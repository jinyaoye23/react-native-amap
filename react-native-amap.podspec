require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']
  s.license      = package['license']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platform     = :ios, "9.0"

  s.source       = { :git => "https://github.com/fenglu09/react-native-amap.git", :tag => "#{s.version}" }
  s.source_files  = "ios/**/*.{h,m}"

  s.dependency 'React'
  s.dependency 'AMapSearch-NO-IDFA', '6.1.1'  #地图SDK搜索功能
  s.dependency 'AMapLocation-NO-IDFA', '2.6.0'  #定位SDK
  s.dependency 'AMapNavi-NO-IDFA', '6.2.0'  #导航SDK
end