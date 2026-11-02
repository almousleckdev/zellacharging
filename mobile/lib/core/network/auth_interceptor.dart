import 'package:dio/dio.dart';

import '../storage/token_store.dart';
import 'refresh_outcome.dart';
import 'token_refresher.dart';

class AuthInterceptor extends Interceptor {
  AuthInterceptor({
    required this.store,
    required this.refresher,
    required this.client,
    required this.onSessionExpired,
  });

  static const _retriedKey = 'retried';
  static const _replacedCode = 'SESSION_REPLACED';

  final TokenStore store;
  final TokenRefresher refresher;
  final Dio client;
  final void Function({required bool replaced}) onSessionExpired;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await store.readAccessToken();
    if (token != null) options.headers['Authorization'] = 'Bearer $token';
    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final request = err.requestOptions;
    if (_replaced(err)) {
      await store.clear();
      onSessionExpired(replaced: true);
      return handler.next(err);
    }
    final canRetry =
        err.response?.statusCode == 401 &&
        request.extra[_retriedKey] != true &&
        !request.path.startsWith('/v1/auth/');
    if (!canRetry) return handler.next(err);

    switch (await refresher.refresh()) {
      case RefreshOutcome.rejected:
        onSessionExpired(replaced: false);
        return handler.next(err);
      case RefreshOutcome.unavailable:
        return handler.next(
          DioException(
            requestOptions: request,
            type: DioExceptionType.connectionError,
            error: err,
          ),
        );
      case RefreshOutcome.refreshed:
        request.extra[_retriedKey] = true;
        try {
          handler.resolve(await client.fetch<dynamic>(request));
        } on DioException catch (retryError) {
          handler.next(retryError);
        }
    }
  }

  bool _replaced(DioException err) {
    final data = err.response?.data;
    return err.response?.statusCode == 401 &&
        data is Map &&
        data['code'] == _replacedCode;
  }
}
