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

  testWidgets('the email and the phone are both required', (tester) async {
    await pumpApp(tester, const RegisterPage(), overrides: overrides());

    await _type(tester, 0, 'Aline');
    await _type(tester, 1, 'Obame');
    await _type(tester, 4, 'Secret123!');
    await _type(tester, 5, 'Secret123!');
    await _submit(tester);

    expect(find.text(Strings.required), findsNWidgets(2));
    await closeApp(tester);
  });

  testWidgets('the phone is fixed to Gabon with no country picker', (
    tester,
  ) async {
    await pumpApp(tester, const RegisterPage(), overrides: overrides());

    expect(find.text('+241'), findsOneWidget);
    expect(find.text(AuthText.phoneNumberLabel), findsOneWidget);
    expect(find.text(AuthText.emailLabel), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('a registration sends both contacts and the chosen way', (
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

    await _type(tester, 0, 'Aline');
    await _type(tester, 1, 'Obame');
    await _type(tester, 2, 'aline@example.com');
    await _type(tester, 3, '077 12 34 56');
    await _type(tester, 4, 'Secret123!');
    await _type(tester, 5, 'Secret123!');
    await tester.ensureVisible(find.byType(Checkbox).first);
    await tester.tap(find.byType(Checkbox).first);
    await settle(tester);
    await _submit(tester);
    await tester.tap(find.text(ProfileText.otpViaSms));
    await settle(tester);

    expect(auth.registered.single.phone, '+24177123456');
    expect(auth.registered.single.email, 'aline@example.com');
    expect(auth.registered.single.delivery, OtpDelivery.sms);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('different passwords are refused', (tester) async {
    await pumpApp(tester, const RegisterPage(), overrides: overrides());

    await _type(tester, 0, 'Aline');
    await _type(tester, 1, 'Obame');
    await _type(tester, 2, 'aline@example.com');
    await _type(tester, 3, '077123456');
    await _type(tester, 4, 'Secret123!');
    await _type(tester, 5, 'Other123!');
    await _submit(tester);

    expect(find.text(AuthText.passwordsDiffer), findsOneWidget);
    await closeApp(tester);
  });
}
