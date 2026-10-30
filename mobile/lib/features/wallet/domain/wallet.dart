import 'mobile_operator.dart';
import 'payout_number.dart';

class Wallet {
  const Wallet({
    required this.balanceMinor,
    required this.reservedMinor,
    required this.availableMinor,
    required this.currency,
    required this.decimals,
    this.withdrawalsEnabled = false,
    this.payoutNumber,
    this.payoutOperator,
    this.payoutNumbers = const [],
  });

  final int balanceMinor;
  final int reservedMinor;
  final int availableMinor;
  final String currency;
  final int decimals;
  final bool withdrawalsEnabled;
  final String? payoutNumber;
  final MobileOperator? payoutOperator;
  final List<PayoutNumber> payoutNumbers;
}
