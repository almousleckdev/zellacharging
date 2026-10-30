import 'dart:io';

import 'package:dio/dio.dart';

import '../l10n/app_language.dart';

class DeviceRegistry {
  DeviceRegistry(this._dio);

  final Dio _dio;

  Map<String, String> _body(String token) => {
    'token': token,
    'platform': Platform.isIOS ? 'IOS' : 'ANDROID',
    'language': AppLanguage.current.code,
  };

  Future<void> register(String token) =>
      _dio.put<void>('/v1/devices', data: _body(token));

  Future<void> unregister(String token) =>
      _dio.delete<void>('/v1/devices', data: _body(token));
}
