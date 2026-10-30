import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../domain/station.dart';
import 'connector_tile.dart';
import 'detail/station_hero.dart';
import 'detail/station_info_card.dart';
import 'detail/station_map_card.dart';
import 'detail/station_summary.dart';
import 'station_providers.dart';

class StationDetailPage extends ConsumerWidget {
  const StationDetailPage({
    super.key,
    required this.stationId,
    this.highlightedConnectorId,
  });

  final String stationId;
  final int? highlightedConnectorId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final station = ref.watch(stationProvider(stationId));
    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(backgroundColor: Colors.transparent),
      body: AsyncBody(
        value: station,
        onRetry: () => ref.invalidate(stationProvider(stationId)),
        builder: (data) => RefreshIndicator.adaptive(
          onRefresh: () async => ref.invalidate(stationProvider(stationId)),
          child: ListView(
            padding: EdgeInsets.zero,
            children: [
              StationHero(station: data),
              Padding(
                padding: const EdgeInsets.all(AppSpacing.md),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    StationSummary(station: data),
                    const SizedBox(height: AppSpacing.lg),
                    _connectors(context, data),
                    const SizedBox(height: AppSpacing.lg),
                    StationInfoCard(station: data),
                    if (data.latitude != null && data.longitude != null) ...[
                      const SizedBox(height: AppSpacing.md),
                      StationMapCard(
                        latitude: data.latitude!,
                        longitude: data.longitude!,
                      ),
                    ],
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _connectors(BuildContext context, Station station) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          StationText.plugInFirst,
          style: const TextStyle(color: AppColors.textSecondary),
        ),
        const SizedBox(height: AppSpacing.md),
        Text(
          StationText.connectors,
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: AppSpacing.md),
        for (final connector in station.connectors) ...[
          ConnectorTile(
            connector: connector,
            highlighted: connector.connectorId == highlightedConnectorId,
            onStart: () => context.push(
              Routes.chargeSetupPath(
                station.id,
                connectorId: connector.connectorId,
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
        ],
      ],
    );
  }
}
