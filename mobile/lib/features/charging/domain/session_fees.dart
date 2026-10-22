class SessionFees {
  const SessionFees({
    required this.electricMinor,
    required this.vatMinor,
    required this.serviceMinor,
    required this.totalMinor,
    required this.currency,
    required this.decimals,
  });

  final int electricMinor;
  final int vatMinor;
  final int serviceMinor;
  final int totalMinor;
  final String currency;
  final int decimals;
}
