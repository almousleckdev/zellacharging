import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/core/theme/app_theme.dart';
import 'package:ndzela/features/charging/presentation/charging_page.dart';

import 'test_fonts.dart';
import 'screens.dart';

Future<void> _openSheet(WidgetTester tester) async {
  tester.view.physicalSize = const Size(780, 1688);
  tester.view.devicePixelRatio = 2;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(
    ProviderScope(
      overrides: overrides(),
      child: MaterialApp(
        theme: AppTheme.light(),
        home: const ChargingPage(sessionId: 'a'),
      ),
    ),
  );
  await tester.pump(const Duration(milliseconds: 300));
  await tester.tap(find.text(Strings.stopCharging));
  await _settle(tester);
}

Future<void> _settle(WidgetTester tester) async {
  for (var i = 0; i < 8; i++) {
    await tester.pump(const Duration(milliseconds: 150));
  }
}

Future<void> _close(WidgetTester tester) async {
  await tester.pumpWidget(const SizedBox());
  await tester.pump(const Duration(seconds: 6));
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('stop asks for confirmation before stopping', (tester) async {
    await _openSheet(tester);
    expect(find.text(Strings.stopConfirmTitle), findsOneWidget);
    await tester.tap(find.text(Strings.keepCharging));
    await _settle(tester);
    expect(find.text(Strings.stopConfirmTitle), findsNothing);
    await _close(tester);
  });

  testWidgets('confirming closes the sheet', (tester) async {
    await _openSheet(tester);
    await tester.tap(find.text(Strings.stopCharging).last);
    await _settle(tester);
    expect(find.text(Strings.stopConfirmTitle), findsNothing);
    await _close(tester);
  });
}
