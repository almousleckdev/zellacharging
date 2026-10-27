import '../../../core/network/api_error.dart';
import 'mobile_operator.dart';
import 'wallet_repository.dart';
import 'withdraw_stage.dart';
import 'withdrawal_result.dart';

class WithdrawMoney {
  WithdrawMoney(
    this._wallets, {
    this.pollInterval = const Duration(seconds: 3),
    this.maxPolls = 20,
  });

  final WalletRepository _wallets;
  final Duration pollInterval;
  final int maxPolls;

  Future<WithdrawalResult> call({
    required int amountMinor,
    required MobileOperator operator,
    required String password,
    required String idempotencyKey,
    void Function(WithdrawStage stage)? onStage,
  }) async {
    onStage?.call(WithdrawStage.requesting);
    var result = await _wallets.withdraw(
      amountMinor: amountMinor,
      operator: operator,
      password: password,
      idempotencyKey: idempotencyKey,
    );
    if (!result.settled) onStage?.call(WithdrawStage.processing);
    for (var poll = 0; !result.settled; poll++) {
      if (poll >= maxPolls) break;
      await Future<void>.delayed(pollInterval);
      result = await _wallets.withdrawalStatus(result.id);
    }
    if (result.paid) {
      onStage?.call(WithdrawStage.completed);
      return result;
    }
    throw ApiError.fromCode(
      result.failed ? 'WITHDRAWAL_FAILED' : 'WITHDRAWAL_VERIFYING',
    );
  }
}
