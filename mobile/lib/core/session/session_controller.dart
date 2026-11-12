import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../network/network_providers.dart';
import '../notifications/push_providers.dart';
import '../storage/preference_providers.dart';
import 'stay_signed_in.dart';
import 'suspended_on_sign_out.dart';

enum SessionStatus { unknown, authenticated, unauthenticated }

class SignedOutElsewhere extends Notifier<bool> {
  @override
  bool build() => false;

  void mark() => state = true;

  void acknowledge() => state = false;
}

final signedOutElsewhereProvider = NotifierProvider<SignedOutElsewhere, bool>(
  SignedOutElsewhere.new,
);

class SessionController extends Notifier<SessionStatus> {
  static const _cleanupLimit = Duration(seconds: 3);

  @override
  SessionStatus build() {
    _restore();
    return SessionStatus.unknown;
  }

  Future<void> _restore() async {
    final stay = await ref.read(preferenceStoreProvider).read(stayKey);
    if (stay == '0') await ref.read(tokenStoreProvider).clear();
    final token = await ref.read(tokenStoreProvider).readRefreshToken();
    state = token == null
        ? SessionStatus.unauthenticated
        : SessionStatus.authenticated;
  }

  void signedIn() => state = SessionStatus.authenticated;

  void expire({bool replaced = false}) {
    if (replaced) ref.read(signedOutElsewhereProvider.notifier).mark();
    state = SessionStatus.unauthenticated;
  }

  Future<void> suspend(int? owedMinor) async {
    if (state == SessionStatus.unauthenticated) return;
    ref.read(suspensionNoticeProvider.notifier).mark(owedMinor);
    await ref.read(tokenStoreProvider).clear();
    state = SessionStatus.unauthenticated;
  }

  Future<void> signOut() async {
    await _bestEffort(() => ref.read(pushRegistrationProvider).stop());
    await _bestEffort(() => ref.read(appBadgeProvider).set(0));
    await ref.read(tokenStoreProvider).clear();
    state = SessionStatus.unauthenticated;
  }

  Future<void> _bestEffort(Future<void> Function() cleanup) async {
    try {
      await cleanup().timeout(_cleanupLimit);
    } on Object {
      return;
    }
  }
}

final sessionControllerProvider =
    NotifierProvider<SessionController, SessionStatus>(SessionController.new);
