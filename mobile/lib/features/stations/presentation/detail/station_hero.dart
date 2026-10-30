import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/status_chip.dart';
import '../../domain/station.dart';
import 'station_image.dart';

class StationHero extends StatelessWidget {
  const StationHero({super.key, required this.station});

  final Station station;

  @override
  Widget build(BuildContext context) {
    return AspectRatio(
      aspectRatio: 4 / 3,
      child: Stack(
        fit: StackFit.expand,
        children: [
          Image.asset(
            StationImage.forCountry(station.isoCode),
            fit: BoxFit.cover,
          ),
          DecoratedBox(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  AppColors.textPrimary.withValues(alpha: 0.25),
                  AppColors.textPrimary.withValues(alpha: 0),
                  AppColors.textPrimary.withValues(alpha: 0),
                  AppColors.textPrimary.withValues(alpha: 0.35),
                ],
                stops: const [0, 0.3, 0.7, 1],
              ),
            ),
          ),
          Positioned(
            left: AppSpacing.md,
            bottom: AppSpacing.md,
            child: DecoratedBox(
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(AppSpacing.radius),
              ),
              child: StatusChip(
                label: station.online
                    ? StationDetailText.online
                    : StationText.stationOffline,
                color: station.online ? AppColors.success : AppColors.neutral,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
