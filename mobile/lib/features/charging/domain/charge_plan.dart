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
  });

  static const minimumTopupMinor = 100;
  static const minimumChargeMinor = 100;
  static const maximumChargeMinor = 200000;

  factory ChargePlan.of({
    required int amountMinor,
    Wallet? wallet,
    SessionQuote? server,
  }) {
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
      );
    }
    final available = wallet?.availableMinor ?? 0;
    return ChargePlan(
      amountMinor: amountMinor,
      shortfallMinor: wallet == null
          ? amountMinor
          : (amountMinor - available).clamp(0, amountMinor),
      availableMinor: available,
      currency: wallet?.currency ?? '',
      decimals: wallet?.decimals ?? 0,
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

  bool get needsPayment => shortfallMinor > 0;

  bool get belowMinimum => amountMinor < (minimumMinor ?? minimumChargeMinor);

  int get fromWalletMinor => amountMinor - shortfallMinor;

  int get payNowMinor => !needsPayment
      ? 0
      : shortfallMinor < minimumTopupMinor
      ? minimumTopupMinor
      : shortfallMinor;

  bool get hasFees => vatPercent > 0 || serviceFeePercent > 0;
}
