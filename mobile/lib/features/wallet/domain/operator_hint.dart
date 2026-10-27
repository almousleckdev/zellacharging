import 'mobile_operator.dart';

abstract final class OperatorHint {
  static const _airtel = {'74', '76', '77'};
  static const _moov = {'60', '62', '65', '66'};

  static MobileOperator resolve(String? msisdn, MobileOperator chosen) =>
      forNumber(msisdn) ?? chosen;

  static MobileOperator? forNumber(String? msisdn) {
    final digits = (msisdn ?? '').replaceAll(RegExp(r'\D'), '');
    if (!digits.startsWith('241') || digits.length < 5) return null;
    final prefix = digits.substring(3, 5);
    if (_airtel.contains(prefix)) return MobileOperator.airtel;
    if (_moov.contains(prefix)) return MobileOperator.moov;
    return null;
  }
}
