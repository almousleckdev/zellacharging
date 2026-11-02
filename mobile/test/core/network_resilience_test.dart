import 'dart:async';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/network/auth_interceptor.dart';
import 'package:zella/core/network/refresh_outcome.dart';
import 'package:zella/core/network/retry_interceptor.dart';
import 'package:zella/core/network/token_refresher.dart';
import 'package:zella/core/storage/token_store.dart';

class _MemoryStore implements TokenStore {
  String? access = 'old-access';
  String? refresh = 'old-refresh';

  @override
  Future<String?> readAccessToken() async => access;

  @override
  Future<String?> readRefreshToken() async => refresh;

  @override
  Future<void> save({
    required String accessToken,
    required String refreshToken,
  }) async {
    access = accessToken;
    refresh = refreshToken;
  }

  @override
  Future<void> clear() async {
    access = null;
    refresh = null;
  }
}

class _Scripted implements HttpClientAdapter {
  _Scripted(this.answers);

  final List<Object> answers;
  final List<String> calls = [];

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    calls.add('${options.method} ${options.path}');
    final next = answers.removeAt(0);
    if (next is int) {
      return ResponseBody.fromString(
        '{"code":"X"}',
        next,
        headers: {
          Headers.contentTypeHeader: [Headers.jsonContentType],
        },
      );
    }
    if (next is Map) {
      return ResponseBody.fromString(
        '{"code":"${next['code']}"}',
        next['status'] as int,
        headers: {
          Headers.contentTypeHeader: [Headers.jsonContentType],
        },
      );
    }
    throw DioException(
      requestOptions: options,
      type: DioExceptionType.connectionError,
    );
  }

  @override
  void close({bool force = false}) {}
}

Dio _dio(_Scripted adapter) =>
    Dio(BaseOptions(baseUrl: 'http://test'))..httpClientAdapter = adapter;

void main() {
  test('a lost connection does not wipe the saved session', () async {
    final store = _MemoryStore();
    final refresher = TokenRefresher(
      plainClient: _dio(_Scripted(['offline'])),
      store: store,
    );

    expect(await refresher.refresh(), RefreshOutcome.unavailable);
    expect(store.refresh, 'old-refresh');
  });

  test('a server error during refresh keeps the session', () async {
    final store = _MemoryStore();
    final refresher = TokenRefresher(
      plainClient: _dio(_Scripted([503])),
      store: store,
    );

    expect(await refresher.refresh(), RefreshOutcome.unavailable);
    expect(store.refresh, 'old-refresh');
  });

  test('a refused refresh token clears the session', () async {
    final store = _MemoryStore();
    final refresher = TokenRefresher(
      plainClient: _dio(_Scripted([401])),
      store: store,
    );

    expect(await refresher.refresh(), RefreshOutcome.rejected);
    expect(store.refresh, isNull);
  });

  test('a replaced session signs out at once without refreshing', () async {
    final store = _MemoryStore();
    final adapter = _Scripted([
      {'status': 401, 'code': 'SESSION_REPLACED'},
    ]);
    final client = _dio(adapter);
    final refresher = TokenRefresher(
      plainClient: _dio(_Scripted([])),
      store: store,
    );
    bool? sawReplaced;
    client.interceptors.add(
      AuthInterceptor(
        store: store,
        refresher: refresher,
        client: client,
        onSessionExpired: ({required bool replaced}) => sawReplaced = replaced,
      ),
    );

    await expectLater(
      client.get<dynamic>('/v1/me'),
      throwsA(isA<DioException>()),
    );
    expect(sawReplaced, isTrue);
    expect(store.access, isNull);
  });

  test(
    'a failed refresh caused by the network does not sign the user out',
    () async {
      final store = _MemoryStore();
      final client = _dio(_Scripted([401]));
      final refresher = TokenRefresher(
        plainClient: _dio(_Scripted(['offline'])),
        store: store,
      );
      var expired = false;
      client.interceptors.add(
        AuthInterceptor(
          store: store,
          refresher: refresher,
          client: client,
          onSessionExpired: ({required bool replaced}) => expired = true,
        ),
      );

      await expectLater(
        client.get<dynamic>('/v1/me'),
        throwsA(
          isA<DioException>().having(
            (e) => e.type,
            'type',
            DioExceptionType.connectionError,
          ),
        ),
      );
      expect(expired, isFalse);
      expect(store.refresh, 'old-refresh');
    },
  );

  test('a read is retried after a dropped connection', () async {
    final adapter = _Scripted(['offline', 200]);
    final client = _dio(adapter);
    client.interceptors.add(
      RetryInterceptor(client: client, baseDelay: Duration.zero),
    );

    final response = await client.get<dynamic>('/v1/stations');

    expect(response.statusCode, 200);
    expect(adapter.calls, hasLength(2));
  });

  test('a payment-style write is never retried', () async {
    final adapter = _Scripted(['offline', 200]);
    final client = _dio(adapter);
    client.interceptors.add(
      RetryInterceptor(client: client, baseDelay: Duration.zero),
    );

    await expectLater(
      client.post<dynamic>('/v1/wallet/topups'),
      throwsA(isA<DioException>()),
    );
    expect(adapter.calls, hasLength(1));
  });

  test('retries stop after the limit', () async {
    final adapter = _Scripted(['offline', 'offline', 'offline', 200]);
    final client = _dio(adapter);
    client.interceptors.add(
      RetryInterceptor(client: client, baseDelay: Duration.zero),
    );

    await expectLater(
      client.get<dynamic>('/v1/stations'),
      throwsA(isA<DioException>()),
    );
    expect(adapter.calls, hasLength(3));
  });
}
