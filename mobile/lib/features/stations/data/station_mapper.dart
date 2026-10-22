import '../domain/station.dart';
import '../domain/station_connector.dart';

abstract final class StationMapper {
  static StationConnector connector(Map<String, dynamic> json) =>
      StationConnector(
        connectorId: json['connectorId'] as int,
        status: json['status'] as String? ?? 'Unknown',
        available: json['available'] as bool? ?? false,
        connectorType: json['connectorType'] as String?,
        maxPowerKw: (json['maxPowerKw'] as num?)?.toDouble(),
      );

  static Station station(Map<String, dynamic> json) => Station(
    id: json['id'] as String,
    online: json['online'] as bool? ?? false,
    availableConnectors: (json['availableConnectors'] as num).toInt(),
    totalConnectors: json['totalConnectors'] as int,
    distanceKm: (json['distanceKm'] as num?)?.toDouble(),
    latitude: (json['latitude'] as num?)?.toDouble(),
    longitude: (json['longitude'] as num?)?.toDouble(),
    connectors: (json['connectors'] as List<dynamic>)
        .map((c) => connector(c as Map<String, dynamic>))
        .toList(),
  );
}
