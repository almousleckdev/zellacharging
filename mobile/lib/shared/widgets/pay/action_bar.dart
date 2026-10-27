import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../primary_button.dart';

class ActionBar extends StatelessWidget {
  const ActionBar({
    super.key,
    required this.label,
    required this.onPressed,
    this.loading = false,
    this.hint,
  });

  final String label;
  final VoidCallback? onPressed;
  final bool loading;
  final String? hint;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: AppColors.surface,
        border: const Border(top: BorderSide(color: AppColors.border)),
        boxShadow: [
          BoxShadow(
            color: AppColors.textPrimary.withValues(alpha: 0.05),
            blurRadius: 16,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(
            AppSpacing.lg,
            AppSpacing.md,
            AppSpacing.lg,
            AppSpacing.md,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (hint != null) ...[
                Text(
                  hint!,
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
                const SizedBox(height: AppSpacing.sm),
              ],
              PrimaryButton(
                label: label,
                loading: loading,
                onPressed: onPressed,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
