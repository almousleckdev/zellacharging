import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_spacing.dart';
import '../domain/charging_session.dart';
import 'metric_panel.dart';
import 'panel_metric.dart';
import 'price_card.dart';
import 'session_formatting.dart';
import 'unplug_notice.dart';
import '../../../core/theme/app_icons.dart';

class LiveMetrics extends StatelessWidget {
  const LiveMetrics({super.key, required this.session});

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
              icon: AppIcons.plugs,
              label: ChargeText.current,
              value: session.currentText,
            ),
            PanelMetric(
              icon: AppIcons.gauge,
              label: ChargeText.voltage,
              value: session.voltageText,
            ),
            PanelMetric(
              icon: AppIcons.lightning,
              label: ChargeText.energy,
              value: session.energyText,
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.sm),
        const UnplugNotice(),
      ],
    );
  }
}
