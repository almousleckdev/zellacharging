import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/metric_tile.dart';
import '../domain/charging_session.dart';
import 'session_formatting.dart';

class ChargingMetrics extends StatelessWidget {
  const ChargingMetrics({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      mainAxisSpacing: AppSpacing.sm,
      crossAxisSpacing: AppSpacing.sm,
      childAspectRatio: 1.9,
      children: [
        MetricTile(
          icon: Icons.bolt_rounded,
          label: Strings.energy,
          value: session.energyText,
        ),
        MetricTile(
          icon: Icons.speed_rounded,
          label: Strings.power,
          value: session.powerText,
        ),
        MetricTile(
          icon: Icons.battery_charging_full_rounded,
          label: Strings.battery,
          value: session.batteryText,
        ),
        MetricTile(
          icon: Icons.timer_outlined,
          label: Strings.duration,
          value: session.durationText,
        ),
        if (!session.state.isLive)
          MetricTile(
            icon: Icons.payments_outlined,
            label: Strings.cost,
            value: session.costText,
          ),
      ],
    );
  }
}
