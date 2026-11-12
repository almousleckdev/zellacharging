import 'dart:async';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/network/network_providers.dart';
import 'package:zella/core/notifications/app_badge.dart';
import 'package:zella/core/notifications/device_registry.dart';
import 'package:zella/core/notifications/push_message.dart';
import 'package:zella/core/notifications/push_messaging.dart';
import 'package:zella/core/notifications/push_providers.dart';
import 'package:zella/core/notifications/push_registration.dart';
import 'package:zella/core/session/session_controller.dart';
import 'package:zella/core/session/session_verifier.dart';
import 'package:zella/core/session/suspended_on_sign_out.dart';
import 'package:zella/core/storage/preference_providers.dart';
import 'package:zella/core/storage/preference_store.dart';
import 'package:zella/core/storage/token_store.dart';

class _MemoryTokens implements TokenStore {
  String? access = 'access';
  String? refresh = 'refresh';

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

class _Preferences implements PreferenceStore {
  @override
  Future<String?> read(String key) async => null;

  @override
  Future<void> save(String key, String value) async {}
}

class _BrokenMessaging implements PushMessaging {
  @override
  Future<bool> requestPermission() async => true;

  @override
  Future<String?> token() => throw StateError('apns-token-not-set');

  @override
  Stream<String> get tokenRefreshed => const Stream.empty();

  @override
  Stream<PushMessage> get foreground => const Stream.empty();

  @override
  Stream<PushMessage> get opened => const Stream.empty();

  @override
  Future<PushMessage?> get launchedFrom async => null;

  @override
  Future<void> forgetToken() async {}
}

class _HangingPush extends PushRegistration {
  _HangingPush() : super(_BrokenMessaging(), DeviceRegistry(Dio()));

  @override
  Future<void> stop() => Completer<void>().future;
}

class _SpyPush extends PushRegistration {
  _SpyPush() : super(_BrokenMessaging(), DeviceRegistry(Dio()));

  bool stopped = false;

  @override
  Future<void> stop() async => stopped = true;
}

class _Badge implements AppBadge {
  @override
  Future<void> set(int count) => throw StateError('no badge');
}

class _Recording implements HttpClientAdapter {
  final calls = <String>[];

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    calls.add('${options.method} ${options.path}');
    return ResponseBody.fromString(
      '{}',
      200,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

ProviderContainer _container(
  _MemoryTokens tokens,
  PushRegistration push, {
  Dio? dio,
}) {
  final container = ProviderContainer(
    overrides: [
      if (dio != null) dioProvider.overrideWithValue(dio),
      tokenStoreProvider.overrideWithValue(tokens),
      preferenceStoreProvider.overrideWithValue(_Preferences()),
      pushRegistrationProvider.overrideWithValue(push),
      appBadgeProvider.overrideWithValue(_Badge()),
    ],
  );
  addTearDown(container.dispose);
  return container;
}

Future<void> _restored(ProviderContainer container) async {
  container.listen(sessionControllerProvider, (_, _) {});
  await Future<void>.delayed(const Duration(milliseconds: 50));
}

void main() {
  test('push cleanup that fails never keeps the customer signed in', () async {
    final push = PushRegistration(_BrokenMessaging(), DeviceRegistry(Dio()));

    await push.stop();
  });

  test(
    'signing out clears the session even when cleanup hangs or fails',
    () async {
      final tokens = _MemoryTokens();
      final container = _container(tokens, _HangingPush());
      await _restored(container);
      expect(
        container.read(sessionControllerProvider),
        SessionStatus.authenticated,
      );

      await container.read(sessionControllerProvider.notifier).signOut();

      expect(
        container.read(sessionControllerProvider),
        SessionStatus.unauthenticated,
      );
      expect(tokens.refresh, isNull);
    },
  );

  test(
    'a new session gets a fresh client so no account data is kept',
    () async {
      final tokens = _MemoryTokens();
      final container = _container(
        tokens,
        PushRegistration(_BrokenMessaging(), DeviceRegistry(Dio())),
      );
      await _restored(container);
      final before = container.read(dioProvider);

      await container.read(sessionControllerProvider.notifier).signOut();
      container.read(sessionControllerProvider.notifier).signedIn();

      expect(container.read(dioProvider), isNot(same(before)));
    },
  );

  test(
    'a suspension signs out at once, keeps the device registered and remembers the amount owed',
    () async {
      final tokens = _MemoryTokens();
      final push = _SpyPush();
      final container = _container(tokens, push);
      await _restored(container);

      await container.read(sessionControllerProvider.notifier).suspend(69000);

      expect(
        container.read(sessionControllerProvider),
        SessionStatus.unauthenticated,
      );
      expect(tokens.refresh, isNull);
      expect(push.stopped, isFalse);
      expect(container.read(suspensionNoticeProvider)?.owedMinor, 69000);
    },
  );

  test('a suspension without an amount is still remembered', () async {
    final container = _container(_MemoryTokens(), _SpyPush());
    await _restored(container);

    await container.read(sessionControllerProvider.notifier).suspend(null);

    expect(container.read(suspensionNoticeProvider), isNotNull);
    expect(container.read(suspensionNoticeProvider)?.owedMinor, isNull);
  });

  test(
    'a suspension push only makes the app ask the server and never signs out by itself',
    () async {
      final adapter = _Recording();
      final dio = Dio(BaseOptions(baseUrl: 'http://test'))
        ..httpClientAdapter = adapter;
      final container = _container(_MemoryTokens(), _SpyPush(), dio: dio);
      await _restored(container);

      await container.read(sessionVerifierProvider).check();

      expect(adapter.calls, ['GET /v1/me']);
      expect(
        container.read(sessionControllerProvider),
        SessionStatus.authenticated,
      );
    },
  );

  test('nothing is asked of the server when nobody is signed in', () async {
    final adapter = _Recording();
    final dio = Dio(BaseOptions(baseUrl: 'http://test'))
      ..httpClientAdapter = adapter;
    final tokens = _MemoryTokens()..refresh = null;
    final container = _container(tokens, _SpyPush(), dio: dio);
    await _restored(container);

    await container.read(sessionVerifierProvider).check();

    expect(adapter.calls, isEmpty);
  });

  test(
    'a late suspension answer does not leave a stale notice behind',
    () async {
      final container = _container(_MemoryTokens(), _SpyPush());
      await _restored(container);
      final session = container.read(sessionControllerProvider.notifier);

      await session.suspend(69000);
      container.read(suspensionNoticeProvider.notifier).acknowledge();
      await session.suspend(69000);

      expect(container.read(suspensionNoticeProvider), isNull);
    },
  );
}
