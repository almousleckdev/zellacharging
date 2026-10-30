const _serverClientId = String.fromEnvironment('GOOGLE_SERVER_CLIENT_ID');
const _iosClientId = String.fromEnvironment('GOOGLE_IOS_CLIENT_ID');

class SocialConfig {
  const SocialConfig._();

  static String? get googleServerClientId =>
      _serverClientId.isEmpty ? null : _serverClientId;

  static String? get googleIosClientId =>
      _iosClientId.isEmpty ? null : _iosClientId;
}
