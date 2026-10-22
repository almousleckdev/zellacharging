import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/app_language.dart';
import 'package:ndzela/core/l10n/language_controller.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';
import 'package:ndzela/features/profile/presentation/profile_page.dart';
import 'package:ndzela/features/support/domain/support_info.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('lays out the profile like the design, logout last', (
    tester,
  ) async {
    await pumpApp(tester, const ProfilePage(), overrides: overrides());

    final order = [
      Strings.profile,
      ProfileText.myCoupons,
      ProfileText.contactUs,
      ProfileText.language,
      ProfileText.agreement,
      ProfileText.settingsTitle,
      ProfileText.logOut,
    ];
    final tops = [for (final text in order) topOf(tester, text)];
    expect(tops, orderedEquals([...tops]..sort()));
    expect(find.text(ProfileText.myWallet), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the tiles show the coupons count and the wallet balance', (
    tester,
  ) async {
    await pumpApp(tester, const ProfilePage(), overrides: overrides());

    expect(find.text('2'), findsOneWidget);
    expect(find.textContaining('984'), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('Android picks the language in a dialog and switches live', (
    tester,
  ) async {
    final preferences = MemoryPreferences();
    await pumpApp(
      tester,
      const ProfilePage(),
      overrides: overrides(SessionState.active, null, null, null, preferences),
    );

    await tester.tap(find.text(ProfileText.language));
    await settle(tester);
    expect(find.byType(SimpleDialog), findsOneWidget);
    expect(find.byType(CupertinoActionSheet), findsNothing);

    await tester.tap(find.text('English'));
    await settle(tester);

    expect(AppLanguage.current, AppLanguage.en);
    expect(find.text('Contact us'), findsOneWidget);
    expect(preferences.values[LanguageController.storageKey], 'en');
    await closeApp(tester);
  });

  testWidgets('iOS picks the language in a native action sheet', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const ProfilePage(),
      overrides: overrides(),
      platform: TargetPlatform.iOS,
    );

    await tester.tap(find.text(ProfileText.language));
    await settle(tester);

    expect(find.byType(CupertinoActionSheet), findsOneWidget);
    expect(find.byType(SimpleDialog), findsNothing);
    expect(find.text(Strings.cancel), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('logging out asks for confirmation', (tester) async {
    await pumpApp(tester, const ProfilePage(), overrides: overrides());

    await tester.tap(find.text(ProfileText.logOut));
    await settle(tester);
    expect(find.text(ProfileText.logOutTitle), findsOneWidget);

    await tester.tap(find.text(Strings.cancel));
    await settle(tester);
    expect(FakeSession.signOuts, 0);

    await tester.tap(find.text(ProfileText.logOut));
    await settle(tester);
    await tester.tap(find.text(ProfileText.logOut).last);
    await settle(tester);
    expect(FakeSession.signOuts, 1);
    await closeApp(tester);
  });

  testWidgets('the platform agreement opens its link', (tester) async {
    final links = FakeLinks();
    await pumpApp(
      tester,
      const ProfilePage(),
      overrides: overrides(SessionState.active, null, null, links),
    );

    await tester.tap(find.text(ProfileText.agreement));
    await settle(tester);

    expect(links.opened.single.toString(), 'https://ndzela.example/terms');
    await closeApp(tester);
  });

  testWidgets('without a configured agreement a message is shown', (
    tester,
  ) async {
    final links = FakeLinks();
    await pumpApp(
      tester,
      const ProfilePage(),
      overrides: overrides(
        SessionState.active,
        null,
        null,
        links,
        null,
        FakeSupport(const SupportInfo()),
      ),
    );

    await tester.tap(find.text(ProfileText.agreement));
    await settle(tester);

    expect(find.text(ProfileText.agreementUnavailable), findsOneWidget);
    expect(links.opened, isEmpty);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });
}
