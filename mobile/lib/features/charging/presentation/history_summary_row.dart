import 'package:flutter/material.dart';

import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/metric_tile.dart';
import '../domain/history_summary.dart';
import '../../../core/theme/app_icons.dart';

class HistorySummaryRow extends StatelessWidget {
  const HistorySummaryRow({super.key, required this.summary});

  final HistorySummary summary;

  String get _spent => summary.currency == null
      ? '--'
      : MoneyFormat.format(
          summary.spentMinor,
          summary.currency!,
          summary.currencyDecimals,
        );

  @override
  Widget build(BuildContext context) {
    return IntrinsicHeight(
      child: Row(
        children: [
          Expanded(
            child: MetricTile(
              icon: AppIcons.lightning,
              label: HistoryText.totalSessions,
              color: AppColors.primary,
              value: '${summary.sessions}',
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: MetricTile(
              icon: AppIcons.batteryCharging,
              label: HistoryText.totalEnergy,
              color: AppColors.chargeDeep,
              value: summary.energyKwh.toStringAsFixed(1),
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: MetricTile(
              icon: AppIcons.coins,
              label: HistoryText.totalSpent,
              color: AppColors.chargeGlow,
              value: _spent,
            ),
          ),
        ],
      ),
    );
  }
}
