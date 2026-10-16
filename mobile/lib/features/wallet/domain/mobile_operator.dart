enum MobileOperator {
  airtel('AIRTEL', 'Airtel Money'),
  moov('MOOV', 'Moov Money');

  const MobileOperator(this.code, this.label);

  final String code;
  final String label;
}
