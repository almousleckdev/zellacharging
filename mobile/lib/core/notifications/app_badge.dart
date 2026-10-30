import 'package:app_badge_plus/app_badge_plus.dart';

abstract class AppBadge {
  Future<void> set(int count);
}

class PluginAppBadge implements AppBadge {
  @override
  Future<void> set(int count) async {
    try {
      await AppBadgePlus.updateBadge(count < 0 ? 0 : count);
    } on Object {
      return;
    }
  }
}
