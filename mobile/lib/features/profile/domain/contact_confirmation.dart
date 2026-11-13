import 'otp_delivery.dart';

class ContactConfirmation {
  const ContactConfirmation({required this.target, this.delivery});

  final String target;
  final OtpDelivery? delivery;
}
