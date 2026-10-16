import 'registration_data.dart';
import 'verification_target.dart';

abstract class AuthRepository {
  Future<VerificationTarget> register(RegistrationData data);
  Future<void> verify(VerificationTarget target, String code);
  Future<void> resendCode(VerificationTarget target);
  Future<void> login({required String identifier, required String password});
}
