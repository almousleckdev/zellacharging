import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/format/phone_mask.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/features/wallet/domain/operator_hint.dart';

void main() {
  test('Moov and Airtel numbers are recognised from their usual prefixes', () {
    expect(OperatorHint.forNumber('+24162820745'), MobileOperator.moov);
    expect(OperatorHint.forNumber('+241 66 12 34 56'), MobileOperator.moov);
    expect(OperatorHint.forNumber('+24177123456'), MobileOperator.airtel);
    expect(OperatorHint.forNumber('+24174000000'), MobileOperator.airtel);
  });

  test('an unknown prefix or a half-typed number gives no hint', () {
    expect(OperatorHint.forNumber('+24111111111'), isNull);
    expect(OperatorHint.forNumber('+2416'), isNull);
    expect(OperatorHint.forNumber('+22370000000'), isNull);
    expect(OperatorHint.forNumber(''), isNull);
    expect(OperatorHint.forNumber(null), isNull);
  });

  test('a phone number is masked in the middle', () {
    expect(PhoneMask.of('+24162820745'), '+24162••••45');
    expect(PhoneMask.of('123'), '123');
    expect(PhoneMask.of(null), '');
  });
}
