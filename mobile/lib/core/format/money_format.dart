abstract final class MoneyFormat {
  static String format(int minor, String currency, int decimals) {
    final text = decimals == 0
        ? '$minor'
        : (minor / _pow10(decimals)).toStringAsFixed(decimals);
    return '$text $currency';
  }

  static int _pow10(int exponent) {
    var result = 1;
    for (var i = 0; i < exponent; i++) {
      result *= 10;
    }
    return result;
  }
}
