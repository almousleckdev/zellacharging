import 'package:firebase_messaging/firebase_messaging.dart';

import 'push_message.dart';
import 'push_messaging.dart';

class FirebasePushMessaging implements PushMessaging {
  FirebasePushMessaging(this._messaging);

  final FirebaseMessaging _messaging;

  @override
  Future<bool> requestPermission() async {
    final settings = await _messaging.requestPermission();
    await _messaging.setForegroundNotificationPresentationOptions(
      alert: true,
      badge: true,
      sound: true,
    );
    return settings.authorizationStatus == AuthorizationStatus.authorized ||
        settings.authorizationStatus == AuthorizationStatus.provisional;
  }

  @override
  Future<String?> token() => _messaging.getToken();

  @override
  Stream<String> get tokenRefreshed => _messaging.onTokenRefresh;

  @override
  Stream<PushMessage> get foreground =>
      FirebaseMessaging.onMessage.map(_convert);

  @override
  Stream<PushMessage> get opened =>
      FirebaseMessaging.onMessageOpenedApp.map(_convert);

  @override
  Future<PushMessage?> get launchedFrom async {
    final message = await _messaging.getInitialMessage();
    return message == null ? null : _convert(message);
  }

  @override
  Future<void> forgetToken() => _messaging.deleteToken();

  PushMessage _convert(RemoteMessage message) => PushMessage(
    title: message.notification?.title ?? '',
    body: message.notification?.body ?? '',
    data: message.data.map((key, value) => MapEntry(key, '$value')),
  );
}
