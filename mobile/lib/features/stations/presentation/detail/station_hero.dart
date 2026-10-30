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
    final photo = StationImage.forCountry(station.isoCode);
    return ClipRRect(
      borderRadius: BorderRadius.circular(AppSpacing.radius),
      child: AspectRatio(
        aspectRatio: 16 / 9,
        child: Stack(
          fit: StackFit.expand,
          children: [
            photo == null
                ? _placeholder()
                : Image.asset(photo, fit: BoxFit.cover),
            Positioned(
              top: AppSpacing.sm,
              left: AppSpacing.sm,
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
      ),
    );
  }

  Widget _placeholder() => const ColoredBox(
    color: AppColors.primarySoft,
    child: Center(
      child: Image(
        image: AssetImage(StationImage.fallback),
        width: 96,
        height: 96,
      ),
    ),
  );
}
