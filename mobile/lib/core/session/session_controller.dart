import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../network/network_providers.dart';
import '../notifications/push_providers.dart';
import '../storage/preference_providers.dart';
import 'stay_signed_in.dart';

enum SessionStatus { unknown, authenticated, unauthenticated }

class SessionController extends Notifier<SessionStatus> {
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

  void expire() => state = SessionStatus.unauthenticated;

  Future<void> signOut() async {
    await ref.read(pushRegistrationProvider).stop();
    await ref.read(appBadgeProvider).set(0);
    await ref.read(tokenStoreProvider).clear();
    state = SessionStatus.unauthenticated;
  }
}

final sessionControllerProvider =
    NotifierProvider<SessionController, SessionStatus>(SessionController.new);
