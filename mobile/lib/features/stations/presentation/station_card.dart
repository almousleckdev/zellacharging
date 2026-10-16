import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import '../../../shared/widgets/soft_card.dart';
import '../../../shared/widgets/status_chip.dart';
import '../domain/station.dart';

class StationCard extends StatelessWidget {
  const StationCard({super.key, required this.station, required this.onTap});

  final Station station;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SoftCard(
      onTap: onTap,
      child: Row(
        children: [
          const IconBadge(icon: Icons.ev_station_rounded, size: 48),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  station.id,
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(_details, style: _detailsStyle(theme)),
                const SizedBox(height: AppSpacing.sm),
                _availability,
              ],
            ),
          ),
          const Icon(Icons.chevron_right, color: AppColors.neutral),
        ],
      ),
    );
  }

  TextStyle? _detailsStyle(ThemeData theme) =>
      theme.textTheme.bodySmall?.copyWith(color: AppColors.textSecondary);

  String get _details => [
    if (station.distanceKm != null)
      '${station.distanceKm!.toStringAsFixed(1)} ${Strings.distanceUnit}',
    if (station.maxPowerKw != null)
      '${station.maxPowerKw!.toStringAsFixed(0)} ${Strings.powerUnit}',
  ].join(' · ');

  Widget get _availability => station.online
      ? StatusChip(
          label:
              '${Strings.available} ${station.availableConnectors}/${station.totalConnectors}',
          color: station.availableConnectors > 0
              ? AppColors.success
              : AppColors.primary,
        )
      : const StatusChip(
          label: Strings.stationOffline,
          color: AppColors.neutral,
        );
}
