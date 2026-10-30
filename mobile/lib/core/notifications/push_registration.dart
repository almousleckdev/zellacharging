import 'dart:async';

import 'push_messaging.dart';
import 'device_registry.dart';

class PushRegistration {
  PushRegistration(this._messaging, this._registry);

  final PushMessaging _messaging;
  final DeviceRegistry _registry;
  StreamSubscription<String>? _refresh;
  String? _registered;

  Future<void> start() async {
    if (!await _messaging.requestPermission()) return;
    final token = await _messaging.token();
    if (token != null) await _register(token);
    _refresh ??= _messaging.tokenRefreshed.listen(_register);
  }

  Future<void> _register(String token) async {
    try {
      await _registry.register(token);
      _registered = token;
    } on Object {
      _registered = null;
    }
  }

  Future<void> stop() async {
    await _refresh?.cancel();
    _refresh = null;
    final token = _registered ?? await _messaging.token();
    _registered = null;
    if (token == null) return;
    try {
      await _registry.unregister(token);
    } on Object {
      return;
    }
  }
}
