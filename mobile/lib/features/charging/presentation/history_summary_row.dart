import 'package:flutter/material.dart';

import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/metric_tile.dart';
import '../../history/domain/stat_metric.dart';
import '../domain/history_summary.dart';
import '../../../core/theme/app_icons.dart';

class HistorySummaryRow extends StatelessWidget {
  const HistorySummaryRow({
    super.key,
    required this.summary,
    required this.onMetric,
  });

  final HistorySummary summary;
  final ValueChanged<StatMetric> onMetric;

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
              onTap: () => onMetric(StatMetric.charges),
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: MetricTile(
              icon: AppIcons.batteryCharging,
              label: HistoryText.totalEnergy,
              color: AppColors.chargeDeep,
              value: summary.energyKwh.toStringAsFixed(1),
              onTap: () => onMetric(StatMetric.energy),
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: MetricTile(
              icon: AppIcons.coins,
              label: HistoryText.totalSpent,
              color: AppColors.chargeGlow,
              value: _spent,
              onTap: () => onMetric(StatMetric.spent),
            ),
          ),
        ],
      ),
    );
  }
}
