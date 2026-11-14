import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../wallet/presentation/wallet_providers.dart';
import '../../domain/charge_plan.dart';
import '../../domain/quote_request.dart';
import '../session_providers.dart';

ChargePlan watchChargePlan(
  WidgetRef ref, {
  required String stationId,
  required int? connector,
  required int? entered,
}) {
  final wallet = ref.watch(walletProvider).value;
  final free = wallet?.freeCharging ?? false;
  final amount = free ? ChargePlan.maximumChargeMinor : entered ?? 0;
  final quotable =
      amount >= ChargePlan.minimumChargeMinor &&
      amount <= ChargePlan.maximumChargeMinor;
  final request = connector == null || !quotable
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
