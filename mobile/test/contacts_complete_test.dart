import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/profile/domain/profile.dart';
import 'package:zella/features/profile/presentation/contact_step.dart';

Profile _profile({
  String? email = 'a@b.co',
  String? phone = '+24177123456',
  bool emailVerified = true,
  bool phoneVerified = true,
}) => Profile(
  firstName: 'A',
  lastName: 'B',
  email: email,
  phone: phone,
  emailVerified: emailVerified,
  phoneVerified: phoneVerified,
);

void main() {
  test('a profile with both contacts verified is complete', () {
    expect(_profile().contactsComplete, isTrue);
    expect(ContactStep.next(_profile()), isNull);
  });

  test('a social account without a phone must add it first', () {
    final profile = _profile(phone: null, phoneVerified: false);
    expect(profile.contactsComplete, isFalse);
    expect(ContactStep.next(profile), ContactStep.addPhone);
  });

  test('an unverified phone is verified before the email', () {
    final profile = _profile(phoneVerified: false, emailVerified: false);
    expect(ContactStep.next(profile), ContactStep.verifyPhone);
  });

  test('a missing email is added once the phone is verified', () {
    final profile = _profile(email: null, emailVerified: false);
    expect(ContactStep.next(profile), ContactStep.addEmail);
  });

  test('an unverified email is the last step', () {
    final profile = _profile(emailVerified: false);
    expect(profile.contactsComplete, isFalse);
    expect(ContactStep.next(profile), ContactStep.verifyEmail);
  });

  test('existing contacts are verified, missing ones are added', () {
    expect(ContactStep.verifyPhone.existing, isTrue);
    expect(ContactStep.addPhone.existing, isFalse);
  });
}
