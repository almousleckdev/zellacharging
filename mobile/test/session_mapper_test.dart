import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/features/charging/data/session_mapper.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';

Map<String, dynamic> _json({Object? battery, Map<String, dynamic>? metering}) =>
    {
      'id': 's1',
      'stationId': 'CP-1',
      'connectorId': 1,
      'state': 'COMPLETED',
      'createdAt': '2026-10-16T12:00:00Z',
      'batteryPercent': battery,
      'metering': metering,
    };

void main() {
  test('reads the battery level of a finished session', () {
    final session = SessionMapper.session(_json(battery: 64.5));

    expect(session.state, SessionState.completed);
    expect(session.batteryPercent, 64.5);
  });

  test('leaves the battery level unknown when the server has none', () {
    expect(SessionMapper.session(_json()).batteryPercent, isNull);
  });

  test('reads voltage, current and the estimated cost', () {
    final session = SessionMapper.session({
      ..._json(
        battery: 40,
        metering: {'voltage': 402.1, 'currentAmpere': 74.6},
      ),
      'estimatedCost': {'minor': 375, 'currency': 'XAF', 'decimals': 0},
    });

    expect(session.metering?.voltage, 402.1);
    expect(session.metering?.currentAmpere, 74.6);
    expect(session.estimatedCost?.minor, 375);
  });
}
