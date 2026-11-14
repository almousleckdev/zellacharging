import 'package:flutter/material.dart';

import '../../../core/format/amount_input.dart';
import '../../../core/format/money_format.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../app_text_field.dart';
import 'amount_chips.dart';
import 'pay_card.dart';

class AmountCard extends StatelessWidget {
  const AmountCard({
    super.key,
    required this.label,
    required this.hint,
    required this.controller,
    required this.currency,
    required this.onChanged,
    this.validator,
    this.helper,
    this.errorText,
    this.quickAmounts = const [],
  });

  final String label;
  final String hint;
  final TextEditingController controller;
  final String currency;
  final String? Function(String?)? validator;
  final VoidCallback onChanged;
  final String? helper;
  final String? errorText;
  final List<int> quickAmounts;

  @override
  Widget build(BuildContext context) {
    return PayCard(
      child: Column(
        children: [
          AppTextField(
            controller: controller,
            label: label,
            hint: hint,
            icon: AppIcons.wallet,
            helper: helper,
            errorText: errorText,
            validator: validator,
            keyboardType: TextInputType.number,
            inputFormatters: [AmountInput.formatter],
            suffixText: MoneyFormat.symbol(currency),
            onChanged: (_) => onChanged(),
          ),
          if (quickAmounts.isNotEmpty) ...[
            const SizedBox(height: AppSpacing.md),
            AmountChips(
              amounts: quickAmounts,
              selected: AmountInput.parse(controller.text) ?? 0,
              currency: currency,
              onSelected: (value) {
                controller.text = AmountInput.format(value);
                onChanged();
              },
            ),
          ],
        ],
      ),
    );
  }
}
