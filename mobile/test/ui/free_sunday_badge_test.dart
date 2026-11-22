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

  group('My coupons', () {
    testWidgets('shows the card with the Gabon end time when available', (
      tester,
    ) async {
      await pumpApp(
        tester,
        const CouponsPage(),
        overrides: overridesWith(freeSunday: availableFake()),
      );

      expect(find.text(FreeSundayText.title), findsOneWidget);
      expect(find.text(FreeSundayText.availableUntil('22:00')), findsOneWidget);
      expect(
        find.text(FreeSundayText.explanationOf(noonToTen)),
        findsOneWidget,
      );
      expect(find.text('Première recharge offerte'), findsOneWidget);
      expect(
        topOf(tester, FreeSundayText.title),
        lessThan(topOf(tester, 'Première recharge offerte')),
      );
      await closeApp(tester);
    });

    for (final reason in ['SUNDAY_USED', 'UNPAID_DAYS', 'OUTSIDE_WINDOW']) {
      testWidgets('shows the reason text for $reason', (tester) async {
        await pumpApp(
          tester,
          const CouponsPage(),
          overrides: overridesWith(freeSunday: reasonFake(reason)),
        );

        expect(find.text(FreeSundayText.title), findsOneWidget);
        expect(find.text(FreeSundayText.reasonOf(reason)), findsOneWidget);
        await closeApp(tester);
      });
    }

    for (final reason in ['NOT_LINKED', 'NO_AGREEMENT']) {
      testWidgets('hides the card for $reason', (tester) async {
        await pumpApp(
          tester,
          const CouponsPage(),
          overrides: overridesWith(freeSunday: reasonFake(reason)),
        );

        expect(find.text(FreeSundayText.title), findsNothing);
        expect(find.text('Première recharge offerte'), findsOneWidget);
        await closeApp(tester);
      });
    }

    testWidgets('hides the card when the request fails', (tester) async {
      await pumpApp(
        tester,
        const CouponsPage(),
        overrides: overridesWith(
          freeSunday: FakeFreeSunday(
            availableOffer,
            failure: StateError('offline'),
          ),
        ),
      );

      expect(find.text(FreeSundayText.title), findsNothing);
      expect(find.text(Strings.retry), findsNothing);
      await closeApp(tester);
    });

    testWidgets('shows the card even without coupons', (tester) async {
      await pumpApp(
        tester,
        const CouponsPage(),
        overrides: overridesWith(
          freeSunday: availableFake(),
          couponList: FakeCoupons(),
        ),
      );

      expect(find.text(FreeSundayText.title), findsOneWidget);
      await closeApp(tester);
    });
  });

  group('Home badge', () {
    testWidgets('appears only when available', (tester) async {
      await pumpApp(
        tester,
        const Scaffold(body: HomePage()),
        overrides: overridesWith(freeSunday: availableFake()),
      );
      expect(find.text(FreeSundayText.badge('22:00')), findsOneWidget);
      await closeApp(tester);

      await pumpApp(
        tester,
        const Scaffold(body: HomePage()),
        overrides: overridesWith(freeSunday: reasonFake('SUNDAY_USED')),
      );
      expect(find.textContaining(FreeSundayText.title), findsNothing);
      await closeApp(tester);
    });

    testWidgets('stays hidden when the request fails or is rate limited', (
      tester,
    ) async {
      for (final failure in [StateError('offline'), rateLimited()]) {
        await pumpApp(
          tester,
          const Scaffold(body: HomePage()),
          overrides: overridesWith(
            freeSunday: FakeFreeSunday(availableOffer, failure: failure),
          ),
        );
        expect(find.textContaining(FreeSundayText.title), findsNothing);
        expect(find.textContaining(FreeSundayText.badgeNoTime), findsNothing);
        expect(find.text(Strings.unknownError), findsNothing);
        await closeApp(tester);
      }
    });
  });
}
