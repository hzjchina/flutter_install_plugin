
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterInstallPlugin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_install_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> installApk(String filePath, String appId) async {
    Map<String, String> params = {'filePath': filePath, 'appId': appId};
    return await _channel.invokeMethod('installApk', params);
  }

  /// for iOS: go to app store by the url
  static Future<String> gotoAppStore(String urlString) async {
    Map<String, String> params = {'urlString': urlString};
    return await _channel.invokeMethod('gotoAppStore', params);
  }

  /// for android: go to market by market package name
  static Future<String> gotoAndroidMarket(String marketPackageName) async {
    Map<String, String> params = {'marketPackageName': marketPackageName};
    return await _channel.invokeMethod('gotoAndroidMarket', params);
  }
}
