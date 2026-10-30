import 'social_provider.dart';

class SocialCredential {
  const SocialCredential({
    required this.provider,
    required this.idToken,
    this.firstName,
    this.lastName,
    this.authorizationCode,
  });

  final SocialProvider provider;
  final String idToken;
  final String? firstName;
  final String? lastName;
  final String? authorizationCode;

  Map<String, dynamic> toJson() => {
    'provider': provider.apiName,
    'idToken': idToken,
    if (firstName != null) 'firstName': firstName,
    if (lastName != null) 'lastName': lastName,
    if (authorizationCode != null) 'authorizationCode': authorizationCode,
  };
}
