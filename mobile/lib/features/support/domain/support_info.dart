class SupportInfo {
  const SupportInfo({this.email, this.phone, this.whatsapp, this.agreementUrl});

  final String? email;
  final String? phone;
  final String? whatsapp;
  final String? agreementUrl;

  bool get hasContact => email != null || phone != null || whatsapp != null;
}
