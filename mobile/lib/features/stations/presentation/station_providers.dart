import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/location/location_providers.dart';
import '../../../core/network/network_providers.dart';
import '../data/station_repository_impl.dart';
import '../domain/station.dart';
import '../domain/station_repository.dart';

final stationRepositoryProvider = Provider<StationRepository>(
  (ref) => StationRepositoryImpl(ref.watch(dioProvider)),
);

final nearbyStationsProvider = FutureProvider<List<Station>>((ref) async {
  final location = await ref.watch(userLocationProvider.future);
  final repository = ref.watch(stationRepositoryProvider);
  final nearby = await repository.nearby(location);
  if (nearby.isNotEmpty || location == null) return nearby;
  return repository.nearby(null);
});

final stationProvider = FutureProvider.family<Station, String>(
  (ref, id) => ref.watch(stationRepositoryProvider).byId(id),
);
