import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/empty_state.dart';
import '../domain/station.dart';
import 'station_card.dart';
import '../../../core/theme/app_icons.dart';

class StationList extends StatelessWidget {
  const StationList({super.key, required this.stations});

  final List<Station> stations;

  @override
  Widget build(BuildContext context) {
    if (stations.isEmpty) {
      return EmptyState(
        icon: AppIcons.crosshair,
        message: StationText.noStations,
      );
    }
    return Column(
      children: [
        for (final station in stations) ...[
          StationCard(
            station: station,
            onTap: () => context.push(Routes.stationPath(station.id)),
          ),
          const SizedBox(height: AppSpacing.sm),
        ],
      ],
    );
  }
}
