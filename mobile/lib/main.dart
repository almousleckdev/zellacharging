import 'dart:async';

import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:zella/firebase_options.dart';

import 'app.dart';
import 'core/l10n/app_language.dart';
import 'core/l10n/language_controller.dart';
import 'core/storage/secure_preference_store.dart';

void _report(Object error, StackTrace stack) {
  debugPrint('Unhandled error: $error\n$stack');
}

Future<void> _start() async {
  WidgetsFlutterBinding.ensureInitialized();
  FlutterError.onError = (details) {
    FlutterError.presentError(details);
    _report(details.exception, details.stack ?? StackTrace.empty);
  };
  PlatformDispatcher.instance.onError = (error, stack) {
    _report(error, stack);
    return true;
  };

  try {
    await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform,
    );
  } on Object catch (error, stack) {
    _report(error, stack);
  }

  AppLanguage.current = AppLanguage.fromCode(
    await SecurePreferenceStore().read(LanguageController.storageKey),
  );
  runApp(const ProviderScope(child: ZellaApp()));
}

void main() {
  runZonedGuarded(_start, _report);
}
