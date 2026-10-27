import 'package:flutter/material.dart';

import '../../../core/format/amount_input.dart';
import '../../../core/format/money_format.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import 'amount_chips.dart';
import 'pay_card.dart';

class AmountHero extends StatelessWidget {
  const AmountHero({
    super.key,
    required this.title,
    required this.controller,
    required this.currency,
    required this.quickAmounts,
    required this.onChanged,
    required this.validator,
    this.helper,
  });

  final String title;
  final TextEditingController controller;
  final String currency;
  final List<int> quickAmounts;
  final VoidCallback onChanged;
  final String? Function(String?) validator;
  final String? helper;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return PayCard(
      title: title,
      child: FormField<String>(
        validator: (_) => validator(controller.text),
        builder: (field) => Column(
          children: [
            _input(theme, field),
            if (field.hasError) ...[
              const SizedBox(height: AppSpacing.xs),
              Text(
                field.errorText!,
                textAlign: TextAlign.center,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: AppColors.danger,
                ),
              ),
            ],
            if (helper != null) ...[
              const SizedBox(height: AppSpacing.xs),
              Text(
                helper!,
                textAlign: TextAlign.center,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
            ],
            const SizedBox(height: AppSpacing.md),
            AmountChips(
              amounts: quickAmounts,
              selected: AmountInput.parse(controller.text) ?? 0,
              currency: currency,
              onSelected: (value) {
                controller.text = AmountInput.format(value);
                field.reset();
                onChanged();
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _input(ThemeData theme, FormFieldState<String> field) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.baseline,
      textBaseline: TextBaseline.alphabetic,
      children: [
        ConstrainedBox(
          constraints: const BoxConstraints(minWidth: 56, maxWidth: 230),
          child: IntrinsicWidth(
            child: TextField(
              controller: controller,
              onChanged: (_) {
                field.reset();
                onChanged();
              },
              textAlign: TextAlign.center,
              keyboardType: TextInputType.number,
              inputFormatters: [AmountInput.formatter],
              cursorColor: AppColors.primary,
              style: theme.textTheme.displaySmall?.copyWith(
                fontWeight: FontWeight.w800,
                color: AppColors.textPrimary,
              ),
              decoration: const InputDecoration(
                isDense: true,
                border: InputBorder.none,
                enabledBorder: InputBorder.none,
                focusedBorder: InputBorder.none,
                filled: false,
                hintText: '0',
              ),
            ),
          ),
        ),
        const SizedBox(width: AppSpacing.sm),
        Text(
          MoneyFormat.symbol(currency),
          style: theme.textTheme.titleMedium?.copyWith(
            color: AppColors.textSecondary,
            fontWeight: FontWeight.w700,
          ),
        ),
      ],
    );
  }
}
