import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../domain/mobile_operator.dart';

class OperatorSelector extends StatelessWidget {
  const OperatorSelector({
    super.key,
    required this.selected,
    required this.onSelected,
  });

  final MobileOperator selected;
  final ValueChanged<MobileOperator> onSelected;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        for (final operator in MobileOperator.values) ...[
          Expanded(child: _option(operator)),
          if (operator != MobileOperator.values.last)
            const SizedBox(width: AppSpacing.sm),
        ],
      ],
    );
  }

  Widget _option(MobileOperator operator) {
    final active = operator == selected;
    return InkWell(
      borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
      onTap: () => onSelected(operator),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: active ? AppColors.primarySoft : AppColors.surface,
          borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
          border: Border.all(
            color: active ? AppColors.primary : AppColors.border,
          ),
        ),
        child: Text(
          operator.label,
          style: TextStyle(
            fontWeight: FontWeight.w600,
            color: active ? AppColors.primary : AppColors.textPrimary,
          ),
        ),
      ),
    );
  }
}
