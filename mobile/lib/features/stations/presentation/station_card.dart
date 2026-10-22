import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import '../../../shared/widgets/meta_pill.dart';
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
          IconBadge(
            icon: AppIcons.chargingStation,
            size: 48,
            color: station.online ? AppColors.chargeDeep : AppColors.neutral,
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  station.id,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: AppSpacing.sm),
                Wrap(
                  spacing: AppSpacing.xs + 2,
                  runSpacing: AppSpacing.xs + 2,
                  children: [
                    if (station.distanceKm != null)
                      MetaPill(
                        icon: AppIcons.mapPin,
                        label:
                            '${station.distanceKm!.toStringAsFixed(1)} ${Strings.distanceUnit}',
                      ),
                    if (station.maxPowerKw != null)
                      MetaPill(
                        icon: AppIcons.lightningLine,
                        label:
                            '${station.maxPowerKw!.toStringAsFixed(0)} ${Strings.powerUnit}',
                      ),
                    _availability,
                  ],
                ),
              ],
            ),
          ),
          const Icon(AppIcons.caretRight, color: AppColors.neutral, size: 20),
        ],
      ),
    );
  }

  Widget get _availability => station.online
      ? StatusChip(
          label:
              '${StationText.available} ${station.availableConnectors}/${station.totalConnectors}',
          color: station.availableConnectors > 0
              ? AppColors.success
              : AppColors.primary,
        )
      : StatusChip(label: StationText.stationOffline, color: AppColors.neutral);
}
