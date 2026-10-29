import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:zella/firebase_options.dart';

import 'app.dart';
import 'core/l10n/app_language.dart';
import 'core/l10n/language_controller.dart';
import 'core/storage/secure_preference_store.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);

  AppLanguage.current = AppLanguage.fromCode(
    await SecurePreferenceStore().read(LanguageController.storageKey),
  );
  runApp(const ProviderScope(child: ZellaApp()));
}
