import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/page_header.dart';
import '../domain/station_filter.dart';
import 'station_list.dart';
import 'station_providers.dart';
import '../../../core/theme/app_icons.dart';

class LocationsPage extends ConsumerStatefulWidget {
  const LocationsPage({super.key});

  @override
  ConsumerState<LocationsPage> createState() => _LocationsPageState();
}

class _LocationsPageState extends ConsumerState<LocationsPage> {
  String _query = '';

  @override
  Widget build(BuildContext context) {
    final stations = ref.watch(nearbyStationsProvider);
    return SafeArea(
      child: Column(
        children: [
          PageHeader(title: StationText.exploreLocations),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md),
            child: TextField(
              onChanged: (value) => setState(() => _query = value),
              decoration: InputDecoration(
                hintText: StationText.searchStations,
                prefixIcon: Icon(AppIcons.search),
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          Expanded(
            child: RefreshIndicator(
              onRefresh: () => ref.refresh(nearbyStationsProvider.future),
              child: AsyncBody(
                value: stations,
                onRetry: () => ref.invalidate(nearbyStationsProvider),
                builder: (data) => ListView(
                  padding: const EdgeInsets.symmetric(
                    horizontal: AppSpacing.md,
                  ),
                  children: [
                    StationList(stations: StationFilter.byQuery(data, _query)),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
