import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'station_list_skeleton.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/filter_chips.dart';
import '../../../shared/widgets/page_header.dart';
import '../../../shared/widgets/search_field.dart';
import '../domain/station.dart';
import '../domain/station_filter.dart';
import 'station_list.dart';
import 'station_providers.dart';

class LocationsPage extends ConsumerStatefulWidget {
  const LocationsPage({super.key});

  @override
  ConsumerState<LocationsPage> createState() => _LocationsPageState();
}

class _LocationsPageState extends ConsumerState<LocationsPage> {
  String _query = '';
  int _filter = 0;

  List<Station> _visible(List<Station> all) {
    final found = StationFilter.byQuery(all, _query);
    return _filter == 1 ? StationFilter.availableOnly(found) : found;
  }

  @override
  Widget build(BuildContext context) {
    final stations = ref.watch(nearbyStationsProvider);
    return SafeArea(
      child: Column(
        children: [
          PageHeader(title: StationText.exploreLocations),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md),
            child: SearchField(
              hint: StationText.searchStations,
              onChanged: (value) => setState(() => _query = value),
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          FilterChips(
            labels: [StationText.filterAll, StationText.filterAvailable],
            selected: _filter,
            onSelected: (value) => setState(() => _filter = value),
          ),
          Expanded(
            child: RefreshIndicator.adaptive(
              onRefresh: () => ref.refresh(nearbyStationsProvider.future),
              child: AsyncBody(
                value: stations,
                onRetry: () => ref.invalidate(nearbyStationsProvider),
                loading: ListView(
                  padding: const EdgeInsets.all(AppSpacing.md),
                  children: const [StationListSkeleton(count: 6)],
                ),
                builder: (data) => _list(context, _visible(data)),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _list(BuildContext context, List<Station> shown) {
    return ListView(
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        if (shown.isNotEmpty)
          Padding(
            padding: const EdgeInsets.only(bottom: AppSpacing.sm),
            child: Text(
              StationText.count(shown.length),
              style: Theme.of(
                context,
              ).textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
            ),
          ),
        StationList(stations: shown),
      ],
    );
  }
}
