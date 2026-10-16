import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'coordinates.dart';
import 'location_service.dart';

final locationServiceProvider = Provider<LocationService>(
  (ref) => GeolocatorLocationService(),
);

final userLocationProvider = FutureProvider<Coordinates?>((ref) async {
  try {
    return await ref
        .watch(locationServiceProvider)
        .currentPosition()
        .timeout(const Duration(seconds: 8));
  } catch (_) {
    return null;
  }
});
