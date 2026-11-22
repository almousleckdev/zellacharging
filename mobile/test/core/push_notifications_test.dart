import 'dart:async';

import 'package:dio/dio.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/notifications/device_registry.dart';
import 'package:zella/core/notifications/push_message.dart';
import 'package:zella/core/notifications/push_messaging.dart';
import 'package:zella/core/notifications/push_registration.dart';
import 'package:zella/core/notifications/push_route.dart';
import 'package:zella/core/notifications/push_suspension.dart';
import 'package:zella/core/router/routes.dart';

class _FakeMessaging implements PushMessaging {
  _FakeMessaging({this.granted = true});

  final bool granted;
  String? current = 'token-1';
  final script = <Object?>[];
  final calls = <String>[];
  var tokenCalls = 0;
  late final refresh = StreamController<String>.broadcast(
    onListen: () => calls.add('listen'),
  );

  @override
  Future<bool> requestPermission() async => granted;

  @override
  Future<String?> token() async {
    calls.add('token');
    tokenCalls++;
    if (script.isEmpty) return current;
    final next = script.removeAt(0);
    if (next is Exception) throw next;
    return next as String?;
  }

  @override
  Stream<String> get tokenRefreshed => refresh.stream;

  @override
  Stream<PushMessage> get foreground => const Stream.empty();

  @override
  Stream<PushMessage> get opened => const Stream.empty();

  @override
  Future<PushMessage?> get launchedFrom async => null;

  @override
  Future<void> forgetToken() async {}
}

class _RecordingRegistry extends DeviceRegistry {
  _RecordingRegistry({this.failing = false}) : super(Dio());

  bool failing;
  final registered = <String>[];
  final unregistered = <String>[];

  @override
  Future<void> register(String token) async {
    if (failing) throw DioException(requestOptions: RequestOptions());
    registered.add(token);
  }

  @override
  Future<void> unregister(String token) async => unregistered.add(token);
}

Future<void> _instant(Duration _) async {}

final _apnsNotSet = FirebaseException(
  plugin: 'firebase_messaging',
  code: 'apns-token-not-set',
);

PushRegistration _registration(_FakeMessaging m, _RecordingRegistry r) =>
    PushRegistration(m, r, wait: _instant);

Future<void> _flush() => Future<void>.delayed(Duration.zero);

PushMessage _message(Map<String, String> data) =>
    PushMessage(title: 't', body: 'b', data: data);

void main() {
  group('routing a tapped notification', () {
    test('wallet events open the wallet', () {
      expect(routeFor(_message({'screen': 'wallet'})), Routes.wallet);
    });

    test('charging events open that charge', () {
      expect(
        routeFor(_message({'screen': 'charging', 'id': 'abc'})),
        Routes.chargingPath('abc'),
      );
    });

    test('a charging event without an id goes nowhere', () {
      expect(routeFor(_message({'screen': 'charging'})), isNull);
    });

    test('an unknown screen goes nowhere', () {
      expect(routeFor(_message({'screen': 'admin'})), isNull);
      expect(routeFor(_message({})), isNull);
    });
  });

  group('device registration', () {
    test('registers the token once permission is granted', () async {
      final registry = _RecordingRegistry();
      await PushRegistration(_FakeMessaging(), registry).start();

      expect(registry.registered, ['token-1']);
    });

    test('registers nothing when the customer refuses notifications', () async {
      final registry = _RecordingRegistry();
      await PushRegistration(_FakeMessaging(granted: false), registry).start();

      expect(registry.registered, isEmpty);
    });

    test('a refreshed token is registered again', () async {
      final registry = _RecordingRegistry();
      final messaging = _FakeMessaging();
      await PushRegistration(messaging, registry).start();

      messaging.refresh.add('token-2');
      await Future<void>.delayed(Duration.zero);

      expect(registry.registered, ['token-1', 'token-2']);
    });

    test('a failing server never crashes the app', () async {
      final registry = _RecordingRegistry(failing: true);

      await PushRegistration(_FakeMessaging(), registry).start();

      expect(registry.registered, isEmpty);
    });

    test('retries while the APNs token is not set yet', () async {
      final registry = _RecordingRegistry();
      final messaging = _FakeMessaging()
        ..script.addAll([_apnsNotSet, _apnsNotSet, 'apns-ok']);

      await _registration(messaging, registry).start();

      expect(registry.registered, ['apns-ok']);
    });

    test('listens for refreshed tokens before asking for the token', () async {
      final messaging = _FakeMessaging();

      await _registration(messaging, _RecordingRegistry()).start();

      expect(messaging.calls.take(2), ['listen', 'token']);
    });

    test(
      'a token that is null at first is registered when it arrives',
      () async {
        final registry = _RecordingRegistry();
        final messaging = _FakeMessaging()..current = null;
        await _registration(messaging, registry).start();
        expect(registry.registered, isEmpty);

        messaging.refresh.add('late');
        await _flush();

        expect(registry.registered, ['late']);
      },
    );

    test('refusing notifications registers nothing and never throws', () async {
      final registry = _RecordingRegistry();
      final messaging = _FakeMessaging(granted: false);

      await _registration(messaging, registry).start();

      expect(registry.registered, isEmpty);
      expect(messaging.tokenCalls, 0);
    });

    test(
      'a token that always fails never throws and registers later',
      () async {
        final registry = _RecordingRegistry();
        final messaging = _FakeMessaging()
          ..script.addAll(List.filled(50, _apnsNotSet));

        await _registration(messaging, registry).start();
        expect(registry.registered, isEmpty);

        messaging.refresh.add('arrived');
        await _flush();

        expect(registry.registered, ['arrived']);
      },
    );

    test('retryIfUnregistered registers after a server failure', () async {
      final registry = _RecordingRegistry(failing: true);
      final registration = _registration(_FakeMessaging(), registry);
      await registration.start();
      registry.failing = false;

      await registration.retryIfUnregistered();

      expect(registry.registered, ['token-1']);
    });

    test('the same token delivered twice is registered once', () async {
      final registry = _RecordingRegistry();
      final messaging = _FakeMessaging();
      await _registration(messaging, registry).start();

      messaging.refresh.add('token-1');
      await _flush();

      expect(registry.registered, ['token-1']);
    });

    test('retryIfUnregistered does nothing when already registered', () async {
      final registry = _RecordingRegistry();
      final messaging = _FakeMessaging();
      final registration = _registration(messaging, registry);
      await registration.start();
      final calls = messaging.tokenCalls;

      await registration.retryIfUnregistered();

      expect(registry.registered, ['token-1']);
      expect(messaging.tokenCalls, calls);
    });

    test('signing out cancels the listener and unregisters', () async {
      final registry = _RecordingRegistry();
      final messaging = _FakeMessaging();
      final registration = _registration(messaging, registry);
      await registration.start();

      await registration.stop();
      messaging.refresh.add('after');
      await _flush();

      expect(registry.unregistered, ['token-1']);
      expect(registry.registered, ['token-1']);
    });

    test('signing out removes the device from the account', () async {
      final registry = _RecordingRegistry();
      final registration = PushRegistration(_FakeMessaging(), registry);
      await registration.start();

      await registration.stop();

      expect(registry.unregistered, ['token-1']);
    });
  });

  test('a suspension push is recognised', () {
    const suspended = PushMessage(
      title: 't',
      body: 'b',
      data: {'type': 'ACCOUNT_SUSPENDED_ARREARS', 'owedMinor': '69000'},
    );
    const withoutAmount = PushMessage(
      title: 't',
      body: 'b',
      data: {'type': 'ACCOUNT_SUSPENDED_ARREARS'},
    );
    const other = PushMessage(
      title: 't',
      body: 'b',
      data: {'type': 'CHARGE_STARTED', 'owedMinor': '5'},
    );

    expect(PushSuspension.isSuspension(suspended), isTrue);
    expect(PushSuspension.isSuspension(withoutAmount), isTrue);
    expect(PushSuspension.isSuspension(other), isFalse);
  });
}
