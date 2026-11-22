import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../data/free_sunday_repository_impl.dart';
import '../domain/free_sunday_offer.dart';
import '../domain/free_sunday_repository.dart';

final freeSundayClockProvider = Provider<DateTime Function()>(
  (ref) => DateTime.now,
);

final freeSundayRepositoryProvider = Provider<FreeSundayRepository>(
  (ref) => FreeSundayRepositoryImpl(ref.watch(dioProvider)),
);

final freeSundayProvider = FutureProvider.autoDispose<FreeSundayOffer>((
  ref,
) async {
  try {
    return await ref.watch(freeSundayRepositoryProvider).current();
  } catch (_) {
    return FreeSundayOffer.unavailable;
  }
});
