import 'social_credential.dart';
import 'social_provider.dart';

abstract class SocialAuthenticator {
  Future<SocialCredential?> authenticate(SocialProvider provider);
}
