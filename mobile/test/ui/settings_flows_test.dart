import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';
import 'package:ndzela/features/profile/presentation/change_password_page.dart';
import 'package:ndzela/features/profile/presentation/delete_account_page.dart';
import 'package:ndzela/features/profile/presentation/notifications_controller.dart';
import 'package:ndzela/features/profile/presentation/settings_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

Future<void> _type(WidgetTester tester, int field, String text) async {
  await tester.enterText(find.byType(TextFormField).at(field), text);
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('settings hold notifications, password and account deletion', (
    tester,
  ) async {
    await pumpApp(tester, const SettingsPage(), overrides: overrides());

    expect(find.text(ProfileText.notificationsTitle), findsWidgets);
    expect(find.text(ProfileText.changePassword), findsOneWidget);
    expect(find.text(ProfileText.deleteAccount), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the notifications switch is on by default and is remembered', (
    tester,
  ) async {
    final preferences = MemoryPreferences();
    await pumpApp(
      tester,
      const SettingsPage(),
      overrides: overrides(SessionState.active, null, null, null, preferences),
    );

    expect(tester.widget<Switch>(find.byType(Switch)).value, isTrue);
    await tester.tap(find.byType(Switch));
    await settle(tester);

    expect(tester.widget<Switch>(find.byType(Switch)).value, isFalse);
    expect(preferences.values[NotificationsController.storageKey], 'off');
    await closeApp(tester);
  });

  testWidgets('the switch is a native switch on iOS', (tester) async {
    await pumpApp(
      tester,
      const SettingsPage(),
      overrides: overrides(),
      platform: TargetPlatform.iOS,
    );

    final adaptive = tester.widget<Switch>(find.byType(Switch));
    expect(adaptive.value, isTrue);
    expect(find.byType(Switch), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('changing the password needs the right current password', (
    tester,
  ) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const ChangePasswordPage(),
      overrides: overrides(SessionState.active, profile),
    );

    await _type(tester, 0, 'wrong');
    await _type(tester, 1, 'NewPass456');
    await tester.tap(find.text(ProfileText.saveChanges));
    await settle(tester);

    expect(find.text(ErrorText.byCode['WRONG_PASSWORD']!), findsOneWidget);
    expect(profile.passwordChanges, isEmpty);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('a valid change is sent and a too short password is refused', (
    tester,
  ) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const ChangePasswordPage(),
      overrides: overrides(SessionState.active, profile),
    );

    await _type(tester, 0, 'OldPass123');
    await _type(tester, 1, 'short');
    await tester.tap(find.text(ProfileText.saveChanges));
    await settle(tester);
    expect(find.text(Strings.shortPassword), findsOneWidget);
    expect(profile.passwordChanges, isEmpty);

    await _type(tester, 1, 'NewPass456');
    await tester.tap(find.text(ProfileText.saveChanges));
    await settle(tester);
    expect(profile.passwordChanges, ['NewPass456']);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('deleting the account asks for confirmation then signs out', (
    tester,
  ) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const DeleteAccountPage(),
      overrides: overrides(SessionState.active, profile),
    );

    await _type(tester, 0, 'OldPass123');
    await tester.tap(find.text(ProfileText.deleteAccountConfirm));
    await settle(tester);
    expect(find.text(ProfileText.deleteAccountTitle), findsOneWidget);
    expect(profile.closedWith, isEmpty);

    await tester.tap(find.text(Strings.cancel));
    await settle(tester);
    expect(profile.closedWith, isEmpty);
    expect(FakeSession.signOuts, 0);

    await tester.tap(find.text(ProfileText.deleteAccountConfirm));
    await settle(tester);
    await tester.tap(find.text(ProfileText.deleteAccountConfirm).last);
    await settle(tester);
    expect(profile.closedWith, ['OldPass123']);
    expect(FakeSession.signOuts, 1);
    await closeApp(tester);
  });

  testWidgets('a wrong password does not delete the account', (tester) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const DeleteAccountPage(),
      overrides: overrides(SessionState.active, profile),
    );

    await _type(tester, 0, 'wrong');
    await tester.tap(find.text(ProfileText.deleteAccountConfirm));
    await settle(tester);
    await tester.tap(find.text(ProfileText.deleteAccountConfirm).last);
    await settle(tester);

    expect(find.text(ErrorText.byCode['WRONG_PASSWORD']!), findsOneWidget);
    expect(FakeSession.signOuts, 0);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('the password is required to delete the account', (tester) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const DeleteAccountPage(),
      overrides: overrides(SessionState.active, profile),
    );

    await tester.tap(find.text(ProfileText.deleteAccountConfirm));
    await settle(tester);

    expect(find.text(Strings.required), findsOneWidget);
    expect(find.text(ProfileText.deleteAccountTitle), findsNothing);
    await closeApp(tester);
  });
}
