import '../../../core/network/api_error.dart';
import '../../wallet/domain/wallet_repository.dart';
import 'charge_quote.dart';
import 'charge_request.dart';
import 'charging_session.dart';
import 'session_repository.dart';

class StartCharge {
  StartCharge(
    this._wallets,
    this._sessions, {
    this.pollInterval = const Duration(seconds: 3),
    this.maxPolls = 20,
    this.minimumTopupMinor = 500,
  });

  final WalletRepository _wallets;
  final SessionRepository _sessions;
  final Duration pollInterval;
  final int maxPolls;
  final int minimumTopupMinor;

  Future<ChargingSession> call(ChargeRequest request) async {
    final wallet = await _wallets.fetch();
    final quote = ChargeQuote.of(request.amountMinor, wallet);
    if (quote.needsPayment) {
      await _collect(request, quote.shortfallMinor);
    }
    return _sessions.start(
      request.stationId,
      request.connectorId,
      request.amountMinor,
    );
  }

  Future<void> _collect(ChargeRequest request, int shortfall) async {
    final payment = request.payment;
    if (payment == null) throw ApiError.fromCode('INSUFFICIENT_FUNDS');
    var result = await _wallets.topUp(
      amountMinor: shortfall < minimumTopupMinor
          ? minimumTopupMinor
          : shortfall,
      msisdn: payment.msisdn,
      operator: payment.operator,
      idempotencyKey: request.attemptKey,
    );
    for (var poll = 0; !result.succeeded && !result.failed; poll++) {
      if (poll >= maxPolls) break;
      await Future<void>.delayed(pollInterval);
      result = await _wallets.topupStatus(result.id);
    }
    if (!result.succeeded) throw ApiError.fromCode('PAYMENT_NOT_COMPLETED');
  }
}
