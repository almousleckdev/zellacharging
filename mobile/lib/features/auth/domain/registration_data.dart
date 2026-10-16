class RegistrationData {
  const RegistrationData({
    required this.firstName,
    required this.lastName,
    required this.password,
    this.email,
    this.phone,
  });

  final String firstName;
  final String lastName;
  final String password;
  final String? email;
  final String? phone;
}
