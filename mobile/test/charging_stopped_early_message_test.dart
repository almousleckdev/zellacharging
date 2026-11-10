import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/charge_text.dart';
import 'package:zella/features/charging/domain/charging_session.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/charging/presentation/session_formatting.dart';

ChargingSession failed(String? code) => ChargingSession(
  id: 's',
  stationId: 'CP-1',
  connectorId: 1,
  state: SessionState.failed,
  failureCode: code,
  createdAt: DateTime(2026, 11, 10),
);

void main() {
  test('a charge the charger stopped early tells the driver to replug', () {
    expect(
      failed('CHARGER_STOPPED_EARLY').statusMessage,
      ChargeText.chargerStoppedEarly,
    );
  });

  test('any other failure keeps the generic message', () {
    expect(failed('CHARGER_REFUSED').statusMessage, ChargeText.sessionFailed);
  });
}
