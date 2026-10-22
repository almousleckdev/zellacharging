import 'package:flutter/material.dart';

import '../../../core/format/date_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/soft_card.dart';
import '../../../shared/widgets/status_chip.dart';
import '../domain/charging_session.dart';
import 'session_formatting.dart';

class SessionHistoryCard extends StatelessWidget {
  const SessionHistoryCard({
    super.key,
    required this.session,
    required this.onTap,
  });

  final ChargingSession session;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SoftCard(
      onTap: onTap,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  session.stationId,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                ),
              ),
              const SizedBox(width: AppSpacing.sm),
              StatusChip(label: session.stateLabel, color: session.stateColor),
            ],
          ),
          const SizedBox(height: AppSpacing.sm + 2),
          _Fact(
            color: AppColors.primary,
            label: ChargeText.duration,
            value: session.durationText,
          ),
          _Fact(
            color: AppColors.chargeGreen,
            label: ChargeText.energy,
            value: session.energyText,
          ),
          const Divider(height: AppSpacing.lg - 2, color: AppColors.border),
          Row(
            children: [
              Expanded(
                child: Text(
                  DateFormatter.dateTime(session.createdAt),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
              ),
              Text(
                session.costText,
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w800,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _Fact extends StatelessWidget {
  const _Fact({required this.color, required this.label, required this.value});

  final Color color;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Row(
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(color: color, shape: BoxShape.circle),
          ),
          const SizedBox(width: AppSpacing.sm),
          Text(
            '$label : ',
            style: theme.textTheme.bodyMedium?.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          Flexible(
            child: Text(
              value,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.bodyMedium?.copyWith(
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
