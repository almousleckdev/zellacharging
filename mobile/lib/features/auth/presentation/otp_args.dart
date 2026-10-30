enum OtpPurpose { registration, passwordReset }

class OtpArgs {
  const OtpArgs({required this.email, required this.purpose});

  const OtpArgs.registration(String email)
    : this(email: email, purpose: OtpPurpose.registration);

  const OtpArgs.passwordReset(String email)
    : this(email: email, purpose: OtpPurpose.passwordReset);

  final String email;
  final OtpPurpose purpose;
}
