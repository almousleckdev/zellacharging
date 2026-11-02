import 'package:dio/dio.dart';

class RetryInterceptor extends Interceptor {
  RetryInterceptor({
    required this.client,
    this.maxRetries = 2,
    this.baseDelay = const Duration(milliseconds: 400),
  });

  static const _attemptKey = 'retryAttempt';
  static const _transientStatuses = {502, 503, 504};

  final Dio client;
  final int maxRetries;
  final Duration baseDelay;

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final request = err.requestOptions;
    final attempt = (request.extra[_attemptKey] as int?) ?? 0;
    if (!_shouldRetry(err) || attempt >= maxRetries) return handler.next(err);

    request.extra[_attemptKey] = attempt + 1;
    await Future<void>.delayed(baseDelay * (attempt + 1));
    try {
      handler.resolve(await client.fetch<dynamic>(request));
    } on DioException catch (retryError) {
      handler.next(retryError);
    }
  }

  bool _shouldRetry(DioException err) {
    if (err.requestOptions.method.toUpperCase() != 'GET') return false;
    if (err.type == DioExceptionType.cancel) return false;
    final status = err.response?.statusCode;
    if (status != null) return _transientStatuses.contains(status);
    return true;
  }
}
