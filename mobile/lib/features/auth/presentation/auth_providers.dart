import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../data/auth_repository_impl.dart';
import '../data/native_social_authenticator.dart';
import '../domain/auth_repository.dart';
import '../domain/sign_in_with_password.dart';
import '../domain/sign_in_with_social.dart';
import '../domain/social_authenticator.dart';

final authRepositoryProvider = Provider<AuthRepository>(
  (ref) => AuthRepositoryImpl(
    client: ref.watch(dioProvider),
    store: ref.watch(tokenStoreProvider),
  ),
);

final socialAuthenticatorProvider = Provider<SocialAuthenticator>(
  (ref) => NativeSocialAuthenticator(),
);

final signInWithSocialProvider = Provider<SignInWithSocial>(
  (ref) => SignInWithSocial(
    ref.watch(socialAuthenticatorProvider),
    ref.watch(authRepositoryProvider),
  ),
);

final signInWithPasswordProvider = Provider<SignInWithPassword>(
  (ref) => SignInWithPassword(ref.watch(authRepositoryProvider)),
);
