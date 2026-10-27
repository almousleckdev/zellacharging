import 'package:dio/dio.dart';

import '../l10n/app_language.dart';

class LanguageInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    options.headers['Accept-Language'] = AppLanguage.current.code;
    handler.next(options);
  }
}
