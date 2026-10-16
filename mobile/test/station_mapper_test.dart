import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/features/stations/data/station_mapper.dart';

void main() {
  test('maps a station payload with connectors', () {
    final station = StationMapper.station({
      'id': 'CP-1',
      'online': true,
      'availableConnectors': 1,
      'totalConnectors': 2,
      'distanceKm': 1.5,
      'connectors': [
        {
          'connectorId': 1,
          'status': 'Available',
          'available': true,
          'maxPowerKw': 60,
        },
        {'connectorId': 2, 'status': 'Charging', 'available': false},
      ],
    });
    expect(station.id, 'CP-1');
    expect(station.distanceKm, 1.5);
    expect(station.maxPowerKw, 60);
    expect(station.connectors.last.available, isFalse);
  });
}
