import '../../../shared/otp_delivery.dart';

class ContactConfirmation {
  const ContactConfirmation({
    required this.target,
    this.delivery,
    this.changing = false,
  });

  final String target;
  final OtpDelivery? delivery;
  final bool changing;
}
