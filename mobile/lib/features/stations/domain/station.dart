import 'station_connector.dart';

class Station {
  const Station({
    required this.id,
    required this.online,
    required this.availableConnectors,
    required this.totalConnectors,
    required this.connectors,
    this.distanceKm,
    this.latitude,
    this.longitude,
    this.isoCode,
    this.vendor,
    this.model,
    this.lastSeenAt,
  });

  final String id;
  final bool online;
  final int availableConnectors;
  final int totalConnectors;
  final double? distanceKm;
  final double? latitude;
  final double? longitude;
  final String? isoCode;
  final String? vendor;
  final String? model;
  final DateTime? lastSeenAt;
  final List<StationConnector> connectors;

  double? get maxPowerKw => connectors
      .map((c) => c.maxPowerKw)
      .whereType<double>()
      .fold<double?>(null, (best, p) => best == null || p > best ? p : best);
}
