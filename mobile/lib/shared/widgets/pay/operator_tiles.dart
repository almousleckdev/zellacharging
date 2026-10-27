import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../features/wallet/domain/mobile_operator.dart';

class OperatorTiles extends StatelessWidget {
  const OperatorTiles({
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
          Expanded(
            child: _Tile(
              operator: operator,
              active: operator == selected,
              onTap: () => onSelected(operator),
            ),
          ),
          if (operator != MobileOperator.values.last)
            const SizedBox(width: AppSpacing.sm),
        ],
      ],
    );
  }
}

class _Tile extends StatelessWidget {
  const _Tile({
    required this.operator,
    required this.active,
    required this.onTap,
  });

  final MobileOperator operator;
  final bool active;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Semantics(
      button: true,
      selected: active,
      label: operator.label,
      child: InkWell(
        borderRadius: BorderRadius.circular(AppSpacing.radius),
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 160),
          padding: const EdgeInsets.all(AppSpacing.md),
          decoration: BoxDecoration(
            color: active ? AppColors.primarySoft : AppColors.surface,
            borderRadius: BorderRadius.circular(AppSpacing.radius),
            border: Border.all(
              color: active ? AppColors.primary : AppColors.border,
              width: active ? 2 : 1,
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Icon(
                    AppIcons.phone,
                    color: active ? AppColors.primary : AppColors.neutral,
                  ),
                  Icon(
                    active ? AppIcons.checkCircle : AppIcons.circle,
                    size: 20,
                    color: active ? AppColors.primary : AppColors.border,
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.sm),
              Text(
                operator.label,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
