import 'package:flutter/material.dart';

import '../../../core/theme/app_spacing.dart';
import '../domain/charging_session.dart';
import 'charged_pill.dart';
import 'finished_metrics.dart';
import 'live_metrics.dart';
import 'session_formatting.dart';
import 'session_message_card.dart';
import 'session_station_card.dart';

class ChargingMetrics extends StatelessWidget {
  const ChargingMetrics({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    final message = session.statusMessage;
    return Column(
      children: [
        if (session.state.isLive) ...[
          ChargedPill(session: session),
          const SizedBox(height: AppSpacing.md),
        ],
        SessionStationCard(session: session),
        if (message != null) ...[
          const SizedBox(height: AppSpacing.sm),
          SessionMessageCard(message: message, color: session.stateColor),
        ],
        const SizedBox(height: AppSpacing.sm),
        if (session.state.isLive)
          LiveMetrics(session: session)
        else
          FinishedMetrics(session: session),
      ],
    );
  }
}
