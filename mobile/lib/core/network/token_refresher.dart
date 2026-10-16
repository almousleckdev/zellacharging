import 'package:dio/dio.dart';

import '../storage/token_store.dart';

class TokenRefresher {
  TokenRefresher({required this.plainClient, required this.store});

  final Dio plainClient;
  final TokenStore store;
  Future<bool>? _inFlight;

  Future<bool> refresh() => _inFlight ??= _run().whenComplete(() {
    _inFlight = null;
  });

  Future<bool> _run() async {
    final refreshToken = await store.readRefreshToken();
    if (refreshToken == null) return false;
    try {
      final response = await plainClient.post<Map<String, dynamic>>(
        '/v1/auth/refresh',
        data: {'refreshToken': refreshToken},
      );
      await store.save(
        accessToken: response.data!['accessToken'] as String,
        refreshToken: response.data!['refreshToken'] as String,
      );
      return true;
    } on DioException {
      await store.clear();
      return false;
    }
  }
}
