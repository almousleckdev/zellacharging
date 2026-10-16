import 'station.dart';

abstract final class StationFilter {
  static List<Station> byQuery(List<Station> stations, String query) {
    final needle = query.trim().toLowerCase();
    if (needle.isEmpty) return stations;
    return stations
        .where((station) => station.id.toLowerCase().contains(needle))
        .toList();
  }
}
