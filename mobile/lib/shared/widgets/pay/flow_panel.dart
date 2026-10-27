import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../primary_button.dart';
import '../secondary_button.dart';
import 'payment_step.dart';
import 'payment_steps.dart';
import 'secure_note.dart';

enum FlowKind { progress, success, failed, verifying }

class FlowPanel extends StatelessWidget {
  const FlowPanel({
    super.key,
    required this.kind,
    required this.title,
    required this.message,
    this.steps = const [],
    this.primaryLabel,
    this.onPrimary,
    this.secondaryLabel,
    this.onSecondary,
  });

  final FlowKind kind;
  final String title;
  final String message;
  final List<PaymentStep> steps;
  final String? primaryLabel;
  final VoidCallback? onPrimary;
  final String? secondaryLabel;
  final VoidCallback? onSecondary;

  IconData get _icon => switch (kind) {
    FlowKind.progress => AppIcons.phone,
    FlowKind.success => AppIcons.checkCircle,
    FlowKind.failed => AppIcons.warning,
    FlowKind.verifying => AppIcons.timer,
  };

  Color get _tone => switch (kind) {
    FlowKind.success => AppColors.success,
    FlowKind.failed => AppColors.danger,
    _ => AppColors.primary,
  };

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return ListView(
      padding: const EdgeInsets.all(AppSpacing.lg),
      children: [
        const SizedBox(height: AppSpacing.lg),
        Center(
          child: Container(
            width: 84,
            height: 84,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: _tone.withValues(alpha: 0.12),
            ),
            child: Icon(_icon, size: 40, color: _tone),
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        Text(
          title,
          textAlign: TextAlign.center,
          style: theme.textTheme.titleLarge?.copyWith(
            fontWeight: FontWeight.w800,
          ),
        ),
        const SizedBox(height: AppSpacing.sm),
        Text(
          message,
          textAlign: TextAlign.center,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: AppColors.textSecondary,
          ),
        ),
        if (steps.isNotEmpty) ...[
          const SizedBox(height: AppSpacing.xl),
          PaymentSteps(steps: steps),
        ],
        const SizedBox(height: AppSpacing.lg),
        const SecureNote(),
        if (primaryLabel != null) ...[
          const SizedBox(height: AppSpacing.xl),
          PrimaryButton(label: primaryLabel!, onPressed: onPrimary),
        ],
        if (secondaryLabel != null) ...[
          const SizedBox(height: AppSpacing.sm),
          SecondaryButton(label: secondaryLabel!, onPressed: onSecondary),
        ],
      ],
    );
  }
}
