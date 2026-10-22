import '../l10n/strings.dart';

abstract final class Validators {
  static final _email = RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$');

  static String? required(String? value) =>
      value == null || value.trim().isEmpty ? Strings.required : null;

  static String? optionalEmail(String? value) {
    if (value == null || value.trim().isEmpty) return null;
    return _email.hasMatch(value.trim()) ? null : Strings.invalidEmail;
  }

  static String? password(String? value) {
    if (value == null || value.isEmpty) return Strings.required;
    return value.length < 8 ? Strings.shortPassword : null;
  }

  static String? otp(String? value) =>
      value != null && RegExp(r'^\d{6}$').hasMatch(value.trim())
      ? null
      : Strings.invalidCode;

  static String? mobileMoneyNumber(String? value) =>
      value != null && RegExp(r'^\+[1-9]\d{7,14}$').hasMatch(value.trim())
      ? null
      : WalletText.invalidMobileMoney;

  static String? chargeAmount(String? value) {
    final amount = int.tryParse(value?.trim() ?? '');
    return amount != null && amount >= ChargeText.minimumChargeMinor
        ? null
        : ChargeText.invalidAmount;
  }
}
