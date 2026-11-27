import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/pay/pay_card.dart';

class FreeChargingCard extends StatelessWidget {
  const FreeChargingCard({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return PayCard(
      title: PayText.freeCharging,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(AppIcons.checkCircle, color: AppColors.success),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: Text(
              PayText.freeChargingNote,
              style: theme.textTheme.bodySmall?.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
