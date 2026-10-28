import '../../../core/format/msisdn.dart';
import 'mobile_operator.dart';

abstract final class OperatorHint {
  static const _airtel = {'74', '76', '77'};
  static const _moov = {'60', '62', '65', '66'};

  static MobileOperator resolve(String? msisdn, MobileOperator chosen) =>
      forNumber(msisdn) ?? chosen;

  static MobileOperator? forNumber(String? msisdn) {
    final national = Msisdn.national(msisdn);
    if (national.length < 2) return null;
    final prefix = national.substring(0, 2);
    if (_airtel.contains(prefix)) return MobileOperator.airtel;
    if (_moov.contains(prefix)) return MobileOperator.moov;
    return null;
  }
}
