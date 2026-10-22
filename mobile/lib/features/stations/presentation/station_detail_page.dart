import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import 'connector_tile.dart';
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
      appBar: AppBar(title: Text(stationId)),
      body: AsyncBody(
        value: station,
        onRetry: () => ref.invalidate(stationProvider(stationId)),
        builder: (data) => ListView(
          padding: const EdgeInsets.all(AppSpacing.md),
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
            for (final connector in data.connectors) ...[
              ConnectorTile(
                connector: connector,
                highlighted: connector.connectorId == highlightedConnectorId,
                onStart: () => context.push(
                  Routes.chargeSetupPath(
                    stationId,
                    connectorId: connector.connectorId,
                  ),
                ),
              ),
              const SizedBox(height: AppSpacing.sm),
            ],
          ],
        ),
      ),
    );
  }
}
