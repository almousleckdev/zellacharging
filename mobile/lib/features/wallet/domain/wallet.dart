class Wallet {
  const Wallet({
    required this.balanceMinor,
    required this.reservedMinor,
    required this.availableMinor,
    required this.currency,
    required this.decimals,
  });

  final int balanceMinor;
  final int reservedMinor;
  final int availableMinor;
  final String currency;
  final int decimals;
}
