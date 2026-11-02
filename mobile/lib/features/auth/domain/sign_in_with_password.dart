import '../../../core/network/api_error.dart';
import 'auth_repository.dart';
import 'contact_channel.dart';
import 'verification_target.dart';

class SignInWithPassword {
  const SignInWithPassword(this._repository);

  static const unverifiedCode = 'ACCOUNT_NOT_VERIFIED';

  final AuthRepository _repository;

  Future<VerificationTarget?> call({
    required String identifier,
    required String password,
  }) async {
    try {
      await _repository.login(identifier: identifier, password: password);
      return null;
    } on ApiError catch (error) {
      if (error.code != unverifiedCode || !identifier.contains('@')) rethrow;
      final target = VerificationTarget(
        channel: ContactChannel.email,
        value: identifier,
      );
      await _repository.resendCode(target);
      return target;
    }
  }
}
