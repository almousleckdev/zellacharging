enum OtpDelivery {
  whatsapp('WHATSAPP'),
  sms('SMS');

  const OtpDelivery(this.wire);

  final String wire;
}
