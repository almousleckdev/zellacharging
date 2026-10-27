class QuoteRequest {
  const QuoteRequest(this.stationId, this.connectorId, this.amountMinor);

  final String stationId;
  final int connectorId;
  final int amountMinor;

  @override
  bool operator ==(Object other) =>
      other is QuoteRequest &&
      other.stationId == stationId &&
      other.connectorId == connectorId &&
      other.amountMinor == amountMinor;

  @override
  int get hashCode => Object.hash(stationId, connectorId, amountMinor);
}
