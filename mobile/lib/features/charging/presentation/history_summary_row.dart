import 'package:flutter/material.dart';

import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/metric_tile.dart';
import '../domain/history_summary.dart';

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
              icon: Icons.bolt_rounded,
              label: Strings.totalSessions,
              value: '${summary.sessions}',
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: MetricTile(
              icon: Icons.battery_charging_full_rounded,
              label: Strings.totalEnergy,
              value: summary.energyKwh.toStringAsFixed(1),
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: MetricTile(
              icon: Icons.payments_outlined,
              label: Strings.totalSpent,
              value: _spent,
            ),
          ),
        ],
      ),
    );
  }
}
