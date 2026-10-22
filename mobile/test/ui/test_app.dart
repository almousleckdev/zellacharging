import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/misc.dart' show Override;
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/app_language.dart';
import 'package:ndzela/core/l10n/language_controller.dart';
import 'package:ndzela/core/session/session_controller.dart';
import 'package:ndzela/core/theme/app_theme.dart';

import 'ui_fakes.dart';

Future<void> pumpApp(
  WidgetTester tester,
  Widget home, {
  required List<Override> overrides,
  TargetPlatform platform = TargetPlatform.android,
}) async {
  FakeSession.signOuts = 0;
  tester.view.physicalSize = const Size(780, 1688);
  tester.view.devicePixelRatio = 2;
  addTearDown(tester.view.reset);
  addTearDown(() => AppLanguage.current = AppLanguage.fr);
  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        ...overrides,
        sessionControllerProvider.overrideWith(FakeSession.new),
      ],
      child: Consumer(
        builder: (context, ref, _) {
          final language = ref.watch(languageProvider);
          return MaterialApp(
            key: ValueKey(language),
            theme: AppTheme.light().copyWith(platform: platform),
            locale: language.locale,
            supportedLocales: [for (final l in AppLanguage.values) l.locale],
            localizationsDelegates: GlobalMaterialLocalizations.delegates,
            home: home,
          );
        },
      ),
    ),
  );
  await settle(tester);
}

Future<void> settle(WidgetTester tester) async {
  for (var i = 0; i < 6; i++) {
    await tester.pump(const Duration(milliseconds: 150));
  }
}

Future<void> closeApp(WidgetTester tester) async {
  await tester.pumpWidget(const SizedBox());
  await tester.pump(const Duration(seconds: 6));
}

double topOf(WidgetTester tester, String text) =>
    tester.getTopLeft(find.text(text).first).dy;
