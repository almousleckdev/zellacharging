import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_error.dart';
import '../domain/mobile_operator.dart';
import '../domain/withdraw_money.dart';
import '../domain/withdraw_stage.dart';
import 'wallet_providers.dart';
import 'withdraw_flow_state.dart';

final withdrawMoneyProvider = Provider<WithdrawMoney>(
  (ref) => WithdrawMoney(ref.watch(walletRepositoryProvider)),
);

final withdrawFlowProvider =
    NotifierProvider.autoDispose<WithdrawFlowController, WithdrawFlowState>(
      WithdrawFlowController.new,
    );

class WithdrawFlowController extends Notifier<WithdrawFlowState> {
  @override
  WithdrawFlowState build() => const WithdrawFlowState.idle();

  Future<void> withdraw({
    required int amountMinor,
    required MobileOperator operator,
    required String msisdn,
    required String password,
    required String idempotencyKey,
  }) async {
    state = const WithdrawFlowState.running(WithdrawStage.requesting);
    try {
      final result = await ref
          .read(withdrawMoneyProvider)
          .call(
            amountMinor: amountMinor,
            operator: operator,
            msisdn: msisdn,
            password: password,
            idempotencyKey: idempotencyKey,
            onStage: _advance,
          );
      if (ref.mounted) {
        state = WithdrawFlowState.done(
          amountMinor,
          awaitingApproval: result.awaitingApproval,
        );
      }
    } on ApiError catch (error) {
      if (ref.mounted) state = WithdrawFlowState.failed(error);
    } finally {
      if (ref.mounted) {
        ref.invalidate(walletProvider);
        ref.invalidate(walletEntriesProvider);
      }
    }
  }

  void reset() => state = const WithdrawFlowState.idle();

  void _advance(WithdrawStage stage) {
    if (ref.mounted) state = WithdrawFlowState.running(stage);
  }
}
