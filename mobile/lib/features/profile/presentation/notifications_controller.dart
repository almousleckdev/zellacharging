import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/storage/preference_providers.dart';

final notificationsProvider =
    AsyncNotifierProvider<NotificationsController, bool>(
      NotificationsController.new,
    );

class NotificationsController extends AsyncNotifier<bool> {
  static const storageKey = 'notifications';
  static const _off = 'off';

  @override
  Future<bool> build() async {
    final saved = await ref.read(preferenceStoreProvider).read(storageKey);
    return saved != _off;
  }

  Future<void> set(bool enabled) async {
    state = AsyncData(enabled);
    await ref
        .read(preferenceStoreProvider)
        .save(storageKey, enabled ? 'on' : _off);
  }
}
