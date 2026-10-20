import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../shared/widgets/metric_grid.dart';
import '../../../shared/widgets/metric_tile.dart';
import '../domain/charging_session.dart';
import 'session_formatting.dart';

class ChargingMetrics extends StatelessWidget {
  const ChargingMetrics({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    return MetricGrid(
      tiles: [
        MetricTile(
          icon: Icons.timer_outlined,
          label: Strings.elapsed,
          value: session.durationText,
        ),
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
          icon: Icons.payments_outlined,
          label: session.priceIsEstimate
              ? Strings.priceEstimated
              : Strings.price,
          value: session.priceText,
        ),
        MetricTile(
          icon: Icons.electric_meter_outlined,
          label: Strings.voltage,
          value: session.voltageText,
        ),
        MetricTile(
          icon: Icons.electrical_services_rounded,
          label: Strings.current,
          value: session.currentText,
        ),
      ],
    );
  }
}
