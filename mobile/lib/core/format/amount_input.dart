import 'package:flutter/services.dart';

import 'money_format.dart';

abstract final class AmountInput {
  static final formatter = _GroupedDigitsFormatter();

  static int? parse(String? text) {
    final digits = (text ?? '').replaceAll(RegExp(r'\D'), '');
    return digits.isEmpty ? null : int.tryParse(digits);
  }

  static String format(int value) => MoneyFormat.number(value, 0);
}

class _GroupedDigitsFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    final value = AmountInput.parse(newValue.text);
    if (value == null) return const TextEditingValue();
    final text = AmountInput.format(value);
    return TextEditingValue(
      text: text,
      selection: TextSelection.collapsed(offset: text.length),
    );
  }
}
