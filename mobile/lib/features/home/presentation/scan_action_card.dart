import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/gradient_card.dart';

class ScanActionCard extends StatelessWidget {
  const ScanActionCard({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return GestureDetector(
      onTap: () => context.push(Routes.scan),
      child: GradientCard(
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    ChargeText.scanToCharge,
                    style: theme.textTheme.titleLarge?.copyWith(
                      color: AppColors.onPrimary,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    StationText.scanHint,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: AppColors.onPrimary,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: AppSpacing.md),
            Container(
              width: 60,
              height: 60,
              decoration: BoxDecoration(
                color: AppColors.onPrimary.withValues(alpha: 0.2),
                borderRadius: BorderRadius.circular(AppSpacing.radius),
              ),
              child: const Icon(
                AppIcons.qrCode,
                color: AppColors.onPrimary,
                size: 34,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
