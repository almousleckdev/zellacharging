import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/app_language.dart';
import 'package:zella/core/l10n/free_sunday_text.dart';
import 'package:zella/core/l10n/history_text.dart';
import 'package:zella/features/free_sunday/data/free_sunday_mapper.dart';
import 'package:zella/features/free_sunday/domain/free_sunday_offer.dart';
import 'package:zella/features/free_sunday/domain/free_sunday_window.dart';
import 'package:zella/features/free_sunday/presentation/free_sunday_time.dart';

const wholeSunday = FreeSundayWindow(
  day: 'SUNDAY',
  from: '00:00',
  until: '23:59',
  endOfDay: true,
);
const noonToTen = FreeSundayWindow(
  day: 'SUNDAY',
  from: '12:00',
  until: '22:00',
  endOfDay: false,
);

void main() {
  tearDown(() => AppLanguage.current = AppLanguage.fr);

  group('the window sent by the server', () {
    test('is read from the JSON of the offer', () {
      final offer = freeSundayOfferFromJson({
        'available': false,
        'reason': 'OUTSIDE_WINDOW',
        'endsAt': null,
        'window': {
          'day': 'SATURDAY',
          'from': '09:00',
          'until': '17:30',
          'endOfDay': false,
        },
      });

      expect(offer.window?.day, 'SATURDAY');
      expect(offer.window?.from, '09:00');
      expect(offer.window?.until, '17:30');
      expect(offer.window?.endOfDay, isFalse);
    });

    test('may be missing, for a customer outside the program', () {
      final offer = freeSundayOfferFromJson({
        'available': false,
        'reason': 'NOT_LINKED',
      });

      expect(offer.window, isNull);
    });

    test('is ignored when it is malformed instead of breaking the offer', () {
      final offer = freeSundayOfferFromJson({
        'available': true,
        'reason': 'ELIGIBLE',
        'endsAt': '2026-11-22T22:00:00Z',
        'window': {'day': 5},
      });

      expect(offer.available, isTrue);
      expect(offer.window, isNull);
    });
  });

  group('the texts', () {
    test('explain the real window in French', () {
      expect(
        FreeSundayText.explanationOf(wholeSunday),
        'Une charge le dimanche, de 00h00 à 23h59, jusqu’à 100 %. Elle s’arrête à 23h59.',
      );
      expect(
        FreeSundayText.explanationOf(noonToTen),
        'Une charge le dimanche, de 12h00 à 22h00, jusqu’à 100 %. Elle s’arrête à 22h00.',
      );
    });

    test('explain the real window in English', () {
      AppLanguage.current = AppLanguage.en;

      expect(
        FreeSundayText.explanationOf(wholeSunday),
        'One charge on Sundays, from 00:00 to 23:59, up to 100%. It stops at 23:59.',
      );
    });

    test('stay generic without a window instead of inventing times', () {
      expect(
        FreeSundayText.explanationOf(null),
        'Une charge par jour de gratuité, jusqu’à 100 %. Elle s’arrête à la fin du créneau.',
      );
      expect(FreeSundayText.explanationOf(null), isNot(contains('12h')));
      expect(FreeSundayText.explanationOf(null), isNot(contains('22h')));
    });

    test('say when the free charge opens outside the window', () {
      expect(
        FreeSundayText.reasonOf('OUTSIDE_WINDOW', window: noonToTen),
        'Disponible le dimanche de 12h00 à 22h00',
      );
      AppLanguage.current = AppLanguage.en;
      expect(
        FreeSundayText.reasonOf('OUTSIDE_WINDOW', window: noonToTen),
        'Available on Sundays from 12:00 to 22:00',
      );
    });

    test('name every weekday in both languages', () {
      for (final day in [
        'MONDAY',
        'TUESDAY',
        'WEDNESDAY',
        'THURSDAY',
        'FRIDAY',
        'SATURDAY',
        'SUNDAY',
      ]) {
        final window = FreeSundayWindow(
          day: day,
          from: '08:00',
          until: '18:00',
          endOfDay: false,
        );
        expect(
          FreeSundayText.explanationOf(window),
          contains('de 08h00 à 18h00'),
        );
        AppLanguage.current = AppLanguage.en;
        expect(
          FreeSundayText.explanationOf(window),
          contains('from 08:00 to 18:00'),
        );
        AppLanguage.current = AppLanguage.fr;
      }
    });

    test('keep the other reasons unchanged', () {
      expect(
        FreeSundayText.reasonOf('UNPAID_DAYS'),
        'Régularisez vos jours impayés',
      );
      expect(
        FreeSundayText.reasonOf('SUNDAY_USED'),
        'Déjà utilisée ce dimanche',
      );
    });

    test('say the window ended without a hard-coded hour', () {
      expect(
        HistoryText.stopReasonOf('FREE_WINDOW_ENDED', false),
        'Fin du créneau gratuit',
      );
      AppLanguage.current = AppLanguage.en;
      expect(
        HistoryText.stopReasonOf('FREE_WINDOW_ENDED', false),
        'Free window ended',
      );
    });
  });
  group('the end time shown', () {
    test('is the end of the window in Gabon time', () {
      final offer = FreeSundayOffer(
        available: true,
        reason: 'ELIGIBLE',
        endsAt: DateTime.utc(2026, 11, 22, 21),
        window: noonToTen,
      );

      expect(freeSundayEndTime(offer), '22:00');
    });

    test('is 23:59 when the window runs to the end of the day', () {
      final offer = FreeSundayOffer(
        available: true,
        reason: 'ELIGIBLE',
        endsAt: DateTime.utc(2026, 11, 22, 23),
        window: wholeSunday,
      );

      expect(freeSundayEndTime(offer), '23:59');
    });

    test('falls back to the Gabon time of the end without a window', () {
      final offer = FreeSundayOffer(
        available: true,
        reason: 'ELIGIBLE',
        endsAt: DateTime.utc(2026, 11, 22, 14, 30),
      );

      expect(freeSundayEndTime(offer), '15:30');
    });

    test('is absent without an end', () {
      expect(
        freeSundayEndTime(
          const FreeSundayOffer(available: false, reason: 'UNPAID_DAYS'),
        ),
        isNull,
      );
    });
  });
}
