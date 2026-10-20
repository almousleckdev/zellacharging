import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/metric_tile.dart';
import '../domain/charging_session.dart';
import 'price_card.dart';
import 'session_formatting.dart';
import 'unplug_notice.dart';

class LiveMetrics extends StatelessWidget {
  const LiveMetrics({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        PriceCard(session: session),
        const SizedBox(height: AppSpacing.sm),
        IntrinsicHeight(
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Expanded(
                child: MetricTile(
                  icon: Icons.electrical_services_rounded,
                  label: Strings.current,
                  value: session.currentText,
                ),
              ),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: MetricTile(
                  icon: Icons.electric_meter_outlined,
                  label: Strings.voltage,
                  value: session.voltageText,
                ),
              ),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: MetricTile(
                  icon: Icons.bolt_rounded,
                  label: Strings.energy,
                  value: session.energyText,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.sm),
        const UnplugNotice(),
      ],
    );
  }
}
