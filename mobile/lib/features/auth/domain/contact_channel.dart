enum ContactChannel {
  email('EMAIL'),
  phone('PHONE');

  const ContactChannel(this.apiName);

  final String apiName;

  static ContactChannel parse(String value) => values.firstWhere(
    (channel) => channel.apiName == value,
    orElse: () => email,
  );

  static ContactChannel of(String identifier) =>
      identifier.contains('@') ? email : phone;
}
