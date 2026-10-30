enum MobileOperator {
  airtel('AIRTEL', 'Airtel Money', 'assets/icons/airtel.png'),
  moov('MOOV', 'Moov Money', 'assets/icons/moov.png');

  const MobileOperator(this.code, this.label, this.logo);

  final String code;
  final String label;
  final String logo;

  static MobileOperator? fromCode(String? code) =>
      values.where((operator) => operator.code == code).firstOrNull;
}
