import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/soft_card.dart';
import 'panel_metric.dart';

class MetricPanel extends StatelessWidget {
  const MetricPanel({super.key, required this.metrics});

  final List<PanelMetric> metrics;

  @override
  Widget build(BuildContext context) {
    return SoftCard(
      padding: const EdgeInsets.symmetric(
        vertical: AppSpacing.lg - 4,
        horizontal: AppSpacing.sm,
      ),
      child: IntrinsicHeight(
        child: Row(
          children: [
            for (var i = 0; i < metrics.length; i++) ...[
              if (i > 0)
                const VerticalDivider(width: 1, color: AppColors.border),
              Expanded(child: metrics[i]),
            ],
          ],
        ),
      ),
    );
  }
}
