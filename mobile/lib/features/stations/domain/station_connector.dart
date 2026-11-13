class StationConnector {
  const StationConnector({
    required this.connectorId,
    required this.status,
    required this.available,
    bool? startable,
    this.connectorType,
    this.maxPowerKw,
    this.freeInMinutes,
  }) : startable = startable ?? available;

  final int connectorId;
  final String status;
  final bool available;
  final bool startable;
  final String? connectorType;
  final double? maxPowerKw;
  final int? freeInMinutes;
}
