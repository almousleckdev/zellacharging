import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/auth/presentation/auth_providers.dart';
import 'package:zella/features/auth/presentation/register_page.dart';
import 'package:zella/shared/otp_delivery.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

Future<void> _type(WidgetTester tester, int field, String text) async {
  await tester.enterText(find.byType(TextFormField).at(field), text);
}

Future<void> _submit(WidgetTester tester) async {
  await tester.ensureVisible(find.text(AuthText.createAccount).last);
  await tester.tap(find.text(AuthText.createAccount).last);
  await settle(tester);
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('the contact is required', (tester) async {
    await pumpApp(tester, const RegisterPage(), overrides: overrides());

    await _type(tester, 0, 'Aline');
    await _type(tester, 1, 'Obame');
    await _type(tester, 3, 'Secret123!');
    await _type(tester, 4, 'Secret123!');
    await _submit(tester);

    expect(find.text(Strings.required), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the phone tab offers a country picker that starts on Gabon', (
    tester,
  ) async {
    await pumpApp(tester, const RegisterPage(), overrides: overrides());

    await tester.tap(find.text(AuthText.signUpWithPhone));
    await settle(tester);

    expect(find.text('+241'), findsOneWidget);
    expect(find.text(AuthText.phoneNumberLabel), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('a phone registration sends the full number and the chosen way', (
    tester,
  ) async {
    final auth = FakeAuth();
    await pumpApp(
      tester,
      const RegisterPage(),
      overrides: [
        ...overrides(),
        authRepositoryProvider.overrideWithValue(auth),
      ],
    );

    await tester.tap(find.text(AuthText.signUpWithPhone));
    await settle(tester);
    await _type(tester, 0, 'Aline');
    await _type(tester, 1, 'Obame');
    await _type(tester, 2, '077 12 34 56');
    await _type(tester, 3, 'Secret123!');
    await _type(tester, 4, 'Secret123!');
    await tester.ensureVisible(find.byType(Checkbox).first);
    await tester.tap(find.byType(Checkbox).first);
    await settle(tester);
    await _submit(tester);
    await tester.tap(find.text(ProfileText.otpViaSms));
    await settle(tester);

    expect(auth.registered.single.phone, '+24177123456');
    expect(auth.registered.single.email, isNull);
    expect(auth.registered.single.delivery, OtpDelivery.sms);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('different passwords are refused', (tester) async {
    await pumpApp(tester, const RegisterPage(), overrides: overrides());

    await _type(tester, 0, 'Aline');
    await _type(tester, 1, 'Obame');
    await _type(tester, 2, 'aline@example.com');
    await _type(tester, 3, 'Secret123!');
    await _type(tester, 4, 'Other123!');
    await _submit(tester);

    expect(find.text(AuthText.passwordsDiffer), findsOneWidget);
    await closeApp(tester);
  });
}
