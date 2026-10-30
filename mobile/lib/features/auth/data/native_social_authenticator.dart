import 'package:google_sign_in/google_sign_in.dart';
import 'package:sign_in_with_apple/sign_in_with_apple.dart';

import '../../../core/config/social_config.dart';
import '../domain/social_authenticator.dart';
import '../domain/social_credential.dart';
import '../domain/social_provider.dart';

class NativeSocialAuthenticator implements SocialAuthenticator {
  var _googleReady = false;

  @override
  Future<SocialCredential?> authenticate(SocialProvider provider) =>
      switch (provider) {
        SocialProvider.google => _google(),
        SocialProvider.apple => _apple(),
      };

  Future<SocialCredential?> _google() async {
    final google = GoogleSignIn.instance;
    if (!_googleReady) {
      await google.initialize(
        serverClientId: SocialConfig.googleServerClientId,
        clientId: SocialConfig.googleIosClientId,
      );
      _googleReady = true;
    }
    try {
      final account = await google.authenticate();
      final token = account.authentication.idToken;
      if (token == null) return null;
      return SocialCredential(provider: SocialProvider.google, idToken: token);
    } on GoogleSignInException catch (error) {
      if (error.code == GoogleSignInExceptionCode.canceled) return null;
      rethrow;
    }
  }

  Future<SocialCredential?> _apple() async {
    try {
      final apple = await SignInWithApple.getAppleIDCredential(
        scopes: [
          AppleIDAuthorizationScopes.email,
          AppleIDAuthorizationScopes.fullName,
        ],
      );
      final token = apple.identityToken;
      if (token == null) return null;
      return SocialCredential(
        provider: SocialProvider.apple,
        idToken: token,
        firstName: apple.givenName,
        lastName: apple.familyName,
        authorizationCode: apple.authorizationCode,
      );
    } on SignInWithAppleAuthorizationException catch (error) {
      if (error.code == AuthorizationErrorCode.canceled) return null;
      rethrow;
    }
  }
}
