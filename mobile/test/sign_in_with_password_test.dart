import 'package:flutter_test/flutter_test.dart';
import 'package:zella/shared/otp_delivery.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/features/auth/domain/auth_repository.dart';
import 'package:zella/features/auth/domain/contact_channel.dart';
import 'package:zella/features/auth/domain/registration_data.dart';
import 'package:zella/features/auth/domain/sign_in_with_password.dart';
import 'package:zella/features/auth/domain/social_credential.dart';
import 'package:zella/features/auth/domain/verification_target.dart';

class _Auth implements AuthRepository {
  _Auth(this.loginError);

  final ApiError? loginError;
  final resent = <String>[];

  @override
  Future<void> login({required String identifier, required String password}) {
    final error = loginError;
    return error == null ? Future.value() : Future.error(error);
  }

  @override
  Future<void> resendCode(
    VerificationTarget target, {
    OtpDelivery? delivery,
  }) async => resent.add(target.value);

  @override
  Future<void> forgotPassword(String identifier, {OtpDelivery? delivery}) =>
      throw UnimplementedError();

  @override
  Future<VerificationTarget> register(RegistrationData data) =>
      throw UnimplementedError();

  @override
  Future<void> resetPassword({
    required String identifier,
    required String code,
    required String newPassword,
  }) => throw UnimplementedError();

  @override
  Future<void> socialLogin(SocialCredential credential) =>
      throw UnimplementedError();

  @override
  Future<void> verify(VerificationTarget target, String code) =>
      throw UnimplementedError();
}

ApiError _error(String code) => ApiError.fromCode(code);

void main() {
  test('a verified account signs in without resending a code', () async {
    final auth = _Auth(null);

    final pending = await SignInWithPassword(auth)(
      identifier: 'a@b.com',
      password: 'secret',
    );

    expect(pending, isNull);
    expect(auth.resent, isEmpty);
  });

  test(
    'an unverified account is handed back as the target to verify',
    () async {
      final auth = _Auth(_error('ACCOUNT_NOT_VERIFIED'));

      final email = await SignInWithPassword(auth)(
        identifier: 'a@b.com',
        password: 'secret',
      );
      final phone = await SignInWithPassword(auth)(
        identifier: '+24162820745',
        password: 'secret',
      );

      expect(email?.channel, ContactChannel.email);
      expect(email?.value, 'a@b.com');
      expect(phone?.channel, ContactChannel.phone);
      expect(auth.resent, isEmpty);
    },
  );

  test('any other error is passed through without a resend', () async {
    final auth = _Auth(_error('INVALID_CREDENTIALS'));

    await expectLater(
      SignInWithPassword(auth)(identifier: 'a@b.com', password: 'x'),
      throwsA(isA<ApiError>()),
    );
    expect(auth.resent, isEmpty);
  });
}
