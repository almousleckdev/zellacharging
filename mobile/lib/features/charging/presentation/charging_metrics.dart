import 'package:flutter/material.dart';

import '../../../core/theme/app_spacing.dart';
import '../domain/charging_session.dart';
import '../domain/session_state.dart';
import 'charged_pill.dart';
import 'live_metrics.dart';
import 'plug_in_prompt.dart';
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
        ChargedPill(session: session),
        const SizedBox(height: AppSpacing.md),
        SessionStationCard(session: session),
        if (session.state == SessionState.startAccepted) ...[
          const SizedBox(height: AppSpacing.sm),
          PlugInPrompt(deadline: session.waitingUntil),
        ],
        if (message != null) ...[
          const SizedBox(height: AppSpacing.sm),
          SessionMessageCard(message: message, color: session.stateColor),
        ],
        const SizedBox(height: AppSpacing.sm),
        LiveMetrics(session: session),
      ],
    );
  }
}
