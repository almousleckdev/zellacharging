import '../l10n/app_language.dart';

abstract final class MoneyFormat {
  static const _symbols = {'XAF': 'FCFA', 'XOF': 'FCFA'};
  static const _space = ' ';

  static String symbol(String currency) => _symbols[currency] ?? currency;

  static String format(int minor, String currency, int decimals) =>
      '${number(minor, decimals)}$_space${symbol(currency)}';

  static String number(int minor, int decimals) {
    final divisor = _pow10(decimals);
    final whole = _group('${minor ~/ divisor}');
    if (decimals == 0) return whole;
    final fraction = (minor % divisor).toString().padLeft(decimals, '0');
    final separator = AppLanguage.pick(fr: ',', en: '.');
    return '$whole$separator$fraction';
  }

  static String _group(String digits) {
    final buffer = StringBuffer();
    for (var i = 0; i < digits.length; i++) {
      if (i > 0 && (digits.length - i) % 3 == 0) buffer.write(_space);
      buffer.write(digits[i]);
    }
    return buffer.toString();
  }

  static int _pow10(int exponent) {
    var result = 1;
    for (var i = 0; i < exponent; i++) {
      result *= 10;
    }
    return result;
  }
}
