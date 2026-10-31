import 'registration_data.dart';
import 'social_credential.dart';
import 'verification_target.dart';

abstract class AuthRepository {
  Future<VerificationTarget> register(RegistrationData data);
  Future<void> verify(VerificationTarget target, String code);
  Future<void> resendCode(VerificationTarget target);
  Future<void> forgotPassword(String email);
  Future<void> resetPassword({
    required String email,
    required String code,
    required String newPassword,
  });
  Future<void> login({required String identifier, required String password});
  Future<void> socialLogin(SocialCredential credential);
}
