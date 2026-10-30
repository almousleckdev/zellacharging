import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/links/link_providers.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_icons.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/app_toast.dart';
import '../../../../shared/widgets/primary_button.dart';
import '../../../../shared/widgets/soft_card.dart';
import 'directions_link.dart';
import 'map_providers.dart';

class StationMapCard extends ConsumerWidget {
  const StationMapCard({
    super.key,
    required this.latitude,
    required this.longitude,
  });

  final double latitude;
  final double longitude;

  Future<void> _navigate(BuildContext context, WidgetRef ref) async {
    final link = DirectionsLink.to(
      latitude,
      longitude,
      Theme.of(context).platform,
    );
    final opened = await ref.read(linkLauncherProvider).open(link);
    if (!opened && context.mounted) {
      AppToast.error(context, StationDetailText.directionsFailed);
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final point = LatLng(latitude, longitude);
    return SoftCard(
      padding: const EdgeInsets.all(AppSpacing.md),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            StationDetailText.location,
            style: Theme.of(
              context,
            ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800),
          ),
          const SizedBox(height: AppSpacing.sm),
          ClipRRect(
            borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
            child: SizedBox(
              height: 180,
              child: FlutterMap(
                options: MapOptions(
                  initialCenter: point,
                  initialZoom: 15,
                  interactionOptions: const InteractionOptions(
                    flags: InteractiveFlag.none,
                  ),
                ),
                children: [
                  TileLayer(
                    urlTemplate:
                        'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                    userAgentPackageName: 'com.zella.ocpp.mobile',
                    tileProvider: ref.watch(mapTileProvider),
                  ),
                  MarkerLayer(
                    markers: [
                      Marker(
                        point: point,
                        width: 40,
                        height: 40,
                        alignment: Alignment.topCenter,
                        child: const Icon(
                          AppIcons.mapPinFill,
                          size: 36,
                          color: AppColors.primary,
                        ),
                      ),
                    ],
                  ),
                  const _Attribution(),
                ],
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          PrimaryButton(
            label: StationDetailText.directions,
            icon: AppIcons.navigation,
            height: AppSpacing.compactButtonHeight,
            onPressed: () => _navigate(context, ref),
          ),
        ],
      ),
    );
  }
}

class _Attribution extends StatelessWidget {
  const _Attribution();

  @override
  Widget build(BuildContext context) {
    return Align(
      alignment: Alignment.bottomRight,
      child: ColoredBox(
        color: AppColors.surface.withValues(alpha: 0.8),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 1),
          child: Text(
            '© OpenStreetMap contributors',
            style: Theme.of(context).textTheme.labelSmall,
          ),
        ),
      ),
    );
  }
}
