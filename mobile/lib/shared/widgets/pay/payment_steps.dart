import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../adaptive_spinner.dart';
import 'payment_step.dart';

class PaymentSteps extends StatelessWidget {
  const PaymentSteps({super.key, required this.steps});

  final List<PaymentStep> steps;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      children: [
        for (var index = 0; index < steps.length; index++)
          IntrinsicHeight(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                SizedBox(
                  width: 28,
                  child: Column(
                    children: [
                      _marker(steps[index].state),
                      if (index < steps.length - 1)
                        Expanded(
                          child: Container(
                            width: 2,
                            color: steps[index].state == StepStatus.done
                                ? AppColors.success
                                : AppColors.border,
                          ),
                        ),
                    ],
                  ),
                ),
                const SizedBox(width: AppSpacing.md),
                Expanded(
                  child: Padding(
                    padding: const EdgeInsets.only(bottom: AppSpacing.md),
                    child: Text(
                      steps[index].label,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontWeight: steps[index].state == StepStatus.active
                            ? FontWeight.w700
                            : FontWeight.w500,
                        color: steps[index].state == StepStatus.pending
                            ? AppColors.textSecondary
                            : AppColors.textPrimary,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
      ],
    );
  }

  Widget _marker(StepStatus state) => switch (state) {
    StepStatus.done => const Icon(
      AppIcons.checkCircle,
      size: 24,
      color: AppColors.success,
    ),
    StepStatus.active => Container(
      width: 24,
      height: 24,
      alignment: Alignment.center,
      decoration: const BoxDecoration(
        shape: BoxShape.circle,
        color: AppColors.primarySoft,
      ),
      child: const AdaptiveSpinner(size: 14),
    ),
    StepStatus.pending => const Icon(
      AppIcons.circle,
      size: 24,
      color: AppColors.border,
    ),
  };
}
