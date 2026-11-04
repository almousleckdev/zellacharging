import '../../../core/network/api_error.dart';
import '../../wallet/domain/collect_payment.dart';
import '../../wallet/domain/payment_stage.dart';
import '../../wallet/domain/wallet_repository.dart';
import 'charge_quote.dart';
import 'charge_request.dart';
import 'charge_stage.dart';
import 'charging_session.dart';
import 'session_repository.dart';
import 'session_state.dart';

class StartCharge {
  StartCharge(
    this._wallets,
    this._sessions, {
    Duration pollInterval = const Duration(seconds: 3),
    int maxPolls = 20,
    this.minimumTopupMinor = 200,
  }) : _payment = CollectPayment(
         _wallets,
         pollInterval: pollInterval,
         maxPolls: maxPolls,
       );

  final WalletRepository _wallets;
  final SessionRepository _sessions;
  final CollectPayment _payment;
  final int minimumTopupMinor;

  Future<ChargingSession> call(
    ChargeRequest request, {
    void Function(ChargeStage stage)? onStage,
  }) async {
    final wallet = await _wallets.fetch();
    final quote = ChargeQuote.of(request.amountMinor, wallet);
    if (quote.needsPayment) {
      await _collect(request, quote.shortfallMinor, onStage);
    }
    onStage?.call(ChargeStage.startingCharge);
    final session = await _sessions.start(
      request.stationId,
      request.connectorId,
      request.amountMinor,
      idempotencyKey: request.attemptKey,
    );
    if (session.state == SessionState.failed) {
      throw ApiError.fromCode(session.failureCode);
    }
    return session;
  }

  Future<void> _collect(
    ChargeRequest request,
    int shortfall,
    void Function(ChargeStage stage)? onStage,
  ) async {
    final payment = request.payment;
    if (payment == null) throw ApiError.fromCode('INSUFFICIENT_FUNDS');
    await _payment(
      amountMinor: shortfall < minimumTopupMinor
          ? minimumTopupMinor
          : shortfall,
      msisdn: payment.msisdn,
      operator: payment.operator,
      idempotencyKey: request.attemptKey,
      onStage: (stage) => onStage?.call(_charge(stage)),
    );
  }

  ChargeStage _charge(PaymentStage stage) => switch (stage) {
    PaymentStage.requesting => ChargeStage.requestingPayment,
    PaymentStage.awaitingApproval => ChargeStage.awaitingApproval,
    PaymentStage.confirmed => ChargeStage.startingCharge,
  };
}
