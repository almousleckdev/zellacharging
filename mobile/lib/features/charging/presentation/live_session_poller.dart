import '../../../core/network/api_error.dart';
import '../domain/charging_session.dart';
import '../domain/session_repository.dart';

class LiveSessionPoller {
  LiveSessionPoller(
    this._repository, {
    this.interval = const Duration(seconds: 2),
  });

  static const _longestWait = Duration(seconds: 10);
  static const _firstLoadAttempts = 4;

  final SessionRepository _repository;
  final Duration interval;

  Stream<ChargingSession> watch(
    String id, {
    required bool Function() alive,
  }) async* {
    var session = await _firstLoad(id);
    yield session;
    var misses = 0;
    while (session.state.isLive && alive()) {
      await Future<void>.delayed(_waitAfter(misses));
      final next = await _tryGet(id);
      if (next == null) {
        misses++;
        continue;
      }
      misses = 0;
      session = next;
      yield session;
    }
  }

  Future<ChargingSession> _firstLoad(String id) async {
    for (var attempt = 1; ; attempt++) {
      try {
        return await _repository.get(id);
      } on ApiError catch (error) {
        if (!error.isRecoverable || attempt >= _firstLoadAttempts) rethrow;
        await Future<void>.delayed(interval * attempt);
      }
    }
  }

  Future<ChargingSession?> _tryGet(String id) async {
    try {
      return await _repository.get(id);
    } on ApiError catch (error) {
      if (!error.isRecoverable) rethrow;
      return null;
    }
  }

  Duration _waitAfter(int misses) {
    final wait = interval * (misses + 1);
    return wait > _longestWait ? _longestWait : wait;
  }
}
