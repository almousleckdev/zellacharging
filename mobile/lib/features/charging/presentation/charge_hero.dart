import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../domain/charging_session.dart';
import '../domain/session_state.dart';
import 'session_formatting.dart';

class ChargeHero extends StatelessWidget {
  const ChargeHero({super.key, required this.session});

  final ChargingSession session;

  IconData get _icon => switch (session.state) {
    SessionState.completed => Icons.check_rounded,
    SessionState.failed || SessionState.abandoned => Icons.close_rounded,
    _ => Icons.bolt_rounded,
  };

  @override
  Widget build(BuildContext context) {
    final percent = session.batteryPercent;
    if (percent == null) {
      return CircleAvatar(
        radius: 34,
        backgroundColor: session.stateColor.withValues(alpha: 0.12),
        child: Icon(_icon, size: 38, color: session.stateColor),
      );
    }
    final theme = Theme.of(context);
    return Text.rich(
      TextSpan(
        text: percent.toStringAsFixed(0),
        style: theme.textTheme.displaySmall?.copyWith(
          fontSize: 60,
          fontWeight: FontWeight.w800,
        ),
        children: [
          TextSpan(
            text: ' %',
            style: theme.textTheme.titleLarge?.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }
}
