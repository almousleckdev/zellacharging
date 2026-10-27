class WalletEntry {
  const WalletEntry({
    required this.id,
    required this.type,
    required this.amountMinor,
    required this.createdAt,
  });

  static const _shown = {
    'TOPUP',
    'CAPTURE',
    'WITHDRAWAL',
    'WITHDRAWAL_REVERSAL',
  };

  final String id;
  final String type;
  final int amountMinor;
  final DateTime createdAt;

  bool get visible => _shown.contains(type);

  bool get credit => type == 'TOPUP' || type == 'WITHDRAWAL_REVERSAL';
}
