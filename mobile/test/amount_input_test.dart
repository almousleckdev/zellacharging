import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/format/amount_input.dart';
import 'package:zella/core/validation/validators.dart';

void main() {
  test('digits are grouped as the customer types', () {
    var value = const TextEditingValue();
    for (final typed in ['1', '12', '123', '1234', '12345', '123456']) {
      value = AmountInput.formatter.formatEditUpdate(
        value,
        TextEditingValue(text: typed),
      );
    }
    expect(value.text, AmountInput.format(123456));
    expect(value.selection.baseOffset, value.text.length);
  });

  test('letters and separators are stripped', () {
    final value = AmountInput.formatter.formatEditUpdate(
      const TextEditingValue(),
      const TextEditingValue(text: '2a0,0 0'),
    );
    expect(value.text, AmountInput.format(2000));
  });

  test('an empty field stays empty', () {
    final value = AmountInput.formatter.formatEditUpdate(
      const TextEditingValue(text: '5'),
      const TextEditingValue(text: ''),
    );
    expect(value.text, isEmpty);
  });

  test('grouped and plain text parse to the same amount', () {
    expect(AmountInput.parse(AmountInput.format(12500)), 12500);
    expect(AmountInput.parse('12 500'), 12500);
    expect(AmountInput.parse('12500'), 12500);
    expect(AmountInput.parse(''), isNull);
    expect(AmountInput.parse(null), isNull);
  });

  test('the top-up validator accepts grouped amounts and enforces 100', () {
    expect(Validators.topupAmount('1 000'), isNull);
    expect(Validators.topupAmount('100'), isNull);
    expect(Validators.topupAmount('99'), isNotNull);
  });
}
