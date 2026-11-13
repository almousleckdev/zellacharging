import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/auth/presentation/auth_providers.dart';
import 'package:zella/features/auth/presentation/forgot_password_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

Future<FakeAuth> _open(WidgetTester tester) async {
  final auth = FakeAuth();
  await pumpApp(
    tester,
    const ForgotPasswordPage(),
    overrides: [...overrides(), authRepositoryProvider.overrideWithValue(auth)],
  );
  return auth;
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('a phone number asks how to receive the reset code', (
    tester,
  ) async {
    final auth = await _open(tester);

    await tester.enterText(find.byType(TextFormField), '+24177123456');
    await tester.tap(find.text(AuthText.sendCode));
    await settle(tester);
    await tester.tap(find.text(ProfileText.otpViaWhatsapp));
    await settle(tester);

    expect(auth.resets, ['+24177123456:WHATSAPP']);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('an email gets its code without choosing a way', (tester) async {
    final auth = await _open(tester);

    await tester.enterText(find.byType(TextFormField), 'aline@example.com');
    await tester.tap(find.text(AuthText.sendCode));
    await settle(tester);

    expect(find.text(ProfileText.otpSheetTitle), findsNothing);
    expect(auth.resets, ['aline@example.com:null']);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });
}
