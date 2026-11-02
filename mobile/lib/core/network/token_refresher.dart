import 'package:dio/dio.dart';

import '../storage/token_store.dart';
import 'refresh_outcome.dart';

class TokenRefresher {
  TokenRefresher({required this.plainClient, required this.store});

  final Dio plainClient;
  final TokenStore store;
  Future<RefreshOutcome>? _inFlight;

  Future<RefreshOutcome> refresh() => _inFlight ??= _run().whenComplete(() {
    _inFlight = null;
  });

  Future<RefreshOutcome> _run() async {
    final refreshToken = await store.readRefreshToken();
    if (refreshToken == null) return RefreshOutcome.rejected;
    try {
      final response = await plainClient.post<Map<String, dynamic>>(
        '/v1/auth/refresh',
        data: {'refreshToken': refreshToken},
      );
      await store.save(
        accessToken: response.data!['accessToken'] as String,
        refreshToken: response.data!['refreshToken'] as String,
      );
      return RefreshOutcome.refreshed;
    } on DioException catch (error) {
      if (!_isRejection(error)) return RefreshOutcome.unavailable;
      await store.clear();
      return RefreshOutcome.rejected;
    }
  }

  bool _isRejection(DioException error) {
    final status = error.response?.statusCode;
    return status != null && status >= 400 && status < 500 && status != 429;
  }
}
