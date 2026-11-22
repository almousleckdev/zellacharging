import '../../features/free_sunday/domain/free_sunday_window.dart';
import 'app_language.dart';

abstract final class FreeSundayText {
  static String get title => AppLanguage.pick(
    fr: 'Charge gratuite du dimanche',
    en: 'Free Sunday charge',
  );

  static String explanationOf(FreeSundayWindow? window) {
    if (window == null) {
      return AppLanguage.pick(
        fr: 'Une charge par jour de gratuité, jusqu’à 100 %. Elle s’arrête à la fin du créneau.',
        en: 'One charge per free day, up to 100%. It stops at the end of the window.',
      );
    }
    final from = _time(window.from);
    final until = _time(window.until);
    return AppLanguage.pick(
      fr: 'Une charge ${_dayFr(window.day)}, de $from à $until, jusqu’à 100 %. Elle s’arrête à $until.',
      en: 'One charge ${_dayEn(window.day)}, from $from to $until, up to 100%. It stops at $until.',
    );
  }

  static String _time(String time) {
    final parts = time.split(':');
    if (parts.length < 2) return time;
    return AppLanguage.pick(
      fr: '${parts[0]}h${parts[1]}',
      en: '${parts[0]}:${parts[1]}',
    );
  }

  static String _dayFr(String day) => switch (day) {
    'MONDAY' => 'le lundi',
    'TUESDAY' => 'le mardi',
    'WEDNESDAY' => 'le mercredi',
    'THURSDAY' => 'le jeudi',
    'FRIDAY' => 'le vendredi',
    'SATURDAY' => 'le samedi',
    'SUNDAY' => 'le dimanche',
    _ => 'le jour de gratuité',
  };

  static String _dayEn(String day) => switch (day) {
    'MONDAY' => 'on Mondays',
    'TUESDAY' => 'on Tuesdays',
    'WEDNESDAY' => 'on Wednesdays',
    'THURSDAY' => 'on Thursdays',
    'FRIDAY' => 'on Fridays',
    'SATURDAY' => 'on Saturdays',
    'SUNDAY' => 'on Sundays',
    _ => 'on the free day',
  };

  static String get chargeIsFree => AppLanguage.pick(
    fr: 'Cette charge est gratuite',
    en: 'This charge is free',
  );

  static String get availableNow =>
      AppLanguage.pick(fr: 'Disponible', en: 'Available');

  static String availableUntil(String time) => AppLanguage.pick(
    fr: 'Disponible jusqu’à $time',
    en: 'Available until $time',
  );

  static String badge(String time) => AppLanguage.pick(
    fr: 'Charge gratuite jusqu’à $time',
    en: 'Free charge until $time',
  );

  static String closingSoon(String time) => AppLanguage.pick(
    fr: 'Se termine bientôt : la charge s’arrête à $time',
    en: 'Ending soon: the charge stops at $time',
  );

  static String get badgeNoTime => title;

  static String reasonOf(String code, {FreeSundayWindow? window}) =>
      switch (code) {
        'ELIGIBLE' => availableNow,
        'NO_AGREEMENT' => AppLanguage.pick(
          fr: 'Aucun accord actif',
          en: 'No active agreement',
        ),
        'AGREEMENT_SUSPENDED' => AppLanguage.pick(
          fr: 'Votre accord est suspendu',
          en: 'Your agreement is suspended',
        ),
        'AGREEMENT_NOT_STARTED' => AppLanguage.pick(
          fr: 'Votre accord n’a pas encore commencé',
          en: 'Your agreement has not started yet',
        ),
        'AGREEMENT_ENDED' => AppLanguage.pick(
          fr: 'Votre accord est terminé',
          en: 'Your agreement has ended',
        ),
        'OUTSIDE_WINDOW' => _outsideWindow(window),
        'UNPAID_DAYS' => AppLanguage.pick(
          fr: 'Régularisez vos jours impayés',
          en: 'Settle your unpaid days',
        ),
        'DRIVER_SUSPENDED' => AppLanguage.pick(
          fr: 'Votre compte conducteur est suspendu',
          en: 'Your driver account is suspended',
        ),
        'NOT_LINKED' => AppLanguage.pick(
          fr: 'Compte conducteur non lié',
          en: 'Driver account not linked',
        ),
        'SUNDAY_USED' => AppLanguage.pick(
          fr: 'Déjà utilisée ce dimanche',
          en: 'Already used this Sunday',
        ),
        _ => AppLanguage.pick(fr: 'Indisponible', en: 'Unavailable'),
      };

  static String _outsideWindow(FreeSundayWindow? window) {
    if (window == null) {
      return AppLanguage.pick(
        fr: 'Disponible pendant le créneau gratuit',
        en: 'Available during the free window',
      );
    }
    final from = _time(window.from);
    final until = _time(window.until);
    return AppLanguage.pick(
      fr: 'Disponible ${_dayFr(window.day)} de $from à $until',
      en: 'Available ${_dayEn(window.day)} from $from to $until',
    );
  }
}
