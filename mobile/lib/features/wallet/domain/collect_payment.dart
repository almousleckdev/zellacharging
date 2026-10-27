import '../../../core/network/api_error.dart';
import 'mobile_operator.dart';
import 'payment_stage.dart';
import 'topup_result.dart';
import 'wallet_repository.dart';

class CollectPayment {
  CollectPayment(
    this._wallets, {
    this.pollInterval = const Duration(seconds: 3),
    this.maxPolls = 20,
  });

  final WalletRepository _wallets;
  final Duration pollInterval;
  final int maxPolls;

  Future<TopupResult> call({
    required int amountMinor,
    required String msisdn,
    required MobileOperator operator,
    required String idempotencyKey,
    void Function(PaymentStage stage)? onStage,
  }) async {
    onStage?.call(PaymentStage.requesting);
    var result = await _wallets.topUp(
      amountMinor: amountMinor,
      msisdn: msisdn,
      operator: operator,
      idempotencyKey: idempotencyKey,
    );
    if (!result.succeeded && !result.failed) {
      onStage?.call(PaymentStage.awaitingApproval);
    }
    for (var poll = 0; !result.succeeded && !result.failed; poll++) {
      if (poll >= maxPolls) break;
      await Future<void>.delayed(pollInterval);
      result = await _wallets.topupStatus(result.id);
    }
    if (!result.succeeded) {
      throw ApiError.fromCode(
        result.failed ? 'PAYMENT_NOT_COMPLETED' : 'PAYMENT_VERIFYING',
      );
    }
    onStage?.call(PaymentStage.confirmed);
    return result;
  }
}
