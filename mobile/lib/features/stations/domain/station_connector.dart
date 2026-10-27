class StationConnector {
  const StationConnector({
    required this.connectorId,
    required this.status,
    required this.available,
    bool? startable,
    this.connectorType,
    this.maxPowerKw,
  }) : startable = startable ?? available;

  final int connectorId;
  final String status;
  final bool available;
  final bool startable;
  final String? connectorType;
  final double? maxPowerKw;
}
