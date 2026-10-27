import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/format/amount_input.dart';
import '../../../wallet/presentation/wallet_providers.dart';
import '../../domain/charge_plan.dart';
import '../../domain/quote_request.dart';
import '../session_providers.dart';

ChargePlan watchChargePlan(
  WidgetRef ref, {
  required String stationId,
  required int? connector,
  required String amountText,
}) {
  final amount = AmountInput.parse(amountText) ?? 0;
  final request = connector == null || amount <= 0
      ? null
      : QuoteRequest(stationId, connector, amount);
  return ChargePlan.of(
    amountMinor: amount,
    wallet: ref.watch(walletProvider).asData?.value,
    server: request == null
        ? null
        : ref.watch(quoteProvider(request)).asData?.value,
  );
}
