import '../../wallet/domain/wallet.dart';

class ChargeQuote {
  const ChargeQuote({required this.amountMinor, required this.shortfallMinor});

  factory ChargeQuote.of(int amountMinor, Wallet? wallet) => ChargeQuote(
    amountMinor: amountMinor,
    shortfallMinor: wallet == null
        ? amountMinor
        : (amountMinor - wallet.availableMinor).clamp(0, amountMinor),
  );

  final int amountMinor;
  final int shortfallMinor;

  bool get needsPayment => shortfallMinor > 0;
}
