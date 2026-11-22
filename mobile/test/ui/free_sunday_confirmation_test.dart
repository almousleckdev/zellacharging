import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/app_language.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/charging/presentation/setup/charge_setup_page.dart';

import 'free_sunday_support.dart';
import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

const _allReasons = [
  'ELIGIBLE',
  'NO_AGREEMENT',
  'AGREEMENT_SUSPENDED',
  'AGREEMENT_NOT_STARTED',
  'AGREEMENT_ENDED',
  'OUTSIDE_WINDOW',
  'UNPAID_DAYS',
  'DRIVER_SUSPENDED',
  'NOT_LINKED',
  'UNAVAILABLE',
  'SUNDAY_USED',
];

void main() {
  setUpAll(loadTestFonts);

  group('Charge confirmation', () {
    testWidgets('shows the free line only when available', (tester) async {
      await pumpApp(
        tester,
        const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
        overrides: overridesWith(freeSunday: availableFake()),
      );
      await tester.pump(const Duration(milliseconds: 600));
      expect(find.text(FreeSundayText.chargeIsFree), findsOneWidget);
      await closeApp(tester);

      await pumpApp(
        tester,
        const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
        overrides: overridesWith(freeSunday: reasonFake('SUNDAY_USED')),
      );
      await tester.pump(const Duration(milliseconds: 600));
      expect(find.text(FreeSundayText.chargeIsFree), findsNothing);
      await closeApp(tester);
    });

    testWidgets('stays hidden when the request fails or is rate limited', (
      tester,
    ) async {
      for (final failure in [StateError('offline'), rateLimited()]) {
        await pumpApp(
          tester,
          const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
          overrides: overridesWith(
            freeSunday: FakeFreeSunday(availableOffer, failure: failure),
          ),
        );
        await tester.pump(const Duration(milliseconds: 600));
        expect(find.text(FreeSundayText.chargeIsFree), findsNothing);
        expect(find.text(Strings.unknownError), findsNothing);
        await closeApp(tester);
      }
    });

    testWidgets('asks the server once when the screen opens', (tester) async {
      final fake = availableFake();
      await pumpApp(
        tester,
        const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
        overrides: overridesWith(freeSunday: fake),
      );
      await tester.pump(const Duration(seconds: 6));
      expect(fake.calls, 1);
      await closeApp(tester);
    });

    testWidgets('shows the closing notice near the end of the window', (
      tester,
    ) async {
      await pumpApp(
        tester,
        const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
        overrides: overridesWith(
          freeSunday: availableFake(),
          extra: clockAt(endsAt.subtract(const Duration(minutes: 29))),
        ),
      );
      await tester.pump(const Duration(milliseconds: 600));
      expect(find.text(FreeSundayText.closingSoon('22:00')), findsOneWidget);
      await closeApp(tester);
    });
  });

  group('Texts', () {
    test('every reason code has a French and an English text', () {
      for (final reason in _allReasons) {
        AppLanguage.current = AppLanguage.fr;
        final fr = FreeSundayText.reasonOf(reason);
        AppLanguage.current = AppLanguage.en;
        final en = FreeSundayText.reasonOf(reason);
        AppLanguage.current = AppLanguage.fr;
        expect(fr, isNotEmpty, reason: reason);
        expect(en, isNotEmpty, reason: reason);
        if (reason != 'UNAVAILABLE') {
          expect(fr, isNot(FreeSundayText.reasonOf('UNKNOWN')), reason: reason);
        }
      }
    });

    test('the closing stop reason is translated', () {
      expect(
        HistoryText.stopReasonOf('FREE_WINDOW_ENDED', false),
        'Fin du créneau gratuit',
      );
      AppLanguage.current = AppLanguage.en;
      expect(
        HistoryText.stopReasonOf('FREE_WINDOW_ENDED', false),
        'Free window ended',
      );
      AppLanguage.current = AppLanguage.fr;
    });
  });
}
