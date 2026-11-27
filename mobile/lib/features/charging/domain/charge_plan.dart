import '../../wallet/domain/wallet.dart';
import 'session_quote.dart';

class ChargePlan {
  const ChargePlan({
    required this.amountMinor,
    required this.shortfallMinor,
    required this.availableMinor,
    required this.currency,
    required this.decimals,
    this.minimumMinor,
    this.maximumMinor,
    this.vatPercent = 0,
    this.serviceFeePercent = 0,
    this.energyKwh,
    this.freeCharging = false,
    this.walletKnown = true,
  });

  static const minimumTopupMinor = 200;
  static const minimumChargeMinor = 200;
  static const maximumChargeMinor = 200000;

  factory ChargePlan.of({
    required int amountMinor,
    Wallet? wallet,
    SessionQuote? server,
  }) {
    final free = wallet?.freeCharging ?? false;
    if (server != null && server.amountMinor == amountMinor) {
      return ChargePlan(
        amountMinor: amountMinor,
        shortfallMinor: server.shortfallMinor,
        availableMinor: server.availableMinor,
        currency: server.currency,
        decimals: server.decimals,
        minimumMinor: server.minimumMinor,
        maximumMinor: server.maximumMinor,
        vatPercent: server.vatPercent,
        serviceFeePercent: server.serviceFeePercent,
        energyKwh: server.estimatedEnergyKwh,
        freeCharging: free,
        walletKnown: wallet != null,
      );
    }
    final available = wallet?.availableMinor ?? 0;
    return ChargePlan(
      amountMinor: amountMinor,
      shortfallMinor: wallet == null
          ? amountMinor
          : free
          ? 0
          : (amountMinor - available).clamp(0, amountMinor),
      availableMinor: available,
      currency: wallet?.currency ?? '',
      decimals: wallet?.decimals ?? 0,
      freeCharging: free,
      walletKnown: wallet != null,
    );
  }

  final int amountMinor;
  final int shortfallMinor;
  final int availableMinor;
  final String currency;
  final int decimals;
  final int? minimumMinor;
  final int? maximumMinor;
  final double vatPercent;
  final double serviceFeePercent;
  final double? energyKwh;
  final bool freeCharging;
  final bool walletKnown;

  bool get needsPayment => shortfallMinor > 0;

  int get floorMinor => minimumMinor ?? minimumChargeMinor;

  int get ceilingMinor => maximumMinor ?? maximumChargeMinor;

  bool get belowMinimum => amountMinor < floorMinor;

  bool get aboveMaximum => amountMinor > ceilingMinor;

  bool get exceedsBalance => !freeCharging && amountMinor > availableMinor;

  bool get balanceTooLow => !freeCharging && availableMinor < floorMinor;

  bool get needsTopup => balanceTooLow;

  int get fromWalletMinor => amountMinor - shortfallMinor;

  int get payNowMinor => !needsPayment
      ? 0
      : shortfallMinor < minimumTopupMinor
      ? minimumTopupMinor
      : shortfallMinor;

  bool get hasFees => vatPercent > 0 || serviceFeePercent > 0;
}
