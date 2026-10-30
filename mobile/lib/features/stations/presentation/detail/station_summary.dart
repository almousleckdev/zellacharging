import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_icons.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/meta_pill.dart';
import '../../domain/station.dart';

class StationSummary extends StatelessWidget {
  const StationSummary({super.key, required this.station});

  final Station station;

  String? get _hardware {
    final parts = [station.vendor, station.model].whereType<String>();
    return parts.isEmpty ? null : parts.join(' · ');
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final hardware = _hardware;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          station.id,
          style: theme.textTheme.titleLarge?.copyWith(
            fontWeight: FontWeight.w800,
          ),
        ),
        if (hardware != null)
          Text(
            hardware,
            style: theme.textTheme.bodyMedium?.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
        const SizedBox(height: AppSpacing.sm),
        Wrap(
          spacing: AppSpacing.xs + 2,
          runSpacing: AppSpacing.xs + 2,
          children: [
            MetaPill(
              icon: AppIcons.plugs,
              label: StationDetailText.availableOf(
                station.availableConnectors,
                station.totalConnectors,
              ),
            ),
            if (station.maxPowerKw != null)
              MetaPill(
                icon: AppIcons.lightningLine,
                label:
                    '${station.maxPowerKw!.toStringAsFixed(0)} ${Strings.powerUnit}',
              ),
            if (station.distanceKm != null)
              MetaPill(
                icon: AppIcons.mapPin,
                label:
                    '${station.distanceKm!.toStringAsFixed(1)} ${Strings.distanceUnit}',
              ),
          ],
        ),
      ],
    );
  }
}
