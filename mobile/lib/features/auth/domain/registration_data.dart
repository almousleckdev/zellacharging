import '../../../shared/otp_delivery.dart';

class RegistrationData {
  const RegistrationData({
    required this.firstName,
    required this.lastName,
    required this.password,
    required this.email,
    required this.phone,
    this.delivery,
  });

  final String firstName;
  final String lastName;
  final String password;
  final String email;
  final String phone;
  final OtpDelivery? delivery;
}
