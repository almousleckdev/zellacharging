import 'dart:math';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../wallet/presentation/wallet_providers.dart';
import '../../domain/charge_plan.dart';
import '../../domain/quote_request.dart';
import '../session_providers.dart';

ChargePlan watchChargePlan(
  WidgetRef ref, {
  required String stationId,
  required int? connector,
}) {
  final wallet = ref.watch(walletProvider).value;
  final amount = wallet?.freeCharging ?? false
      ? ChargePlan.maximumChargeMinor
      : min(wallet?.availableMinor ?? 0, ChargePlan.maximumChargeMinor);
  final request = connector == null || amount < ChargePlan.minimumChargeMinor
      ? null
      : QuoteRequest(stationId, connector, amount);
  return ChargePlan.of(
    amountMinor: amount,
    wallet: wallet,
    server: request == null
        ? null
        : ref.watch(quoteProvider(request)).asData?.value,
  );
}
