import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'preference_store.dart';
import 'secure_preference_store.dart';

final preferenceStoreProvider = Provider<PreferenceStore>(
  (ref) => SecurePreferenceStore(),
);
