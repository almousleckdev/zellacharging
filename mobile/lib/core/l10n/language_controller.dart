import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../storage/preference_providers.dart';
import 'app_language.dart';

final languageProvider = NotifierProvider<LanguageController, AppLanguage>(
  LanguageController.new,
);

class LanguageController extends Notifier<AppLanguage> {
  static const storageKey = 'language';

  @override
  AppLanguage build() => AppLanguage.current;

  Future<void> select(AppLanguage language) async {
    AppLanguage.current = language;
    state = language;
    await ref.read(preferenceStoreProvider).save(storageKey, language.code);
  }
}
