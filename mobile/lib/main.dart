import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app.dart';
import 'core/l10n/app_language.dart';
import 'core/l10n/language_controller.dart';
import 'core/storage/secure_preference_store.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  AppLanguage.current = AppLanguage.fromCode(
    await SecurePreferenceStore().read(LanguageController.storageKey),
  );
  runApp(const ProviderScope(child: NdzelaApp()));
}
