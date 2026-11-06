import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/charging/domain/charging_session.dart';
import 'package:zella/features/charging/domain/metering.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/charging/presentation/session_formatting.dart';

ChargingSession _session(SessionState state) => ChargingSession(
  id: 's1',
  stationId: 'CP-1',
  connectorId: 1,
  state: state,
  createdAt: DateTime(2026, 11, 6),
  energyKwh: 2.931,
  metering: const Metering(totalKwh: 2.902),
  currency: 'XOF',
  currencyDecimals: 0,
  pricePerKwhMinor: 120,
  stopReason: 'Remote',
);

void main() {
  test('a finished session shows the final energy, not the last live reading', () {
    expect(_session(SessionState.completed).shownEnergyKwh, 2.931);
  });

  test('a running session shows the live reading', () {
    expect(_session(SessionState.active).shownEnergyKwh, 2.902);
  });

  test('the price of the kWh and the stop reason are shown on a finished session', () {
    final session = _session(SessionState.completed);

    expect(session.pricePerKwhText, contains('120'));
    expect(session.stopReasonText, isNotEmpty);
  });
}
