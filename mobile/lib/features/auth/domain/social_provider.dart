enum SocialProvider {
  google('GOOGLE'),
  apple('APPLE');

  const SocialProvider(this.apiName);

  final String apiName;
}
