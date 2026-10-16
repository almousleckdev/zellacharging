enum ContactChannel {
  email('EMAIL'),
  phone('PHONE');

  const ContactChannel(this.apiName);

  final String apiName;
}
