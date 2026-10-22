import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/soft_card.dart';
import '../../domain/charging_session.dart';
import '../session_formatting.dart';

class DetailsSummary extends StatelessWidget {
  const DetailsSummary({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    return SoftCard(
      padding: const EdgeInsets.symmetric(
        vertical: AppSpacing.md,
        horizontal: AppSpacing.sm,
      ),
      child: IntrinsicHeight(
        child: Row(
          children: [
            _cell(context, session.durationText, HistoryText.totalDuration),
            const VerticalDivider(width: 1, color: AppColors.border),
            _cell(context, session.energyText, HistoryText.totalKwh),
            const VerticalDivider(width: 1, color: AppColors.border),
            _cell(context, session.costText, HistoryText.totalFee, true),
          ],
        ),
      ),
    );
  }

  Widget _cell(
    BuildContext context,
    String value,
    String label, [
    bool highlighted = false,
  ]) {
    final theme = Theme.of(context);
    return Expanded(
      child: Column(
        children: [
          SizedBox(
            height: 28,
            child: FittedBox(
              fit: BoxFit.scaleDown,
              child: Text(
                value,
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.w800,
                  color: highlighted
                      ? AppColors.primary
                      : AppColors.textPrimary,
                ),
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          Text(
            label,
            textAlign: TextAlign.center,
            maxLines: 2,
            style: theme.textTheme.bodySmall?.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }
}
