import '../../../shared/otp_delivery.dart';
import '../domain/verification_target.dart';

enum OtpPurpose { registration, passwordReset }

class OtpArgs {
  const OtpArgs({
    required this.target,
    required this.purpose,
    this.delivery,
    this.followUp,
    this.step = 1,
  });

  const OtpArgs.registration(
    VerificationTarget target, {
    OtpDelivery? delivery,
    VerificationTarget? followUp,
    int step = 1,
  }) : this(
         target: target,
         purpose: OtpPurpose.registration,
         delivery: delivery,
         followUp: followUp,
         step: step,
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
  final VerificationTarget? followUp;
  final int step;

  int get totalSteps => followUp != null || step > 1 ? 2 : 1;

  OtpArgs get next => OtpArgs.registration(followUp!, step: step + 1);
}
