import 'package:flutter_test/flutter_test.dart';
import 'package:zella/shared/otp_delivery.dart';
import 'package:zella/features/auth/domain/auth_repository.dart';
import 'package:zella/features/auth/domain/registration_data.dart';
import 'package:zella/features/auth/domain/sign_in_with_social.dart';
import 'package:zella/features/auth/domain/social_credential.dart';
import 'package:zella/features/auth/domain/social_provider.dart';
import 'package:zella/features/auth/domain/verification_target.dart';

import 'ui/ui_fakes.dart';

class _Auth implements AuthRepository {
  final signedIn = <SocialCredential>[];

  @override
  Future<void> socialLogin(SocialCredential credential) async =>
      signedIn.add(credential);

  @override
  Future<void> forgotPassword(String identifier, {OtpDelivery? delivery}) =>
      throw UnimplementedError();

  @override
  Future<void> login({required String identifier, required String password}) =>
      throw UnimplementedError();

  @override
  Future<VerificationTarget> register(RegistrationData data) =>
      throw UnimplementedError();

  @override
  Future<void> resendCode(VerificationTarget target, {OtpDelivery? delivery}) =>
      throw UnimplementedError();

  @override
  Future<void> resetPassword({
    required String identifier,
    required String code,
    required String newPassword,
  }) => throw UnimplementedError();

  @override
  Future<void> verify(VerificationTarget target, String code) =>
      throw UnimplementedError();
}

void main() {
  test('a provider sign-in sends the credential to the server', () async {
    final auth = _Auth();
    final authenticator = FakeSocialAuthenticator();

    final signedIn = await SignInWithSocial(authenticator, auth)(
      SocialProvider.apple,
    );

    expect(signedIn, isTrue);
    expect(authenticator.asked, [SocialProvider.apple]);
    expect(auth.signedIn.single.provider.apiName, 'APPLE');
    expect(auth.signedIn.single.toJson()['idToken'], 'token');
  });

  test('cancelling the provider sheet signs nobody in', () async {
    final auth = _Auth();

    final signedIn = await SignInWithSocial(
      FakeSocialAuthenticator(cancel: true),
      auth,
    )(SocialProvider.google);

    expect(signedIn, isFalse);
    expect(auth.signedIn, isEmpty);
  });
}
