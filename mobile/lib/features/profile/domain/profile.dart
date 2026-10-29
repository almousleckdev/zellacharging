class Profile {
  const Profile({
    required this.firstName,
    required this.lastName,
    this.email,
    this.phone,
    this.emailVerified = false,
    this.phoneVerified = false,
  });

  final String firstName;
  final String lastName;
  final String? email;
  final String? phone;
  final bool emailVerified;
  final bool phoneVerified;

  String get fullName => '$firstName $lastName';

  String get initials => '${firstName[0]}${lastName[0]}'.toUpperCase();

  String get contact => email ?? phone ?? '';
}
