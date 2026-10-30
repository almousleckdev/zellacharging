import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/storage/preference_providers.dart';

const onboardingSeenKey = 'onboarding_seen';

class SplashDone extends Notifier<bool> {
  @override
  bool build() => false;

  void finish() => state = true;
}

final splashDoneProvider = NotifierProvider<SplashDone, bool>(SplashDone.new);

final onboardingSeenProvider = FutureProvider<bool>(
  (ref) async =>
      await ref.read(preferenceStoreProvider).read(onboardingSeenKey) == '1',
);
