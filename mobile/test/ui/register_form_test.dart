import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/auth/presentation/register_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';

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

  testWidgets('the email is required and the phone is not', (tester) async {
    await pumpApp(tester, const RegisterPage(), overrides: overrides());

    await _type(tester, 0, 'Aline');
    await _type(tester, 1, 'Obame');
    await _type(tester, 4, 'Secret123!');
    await _submit(tester);

    expect(find.text(Strings.required), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('a malformed email is refused', (tester) async {
    await pumpApp(tester, const RegisterPage(), overrides: overrides());

    await _type(tester, 0, 'Aline');
    await _type(tester, 1, 'Obame');
    await _type(tester, 2, 'not-an-email');
    await _type(tester, 4, 'Secret123!');
    await _submit(tester);

    expect(find.text(Strings.invalidEmail), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('a phone number with letters is refused', (tester) async {
    await pumpApp(tester, const RegisterPage(), overrides: overrides());

    await _type(tester, 0, 'Aline');
    await _type(tester, 1, 'Obame');
    await _type(tester, 2, 'aline@example.com');
    await _type(tester, 3, 'abc');
    await _type(tester, 4, 'Secret123!');
    await _submit(tester);

    expect(find.text(Strings.invalidPhone), findsOneWidget);
    await closeApp(tester);
  });
}
