import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/core/network/api_error_kind.dart';
import 'package:zella/features/charging/domain/charging_session.dart';
import 'package:zella/features/charging/domain/session_repository.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/charging/presentation/live_session_poller.dart';

ChargingSession _session(SessionState state) => ChargingSession(
  id: 's1',
  stationId: 'CP-1',
  connectorId: 1,
  state: state,
  createdAt: DateTime(2026, 11, 13),
);

const _offline = ApiError(
  code: 'NETWORK',
  message: 'offline',
  kind: ApiErrorKind.recoverable,
);

const _forbidden = ApiError(code: 'FORBIDDEN', message: 'no');

class _ScriptedRepository implements SessionRepository {
  _ScriptedRepository(this.script);

  final List<Object> script;

  @override
  Future<ChargingSession> get(String id) async {
    final next = script.removeAt(0);
    if (next is ApiError) throw next;
    return next as ChargingSession;
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

LiveSessionPoller _poller(List<Object> script) => LiveSessionPoller(
  _ScriptedRepository(script),
  interval: const Duration(milliseconds: 5),
);

void main() {
  test(
    'a dropped connection while charging does not end the session view',
    () async {
      final poller = _poller([
        _session(SessionState.active),
        _offline,
        _offline,
        _session(SessionState.active),
        _session(SessionState.completed),
      ]);

      final seen = await poller.watch('s1', alive: () => true).toList();

      expect(seen.map((s) => s.state), [
        SessionState.active,
        SessionState.active,
        SessionState.completed,
      ]);
    },
  );

  test('the first load is retried when the network is not back yet', () async {
    final poller = _poller([_offline, _session(SessionState.completed)]);

    final seen = await poller.watch('s1', alive: () => true).toList();

    expect(seen, hasLength(1));
  });

  test('an error that retrying cannot fix is still reported', () async {
    final poller = _poller([_session(SessionState.active), _forbidden]);

    expect(
      poller.watch('s1', alive: () => true).toList(),
      throwsA(isA<ApiError>()),
    );
  });
}
