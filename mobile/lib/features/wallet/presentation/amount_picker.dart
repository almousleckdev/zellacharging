import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';

class AmountPicker extends StatelessWidget {
  const AmountPicker({
    super.key,
    required this.amounts,
    required this.selected,
    required this.currency,
    required this.onSelected,
  });

  final List<int> amounts;
  final int selected;
  final String currency;
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: AppSpacing.sm,
      runSpacing: AppSpacing.sm,
      children: [
        for (final amount in amounts)
          ChoiceChip(
            label: Text('$amount $currency'),
            selected: amount == selected,
            onSelected: (_) => onSelected(amount),
            showCheckmark: false,
            selectedColor: AppColors.primary,
            backgroundColor: AppColors.surface,
            side: BorderSide(
              color: amount == selected ? AppColors.primary : AppColors.border,
            ),
            labelStyle: TextStyle(
              color: amount == selected ? Colors.white : AppColors.textPrimary,
            ),
          ),
      ],
    );
  }
}
