import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/core/validation/validators.dart';

void main() {
  test('otp accepts exactly six digits', () {
    expect(Validators.otp('123456'), isNull);
    expect(Validators.otp('12345'), isNotNull);
    expect(Validators.otp('12345a'), isNotNull);
  });

  test('email is required and must be well formed', () {
    expect(Validators.email(null), Strings.required);
    expect(Validators.email('  '), Strings.required);
    expect(Validators.email('nope'), Strings.invalidEmail);
    expect(Validators.email('a@b.co'), isNull);
    expect(Validators.email(' a@b.co '), isNull);
  });

  test('optional phone accepts blank and rejects letters', () {
    expect(Validators.optionalPhone(null), isNull);
    expect(Validators.optionalPhone(''), isNull);
    expect(Validators.optionalPhone('+241 77 12 34 56'), isNull);
    expect(Validators.optionalPhone('077123456'), isNull);
    expect(Validators.optionalPhone('abc'), Strings.invalidPhone);
  });

  test('password needs eight characters', () {
    expect(Validators.password('1234567'), isNotNull);
    expect(Validators.password('12345678'), isNull);
  });

  test('mobile money number accepts international and local formats', () {
    expect(Validators.mobileMoneyNumber('+24177123456'), isNull);
    expect(Validators.mobileMoneyNumber('077123456'), isNull);
    expect(Validators.mobileMoneyNumber('077 12 34 56'), isNull);
    expect(Validators.mobileMoneyNumber('77123456'), isNull);
    expect(Validators.mobileMoneyNumber('0771234'), isNotNull);
    expect(Validators.mobileMoneyNumber(''), isNotNull);
  });
}
