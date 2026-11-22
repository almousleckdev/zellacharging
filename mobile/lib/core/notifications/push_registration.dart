import 'dart:async';

import 'push_messaging.dart';
import 'device_registry.dart';

class PushRegistration {
  PushRegistration(this._messaging, this._registry, {this.wait = _sleep});

  static Future<void> _sleep(Duration duration) =>
      Future<void>.delayed(duration);

  static const tokenAttempts = 10;
  static const tokenRetryDelay = Duration(seconds: 1);

  final PushMessaging _messaging;
  final DeviceRegistry _registry;
  final Future<void> Function(Duration) wait;
  StreamSubscription<String>? _refresh;
  String? _registered;
  bool _permitted = false;
  bool _acquiring = false;

  Future<void> start() async {
    if (!await _messaging.requestPermission()) return;
    _permitted = true;
    _refresh ??= _messaging.tokenRefreshed.listen(_register);
    await _acquire();
  }

  Future<void> retryIfUnregistered() async {
    if (!_permitted || _registered != null) return;
    await _acquire();
  }

  Future<void> _acquire() async {
    if (_acquiring) return;
    _acquiring = true;
    try {
      final token = await _tokenWithRetry();
      if (token != null) await _register(token);
    } finally {
      _acquiring = false;
    }
  }

  Future<String?> _tokenWithRetry() async {
    for (var attempt = 1; attempt <= tokenAttempts; attempt++) {
      try {
        final token = await _messaging.token();
        if (token != null) return token;
      } on Object {
        if (!_permitted) return null;
      }
      if (attempt < tokenAttempts) await wait(tokenRetryDelay);
    }
    return null;
  }

  Future<void> _register(String token) async {
    if (token == _registered) return;
    try {
      await _registry.register(token);
      _registered = token;
    } on Object {
      _registered = null;
    }
  }

  Future<void> stop() async {
    _permitted = false;
    await _refresh?.cancel();
    _refresh = null;
    try {
      final token = _registered ?? await _messaging.token();
      _registered = null;
      if (token != null) await _registry.unregister(token);
    } on Object {
      _registered = null;
    }
  }
}
