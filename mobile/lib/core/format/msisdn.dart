abstract final class Msisdn {
  static const countryCode = '241';
  static const _nationalLength = 8;

  static String digitsOf(String? raw) =>
      (raw ?? '').replaceAll(RegExp(r'\D'), '');

  static String national(String? raw) {
    final digits = digitsOf(raw);
    if (digits.startsWith(countryCode)) {
      return digits.substring(countryCode.length);
    }
    return digits.startsWith('0') ? digits.substring(1) : digits;
  }

  static String normalize(String? raw) {
    final trimmed = (raw ?? '').trim();
    if (trimmed.startsWith('+')) return '+${digitsOf(trimmed)}';
    final local = national(trimmed);
    return local.length == _nationalLength ? '+$countryCode$local' : trimmed;
  }
}
