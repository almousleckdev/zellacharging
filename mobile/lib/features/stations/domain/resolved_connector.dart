import 'station.dart';
import 'station_connector.dart';

class ResolvedConnector {
  const ResolvedConnector({required this.station, required this.connector});

  final Station station;
  final StationConnector connector;
}
