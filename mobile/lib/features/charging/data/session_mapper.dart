import '../domain/charging_session.dart';
import '../domain/estimated_cost.dart';
import '../domain/metering.dart';
import '../domain/session_state.dart';

abstract final class SessionMapper {
  static ChargingSession session(Map<String, dynamic> json) {
    final metering = json['metering'] as Map<String, dynamic>?;
    final estimate = json['estimatedCost'] as Map<String, dynamic>?;
    return ChargingSession(
      id: json['id'] as String,
      stationId: json['stationId'] as String,
      connectorId: json['connectorId'] as int,
      state: SessionState.parse(json['state'] as String),
      createdAt: DateTime.parse(json['createdAt'] as String),
      failureCode: json['failureCode'] as String?,
      startedAt: _time(json['startedAt']),
      stoppedAt: _time(json['stoppedAt']),
      energyKwh: (json['energyKwh'] as num?)?.toDouble(),
      costMinor: (json['costMinor'] as num?)?.toInt(),
      currency: json['currency'] as String?,
      currencyDecimals: json['currencyDecimals'] as int?,
      metering: metering == null ? null : _metering(metering),
      batteryPercent: (json['batteryPercent'] as num?)?.toDouble(),
      estimatedCost: estimate == null ? null : _estimate(estimate),
    );
  }

  static DateTime? _time(Object? value) =>
      value == null ? null : DateTime.parse(value as String);

  static Metering _metering(Map<String, dynamic> json) => Metering(
    totalKwh: (json['totalKwh'] as num?)?.toDouble(),
    powerKw: (json['powerKw'] as num?)?.toDouble(),
    durationSeconds: (json['durationSeconds'] as num?)?.toInt(),
    voltage: (json['voltage'] as num?)?.toDouble(),
    currentAmpere: (json['currentAmpere'] as num?)?.toDouble(),
    sampledAt: _time(json['sampledAt']),
  );

  static EstimatedCost _estimate(Map<String, dynamic> json) => EstimatedCost(
    minor: (json['minor'] as num).toInt(),
    currency: json['currency'] as String,
    decimals: (json['decimals'] as num).toInt(),
  );
}
