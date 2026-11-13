import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/station_text.dart';
import 'package:zella/features/stations/data/station_mapper.dart';

void main() {
  test(
    'a busy connector carries the minutes left that the server estimated',
    () {
      final connector = StationMapper.connector({
        'connectorId': 2,
        'status': 'Charging',
        'available': false,
        'freeInMinutes': 12,
      });

      expect(connector.freeInMinutes, 12);
    },
  );

  test('a free connector has no estimate', () {
    final connector = StationMapper.connector({
      'connectorId': 1,
      'status': 'Available',
      'available': true,
    });

    expect(connector.freeInMinutes, isNull);
  });

  test('long waits read in hours and minutes', () {
    expect(StationText.freeIn(45), contains('45'));
    expect(StationText.freeIn(80), contains('1 h 20'));
  });
}
