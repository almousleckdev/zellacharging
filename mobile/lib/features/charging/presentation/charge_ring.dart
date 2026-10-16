import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../domain/charging_session.dart';
import '../domain/session_state.dart';
import 'session_formatting.dart';

class ChargeRing extends StatelessWidget {
  const ChargeRing({super.key, required this.session});

  final ChargingSession session;

  double? get _progress {
    final soc = session.metering?.stateOfChargePercent;
    if (soc != null) return (soc / 100).clamp(0.0, 1.0);
    return session.state.isLive ? null : 1;
  }

  String get _center => session.metering?.stateOfChargePercent == null
      ? session.energyText
      : session.batteryText;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SizedBox.square(
      dimension: 220,
      child: Stack(
        alignment: Alignment.center,
        children: [
          SizedBox.expand(
            child: CircularProgressIndicator(
              value: _progress,
              strokeWidth: 14,
              strokeCap: StrokeCap.round,
              backgroundColor: AppColors.primarySoft,
            ),
          ),
          Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                _center,
                style: theme.textTheme.displaySmall?.copyWith(
                  fontWeight: FontWeight.w800,
                ),
              ),
              const Icon(Icons.bolt_rounded, color: AppColors.primary),
              Text(
                session.state == SessionState.active
                    ? Strings.chargingStatus
                    : session.stateLabel,
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
