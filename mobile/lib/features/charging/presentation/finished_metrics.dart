import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_spacing.dart';
import '../domain/charging_session.dart';
import 'metric_panel.dart';
import 'panel_metric.dart';
import 'price_card.dart';
import 'session_formatting.dart';

class FinishedMetrics extends StatelessWidget {
  const FinishedMetrics({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        PriceCard(session: session),
        const SizedBox(height: AppSpacing.sm),
        MetricPanel(
          metrics: [
            PanelMetric(
              icon: Icons.timer_outlined,
              label: Strings.duration,
              value: session.durationText,
            ),
            PanelMetric(
              icon: Icons.bolt_rounded,
              label: Strings.energy,
              value: session.energyText,
            ),
            PanelMetric(
              icon: Icons.battery_charging_full_rounded,
              label: Strings.battery,
              value: session.batteryText,
            ),
          ],
        ),
      ],
    );
  }
}
