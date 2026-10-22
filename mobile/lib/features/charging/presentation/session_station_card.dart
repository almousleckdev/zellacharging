import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import '../../../shared/widgets/soft_card.dart';
import '../../../shared/widgets/status_chip.dart';
import '../domain/charging_session.dart';
import 'session_formatting.dart';
import '../../../core/theme/app_icons.dart';

class SessionStationCard extends StatelessWidget {
  const SessionStationCard({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SoftCard(
      child: Row(
        children: [
          const IconBadge(
            icon: AppIcons.chargingStation,
            color: AppColors.chargeDeep,
          ),
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
                  '${StationText.connector} ${session.connectorId}',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          StatusChip(label: session.stateLabel, color: session.stateColor),
        ],
      ),
    );
  }
}
