import 'push_message.dart';

abstract class PushMessaging {
  Future<bool> requestPermission();

  Future<String?> token();

  Stream<String> get tokenRefreshed;

  Stream<PushMessage> get foreground;

  Stream<PushMessage> get opened;

  Future<PushMessage?> get launchedFrom;

  Future<void> forgetToken();
}
