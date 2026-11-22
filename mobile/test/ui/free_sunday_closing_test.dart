import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/coupons/presentation/coupons_page.dart';
import 'package:zella/features/home/presentation/home_page.dart';

import 'free_sunday_support.dart';
import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

void main() {
  setUpAll(loadTestFonts);

  group('Closing notice', () {
    Future<void> pumpAt(
      WidgetTester tester,
      Widget page,
      Duration beforeEnd,
      FakeFreeSunday fake,
    ) => pumpApp(
      tester,
      page,
      overrides: overridesWith(
        freeSunday: fake,
        extra: clockAt(endsAt.subtract(beforeEnd)),
      ),
    );

    final closing = FreeSundayText.closingSoon('22:00');

    for (final entry in {
      'home': const Scaffold(body: HomePage()),
      'coupons': const CouponsPage(),
    }.entries) {
      testWidgets('${entry.key} shows it 29 minutes before the end', (
        tester,
      ) async {
        await pumpAt(
          tester,
          entry.value,
          const Duration(minutes: 29),
          availableFake(),
        );
        expect(find.text(closing), findsOneWidget);
        await closeApp(tester);
      });

      testWidgets('${entry.key} hides it 31 minutes before the end', (
        tester,
      ) async {
        await pumpAt(
          tester,
          entry.value,
          const Duration(minutes: 31),
          availableFake(),
        );
        expect(find.text(closing), findsNothing);
        await closeApp(tester);
      });

      testWidgets('${entry.key} hides it after the end', (tester) async {
        await pumpAt(
          tester,
          entry.value,
          const Duration(minutes: -1),
          availableFake(),
        );
        expect(find.text(closing), findsNothing);
        await closeApp(tester);
      });
    }

    testWidgets('coupons hides it when the offer is not available', (
      tester,
    ) async {
      await pumpAt(
        tester,
        const CouponsPage(),
        const Duration(minutes: 10),
        reasonFake('SUNDAY_USED'),
      );
      expect(find.text(closing), findsNothing);
      await closeApp(tester);
    });
  });
}
