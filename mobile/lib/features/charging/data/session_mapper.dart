import '../domain/charging_session.dart';
import '../domain/metering.dart';
import '../domain/session_state.dart';

abstract final class SessionMapper {
  static ChargingSession session(Map<String, dynamic> json) {
    final metering = json['metering'] as Map<String, dynamic>?;
    return ChargingSession(
      id: json['id'] as String,
      stationId: json['stationId'] as String,
      connectorId: json['connectorId'] as int,
      state: SessionState.parse(json['state'] as String),
      createdAt: DateTime.parse(json['createdAt'] as String),
      failureCode: json['failureCode'] as String?,
      energyKwh: (json['energyKwh'] as num?)?.toDouble(),
      costMinor: (json['costMinor'] as num?)?.toInt(),
      currency: json['currency'] as String?,
      currencyDecimals: json['currencyDecimals'] as int?,
      metering: metering == null ? null : _metering(metering),
    );
  }

  static Metering _metering(Map<String, dynamic> json) => Metering(
    totalKwh: (json['totalKwh'] as num?)?.toDouble(),
    powerKw: (json['powerKw'] as num?)?.toDouble(),
    stateOfChargePercent: (json['stateOfChargePercent'] as num?)?.toDouble(),
    durationSeconds: (json['durationSeconds'] as num?)?.toInt(),
  );
}
