import '../../../shared/otp_delivery.dart';
import '../domain/verification_target.dart';

enum OtpPurpose { registration, passwordReset }

class OtpArgs {
  const OtpArgs({required this.target, required this.purpose, this.delivery});

  const OtpArgs.registration(VerificationTarget target, {OtpDelivery? delivery})
    : this(
        target: target,
        purpose: OtpPurpose.registration,
        delivery: delivery,
      );

  const OtpArgs.passwordReset(
    VerificationTarget target, {
    OtpDelivery? delivery,
  }) : this(
         target: target,
         purpose: OtpPurpose.passwordReset,
         delivery: delivery,
       );

  final VerificationTarget target;
  final OtpPurpose purpose;
  final OtpDelivery? delivery;
}
