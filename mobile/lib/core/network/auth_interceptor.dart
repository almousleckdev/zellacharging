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
    this.onSuspended,
  });

  static const _retriedKey = 'retried';
  static const _replacedCode = 'SESSION_REPLACED';
  static const _suspendedCode = 'ACCOUNT_SUSPENDED';
  static const _authPrefix = '/v1/auth/';

  final TokenStore store;
  final TokenRefresher refresher;
  final Dio client;
  final void Function({required bool replaced}) onSessionExpired;
  final void Function(int? owedMinor)? onSuspended;

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
    if (_suspended(err)) {
      await store.clear();
      onSuspended?.call(_owedMinor(err));
      return handler.next(err);
    }
    if (_replaced(err)) {
      await store.clear();
      onSessionExpired(replaced: true);
      return handler.next(err);
    }
    final canRetry =
        err.response?.statusCode == 401 &&
        request.extra[_retriedKey] != true &&
        !request.path.startsWith(_authPrefix);
    if (!canRetry) return handler.next(err);

    switch (await refresher.refresh()) {
      case RefreshOutcome.rejected:
        onSessionExpired(replaced: false);
        return handler.next(err);
      case RefreshOutcome.suspended:
        onSuspended?.call(refresher.owedMinor);
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

  bool _suspended(DioException err) {
    final data = err.response?.data;
    return err.response?.statusCode == 403 &&
        data is Map &&
        data['code'] == _suspendedCode &&
        !err.requestOptions.path.startsWith(_authPrefix);
  }

  int? _owedMinor(DioException err) {
    final owed = (err.response?.data as Map)['owedMinor'];
    return owed is num ? owed.toInt() : null;
  }

  bool _replaced(DioException err) {
    final data = err.response?.data;
    return err.response?.statusCode == 401 &&
        data is Map &&
        data['code'] == _replacedCode;
  }
}
