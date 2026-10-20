import 'estimated_cost.dart';
import 'metering.dart';
import 'session_state.dart';

class ChargingSession {
  const ChargingSession({
    required this.id,
    required this.stationId,
    required this.connectorId,
    required this.state,
    required this.createdAt,
    this.failureCode,
    this.energyKwh,
    this.costMinor,
    this.currency,
    this.currencyDecimals,
    this.metering,
    this.estimatedCost,
  });

  final String id;
  final String stationId;
  final int connectorId;
  final SessionState state;
  final DateTime createdAt;
  final String? failureCode;
  final double? energyKwh;
  final int? costMinor;
  final String? currency;
  final int? currencyDecimals;
  final Metering? metering;
  final EstimatedCost? estimatedCost;

  double? get shownEnergyKwh => metering?.totalKwh ?? energyKwh;
}
