class Profile {
  const Profile({
    required this.firstName,
    required this.lastName,
    this.email,
    this.phone,
    this.emailVerified = false,
    this.phoneVerified = false,
    this.hasPassword = true,
  });

  final String firstName;
  final String lastName;
  final String? email;
  final String? phone;
  final bool emailVerified;
  final bool phoneVerified;
  final bool hasPassword;

  bool get contactsComplete =>
      email != null && phone != null && emailVerified && phoneVerified;

  String get fullName => '$firstName $lastName'.trim();

  String get initials => [firstName, lastName]
      .where((name) => name.isNotEmpty)
      .map((name) => name[0])
      .join()
      .toUpperCase();

  String get contact => email ?? phone ?? '';
}
