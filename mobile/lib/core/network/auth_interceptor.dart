import 'package:dio/dio.dart';

import '../storage/token_store.dart';
import 'token_refresher.dart';

class AuthInterceptor extends Interceptor {
  AuthInterceptor({
    required this.store,
    required this.refresher,
    required this.client,
    required this.onSessionExpired,
  });

  static const _retriedKey = 'retried';

  final TokenStore store;
  final TokenRefresher refresher;
  final Dio client;
  final void Function() onSessionExpired;

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
    final canRetry =
        err.response?.statusCode == 401 &&
        request.extra[_retriedKey] != true &&
        !request.path.startsWith('/v1/auth/');
    if (!canRetry) return handler.next(err);

    if (!await refresher.refresh()) {
      onSessionExpired();
      return handler.next(err);
    }
    request.extra[_retriedKey] = true;
    try {
      handler.resolve(await client.fetch<dynamic>(request));
    } on DioException catch (retryError) {
      handler.next(retryError);
    }
  }
}
