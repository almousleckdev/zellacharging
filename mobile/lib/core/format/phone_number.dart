import 'package:country_picker/country_picker.dart';

import '../l10n/strings.dart';

abstract final class PhoneNumber {
  static const defaultRegion = 'GA';

  static Country get defaultCountry =>
      CountryParser.parseCountryCode(defaultRegion);

  static String digitsOf(String national) =>
      national.replaceAll(RegExp(r'\D'), '');

  static String e164(Country country, String national) {
    final digits = digitsOf(national);
    final trimmed = digits.startsWith('0') ? digits.substring(1) : digits;
    return '+${country.phoneCode}$trimmed';
  }

  static String? validate(String? national) {
    final digits = digitsOf(national ?? '');
    if (digits.isEmpty) return Strings.required;
    return digits.length >= 6 && digits.length <= 13
        ? null
        : Strings.invalidPhone;
  }
}
