import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/pay/pay_card.dart';
import '../../../../shared/widgets/status_chip.dart';
import '../../../stations/domain/station.dart';
import '../../../stations/presentation/detail/station_image.dart';

class StationHeaderCard extends StatelessWidget {
  const StationHeaderCard({super.key, required this.station});

  final Station station;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final power = station.maxPowerKw;
    return PayCard(
      child: Row(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
            child: Image.asset(
              StationImage.forCountry(station.isoCode),
              width: 56,
              height: 56,
              fit: BoxFit.cover,
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  station.id,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Wrap(
                  spacing: AppSpacing.sm,
                  runSpacing: AppSpacing.xs,
                  children: [
                    StatusChip(
                      label: station.online
                          ? PayText.stationOnline
                          : PayText.stationOffline,
                      color: station.online
                          ? AppColors.success
                          : AppColors.danger,
                    ),
                    if (power != null)
                      StatusChip(
                        label:
                            '${power.toStringAsFixed(0)} ${Strings.powerUnit}',
                        color: AppColors.primary,
                      ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
