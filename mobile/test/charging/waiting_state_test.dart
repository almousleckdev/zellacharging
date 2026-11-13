import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/charging/domain/charging_session.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/charging/presentation/session_formatting.dart';

ChargingSession _session(SessionState state, {String? failureCode}) =>
    ChargingSession(
      id: 's1',
      stationId: 'CP-1',
      connectorId: 1,
      state: state,
      createdAt: DateTime(2026, 11, 13),
      failureCode: failureCode,
    );

void main() {
  test('a driver can cancel while the charger waits for the cable only', () {
    expect(SessionState.startAccepted.canCancel, isTrue);
    expect(SessionState.active.canCancel, isFalse);
    expect(SessionState.startAccepted.canStop, isFalse);
  });

  test(
    'waiting for the cable is explained by the prompt, not a status line',
    () {
      expect(_session(SessionState.startAccepted).statusMessage, isNull);
    },
  );

  test('a cancelled or timed out wait says nothing was charged', () {
    final cancelled = _session(SessionState.failed, failureCode: 'CANCELLED');
    final timedOut = _session(
      SessionState.failed,
      failureCode: 'START_TIMEOUT',
    );

    expect(
      cancelled.statusMessage,
      isNot(_session(SessionState.failed).statusMessage),
    );
    expect(timedOut.statusMessage, isNot(cancelled.statusMessage));
  });
}
