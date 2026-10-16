import '../../wallet/domain/mobile_operator.dart';

class ChargePayment {
  const ChargePayment({required this.operator, required this.msisdn});

  final MobileOperator operator;
  final String msisdn;
}

class ChargeRequest {
  const ChargeRequest({
    required this.stationId,
    required this.connectorId,
    required this.amountMinor,
    required this.attemptKey,
    this.payment,
  });

  final String stationId;
  final int connectorId;
  final int amountMinor;
  final String attemptKey;
  final ChargePayment? payment;
}
