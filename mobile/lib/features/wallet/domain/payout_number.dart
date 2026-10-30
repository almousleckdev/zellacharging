import 'mobile_operator.dart';

class PayoutNumber {
  const PayoutNumber({required this.msisdn, required this.operator});

  final String msisdn;
  final MobileOperator operator;
}
