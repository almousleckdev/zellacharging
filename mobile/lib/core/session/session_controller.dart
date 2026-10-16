import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../network/network_providers.dart';

enum SessionStatus { unknown, authenticated, unauthenticated }

class SessionController extends Notifier<SessionStatus> {
  @override
  SessionStatus build() {
    _restore();
    return SessionStatus.unknown;
  }

  Future<void> _restore() async {
    final token = await ref.read(tokenStoreProvider).readRefreshToken();
    state = token == null
        ? SessionStatus.unauthenticated
        : SessionStatus.authenticated;
  }

  void signedIn() => state = SessionStatus.authenticated;

  void expire() => state = SessionStatus.unauthenticated;

  Future<void> signOut() async {
    await ref.read(tokenStoreProvider).clear();
    state = SessionStatus.unauthenticated;
  }
}

final sessionControllerProvider =
    NotifierProvider<SessionController, SessionStatus>(SessionController.new);
