import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/format/date_format.dart';
import 'package:ndzela/core/format/money_format.dart';
import 'package:ndzela/core/l10n/app_language.dart';
import 'package:ndzela/core/l10n/language_controller.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/core/storage/preference_providers.dart';
import 'package:ndzela/core/storage/preference_store.dart';

class _MemoryStore implements PreferenceStore {
  final saved = <String, String>{};

  @override
  Future<String?> read(String key) async => saved[key];

  @override
  Future<void> save(String key, String value) async => saved[key] = value;
}

void main() {
  tearDown(() => AppLanguage.current = AppLanguage.fr);

  test('French is the default language', () {
    expect(AppLanguage.current, AppLanguage.fr);
    expect(AuthText.signIn, 'Connexion');
    expect(Strings.profile, 'Profil');
    expect(AppLanguage.fromCode(null), AppLanguage.fr);
    expect(AppLanguage.fromCode('de'), AppLanguage.fr);
  });

  test('English is available and texts switch with the language', () {
    AppLanguage.current = AppLanguage.en;

    expect(AuthText.signIn, 'Login');
    expect(Strings.profile, 'Profile');
    expect(ProfileText.logOut, 'Log out');
    expect(HistoryText.filters.first, 'All');
    expect(ErrorText.byCode['INSUFFICIENT_FUNDS'], contains('wallet'));
  });

  test('every error code has a French and an English message', () {
    final french = ErrorText.byCode;
    AppLanguage.current = AppLanguage.en;
    final english = ErrorText.byCode;

    expect(french.keys, english.keys);
    for (final code in french.keys) {
      expect(french[code], isNotEmpty);
      expect(english[code], isNotEmpty);
      expect(french[code], isNot(english[code]), reason: code);
    }
  });

  test('choosing a language switches it and remembers the choice', () async {
    final store = _MemoryStore();
    final container = ProviderContainer(
      overrides: [preferenceStoreProvider.overrideWithValue(store)],
    );
    addTearDown(container.dispose);

    await container.read(languageProvider.notifier).select(AppLanguage.en);

    expect(container.read(languageProvider), AppLanguage.en);
    expect(AppLanguage.current, AppLanguage.en);
    expect(store.saved[LanguageController.storageKey], 'en');
  });

  test('amounts are written in FCFA with grouped digits', () {
    expect(MoneyFormat.format(1234567, 'XAF', 0), '1 234 567 FCFA');
    expect(MoneyFormat.format(950, 'XOF', 0), '950 FCFA');
    expect(MoneyFormat.format(0, 'XAF', 0), '0 FCFA');
  });

  test('decimals follow the language', () {
    expect(MoneyFormat.number(123456, 2), '1 234,56');
    AppLanguage.current = AppLanguage.en;
    expect(MoneyFormat.number(123456, 2), '1 234.56');
  });

  test('dates follow the language', () {
    final moment = DateTime(2026, 10, 16, 14, 5);

    expect(DateFormatter.dateTime(moment), '16/10/2026 14:05');
    AppLanguage.current = AppLanguage.en;
    expect(DateFormatter.dateTime(moment), '2026-10-16 14:05');
  });
}
