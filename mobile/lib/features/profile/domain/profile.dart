class Profile {
  const Profile({
    required this.firstName,
    required this.lastName,
    this.email,
    this.phone,
  });

  final String firstName;
  final String lastName;
  final String? email;
  final String? phone;

  String get fullName => '$firstName $lastName';

  String get initials => '${firstName[0]}${lastName[0]}'.toUpperCase();

  String get contact => email ?? phone ?? '';
}
