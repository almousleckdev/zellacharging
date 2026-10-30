import 'auth_repository.dart';
import 'social_authenticator.dart';
import 'social_provider.dart';

class SignInWithSocial {
  const SignInWithSocial(this._authenticator, this._repository);

  final SocialAuthenticator _authenticator;
  final AuthRepository _repository;

  Future<bool> call(SocialProvider provider) async {
    final credential = await _authenticator.authenticate(provider);
    if (credential == null) return false;
    await _repository.socialLogin(credential);
    return true;
  }
}
