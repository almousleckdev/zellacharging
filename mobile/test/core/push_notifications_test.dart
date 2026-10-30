import 'dart:async';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/notifications/device_registry.dart';
import 'package:zella/core/notifications/push_message.dart';
import 'package:zella/core/notifications/push_messaging.dart';
import 'package:zella/core/notifications/push_registration.dart';
import 'package:zella/core/notifications/push_route.dart';
import 'package:zella/core/router/routes.dart';

class _FakeMessaging implements PushMessaging {
  _FakeMessaging({this.granted = true});

  final bool granted;
  String? current = 'token-1';
  final refresh = StreamController<String>.broadcast();

  @override
  Future<bool> requestPermission() async => granted;

  @override
  Future<String?> token() async => current;

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

  final bool failing;
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

    test('signing out removes the device from the account', () async {
      final registry = _RecordingRegistry();
      final registration = PushRegistration(_FakeMessaging(), registry);
      await registration.start();

      await registration.stop();

      expect(registry.unregistered, ['token-1']);
    });
  });
}
