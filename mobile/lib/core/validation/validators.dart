import '../format/amount_input.dart';
import '../format/msisdn.dart';
import 'package:flutter/widgets.dart';

import '../l10n/strings.dart';

abstract final class Validators {
  static final _email = RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$');
  static final _phone = RegExp(r'^\+?[0-9][0-9 ().-]{5,19}$');

  static String? required(String? value) =>
      value == null || value.trim().isEmpty ? Strings.required : null;

  static String? email(String? value) {
    if (value == null || value.trim().isEmpty) return Strings.required;
    return _email.hasMatch(value.trim()) ? null : Strings.invalidEmail;
  }

  static String? optionalPhone(String? value) {
    if (value == null || value.trim().isEmpty) return null;
    return _phone.hasMatch(value.trim()) ? null : Strings.invalidPhone;
  }

  static String? requiredPhone(String? value) {
    if (value == null || value.trim().isEmpty) return Strings.required;
    return _phone.hasMatch(value.trim()) ? null : Strings.invalidPhone;
  }

  static String? password(String? value) {
    if (value == null || value.isEmpty) return Strings.required;
    return value.length < 8 ? Strings.shortPassword : null;
  }

  static String? newPassword(String? value) {
    if (value == null || value.isEmpty) return Strings.required;
    final strong = value.length >= 8 && RegExp(r'\d').hasMatch(value);
    return strong ? null : AuthText.weakPassword;
  }

  static String? Function(String?) sameAs(TextEditingController other) =>
      (value) => value == other.text ? null : AuthText.passwordsDiffer;

  static String? emailForSignUp(String? value) {
    final text = (value ?? '').trim();
    if (text.isEmpty) return Strings.required;
    return _email.hasMatch(text) ? null : AuthText.phoneNeedsEmail;
  }

  static String? otp(String? value) =>
      value != null && RegExp(r'^\d{6}$').hasMatch(value.trim())
      ? null
      : Strings.invalidCode;

  static String? mobileMoneyNumber(String? value) =>
      value != null &&
          RegExp(r'^\+[1-9]\d{7,14}$').hasMatch(Msisdn.normalize(value))
      ? null
      : WalletText.invalidMobileMoney;

  static String? withdrawAmount(String? value, int availableMinor) {
    final amount = AmountInput.parse(value);
    final valid =
        amount != null &&
        amount >= WalletText.minimumWithdrawMinor &&
        amount <= availableMinor;
    return valid ? null : WithdrawText.invalidAmount;
  }

  static String? payoutNumber(String? value, Iterable<String> allowed) {
    final format = mobileMoneyNumber(value);
    if (format != null) return format;
    final number = Msisdn.normalize(value!);
    return allowed.contains(number) ? null : WithdrawText.numberNotAllowed;
  }

  static String? topupAmount(String? value) {
    final amount = AmountInput.parse(value);
    return amount != null && amount >= WalletText.minimumTopupMinor
        ? null
        : WalletText.invalidTopupAmount;
  }
}
