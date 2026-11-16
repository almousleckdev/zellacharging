import '../../../core/network/api_error.dart';
import 'auth_repository.dart';
import 'contact_channel.dart';
import 'verification_target.dart';

class SignInWithPassword {
  const SignInWithPassword(this._repository);

  static const unverifiedCode = 'ACCOUNT_NOT_VERIFIED';
  static const passwordSetupCode = 'PASSWORD_SETUP_REQUIRED';

  final AuthRepository _repository;

  Future<VerificationTarget?> call({
    required String identifier,
    required String password,
  }) async {
    try {
      await _repository.login(identifier: identifier, password: password);
      return null;
    } on ApiError catch (error) {
      if (error.code != unverifiedCode) rethrow;
      return VerificationTarget(
        channel: ContactChannel.of(identifier),
        value: identifier,
      );
    }
  }
}
