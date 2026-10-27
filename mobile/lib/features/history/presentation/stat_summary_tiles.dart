import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/metric_tile.dart';
import '../domain/stat_report.dart';
import 'stat_value_format.dart';

class StatSummaryTiles extends StatelessWidget {
  const StatSummaryTiles({super.key, required this.report});

  final StatReport report;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        IntrinsicHeight(
          child: Row(
            children: [
              Expanded(
                child: MetricTile(
                  icon: AppIcons.lightning,
                  label: StatText.totalCharges,
                  value: '${report.charges}',
                ),
              ),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: MetricTile(
                  icon: AppIcons.batteryCharging,
                  label: StatText.totalEnergy,
                  color: AppColors.chargeDeep,
                  value: StatValueFormat.energy(report.energyKwh),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.sm),
        IntrinsicHeight(
          child: Row(
            children: [
              Expanded(
                child: MetricTile(
                  icon: AppIcons.coins,
                  label: StatText.totalSpent,
                  color: AppColors.chargeGlow,
                  value: StatValueFormat.money(
                    report,
                    report.spentMinor.toDouble(),
                  ),
                ),
              ),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: MetricTile(
                  icon: AppIcons.chart,
                  label: StatText.averagePerCharge,
                  color: AppColors.primary,
                  value: StatValueFormat.money(
                    report,
                    report.averageSpentMinor,
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
