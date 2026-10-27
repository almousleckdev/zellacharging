import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_error.dart';
import '../domain/collect_payment.dart';
import '../domain/mobile_operator.dart';
import '../domain/payment_stage.dart';
import 'topup_flow_state.dart';
import 'wallet_providers.dart';

final collectPaymentProvider = Provider<CollectPayment>(
  (ref) => CollectPayment(ref.watch(walletRepositoryProvider)),
);

final topupFlowProvider =
    NotifierProvider.autoDispose<TopupFlowController, TopupFlowState>(
      TopupFlowController.new,
    );

class TopupFlowController extends Notifier<TopupFlowState> {
  @override
  TopupFlowState build() => const TopupFlowState.idle();

  Future<void> pay({
    required int amountMinor,
    required String msisdn,
    required MobileOperator operator,
    required String idempotencyKey,
  }) async {
    state = const TopupFlowState.running(PaymentStage.requesting);
    try {
      await ref
          .read(collectPaymentProvider)
          .call(
            amountMinor: amountMinor,
            msisdn: msisdn,
            operator: operator,
            idempotencyKey: idempotencyKey,
            onStage: _advance,
          );
      if (ref.mounted) state = TopupFlowState.done(amountMinor);
    } on ApiError catch (error) {
      if (ref.mounted) state = TopupFlowState.failed(error);
    } finally {
      if (ref.mounted) ref.invalidate(walletProvider);
    }
  }

  void reset() => state = const TopupFlowState.idle();

  void _advance(PaymentStage stage) {
    if (ref.mounted) state = TopupFlowState.running(stage);
  }
}
