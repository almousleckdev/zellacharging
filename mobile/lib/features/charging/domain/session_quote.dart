class SessionQuote {
  const SessionQuote({
    required this.amountMinor,
    required this.currency,
    required this.decimals,
    required this.availableMinor,
    required this.shortfallMinor,
    required this.minimumMinor,
    required this.maximumMinor,
    required this.vatPercent,
    required this.serviceFeePercent,
    this.estimatedEnergyKwh,
  });

  final int amountMinor;
  final String currency;
  final int decimals;
  final int availableMinor;
  final int shortfallMinor;
  final int minimumMinor;
  final int maximumMinor;
  final double vatPercent;
  final double serviceFeePercent;
  final double? estimatedEnergyKwh;
}
