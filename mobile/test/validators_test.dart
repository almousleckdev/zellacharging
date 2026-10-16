import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/validation/validators.dart';

void main() {
  test('otp accepts exactly six digits', () {
    expect(Validators.otp('123456'), isNull);
    expect(Validators.otp('12345'), isNotNull);
    expect(Validators.otp('12345a'), isNotNull);
  });

  test('optional email is valid when blank or well formed', () {
    expect(Validators.optionalEmail(''), isNull);
    expect(Validators.optionalEmail('a@b.co'), isNull);
    expect(Validators.optionalEmail('nope'), isNotNull);
  });

  test('password needs eight characters', () {
    expect(Validators.password('1234567'), isNotNull);
    expect(Validators.password('12345678'), isNull);
  });

  test('mobile money number needs a country code', () {
    expect(Validators.mobileMoneyNumber('+24177123456'), isNull);
    expect(Validators.mobileMoneyNumber('077123456'), isNotNull);
    expect(Validators.mobileMoneyNumber(''), isNotNull);
  });
}
