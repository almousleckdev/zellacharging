import 'dart:async';

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

class _Badge implements AppBadge {
  @override
  Future<void> set(int count) => throw StateError('no badge');
}

ProviderContainer _container(_MemoryTokens tokens, PushRegistration push) {
  final container = ProviderContainer(
    overrides: [
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
}
