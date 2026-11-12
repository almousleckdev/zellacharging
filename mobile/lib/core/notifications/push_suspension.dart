import 'push_message.dart';

abstract final class PushSuspension {
  static const _type = 'ACCOUNT_SUSPENDED_ARREARS';

  static bool isSuspension(PushMessage message) =>
      message.data['type'] == _type;
}
