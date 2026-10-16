class StationConnector {
  const StationConnector({
    required this.connectorId,
    required this.status,
    required this.available,
    this.connectorType,
    this.maxPowerKw,
  });

  final int connectorId;
  final String status;
  final bool available;
  final String? connectorType;
  final double? maxPowerKw;
}
