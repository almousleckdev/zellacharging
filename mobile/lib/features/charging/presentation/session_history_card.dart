import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import '../../../shared/widgets/soft_card.dart';
import '../../../shared/widgets/status_chip.dart';
import '../domain/charging_session.dart';
import '../domain/session_state.dart';
import 'session_formatting.dart';

class SessionHistoryCard extends StatelessWidget {
  const SessionHistoryCard({
    super.key,
    required this.session,
    required this.onTap,
  });

  final ChargingSession session;
  final VoidCallback onTap;

  Color get _color => switch (session.state) {
    SessionState.completed => AppColors.success,
    SessionState.failed || SessionState.abandoned => AppColors.danger,
    _ => AppColors.primary,
  };

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final muted = theme.textTheme.bodySmall?.copyWith(
      color: AppColors.textSecondary,
    );
    return SoftCard(
      onTap: onTap,
      child: Row(
        children: [
          IconBadge(icon: Icons.ev_station_rounded, color: _color),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  session.stationId,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.titleSmall,
                ),
                Text(
                  session.createdAt.toLocal().toString().substring(0, 16),
                  style: muted,
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                session.costMinor == null
                    ? session.energyText
                    : session.costText,
                style: theme.textTheme.titleSmall,
              ),
              const SizedBox(height: AppSpacing.xs),
              StatusChip(label: session.stateLabel, color: _color),
            ],
          ),
        ],
      ),
    );
  }
}
