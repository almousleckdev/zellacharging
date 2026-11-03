import 'package:flutter/widgets.dart';

import '../../shared/app_toast.dart';
import 'push_message.dart';

abstract final class PushToast {
  static void show(BuildContext context, PushMessage message) {
    final text = '${message.title}\n${message.body}'.trim();
    final type = message.data['type'] ?? '';
    if (_failed(type)) {
      AppToast.error(context, text);
    } else if (_succeeded(type)) {
      AppToast.success(context, text);
    } else {
      AppToast.info(context, text);
    }
  }

  static bool _failed(String type) =>
      type.endsWith('FAILED') ||
      type.endsWith('INTERRUPTED') ||
      type.endsWith('REJECTED');

  static bool _succeeded(String type) =>
      type.endsWith('SUCCEEDED') ||
      type.endsWith('COMPLETED') ||
      type.endsWith('APPROVED');
}
